package com.example.email.mail;

import java.io.Serializable;

/** Acceptance by a server/provider is not proof of delivery to the recipient's inbox. */
public record MailDelivery(Transport transport, String provider, FallbackReason fallbackReason)
        implements Serializable {
    public enum Transport { SMTP, EMAIL_API }
    public enum FallbackReason { NONE, SMTP_NOT_CONFIGURED, SMTP_DISABLED, SMTP_UNAVAILABLE }

    public static MailDelivery smtp() {
        return new MailDelivery(Transport.SMTP, "SMTP", FallbackReason.NONE);
    }

    public MailDelivery withFallback(FallbackReason reason) {
        return new MailDelivery(transport, provider, reason);
    }
}
