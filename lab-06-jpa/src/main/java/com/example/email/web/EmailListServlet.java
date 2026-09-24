package com.example.email.web;

import com.example.email.data.JpaLifecycle;
import com.example.email.data.UserRepository;
import com.example.email.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private UserRepository users;

    @Override
    public void init() {
        users = new UserRepository(JpaLifecycle.from(getServletContext()));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/email-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = value(request, "email");
        String firstName = value(request, "firstName");
        String lastName = value(request, "lastName");
        request.setAttribute("email", email);
        request.setAttribute("firstName", firstName);
        request.setAttribute("lastName", lastName);

        if (!EMAIL.matcher(email).matches() || email.length() > 255
                || firstName.isEmpty() || firstName.length() > 100
                || lastName.isEmpty() || lastName.length() > 100) {
            request.setAttribute("error", "Please enter a valid email, first name, and last name.");
            doGet(request, response);
            return;
        }

        try {
            if (!users.insertIfAbsent(new User(email, firstName, lastName))) {
                request.setAttribute("error", "This email address already exists. Please enter another email address.");
                doGet(request, response);
                return;
            }
        } catch (RuntimeException e) {
            getServletContext().log("Cannot save email list entry with JPA", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save your details.");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/thanks.jsp").forward(request, response);
    }

    private static String value(HttpServletRequest request, String name) {
        String input = request.getParameter(name);
        return input == null ? "" : input.trim();
    }
}
