package com.example.vnpay.web;

import com.example.vnpay.payment.Order;
import com.example.vnpay.payment.OrderStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/order")
public final class OrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        String ref = request.getParameter("ref");
        if (!OrderSession.owns(session, ref)) {
            response.sendError(404);
            return;
        }
        Order order = OrderStore.instance().find(ref);
        if (order == null) {
            response.sendError(404);
            return;
        }
        if ("status".equals(request.getParameter("format"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"" + order.getStatus().name() + "\"}");
            return;
        }
        OrderSession.clearPaidCartIfCurrent(session, order);
        request.setAttribute("order", order);
        request.getRequestDispatcher("/WEB-INF/views/result.jsp").forward(request, response);
    }
}
