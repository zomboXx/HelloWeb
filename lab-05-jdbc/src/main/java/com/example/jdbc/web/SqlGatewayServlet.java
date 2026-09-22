package com.example.jdbc.web;

import com.example.jdbc.data.Database;
import com.example.jdbc.data.SqlGatewayRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/sqlGateway")
public class SqlGatewayServlet extends HttpServlet {
    private static final Pattern ALLOWED_COMMAND =
            Pattern.compile("(?is)^\\s*(SELECT|INSERT|UPDATE|DELETE)\\s+.*");
    private SqlGatewayRepository gateway;

    @Override
    public void init() {
        gateway = new SqlGatewayRepository(Database.from(getServletContext()));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLocal(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "SQL Gateway is available on localhost only.");
            return;
        }
        request.setAttribute("sql", "SELECT * FROM User");
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isLocal(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "SQL Gateway is available on localhost only.");
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String sql = request.getParameter("sql");
        sql = sql == null ? "" : sql.trim();
        request.setAttribute("sql", sql);

        // This classroom gateway changes only its disposable in-memory demo database.
        if (sql.length() > 2000 || !ALLOWED_COMMAND.matcher(sql).matches()
                || sql.contains(";") || sql.contains("--") || sql.contains("/*")) {
            request.setAttribute("error", "Enter one SELECT, INSERT, UPDATE, or DELETE statement without comments or a semicolon.");
            show(request, response);
            return;
        }

        try {
            SqlGatewayRepository.Result result = gateway.execute(sql);
            request.setAttribute("hasResult", true);
            request.setAttribute("query", result.isQuery());
            request.setAttribute("columns", result.columns());
            request.setAttribute("rows", result.rows());
            request.setAttribute("affectedRows", result.affectedRows());
        } catch (SQLException e) {
            request.setAttribute("error", e.getMessage());
        }
        show(request, response);
    }

    private static void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/sql-gateway.jsp").forward(request, response);
    }

    private static boolean isLocal(HttpServletRequest request) {
        String address = request.getRemoteAddr();
        return "127.0.0.1".equals(address) || "::1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address);
    }
}
