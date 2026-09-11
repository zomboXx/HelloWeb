<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="container narrow">
    <p class="eyebrow">Murach Music Store</p>
    <h1>Checkout</h1>

    <c:choose>
        <c:when test="${orderComplete}">
            <section class="confirmation">
                <h2>Thank you for your order</h2>
                <p>Your shopping cart has been cleared.</p>
            </section>
        </c:when>
        <c:when test="${empty cart.items}">
            <section class="empty-state">
                <h2>There is nothing to checkout</h2>
                <p>Add at least one CD before completing an order.</p>
            </section>
        </c:when>
        <c:otherwise>
            <section class="checkout-summary">
                <h2>Order summary</h2>
                <ul>
                    <c:forEach var="item" items="${cart.items}">
                        <li>
                            <span>${item.quantity} × <c:out value="${item.product.description}" /></span>
                            <strong>
                                $<fmt:formatNumber value="${item.amount}" type="number"
                                                   minFractionDigits="2" maxFractionDigits="2" />
                            </strong>
                        </li>
                    </c:forEach>
                </ul>
                <p class="checkout-total">
                    <span>Total</span>
                    <strong>
                        $<fmt:formatNumber value="${cart.total}" type="number"
                                           minFractionDigits="2" maxFractionDigits="2" />
                    </strong>
                </p>

                <c:url var="cartUrl" value="/cart" />
                <form method="post" action="${cartUrl}">
                    <input type="hidden" name="action" value="complete">
                    <button type="submit">Complete Order</button>
                </form>
            </section>
        </c:otherwise>
    </c:choose>

    <nav class="actions" aria-label="Checkout actions">
        <c:url var="productsUrl" value="/cart">
            <c:param name="action" value="list" />
        </c:url>
        <c:url var="viewCartUrl" value="/cart">
            <c:param name="action" value="view" />
        </c:url>
        <a class="button secondary" href="${productsUrl}">CD list</a>
        <c:if test="${not orderComplete}">
            <a class="button secondary" href="${viewCartUrl}">Back to cart</a>
        </c:if>
    </nav>
</main>
</body>
</html>
