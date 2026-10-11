package com.example.vnpay.payment;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class VnPayGatewayTest {
    private static final String SECRET = "sandbox-test-secret-not-a-real-key";
    private final VnPayGateway gateway = new VnPayGateway("TESTCODE", SECRET,
            "https://merchant.example/vnpay/return");

    @Test
    void paymentUrlUsesServerAmountAndVietNamTime() {
        Order order = new Order("order123", 150_000, Instant.parse("2026-10-11T02:00:00Z"));
        String url = gateway.paymentUrl(order, "127.0.0.1", Instant.parse("2026-10-11T02:00:00Z"));
        Map<String, String[]> fields = parseQuery(URI.create(url).getRawQuery());

        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertEquals("15000000", fields.get("vnp_Amount")[0]);
        assertEquals("20261011090000", fields.get("vnp_CreateDate")[0]);
        assertEquals("20261011091500", fields.get("vnp_ExpireDate")[0]);
        assertEquals("https://merchant.example/vnpay/return", fields.get("vnp_ReturnUrl")[0]);
        assertTrue(gateway.verifyCallback(fields));
    }

    @Test
    void callbackRejectsTamperingAndDuplicateParameters() {
        Order order = new Order("order123", 150_000, Instant.now());
        Map<String, String[]> fields = signedCallback("order123", "15000000", "00", "00");
        assertTrue(gateway.matchesOrder(fields, order));

        fields.put("vnp_Amount", new String[]{"100"});
        assertFalse(gateway.verifyCallback(fields));

        fields = signedCallback("order123", "15000000", "00", "00");
        fields.put("vnp_ResponseCode", new String[]{"00", "99"});
        assertFalse(gateway.verifyCallback(fields));
    }

    @Test
    void callbackMustMatchOrderAndMerchant() {
        Order order = new Order("order123", 150_000, Instant.now());
        assertFalse(gateway.matchesOrder(signedCallback("wrong", "15000000", "00", "00"), order));
        assertFalse(gateway.matchesOrder(signedCallback("order123", "13000000", "00", "00"), order));
        Map<String, String[]> fields = signedCallback("order123", "15000000", "00", "00");
        fields.put("vnp_TmnCode", new String[]{"OTHER"});
        assertFalse(gateway.verifyCallback(fields));
    }

    @Test
    void orderCanOnlyBeFinalizedOnce() {
        Order order = new Order("order123", 150_000, Instant.now());
        assertTrue(order.finish(Order.Status.PAID, "txn-1"));
        assertFalse(order.finish(Order.Status.FAILED, "txn-2"));
        assertEquals(Order.Status.PAID, order.getStatus());
        assertEquals("txn-1", order.getTransactionNo());
    }

    private static Map<String, String[]> signedCallback(String ref, String amount,
                                                         String responseCode, String transactionStatus) {
        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_TmnCode", "TESTCODE");
        fields.put("vnp_TxnRef", ref);
        fields.put("vnp_Amount", amount);
        fields.put("vnp_ResponseCode", responseCode);
        fields.put("vnp_TransactionStatus", transactionStatus);
        String data = fields.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b).orElse("");
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            fields.put("vnp_SecureHash", HexFormat.of().formatHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        Map<String, String[]> result = new HashMap<>();
        fields.forEach((key, value) -> result.put(key, new String[]{value}));
        return result;
    }

    private static Map<String, String[]> parseQuery(String query) {
        Map<String, String[]> result = new HashMap<>();
        for (String part : query.split("&")) {
            String[] pieces = part.split("=", 2);
            result.put(URLDecoder.decode(pieces[0], StandardCharsets.UTF_8),
                    new String[]{URLDecoder.decode(pieces[1], StandardCharsets.UTF_8)});
        }
        return result;
    }
}
