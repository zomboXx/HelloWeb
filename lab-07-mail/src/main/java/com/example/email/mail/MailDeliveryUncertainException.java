package com.example.email.mail;

import jakarta.mail.MessagingException;

/** A send was attempted, but acceptance cannot be confirmed. Never automatically resend. */
public class MailDeliveryUncertainException extends MessagingException {
    public MailDeliveryUncertainException() {
        super("Email acceptance could not be confirmed; no automatic retry was made.");
    }
}
