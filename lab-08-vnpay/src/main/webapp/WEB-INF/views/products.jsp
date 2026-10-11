<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CD List · VNPay Sandbox</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell">
    <header class="site-header">
        <div><span class="eyebrow">Murach Music Store</span><h1>CD list</h1></div>
        <c:url var="cartUrl" value="/cart"><c:param name="action" value="view" /></c:url>
        <a class="button button-outline" href="${cartUrl}">Xem giỏ hàng →</a>
    </header>
    <p class="intro">Chọn một vài CD và thử quy trình thanh toán qua VNPay Sandbox.</p>
    <div class="notice">Đây là bài thực hành: không sử dụng tiền hoặc thẻ thật.</div>
    <div class="table-wrap">
        <table>
            <thead><tr><th>Mô tả</th><th>Giá</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="product" items="${products}">
                <tr>
                    <td><c:out value="${product.description}" /></td>
                    <td class="amount"><fmt:formatNumber value="${product.priceVnd}" maxFractionDigits="0" /> ₫</td>
                    <td>
                        <c:url var="postUrl" value="/cart" />
                        <form method="post" action="${postUrl}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productCode" value="${product.code}">
                            <button type="submit">Thêm vào giỏ</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
