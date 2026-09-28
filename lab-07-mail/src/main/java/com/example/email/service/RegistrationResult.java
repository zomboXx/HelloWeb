package com.example.email.service;

import com.example.email.model.User;
import java.io.Serializable;

public record RegistrationResult(User user, MailStatus status) implements Serializable {
    public enum MailStatus {
        DUPLICATE, ACCEPTED_BY_SMTP, NOT_CONFIGURED, FAILED
    }
}
