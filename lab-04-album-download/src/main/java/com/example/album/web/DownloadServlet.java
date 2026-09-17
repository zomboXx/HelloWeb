package com.example.album.web;

import com.example.album.model.Album;
import com.example.album.model.Album.Track;
import com.example.album.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final String USER_EMAIL_COOKIE = "album_user_email";
    private static final String USER_FIRST_NAME_COOKIE = "album_user_first_name";
    private static final String USER_LAST_NAME_COOKIE = "album_user_last_name";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30;
    private static final Map<String, Album> CATALOG = createCatalog();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = valueOrDefault(request.getParameter("action"), "listAlbums");

        switch (action) {
            case "checkUser" -> checkUser(request, response);
            case "viewDownloads" -> viewDownloads(request, response);
            case "forgetUser" -> forgetUser(request, response);
            default -> listAlbums(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String action = valueOrDefault(request.getParameter("action"), "registerUser");

        if ("registerUser".equals(action)) {
            registerUser(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }

    private void listAlbums(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("albums", CATALOG.values());
        request.getRequestDispatcher("/WEB-INF/views/albums.jsp").forward(request, response);
    }

    private void checkUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Album album = CATALOG.get(request.getParameter("productCode"));
        if (album == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Album not found");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("selectedAlbum", album);

        User user = getCurrentUser(request);
        if (user == null) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        session.setAttribute("user", user);
        redirectToDownloads(request, response);
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = clean(request.getParameter("email"));
        String firstName = clean(request.getParameter("firstName"));
        String lastName = clean(request.getParameter("lastName"));

        User user = new User(email, firstName, lastName);
        request.setAttribute("formUser", user);

        if (email.isBlank() || firstName.isBlank() || lastName.isBlank() || !email.contains("@")) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ họ tên và một địa chỉ email hợp lệ.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("selectedAlbum") == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/download"));
            return;
        }

        session.setAttribute("user", user);
        addRememberCookie(request, response, USER_EMAIL_COOKIE, email);
        addRememberCookie(request, response, USER_FIRST_NAME_COOKIE, firstName);
        addRememberCookie(request, response, USER_LAST_NAME_COOKIE, lastName);
        redirectToDownloads(request, response);
    }

    private void viewDownloads(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Album album = (Album) session.getAttribute("selectedAlbum");

        if (album == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/download"));
            return;
        }

        User user = getCurrentUser(request);
        if (user == null) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        session.setAttribute("user", user);
        request.getRequestDispatcher("/WEB-INF/views/downloads.jsp").forward(request, response);
    }

    private void forgetUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        deleteCookie(request, response, USER_EMAIL_COOKIE);
        deleteCookie(request, response, USER_FIRST_NAME_COOKIE);
        deleteCookie(request, response, USER_LAST_NAME_COOKIE);
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/download"));
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") instanceof User user) {
            return user;
        }

        Map<String, String> cookies = readCookies(request);
        String email = cookies.get(USER_EMAIL_COOKIE);
        if (email == null || email.isBlank()) {
            return null;
        }

        return new User(
                email,
                cookies.getOrDefault(USER_FIRST_NAME_COOKIE, ""),
                cookies.getOrDefault(USER_LAST_NAME_COOKIE, "")
        );
    }

    private void redirectToDownloads(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getContextPath() + "/download?action=viewDownloads";
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    private void addRememberCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String name,
            String value
    ) {
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setPath(cookiePath(request));
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(cookiePath(request));
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private Map<String, String> readCookies(HttpServletRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return values;
        }

        for (Cookie cookie : cookies) {
            try {
                values.put(cookie.getName(), URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed cookies rather than breaking the request.
            }
        }
        return values;
    }

    private String cookiePath(HttpServletRequest request) {
        return request.getContextPath().isEmpty() ? "/" : request.getContextPath();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static Map<String, Album> createCatalog() {
        Map<String, Album> albums = new LinkedHashMap<>();
        albums.put("8601", new Album(
                "8601",
                "86 (the band)",
                "True Life Songs and Pictures",
                List.of(
                        new Track("You Are a Star", "MP3"),
                        new Track("Don't Make No Difference", "MP3")
                )
        ));
        albums.put("pf01", new Album(
                "pf01",
                "Paddlefoot",
                "The First CD",
                List.of(
                        new Track("Whiskey Before Breakfast", "MP3"),
                        new Track("64 Corvair, Part 2", "MP3")
                )
        ));
        albums.put("pf02", new Album(
                "pf02",
                "Paddlefoot",
                "The Second CD",
                List.of(
                        new Track("Neon Lights", "MP3"),
                        new Track("Tank Hill", "MP3")
                )
        ));
        albums.put("jr01", new Album(
                "jr01",
                "Joe Rut",
                "Genuine Wood Grained Finish",
                List.of(
                        new Track("Filter", "MP3"),
                        new Track("So Long Lazy Ray", "MP3")
                )
        ));
        return Collections.unmodifiableMap(albums);
    }
}
