package com.example.vnpay.web;

import com.example.vnpay.payment.Order;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

/** Keeps only this browser session's order references, never a public admin list. */
final class OrderSession {
    private static final String REFS = "orderRefs";
    private static final int MAX_REFS = 30;

    private OrderSession() { }

    static void remember(HttpSession session, String reference) {
        synchronized (session) {
            List<String> refs = new ArrayList<>(references(session));
            refs.remove(reference);
            refs.add(0, reference);
            if (refs.size() > MAX_REFS) refs = new ArrayList<>(refs.subList(0, MAX_REFS));
            session.setAttribute(REFS, List.copyOf(refs));
            session.setAttribute("pendingOrderRef", reference);
        }
    }

    static boolean owns(HttpSession session, String reference) {
        return session != null && reference != null && references(session).contains(reference);
    }

    static List<String> references(HttpSession session) {
        if (session == null) return List.of();
        synchronized (session) {
            Object value = session.getAttribute(REFS);
            if (value == null) return List.of();
            @SuppressWarnings("unchecked") List<String> refs = (List<String>) value;
            return refs;
        }
    }

    static void clearPaidCartIfCurrent(HttpSession session, Order order) {
        if (session == null || order.getStatus() != Order.Status.PAID) return;
        synchronized (session) {
            if (order.getReference().equals(session.getAttribute("pendingOrderRef"))) {
                session.removeAttribute("cart");
                session.removeAttribute("pendingOrderRef");
            }
        }
    }
}
