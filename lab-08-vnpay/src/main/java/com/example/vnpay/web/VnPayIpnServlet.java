package com.example.vnpay.web;

import com.example.vnpay.payment.Order;
import com.example.vnpay.payment.OrderStore;
import com.example.vnpay.payment.VnPayGateway;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Server-to-server callback. This is the only endpoint that finalizes an order. */
@WebServlet("/vnpay/ipn")
public final class VnPayIpnServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        if (!VnPayGateway.configured()) {
            reply(response, "99", "Configuration missing");
            return;
        }
        VnPayGateway gateway = VnPayGateway.fromEnvironment();
        if (!gateway.verifyCallback(request.getParameterMap())) {
            reply(response, "97", "Invalid signature");
            return;
        }
        Order order = OrderStore.instance().find(request.getParameter("vnp_TxnRef"));
        if (order == null) {
            reply(response, "01", "Order not found");
            return;
        }
        if (!gateway.matchesOrder(request.getParameterMap(), order)) {
            reply(response, "04", "Invalid amount");
            return;
        }
        boolean success = "00".equals(request.getParameter("vnp_ResponseCode"))
                && "00".equals(request.getParameter("vnp_TransactionStatus"));
        Order.Status status = success ? Order.Status.PAID : Order.Status.FAILED;
        if (!order.finish(status, request.getParameter("vnp_TransactionNo"))) {
            reply(response, "02", "Order already confirmed");
            return;
        }
        reply(response, "00", "Confirm Success");
    }

    private static void reply(HttpServletResponse response, String code, String message)
            throws IOException {
        response.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }
}
