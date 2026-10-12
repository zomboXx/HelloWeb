<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thanh toán · Murach Music Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell narrow">
    <header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/cart"><span class="brand-mark" aria-hidden="true">◉</span><span>Murach <strong>Music</strong></span></a><c:url var="ordersUrl" value="/orders" /><a class="header-link" href="${ordersUrl}">Đơn hàng của tôi</a></header>
    <div class="page-heading"><span class="eyebrow">Almost there</span><h1>Xác nhận đơn hàng</h1><p>Kiểm tra lần cuối trước khi chuyển sang cổng thanh toán.</p></div>
    <c:if test="${not empty errorMessage}"><p class="error"><c:out value="${errorMessage}" /></p></c:if>
    <c:choose>
        <c:when test="${empty cart.items}"><section class="panel"><p class="empty">Giỏ hàng trống. Hãy chọn CD trước.</p></section></c:when>
        <c:otherwise>
            <section class="summary">
                <h2>Đơn hàng của bạn</h2>
                <c:forEach var="item" items="${cart.items}">
                    <div class="summary-row">
                        <span class="summary-item"><img src="${pageContext.request.contextPath}${item.product.coverPath}" alt="" width="57" height="57">
                            <span><strong><c:out value="${item.product.title}" /></strong><small>${item.quantity} × <c:out value="${item.product.artist}" /></small></span></span>
                        <strong class="amount"><fmt:formatNumber value="${item.amountVnd}" pattern="#,##0" /> ₫</strong>
                    </div>
                </c:forEach>
                <p class="total">Cần thanh toán <strong><fmt:formatNumber value="${cart.totalVnd}" pattern="#,##0" /> ₫</strong></p>
            </section>
            <p class="notice">Bạn sẽ được chuyển tới VNPay Sandbox. Chỉ dùng thông tin thẻ thử nghiệm, không nhập thẻ thật.</p>
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
