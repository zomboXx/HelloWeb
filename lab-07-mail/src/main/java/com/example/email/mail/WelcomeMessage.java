package com.example.email.mail;

import com.example.email.model.User;

final class WelcomeMessage {
    static final String SUBJECT = "Welcome to our email list";

    private WelcomeMessage() { }

    static String text(User user) {
        return "Dear " + user.getFirstName() + ",\n\n"
                + "Thanks for joining our email list. We'll make sure to send you "
                + "announcements about new products and promotions.\n"
                + "Have a great day and thanks again!\n\n"
                + "Kelly Slivkoff\nMike Murach & Associates";
    }
}
