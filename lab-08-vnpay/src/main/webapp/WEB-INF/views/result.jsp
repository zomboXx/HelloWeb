<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kết quả thanh toán · Murach Music Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/payment-result.js" defer></script>
</head>
<body>
<main class="shell narrow">
    <header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/cart"><span class="brand-mark" aria-hidden="true">◉</span><span>Murach <strong>Music</strong></span></a><span class="eyebrow">VNPay Sandbox</span></header>
    <div class="page-heading"><span class="eyebrow">Your order</span><h1>Kết quả thanh toán</h1></div>
    <section class="summary" aria-live="polite">
        <c:choose>
            <c:when test="${order.status eq 'PAID'}">
                <div class="status-icon" aria-hidden="true">✓</div>
                <h2 class="result-title">Thanh toán thành công!</h2>
                <p class="result-status">Cảm ơn bạn đã trải nghiệm Murach Music Store. Máy chủ đã nhận xác nhận hợp lệ từ VNPay.</p>
            </c:when>
            <c:when test="${order.status eq 'FAILED'}">
                <div class="status-icon failure" aria-hidden="true">×</div>
                <h2 class="result-title">Thanh toán không thành công</h2>
                <p class="result-status">Đơn hàng chưa được thanh toán. Bạn có thể chọn CD và thử lại.</p>
            </c:when>
            <c:otherwise>
                <div class="status-icon pending" aria-hidden="true">…</div>
                <h2 class="result-title">Đang chờ xác nhận</h2>
                <p class="result-status">VNPay chưa gửi IPN xác nhận tới máy chủ. Vui lòng kiểm tra lại sau ít phút.</p>
            </c:otherwise>
        </c:choose>
        <div class="result-details">
            <p>Mã đơn: <code><c:out value="${order.reference}" /></code></p>
            <p>Số tiền: <strong><fmt:formatNumber value="${order.amountVnd}" pattern="#,##0" /> ₫</strong></p>
            <c:if test="${not empty order.transactionNo}"><p>Mã giao dịch VNPay: <c:out value="${order.transactionNo}" /></p></c:if>
        </div>
    </section>
    <nav class="actions">
        <c:url var="productsUrl" value="/cart" />
        <a class="button button-outline" href="${productsUrl}">Về danh sách CD</a>
        <c:if test="${order.status eq 'PENDING'}">
            <c:url var="orderUrl" value="/order"><c:param name="ref" value="${order.reference}" /></c:url>
            <a class="button" href="${orderUrl}">Kiểm tra lại trạng thái</a>
        </c:if>
    </nav>
</main>

<c:if test="${order.status eq 'PAID'}">
    <dialog class="celebration" aria-labelledby="thanks-title" aria-describedby="thanks-message" data-success-dialog>
        <div class="celebration-inner">
            <div aria-hidden="true"><i class="confetti"></i><i class="confetti"></i><i class="confetti"></i><i class="confetti"></i><i class="confetti"></i><i class="confetti"></i><i class="confetti"></i></div>
            <svg class="smile-icon" viewBox="0 0 100 100" aria-hidden="true"><circle cx="50" cy="50" r="46" fill="#f1b967"/><circle cx="35" cy="41" r="4" fill="#21514d"/><circle cx="65" cy="41" r="4" fill="#21514d"/><path d="M30 57c9 18 31 24 42 0" fill="none" stroke="#21514d" stroke-width="5" stroke-linecap="round"/></svg>
            <h2 id="thanks-title">Thanh toán thành công! 🎉</h2>
            <p id="thanks-message">Cảm ơn bạn đã ghé nghe nhạc cùng chúng mình. Đơn hàng đã được VNPay xác nhận.</p>
            <form method="dialog"><button class="button" type="submit">Xem chi tiết đơn hàng</button></form>
        </div>
    </dialog>
</c:if>
</body>
</html>
