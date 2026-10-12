<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Giỏ hàng · Murach Music Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell">
    <header class="site-header">
        <a class="brand" href="${pageContext.request.contextPath}/cart"><span class="brand-mark" aria-hidden="true">◉</span><span>Murach <strong>Music</strong></span></a>
        <span class="eyebrow">VNPay Sandbox</span>
    </header>
    <div class="page-heading"><span class="eyebrow">Your selection</span><h1>Giỏ hàng của bạn</h1><p>Kiểm tra album và số lượng trước khi thanh toán.</p></div>
    <c:if test="${not empty errorMessage}"><p class="error"><c:out value="${errorMessage}" /></p></c:if>
    <section class="panel">
        <c:choose>
            <c:when test="${empty cart.items}"><p class="empty">Giỏ hàng đang trống. Hãy chọn một album bạn thích nhé.</p></c:when>
            <c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Album</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="item" items="${cart.items}">
                        <tr>
                            <td><div class="item-cell">
                                <img src="${pageContext.request.contextPath}${item.product.coverPath}" alt="" width="57" height="57">
                                <span><strong><c:out value="${item.product.title}" /></strong><small><c:out value="${item.product.artist}" /></small></span>
                            </div></td>
                            <td class="amount"><fmt:formatNumber value="${item.product.priceVnd}" pattern="#,##0" /> ₫</td>
                            <td>
                                <c:url var="postUrl" value="/cart" />
                                <form class="inline-form" method="post" action="${postUrl}">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="productCode" value="${item.product.code}">
                                    <input aria-label="Số lượng ${item.product.title}" type="number" name="quantity" min="1" max="99" value="${item.quantity}" required>
                                    <button type="submit" class="button-outline">Cập nhật</button>
                                </form>
                            </td>
                            <td class="amount"><fmt:formatNumber value="${item.amountVnd}" pattern="#,##0" /> ₫</td>
                            <td><form method="post" action="${postUrl}">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="productCode" value="${item.product.code}">
                                <button type="submit" class="text-button" aria-label="Xóa ${item.product.title}">Xóa</button>
                            </form></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table></div>
                <p class="total">Tổng cộng <strong><fmt:formatNumber value="${cart.totalVnd}" pattern="#,##0" /> ₫</strong></p>
            </c:otherwise>
        </c:choose>
    </section>
    <nav class="actions">
        <c:url var="productsUrl" value="/cart" />
        <a class="button button-outline" href="${productsUrl}">← Tiếp tục chọn CD</a>
        <c:if test="${not empty cart.items}">
            <c:url var="checkoutUrl" value="/cart"><c:param name="action" value="checkout" /></c:url>
            <a class="button" href="${checkoutUrl}">Đến thanh toán →</a>
        </c:if>
    </nav>
</main>
</body>
</html>
