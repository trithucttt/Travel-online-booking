package com.trithuc.service.impl;

import com.trithuc.dto.VnpPaymentDTO;
import com.trithuc.response.MessageResponse;
import com.trithuc.response.PaymentInfoResponse;
import com.trithuc.service.ConfigPaymenntService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigPaymentServiceImpl implements ConfigPaymenntService {

    private final StringRedisTemplate redisTemplate;
    private static final String tmnCode = "BY5CIN2M";
    private static final String version = "2.1.0";
    private static final String command = "pay";
    private static final String orderType = "other";
    private static final String ipAddress = "127.0.0.1";
    private static final String currCode = "VND";
    private static final String paymentDomain = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String location = "vn";
    private static final String returnUrl = "http://localhost:3000/payment";
    private static final String CHARACTERS = "0123456789";

    public ConfigPaymentServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    //    @Override
    @Cacheable(value = "TxRef", key = "#username")
    public ResponseEntity<MessageResponse> createUrlPayment(String username) throws UnsupportedEncodingException {

        int amount = 20000 * 100;
        String oderInfo = "Buy tour";
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("vnp_Version", version);
        urlParams.put("vnp_Command", command);
        urlParams.put("vnp_TmnCode", tmnCode);
        urlParams.put("vnp_Amount", String.valueOf(amount));
        urlParams.put("vnp_CurrCode", currCode);

        // add code TxnRef to redis
        String codeTxRef = initTxRef();
        urlParams.put("vnp_TxnRef", codeTxRef);
        redisTemplate.opsForValue().set(username, codeTxRef, 10, TimeUnit.MINUTES);

        urlParams.put("vnp_OrderInfo", oderInfo);
        urlParams.put("vnp_OrderType", orderType);
        urlParams.put("vnp_Locale", location);
        urlParams.put("vnp_ReturnUrl", returnUrl);
        urlParams.put("vnp_IpAddr", ipAddress);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        urlParams.put("vnp_CreateDate", createDate);
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        urlParams.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(urlParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hasData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = urlParams.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hasData.append(fieldName);
                hasData.append('=');
                hasData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hasData.append('&');
                }

            }
        }
        String queryUrl = query.toString();
        String secureHas = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW", hasData.toString());
        queryUrl += "&vnp_SecureHash=" + secureHas;
        String paymentUrl = paymentDomain + "?" + queryUrl;
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("success");
        messageResponse.setResponseCode("200");
        messageResponse.setData(paymentUrl);
        return new ResponseEntity<MessageResponse>(messageResponse, HttpStatus.OK);
    }


    public ResponseEntity<?> handlePaymentResult(VnpPaymentDTO requestData,String username) {

        StringBuilder hashData = new StringBuilder();
        String vnp_SecureHash = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW", hashData.toString());
        String vnpSecureHash = requestData.getVnp_SecureHash();
        String orderInfo = requestData.getVnp_OrderInfo();
        String vnpResponseCode = requestData.getVnp_ResponseCode();
        String vnpTxnRefResponse = requestData.getVnp_TxnRef();
        String vnpTmnCode = requestData.getVnp_TmnCode();
        String vnpBankTranNo = requestData.getVnp_BankTranNo();
        String vpnBankCode = requestData.getVnp_BankCode();
        String vnp_PayDate = requestData.getVnp_PayDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Chuyển đổi chuỗi sang LocalDateTime
        LocalDateTime payDayFormat = LocalDateTime.parse(vnp_PayDate, formatter);

        //  xử lý múi giờ,  chuyển đổi LocalDateTime sang ZonedDateTime
        ZoneId zoneId = ZoneId.of("Etc/GMT-7");
        ZonedDateTime payDayZonedDateTime = payDayFormat.atZone(zoneId);

//        System.out.println("LocalDateTime: " + localDateTime);
//        System.out.println("ZonedDateTime: " + zonedDateTime);

        if (vnpResponseCode.equals("00")) {
            if (!vnpSecureHash.equals(vnp_SecureHash)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_SecureHash incorrect");
            }
            if (!vnpTmnCode.equals(tmnCode)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_TmnCode incorrect");
            }
            if (vnpBankTranNo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_BankTranNo is null");
            }
            if (vpnBankCode.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_BankCode is null");
            }
            // CHECK TxnRef
            String codeTxnRefFromRedis = redisTemplate.opsForValue().get(username);
            if (vnpTxnRefResponse != null && vnpTxnRefResponse.equals(codeTxnRefFromRedis)) {
                PaymentInfoResponse paymentInfoResponse = new PaymentInfoResponse();
                paymentInfoResponse.setOrderInfo(orderInfo);
                paymentInfoResponse.setPaymentCode(vnpTxnRefResponse);
                paymentInfoResponse.setBankTranNo(vnpBankTranNo);
                paymentInfoResponse.setPayDay(payDayFormat);
                paymentInfoResponse.setMessage("Valid successfully");
                paymentInfoResponse.setResCode("200");
//                redisTemplate.delete(username);
                return ResponseEntity.status(HttpStatus.OK).body(payDayZonedDateTime);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_TxnRef code is incorrect");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment Failed");
        }
    }

    private String initTxRef() {

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code + createDate;
    }

    private String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
}
