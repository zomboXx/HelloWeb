package com.example.email.mail;

import com.example.email.model.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;

public class SmtpMailSender implements WelcomeMailSender {
    private final SmtpConfig config;
    private final Session session;

    public SmtpMailSender(SmtpConfig config) {
        this.config = config;
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", config.host());
        properties.setProperty("mail.smtp.port", Integer.toString(config.port()));
        properties.setProperty("mail.smtp.auth", Boolean.toString(config.authenticationRequired()));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(config.security().equals("starttls")));
        properties.setProperty("mail.smtp.starttls.required", Boolean.toString(config.security().equals("starttls")));
        properties.setProperty("mail.smtp.ssl.enable", Boolean.toString(config.security().equals("ssl")));
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.connectiontimeout", "5000");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.writetimeout", "5000");
        // Each application owns its settings. Never enable SMTP debug with credentials.
        session = Session.getInstance(properties);
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public void sendWelcome(User user) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.from(), true));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail(), true));
        message.setSubject("Welcome to our email list", "UTF-8");
        message.setText("Dear " + user.getFirstName() + ",\n\n"
                + "Thanks for joining our email list. We'll make sure to send you "
                + "announcements about new products and promotions.\n"
                + "Have a great day and thanks again!\n\n"
                + "Kelly Slivkoff\nMike Murach & Associates", "UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();
        try (Transport transport = session.getTransport("smtp")) {
            if (config.authenticationRequired()) {
                transport.connect(config.host(), config.port(), config.username(), config.password());
            } else {
                transport.connect(config.host(), config.port(), null, null);
            }
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
}
