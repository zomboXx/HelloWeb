package com.example.email.mail;

import com.example.email.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HttpApiMailSenderTest {
    private HttpServer server;
    private URI endpoint;
    private int status = 201;
    private String response = "{\"messageId\":\"test-message\"}";
    private volatile JsonObject payload;
    private volatile String authentication;
    private volatile String idempotencyKey;
    private volatile String method;
    private volatile boolean disconnectWithoutResponse;
    private final AtomicInteger requests = new AtomicInteger();
    private final User user = new User("student@example.test", "Đức \"Phát\"\n\\", "Smith");

    @BeforeEach
    void startLoopbackApi() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/email");
        server.createContext("/email", exchange -> {
            requests.incrementAndGet();
            method = exchange.getRequestMethod();
            authentication = exchange.getRequestHeaders().getFirst("api-key");
            if (authentication == null) {
                authentication = exchange.getRequestHeaders().getFirst("Authorization");
            }
            idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            payload = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8)).getAsJsonObject();
            if (disconnectWithoutResponse) {
                exchange.close();
                return;
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            if (status == 302) {
                exchange.getResponseHeaders().add("Location", "/unexpected");
            }
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopApi() { server.stop(0); }

    @Test
    void brevoSendsCorrectUtf8JsonAndReturnsAcceptance() throws Exception {
        MailDelivery result = sender(EmailApiConfig.Provider.BREVO).sendWelcome(user);
        assertEquals("Brevo", result.provider());
        assertEquals(MailDelivery.Transport.EMAIL_API, result.transport());
        assertEquals("POST", method);
        assertEquals("test-secret", authentication);
        assertEquals("sender@example.test", payload.getAsJsonObject("sender").get("email").getAsString());
        assertEquals(user.getEmail(), payload.getAsJsonArray("to").get(0)
                .getAsJsonObject().get("email").getAsString());
        assertEquals(WelcomeMessage.SUBJECT, payload.get("subject").getAsString());
        assertEquals(WelcomeMessage.text(user), payload.get("textContent").getAsString());
        assertFalse(payload.toString().contains("test-secret"));
        assertEquals(1, requests.get());
    }

    @Test
    void resendUsesBearerAndIdempotencyWithItsOwnPayloadFormat() throws Exception {
        status = 200;
        response = "{\"id\":\"test-id\"}";
        assertEquals("Resend", sender(EmailApiConfig.Provider.RESEND).sendWelcome(user).provider());
        assertEquals("Bearer test-secret", authentication);
        assertTrue(idempotencyKey.startsWith("welcome/"));
        assertEquals("sender@example.test", payload.get("from").getAsString());
        assertEquals(user.getEmail(), payload.getAsJsonArray("to").get(0).getAsString());
        assertEquals(WelcomeMessage.text(user), payload.get("text").getAsString());
        assertEquals(1, requests.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 429, 500, 302})
    void errorResponsesAreNotReportedAsSuccessAndNeverRetriedOrRedirected(int code) {
        status = code;
        response = "{\"error\":\"private-recipient-and-secret\"}";
        var error = assertThrows(MessagingException.class,
                () -> sender(EmailApiConfig.Provider.BREVO).sendWelcome(user));
        assertTrue(error.getMessage().contains(Integer.toString(code)));
        assertFalse(error.getMessage().contains("private-recipient-and-secret"));
        assertEquals(1, requests.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "not json", "{\"messageId\":null}", "{\"messageId\":\"\"}", "{\"messageId\":123}"})
    void successWithoutProviderReceiptIsUnconfirmed(String body) {
        response = body;
        assertThrows(MailDeliveryUncertainException.class,
                () -> sender(EmailApiConfig.Provider.BREVO).sendWelcome(user));
        assertEquals(1, requests.get());
    }

    @Test
    void connectionFailureDoesNotClaimAcceptance() {
        server.stop(0);
        assertThrows(MailDeliveryUncertainException.class,
                () -> sender(EmailApiConfig.Provider.BREVO).sendWelcome(user));
    }

    @Test
    void lostResponseAfterRequestWasReceivedDoesNotResend() {
        disconnectWithoutResponse = true;
        assertThrows(MailDeliveryUncertainException.class,
                () -> sender(EmailApiConfig.Provider.BREVO).sendWelcome(user));
        assertEquals(1, requests.get());
    }

    private HttpApiMailSender sender(EmailApiConfig.Provider provider) {
        return new HttpApiMailSender(new EmailApiConfig(provider, "test-secret", "sender@example.test"),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1))
                        .followRedirects(HttpClient.Redirect.NEVER).build(), endpoint, Duration.ofSeconds(2));
    }
}
