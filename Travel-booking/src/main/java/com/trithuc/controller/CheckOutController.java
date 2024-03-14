package com.trithuc.controller;

import com.trithuc.response.MessageResponse;
import com.trithuc.service.ConfigPaymenntService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CheckOutController {


    @Autowired
    private ConfigPaymenntService configPaymenntService;
    private static final String CHARACTERS = "0123456789";

    @PostMapping("create")
    public ResponseEntity<MessageResponse> createUrlPayment() throws UnsupportedEncodingException{
        return configPaymenntService.createUrlPayment();
    }


    @PostMapping("create-url")
    public ResponseEntity<MessageResponse> createUrlCheckOut() throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "thanh toan hoa don";
        String orderType = "other";
        String vnp_TxnRef = initHasCode();
        String vnp_IpAddr = "127.0.0.1";
        String vnp_TmnCode = "BY5CIN2M";

        int amount = 20000 * 100;
        Map vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:3000/payment");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());

        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        //Add Params of 2.1.0 Version
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        //Build data to hash and querystring
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW", hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" + "?" + queryUrl;
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("success");
        messageResponse.setResponseCode("200");
        messageResponse.setData(paymentUrl);
        return new ResponseEntity<MessageResponse>(messageResponse, HttpStatus.OK);
    }



    @GetMapping("/payment-result")
    public ResponseEntity<String> handlePaymentResult(@RequestBody Map<String, String> requestData) {

            StringBuilder hashData = new StringBuilder();
            String vnp_SecureHash = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW", hashData.toString());
            String vnpSecureHash = requestData.get("vnp_SecureHash");
            String vnpResponseCode = requestData.get("vnp_ResponseCode");
            String vnpTxnRef = requestData.get("vnp_TxnRef");
            if (!vnpSecureHash.equals(vnp_SecureHash)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_SecureHash incorrect");
            }
            if (vnpResponseCode.equals("00")){
                    // check Bank code
                        /// CardType,OrderInfo
                //BankTranNo ->	Mã giao dịch tại Ngân hàng
                //TxnRef -> Ma thanh toan -> tao lai ma thanh toan cong them chuoi localDate time de tranh trung
                // ma se duoc xu ly va lu vao redis sau do kiem tra xac nhan roi moi luu vao database                //vnp_PayDate
            }


           return null;
    }

    @PostMapping("/vnpay-response")
    public ResponseEntity<String> handleVnPayResponse(HttpServletRequest request) {
        try {
            //Begin process return from VNPAY
            Map<String, String> fields = new HashMap<>();
            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnpSecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            String calculatedHash = calculateHash(fields);

            if (calculatedHash.equals(vnpSecureHash)) {
                if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                    return new ResponseEntity<>("GD Thanh cong", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("GD Khong thanh cong", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Chu ky khong hop le", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Xay ra loi khi xu ly yeu cau", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private String calculateHash(Map<String, String> data) {
        try {
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName).append('=').append(fieldValue);
                }
            }

            String secretKey = "YourSecretKey"; // Thay thế bằng khóa bí mật của bạn
            Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSHA512.init(secretKeySpec);
            byte[] hashBytes = hmacSHA512.doFinal(hashData.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hashHex = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hashHex.append('0');
                }
                hashHex.append(hex);
            }

            return hashHex.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Xử lý ngoại lệ theo nhu cầu của bạn
        }
    }
    public String initHasCode() {
        // sử dụng thư viện  là một lớp trong gói java.security
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    public String hmacSHA512(final String key, final String data) {
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
