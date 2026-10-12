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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/orders")
public final class OrderHistoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        List<Order> orders = new ArrayList<>();
        for (String reference : OrderSession.references(session)) {
            Order order = OrderStore.instance().find(reference);
            if (order != null) {
                OrderSession.clearPaidCartIfCurrent(session, order);
                orders.add(order);
            }
        }
        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
    }
}
