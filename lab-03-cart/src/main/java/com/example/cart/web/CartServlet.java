package com.example.cart.web;

import com.example.cart.model.Cart;
import com.example.cart.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private static final Map<String, Product> CATALOG = createCatalog();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        Cart cart = getOrCreateCart(request.getSession());
        request.setAttribute("cart", cart);

        switch (action) {
            case "view" -> showCart(request, response);
            case "checkout" -> showCheckout(request, response);
            default -> showProducts(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing cart action.");
            return;
        }

        HttpSession session = request.getSession();
        Cart cart = getOrCreateCart(session);

        switch (action) {
            case "add" -> addProduct(request, response, cart);
            case "update" -> updateProduct(request, response, cart);
            case "remove" -> removeProduct(request, response, cart);
            case "complete" -> completeOrder(request, response, session);
            default -> response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Unknown cart action."
            );
        }
    }

    private void showProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("products", CATALOG.values());
        request.getRequestDispatcher("/WEB-INF/views/products.jsp")
                .forward(request, response);
    }

    private void showCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("quantity".equals(request.getParameter("error"))) {
            request.setAttribute("errorMessage", "Quantity must be a positive whole number.");
        }
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp")
                .forward(request, response);
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Boolean orderComplete = (Boolean) session.getAttribute("orderComplete");
        if (Boolean.TRUE.equals(orderComplete)) {
            request.setAttribute("orderComplete", true);
            session.removeAttribute("orderComplete");
        }
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp")
                .forward(request, response);
    }

    private void addProduct(
            HttpServletRequest request,
            HttpServletResponse response,
            Cart cart
    ) throws IOException {
        String productCode = request.getParameter("productCode");
        Product product = CATALOG.get(productCode);
        if (product == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product does not exist.");
            return;
        }

        synchronized (cart) {
            cart.add(product);
        }
        redirectTo(response, request, "?action=view");
    }

    private void updateProduct(
            HttpServletRequest request,
            HttpServletResponse response,
            Cart cart
    ) throws IOException {
        String productCode = request.getParameter("productCode");
        try {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            if (quantity < 1) {
                throw new NumberFormatException();
            }
            synchronized (cart) {
                cart.update(productCode, quantity);
            }
            redirectTo(response, request, "?action=view");
        } catch (NumberFormatException exception) {
            redirectTo(response, request, "?action=view&error=quantity");
        }
    }

    private void removeProduct(
            HttpServletRequest request,
            HttpServletResponse response,
            Cart cart
    ) throws IOException {
        synchronized (cart) {
            cart.remove(request.getParameter("productCode"));
        }
        redirectTo(response, request, "?action=view");
    }

    private void completeOrder(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session
    ) throws IOException {
        session.removeAttribute("cart");
        session.setAttribute("orderComplete", true);
        redirectTo(response, request, "?action=checkout");
    }

    private Cart getOrCreateCart(HttpSession session) {
        synchronized (session) {
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
                session.setAttribute("cart", cart);
            }
            return cart;
        }
    }

    private void redirectTo(
            HttpServletResponse response,
            HttpServletRequest request,
            String queryString
    ) throws IOException {
        String url = request.getContextPath() + "/cart" + queryString;
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    private static Map<String, Product> createCatalog() {
        Map<String, Product> products = new LinkedHashMap<>();
        products.put("8601", new Product(
                "8601",
                "86 (the band) - True Life Songs and Pictures",
                new BigDecimal("14.95")
        ));
        products.put("pf01", new Product(
                "pf01",
                "Paddlefoot - The first CD",
                new BigDecimal("12.95")
        ));
        products.put("pf02", new Product(
                "pf02",
                "Paddlefoot - The second CD",
                new BigDecimal("14.95")
        ));
        products.put("jr01", new Product(
                "jr01",
                "Joe Rut - Genuine Wood Grained Finish",
                new BigDecimal("14.95")
        ));
        return Collections.unmodifiableMap(products);
    }
}
