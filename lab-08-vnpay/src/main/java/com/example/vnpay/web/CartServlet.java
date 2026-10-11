package com.example.vnpay.web;

import com.example.vnpay.model.Cart;
import com.example.vnpay.model.Catalog;
import com.example.vnpay.model.Product;
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
import java.time.Instant;

@WebServlet("/cart")
public final class CartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Cart cart = cart(request.getSession());
        request.setAttribute("cart", cart);
        request.setAttribute("paymentConfigured", VnPayGateway.configured());
        String action = request.getParameter("action");
        String view;
        if ("view".equals(action)) view = "cart.jsp";
        else if ("checkout".equals(action)) view = "checkout.jsp";
        else {
            request.setAttribute("products", Catalog.all());
            view = "products.jsp";
        }
        if ("quantity".equals(request.getParameter("error"))) {
            request.setAttribute("errorMessage", "Số lượng phải từ 1 đến 99.");
        }
        request.getRequestDispatcher("/WEB-INF/views/" + view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Cart cart = cart(request.getSession());
        String action = request.getParameter("action");
        String code = request.getParameter("productCode");
        if ("add".equals(action)) {
            Product product = Catalog.find(code);
            if (product == null) {
                response.sendError(404, "Product not found");
                return;
            }
            cart.add(product);
        } else if ("update".equals(action)) {
            try {
                cart.update(code, Integer.parseInt(request.getParameter("quantity")));
            } catch (IllegalArgumentException exception) {
                redirect(request, response, "?action=view&error=quantity");
                return;
            }
        } else if ("remove".equals(action)) {
            cart.remove(code);
        } else if ("pay".equals(action)) {
            startPayment(request, response, cart);
            return;
        } else {
            response.sendError(400, "Unknown action");
            return;
        }
        redirect(request, response, "?action=view");
    }

    private void startPayment(HttpServletRequest request, HttpServletResponse response, Cart cart)
            throws IOException, ServletException {
        if (cart.isEmpty()) {
            redirect(request, response, "?action=view");
            return;
        }
        if (!VnPayGateway.configured()) {
            request.setAttribute("cart", cart);
            request.setAttribute("paymentConfigured", false);
            request.setAttribute("errorMessage", "Chưa cấu hình VNPay Sandbox; xem README của bài lab.");
            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
            return;
        }
        VnPayGateway gateway = VnPayGateway.fromEnvironment();
        Order order = OrderStore.instance().create(cart.getTotalVnd());
        request.getSession().setAttribute("pendingOrderRef", order.getReference());
        String url = gateway.paymentUrl(order, request.getRemoteAddr(), Instant.now());
        response.sendRedirect(url);
    }

    private static Cart cart(HttpSession session) {
        synchronized (session) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }
            return cart;
        }
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response, String query)
            throws IOException {
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/cart" + query));
    }
}
