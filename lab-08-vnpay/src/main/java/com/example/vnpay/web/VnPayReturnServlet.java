package com.example.vnpay.web;

import com.example.vnpay.payment.Order;
import com.example.vnpay.payment.OrderStore;
import com.example.vnpay.payment.VnPayGateway;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Browser redirect: displays the result, but never marks an order as paid. */
@WebServlet("/vnpay/return")
public final class VnPayReturnServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        String ref = request.getParameter("vnp_TxnRef");
        Order order = OrderStore.instance().find(ref);
        boolean valid = VnPayGateway.configured()
                && VnPayGateway.fromEnvironment().matchesOrder(request.getParameterMap(), order);
        if (!valid) {
            response.sendError(400, "Invalid VNPay response");
            return;
        }
        HttpSession session = request.getSession(false);
        OrderSession.clearPaidCartIfCurrent(session, order);
        request.setAttribute("order", order);
        request.setAttribute("gatewaySuccess", "00".equals(request.getParameter("vnp_ResponseCode"))
                && "00".equals(request.getParameter("vnp_TransactionStatus")));
        request.getRequestDispatcher("/WEB-INF/views/result.jsp").forward(request, response);
    }
}
