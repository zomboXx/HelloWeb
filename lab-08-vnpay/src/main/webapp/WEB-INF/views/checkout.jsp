<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Checkout · VNPay Sandbox</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell narrow">
    <header class="site-header"><div><span class="eyebrow">Murach Music Store</span><h1>Thanh toán</h1></div></header>
    <c:if test="${not empty errorMessage}"><p class="error"><c:out value="${errorMessage}" /></p></c:if>
    <c:choose>
        <c:when test="${empty cart.items}"><p class="empty">Giỏ hàng trống. Hãy chọn CD trước.</p></c:when>
        <c:otherwise>
            <section class="summary">
                <h2>Đơn hàng của bạn</h2>
                <c:forEach var="item" items="${cart.items}">
                    <p class="summary-row"><span>${item.quantity} × <c:out value="${item.product.description}" /></span>
                        <strong><fmt:formatNumber value="${item.amountVnd}" maxFractionDigits="0" /> ₫</strong></p>
                </c:forEach>
                <p class="total">Cần thanh toán <strong><fmt:formatNumber value="${cart.totalVnd}" maxFractionDigits="0" /> ₫</strong></p>
            </section>
            <p class="notice">Bạn sẽ được chuyển tới cổng VNPay Sandbox. Không nhập thông tin thẻ thật.</p>
            <c:url var="postUrl" value="/cart" />
            <form method="post" action="${postUrl}">
                <input type="hidden" name="action" value="pay">
                <c:choose>
                    <c:when test="${paymentConfigured}"><button type="submit">Thanh toán với VNPay Sandbox →</button></c:when>
                    <c:otherwise><button type="submit" disabled>Thanh toán với VNPay Sandbox →</button></c:otherwise>
                </c:choose>
            </form>
            <c:if test="${not paymentConfigured}"><p class="hint">Cần cấu hình sandbox credentials trước khi thử thanh toán.</p></c:if>
        </c:otherwise>
    </c:choose>
    <nav class="actions"><c:url var="cartUrl" value="/cart"><c:param name="action" value="view" /></c:url>
        <a class="button button-outline" href="${cartUrl}">← Quay lại giỏ hàng</a></nav>
</main>
</body>
</html>
