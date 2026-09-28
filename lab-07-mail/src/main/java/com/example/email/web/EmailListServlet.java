package com.example.email.web;

import com.example.email.data.JpaLifecycle;
import com.example.email.data.UserRepository;
import com.example.email.mail.DisabledMailSender;
import com.example.email.mail.SmtpConfig;
import com.example.email.mail.SmtpMailSender;
import com.example.email.mail.WelcomeMailSender;
import com.example.email.model.User;
import com.example.email.service.EmailListService;
import com.example.email.service.RegistrationResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet(value = "/emailList", loadOnStartup = 1)
public class EmailListServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String FLASH_KEY = "registrationResult";
    private EmailListService service;
    private boolean mailConfigured;

    @Override
    public void init() throws ServletException {
        try {
            WelcomeMailSender mail = SmtpConfig.fromEnvironment(System.getenv())
                    .<WelcomeMailSender>map(SmtpMailSender::new).orElseGet(DisabledMailSender::new);
            mailConfigured = mail.isConfigured();
            service = new EmailListService(new UserRepository(JpaLifecycle.from(getServletContext())), mail);
        } catch (IllegalArgumentException e) {
            throw new ServletException("Invalid SMTP configuration: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("thanks".equals(request.getParameter("action"))) {
            HttpSession session = request.getSession(false);
            Object result = session == null ? null : session.getAttribute(FLASH_KEY);
            if (result instanceof RegistrationResult registration) {
                session.removeAttribute(FLASH_KEY);
                request.setAttribute("user", registration.user());
                request.setAttribute("mailStatus", registration.status().name());
                response.setHeader("Cache-Control", "no-store");
                request.getRequestDispatcher("/WEB-INF/views/thanks.jsp").forward(request, response);
            } else {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/emailList"));
            }
            return;
        }
        showForm(request, response);
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
            showForm(request, response);
            return;
        }

        RegistrationResult result;
        try {
            result = service.register(new User(email, firstName, lastName));
        } catch (RuntimeException e) {
            getServletContext().log("Cannot save email list entry", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save your details.");
            return;
        }
        if (result.status() == RegistrationResult.MailStatus.DUPLICATE) {
            request.setAttribute("error", "This email address already exists. Please enter another email address.");
            showForm(request, response);
            return;
        }
        request.getSession().setAttribute(FLASH_KEY, result);
        // Post/Redirect/Get: refreshing the thank-you page cannot send another email.
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", response.encodeRedirectURL(request.getContextPath() + "/emailList?action=thanks"));
    }

    private static String value(HttpServletRequest request, String name) {
        String input = request.getParameter(name);
        return input == null ? "" : input.trim();
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mailConfigured", mailConfigured);
        request.getRequestDispatcher("/WEB-INF/views/email-list.jsp").forward(request, response);
    }
}
