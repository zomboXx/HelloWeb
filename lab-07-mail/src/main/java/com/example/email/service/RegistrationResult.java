package com.example.email.service;

import com.example.email.model.User;
import com.example.email.mail.MailDelivery;
import java.io.Serializable;

public record RegistrationResult(User user, MailStatus status, MailDelivery delivery) implements Serializable {
    public RegistrationResult(User user, MailStatus status) {
        this(user, status, null);
    }

    public enum MailStatus {
        DUPLICATE, ACCEPTED_BY_SMTP, ACCEPTED_BY_API, NOT_CONFIGURED, FAILED, UNCONFIRMED
    }
}
