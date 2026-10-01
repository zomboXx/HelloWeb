package com.example.email.mail;

import com.example.email.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/** Backend-only HTTPS adapter. Credentials never enter a JSP or browser request. */
public class HttpApiMailSender implements WelcomeMailSender {
    private final EmailApiConfig config;
    private final HttpClient client;
    private final URI endpoint;
    private final Duration requestTimeout;

    public HttpApiMailSender(EmailApiConfig config) {
        this(config, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build(), config.provider().endpoint(),
                Duration.ofSeconds(8));
    }

    // Package-private injection for loopback tests; production endpoints are fixed HTTPS URLs.
    HttpApiMailSender(EmailApiConfig config, HttpClient client, URI endpoint, Duration requestTimeout) {
        this.config = config;
        this.client = client;
        this.endpoint = endpoint;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public boolean isConfigured() { return true; }

    @Override
    public MailDelivery sendWelcome(User user) throws MessagingException {
        HttpRequest.Builder request = HttpRequest.newBuilder(endpoint).timeout(requestTimeout)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload(user).toString(), StandardCharsets.UTF_8));
        if (config.provider() == EmailApiConfig.Provider.BREVO) {
            request.header("api-key", config.apiKey());
        } else {
            request.header("Authorization", "Bearer " + config.apiKey());
            request.header("Idempotency-Key", "welcome/" + UUID.randomUUID());
        }
        HttpResponse<String> response;
        try {
            response = client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MailDeliveryUncertainException();
        } catch (IOException e) {
            // A timeout may happen after the provider accepted the email. Do not retry.
            throw new MailDeliveryUncertainException();
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            // Do not include response bodies: they can contain personal details.
            throw new MessagingException("Email API did not confirm acceptance (HTTP "
                    + response.statusCode() + ").");
        }
        String idField = config.provider() == EmailApiConfig.Provider.BREVO ? "messageId" : "id";
        try {
            JsonElement id = JsonParser.parseString(response.body()).getAsJsonObject().get(idField);
            if (id == null || !id.isJsonPrimitive() || !id.getAsJsonPrimitive().isString()
                    || id.getAsString().isBlank()) {
                throw new MailDeliveryUncertainException();
            }
        } catch (RuntimeException e) {
            throw new MailDeliveryUncertainException();
        }
        return new MailDelivery(MailDelivery.Transport.EMAIL_API, config.provider().displayName(),
                MailDelivery.FallbackReason.NONE);
    }

    private JsonObject payload(User user) {
        JsonObject payload = new JsonObject();
        JsonArray recipients = new JsonArray();
        payload.addProperty("subject", WelcomeMessage.SUBJECT);
        if (config.provider() == EmailApiConfig.Provider.BREVO) {
            JsonObject sender = new JsonObject();
            sender.addProperty("email", config.from());
            payload.add("sender", sender);
            JsonObject recipient = new JsonObject();
            recipient.addProperty("email", user.getEmail());
            recipients.add(recipient);
            payload.addProperty("textContent", WelcomeMessage.text(user));
        } else {
            payload.addProperty("from", config.from());
            recipients.add(user.getEmail());
            payload.addProperty("text", WelcomeMessage.text(user));
        }
        payload.add("to", recipients);
        return payload;
    }
}
