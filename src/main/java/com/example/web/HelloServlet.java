package com.example.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    private static String lastSubmission = "Chưa có phản hồi nào được gửi.";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        showForm(response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        String firstName = value(request.getParameter("firstName"));
        String lastName = value(request.getParameter("lastName"));
        String email = value(request.getParameter("email"));
        String birthDate = value(request.getParameter("birthDate"));
        String source = value(request.getParameter("source"));
        String contact = value(request.getParameter("contact"));
        lastSubmission = "Cảm ơn " + firstName + " " + lastName
                + "! Email: " + email + "; ngày sinh: " + birthDate
                + "; biết đến từ: " + source + "; liên hệ qua: " + contact;
        showForm(response, lastSubmission);
    }

    private void showForm(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!doctype html><html lang='en'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>Murach Survey</title>");
        out.println("<style>body{font-family:Arial,sans-serif;color:#333;margin:0;padding:18px;max-width:620px}"
                + ".logo{width:110px;height:110px;object-fit:contain;display:block;margin-bottom:12px}"
                + "h2{color:#167b80;font-size:20px;margin:12px 0 6px}h3{color:#167b80;margin:16px 0 6px}"
                + "p{margin:4px 0 10px}.row{margin:5px 0}.row label{display:inline-block;width:90px;font-weight:bold}"
                + "input[type=text],input[type=email],input[type=date],select{width:220px;padding:4px;border:1px solid #aaa}"
                + "button{margin-top:12px;padding:4px 10px}.message{background:#e8f5e9;padding:8px;margin:12px 0}"
                + "</style></head><body>");
        out.println("<img class='logo' src='murach-logo.png' alt='Murach logo'><h2>Survey</h2>");
        out.println("<p>If you have a moment, we'd appreciate it if you would fill out this survey.</p>");
        if (message != null) out.println("<div class='message'>" + message + "</div>");
        out.println("<form method='post' action='hello'><h3>Your information:</h3>");
        out.println("<div class='row'><label>First Name</label><input type='text' name='firstName' required></div>");
        out.println("<div class='row'><label>Last Name</label><input type='text' name='lastName' required></div>");
        out.println("<div class='row'><label>Email</label><input type='email' name='email' required></div>");
        out.println("<div class='row'><label>Date of Birth</label><input type='date' name='birthDate'></div>");
        out.println("<h3>How did you hear about us?</h3>");
        out.println("<label><input type='radio' name='source' value='Search engine' checked>Search engine</label> ");
        out.println("<label><input type='radio' name='source' value='Word of mouth'>Word of mouth</label> ");
        out.println("<label><input type='radio' name='source' value='Social Media'>Social Media</label> ");
        out.println("<label><input type='radio' name='source' value='Other'>Other</label>");
        out.println("<h3>Would you like to receive announcements<br>about new CDs and special offers?</h3>");
        out.println("<div><label><input type='checkbox' name='offers' value='yes'> YES, I'd like that.</label></div>");
        out.println("<div><label><input type='checkbox' name='emailOffers' value='yes'> YES, please send me email announcements.</label></div>");
        out.println("<p>Please contact me by: <select name='contact'><option>Email or postal mail</option><option>Email</option><option>Postal mail</option></select></p>");
        out.println("<button type='submit'>Submit</button></form></body></html>");
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "(trống)" : value.replace("<", "&lt;").replace(">", "&gt;");
    }
}
