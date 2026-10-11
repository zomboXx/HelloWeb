<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Your Cart · VNPay Sandbox</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell">
    <header class="site-header"><div><span class="eyebrow">Murach Music Store</span><h1>Your cart</h1></div></header>
    <c:if test="${not empty errorMessage}"><p class="error"><c:out value="${errorMessage}" /></p></c:if>
    <c:choose>
        <c:when test="${empty cart.items}"><p class="empty">Giỏ hàng của bạn đang trống.</p></c:when>
        <c:otherwise>
            <div class="table-wrap"><table>
                <thead><tr><th>CD</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="item" items="${cart.items}">
                    <tr>
                        <td><c:out value="${item.product.description}" /></td>
                        <td class="amount"><fmt:formatNumber value="${item.product.priceVnd}" pattern="#,##0" /> ₫</td>
                        <td>
                            <c:url var="postUrl" value="/cart" />
                            <form class="inline-form" method="post" action="${postUrl}">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="productCode" value="${item.product.code}">
                                <input aria-label="Số lượng" type="number" name="quantity" min="1" max="99" value="${item.quantity}" required>
                                <button type="submit" class="button-outline">Cập nhật</button>
                            </form>
                        </td>
                        <td class="amount"><fmt:formatNumber value="${item.amountVnd}" pattern="#,##0" /> ₫</td>
                        <td>
                            <form method="post" action="${postUrl}">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="productCode" value="${item.product.code}">
                                <button type="submit" class="text-button">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table></div>
            <p class="total">Tổng cộng <strong><fmt:formatNumber value="${cart.totalVnd}" pattern="#,##0" /> ₫</strong></p>
        </c:otherwise>
    </c:choose>
    <nav class="actions">
        <c:url var="productsUrl" value="/cart" />
        <a class="button button-outline" href="${productsUrl}">← Tiếp tục mua hàng</a>
        <c:if test="${not empty cart.items}">
            <c:url var="checkoutUrl" value="/cart"><c:param name="action" value="checkout" /></c:url>
            <a class="button" href="${checkoutUrl}">Đến thanh toán →</a>
        </c:if>
    </nav>
</main>
</body>
</html>
