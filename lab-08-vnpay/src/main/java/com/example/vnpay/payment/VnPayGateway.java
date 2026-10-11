package com.example.vnpay.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/** VNPAY PAY v2.1.0 sandbox signing and callback verification. */
public final class VnPayGateway {
    private static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String tmnCode;
    private final String hashSecret;
    private final String returnUrl;

    public VnPayGateway(String tmnCode, String hashSecret, String returnUrl) {
        if (blank(tmnCode) || blank(hashSecret) || blank(returnUrl)) {
            throw new IllegalArgumentException("VNPay sandbox configuration is incomplete");
        }
        if (!returnUrl.startsWith("https://") && !returnUrl.startsWith("http://localhost:")) {
            throw new IllegalArgumentException("Return URL must use HTTPS (or localhost for development)");
        }
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.returnUrl = returnUrl;
    }

    public static boolean configured() {
        return !blank(System.getenv("VNPAY_TMN_CODE"))
                && !blank(System.getenv("VNPAY_HASH_SECRET"))
                && !blank(System.getenv("VNPAY_RETURN_URL"));
    }

    public static VnPayGateway fromEnvironment() {
        return new VnPayGateway(System.getenv("VNPAY_TMN_CODE"),
                System.getenv("VNPAY_HASH_SECRET"), System.getenv("VNPAY_RETURN_URL"));
    }

    public String paymentUrl(Order order, String clientIp, Instant now) {
        long amount = Math.multiplyExact(order.getAmountVnd(), 100);
        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", tmnCode);
        fields.put("vnp_Amount", Long.toString(amount));
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", order.getReference());
        fields.put("vnp_OrderInfo", "Thanh toan don hang " + order.getReference());
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", returnUrl);
        fields.put("vnp_IpAddr", blank(clientIp) ? "127.0.0.1" : clientIp);
        fields.put("vnp_CreateDate", DATE.format(now.atZone(VIETNAM)));
        fields.put("vnp_ExpireDate", DATE.format(now.plusSeconds(15 * 60).atZone(VIETNAM)));
        String query = encodedFields(fields);
        return PAY_URL + "?" + query + "&vnp_SecureHash=" + sign(query);
    }

    public boolean verifyCallback(Map<String, String[]> parameters) {
        String[] supplied = parameters.get("vnp_SecureHash");
        if (supplied == null || supplied.length != 1 || supplied[0] == null) return false;
        Map<String, String> signed = new TreeMap<>();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("vnp_") || key.equals("vnp_SecureHash")
                    || key.equals("vnp_SecureHashType")) continue;
            String[] values = entry.getValue();
            if (values == null || values.length != 1 || values[0] == null) return false;
            signed.put(key, values[0]);
        }
        if (signed.isEmpty() || !tmnCode.equals(signed.get("vnp_TmnCode"))) return false;
        byte[] expected = sign(encodedFields(signed)).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = supplied[0].toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    public boolean matchesOrder(Map<String, String[]> parameters, Order order) {
        if (order == null || !verifyCallback(parameters)) return false;
        try {
            String[] refs = parameters.get("vnp_TxnRef");
            String[] amounts = parameters.get("vnp_Amount");
            return refs != null && refs.length == 1 && order.getReference().equals(refs[0])
                    && amounts != null && amounts.length == 1
                    && Math.multiplyExact(order.getAmountVnd(), 100) == Long.parseLong(amounts[0]);
        } catch (NumberFormatException | ArithmeticException exception) {
            return false;
        }
    }

    private String sign(String payload) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign VNPay request", exception);
        }
    }

    private static String encodedFields(Map<String, String> fields) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(fields).entrySet()) {
            if (result.length() > 0) result.append('&');
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append('=');
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
