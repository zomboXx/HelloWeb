package com.example.email.mail;

import jakarta.mail.MessagingException;

/** Connecting/authenticating failed BEFORE sendMessage: falling back cannot duplicate this mail. */
public class SmtpUnavailableException extends MessagingException {
    public SmtpUnavailableException(MessagingException cause) {
        super("SMTP connection could not be established.", cause);
    }
}
