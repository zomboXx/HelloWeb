<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your cart</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="container">
    <header class="page-header">
        <div>
            <p class="eyebrow">Murach Music Store</p>
            <h1>Your cart</h1>
        </div>
        <span class="cart-count">${cart.itemCount} item(s)</span>
    </header>

    <c:if test="${not empty errorMessage}">
        <p class="message error"><c:out value="${errorMessage}" /></p>
    </c:if>

    <c:choose>
        <c:when test="${empty cart.items}">
            <section class="empty-state">
                <h2>Your cart is empty</h2>
                <p>Choose a CD from the product list to begin.</p>
            </section>
        </c:when>
        <c:otherwise>
            <div class="table-wrapper">
                <table>
                    <thead>
                    <tr>
                        <th>Quantity</th>
                        <th>Description</th>
                        <th class="money">Price</th>
                        <th class="money">Amount</th>
                        <th><span class="visually-hidden">Action</span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" items="${cart.items}">
                        <tr>
                            <td>
                                <c:url var="cartUrl" value="/cart" />
                                <form class="quantity-form" method="post" action="${cartUrl}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="productCode" value="${item.product.code}">
                                    <label class="visually-hidden" for="quantity-${item.product.code}">
                                        Quantity for <c:out value="${item.product.description}" />
                                    </label>
                                    <input id="quantity-${item.product.code}"
                                           class="quantity-input"
                                           type="number"
                                           name="quantity"
                                           min="1"
                                           value="${item.quantity}"
                                           required>
                                    <button type="submit">Update</button>
                                </form>
                            </td>
                            <td><c:out value="${item.product.description}" /></td>
                            <td class="money">
                                $<fmt:formatNumber value="${item.product.price}" type="number"
                                                   minFractionDigits="2" maxFractionDigits="2" />
                            </td>
                            <td class="money">
                                $<fmt:formatNumber value="${item.amount}" type="number"
                                                   minFractionDigits="2" maxFractionDigits="2" />
                            </td>
                            <td class="action-cell">
                                <form method="post" action="${cartUrl}">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="productCode" value="${item.product.code}">
                                    <button class="danger" type="submit">Remove Item</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr>
                        <th colspan="3">Total</th>
                        <th class="money">
                            $<fmt:formatNumber value="${cart.total}" type="number"
                                               minFractionDigits="2" maxFractionDigits="2" />
                        </th>
                        <th></th>
                    </tr>
                    </tfoot>
                </table>
            </div>
            <p class="hint"><strong>To change the quantity,</strong> enter a new value and click Update.</p>
        </c:otherwise>
    </c:choose>

    <nav class="actions" aria-label="Cart actions">
        <c:url var="productsUrl" value="/cart">
            <c:param name="action" value="list" />
        </c:url>
        <a class="button secondary" href="${productsUrl}">Continue Shopping</a>

        <c:if test="${not empty cart.items}">
            <c:url var="checkoutUrl" value="/cart">
                <c:param name="action" value="checkout" />
            </c:url>
            <a class="button" href="${checkoutUrl}">Checkout</a>
        </c:if>
    </nav>
</main>
</body>
</html>
