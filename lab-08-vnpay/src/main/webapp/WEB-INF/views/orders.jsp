<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đơn hàng của tôi · Murach Music Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell narrow">
    <header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/cart"><span class="brand-mark" aria-hidden="true">◉</span><span>Murach <strong>Music</strong></span></a><c:url var="cartUrl" value="/cart"><c:param name="action" value="view" /></c:url><a class="button button-outline" href="${cartUrl}">Giỏ hàng ↗</a></header>
    <div class="page-heading"><span class="eyebrow">Order history</span><h1>Đơn hàng của tôi</h1><p>Xem lại những album đã chọn và trạng thái thanh toán của từng lần thử.</p></div>
    <p class="notice">Bài thực hành lưu đơn tạm trong phiên trình duyệt và RAM của Render. Dữ liệu có thể mất khi phiên hết hạn hoặc server khởi động lại; đây chưa phải lịch sử giao dịch bền vững.</p>
    <c:choose>
        <c:when test="${empty orders}"><section class="panel"><p class="empty">Chưa có đơn hàng nào trong phiên này.</p></section></c:when>
        <c:otherwise>
            <div class="order-list">
                <c:forEach var="order" items="${orders}">
                    <article class="panel order-card">
                        <div class="order-card-header">
                            <div><span class="eyebrow">Đơn ${order.createdAtText}</span><h2><code><c:out value="${order.reference}" /></code></h2></div>
                            <c:choose>
                                <c:when test="${order.status eq 'PAID'}"><span class="status-badge paid">Đã thanh toán</span></c:when>
                                <c:when test="${order.status eq 'FAILED'}"><span class="status-badge failed">Không thành công</span></c:when>
                                <c:otherwise><span class="status-badge pending">Chờ xác nhận</span></c:otherwise>
                            </c:choose>
                        </div>
                        <div class="order-items">
                            <c:forEach var="item" items="${order.items}">
                                <div class="summary-row"><span class="summary-item"><img src="${pageContext.request.contextPath}${item.product.coverPath}" alt="" width="57" height="57"><span><strong><c:out value="${item.product.title}" /></strong><small>${item.quantity} × <c:out value="${item.product.artist}" /></small></span></span><strong class="amount"><fmt:formatNumber value="${item.amountVnd}" pattern="#,##0" /> ₫</strong></div>
                            </c:forEach>
                        </div>
                        <div class="order-card-footer"><strong><fmt:formatNumber value="${order.amountVnd}" pattern="#,##0" /> ₫</strong><c:url var="detailUrl" value="/order"><c:param name="ref" value="${order.reference}" /></c:url><a class="button button-outline" href="${detailUrl}">Xem chi tiết →</a></div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
    <nav class="actions"><a class="button button-outline" href="${pageContext.request.contextPath}/cart">← Tiếp tục chọn CD</a></nav>
</main>
</body>
</html>
