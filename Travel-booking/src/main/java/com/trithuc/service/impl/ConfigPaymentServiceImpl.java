package com.trithuc.service.impl;

import com.trithuc.response.MessageResponse;
import com.trithuc.service.ConfigPaymenntService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ConfigPaymentServiceImpl implements ConfigPaymenntService {

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



    @Override
    public ResponseEntity<MessageResponse> createUrlPayment() throws UnsupportedEncodingException {

        int amount = 20000 * 100;
        String oderInfo = "Buy tour";

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("vnp_Version",version);
        urlParams.put("vnp_Command",command);
        urlParams.put("vnp_TmnCode",tmnCode);
        urlParams.put("vnp_Amount",String.valueOf(amount));
        urlParams.put("vnp_CurrCode",currCode);
        urlParams.put("vnp_TxnRef",initTxRef());
        urlParams.put("vnp_OrderInfo",oderInfo);
        urlParams.put("vnp_OrderType",orderType);
        urlParams.put("vnp_Locale",location);
        urlParams.put("vnp_ReturnUrl",returnUrl);
        urlParams.put("vnp_IpAddr",ipAddress);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        urlParams.put("vnp_CreateDate",createDate);
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        urlParams.put("vnp_ExpireDate",expireDate);

        List<String> fieldNames = new ArrayList<>(urlParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hasData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()){
            String fieldName = itr.next();
            String fieldValue = urlParams.get(fieldName);
            if ((fieldValue!= null) && (!fieldValue.isEmpty())){
                hasData.append(fieldName);
                hasData.append('=');
                hasData.append(URLEncoder.encode(fieldValue,StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName,StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue,StandardCharsets.US_ASCII));
                if (itr.hasNext()){
                    query.append('&');
                    hasData.append('&');
                }

            }
        }
        String queryUrl = query.toString();
        String secureHas = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW",hasData.toString());
        queryUrl += "&vnp_SecureHash=" + secureHas;
        String paymentUrl = paymentDomain + "?" + queryUrl;
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("success");
        messageResponse.setResponseCode("200");
        messageResponse.setData(paymentUrl);
        return new ResponseEntity<MessageResponse>(messageResponse, HttpStatus.OK);
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
