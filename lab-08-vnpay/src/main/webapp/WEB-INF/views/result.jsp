<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Payment result · VNPay Sandbox</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell narrow">
    <header class="site-header"><div><span class="eyebrow">Murach Music Store</span><h1>Kết quả thanh toán</h1></div></header>
    <section class="summary">
        <c:choose>
            <c:when test="${order.status eq 'PAID'}"><h2>Thanh toán thành công</h2><p>Máy chủ đã nhận IPN hợp lệ từ VNPay.</p></c:when>
            <c:when test="${order.status eq 'FAILED'}"><h2>Thanh toán không thành công</h2><p>Đơn hàng chưa được thanh toán.</p></c:when>
            <c:otherwise><h2>Đang chờ xác nhận</h2><p>VNPay chưa gửi IPN xác nhận tới máy chủ. Vui lòng kiểm tra lại sau ít phút.</p></c:otherwise>
        </c:choose>
        <p>Mã đơn: <code><c:out value="${order.reference}" /></code></p>
        <p>Số tiền: <strong><fmt:formatNumber value="${order.amountVnd}" pattern="#,##0" /> ₫</strong></p>
        <c:if test="${not empty order.transactionNo}"><p>Mã giao dịch VNPay: <c:out value="${order.transactionNo}" /></p></c:if>
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
</body>
</html>
