<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CD list</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="container">
    <header class="page-header">
        <div>
            <p class="eyebrow">Murach Music Store</p>
            <h1>CD list</h1>
        </div>
        <c:url var="viewCartUrl" value="/cart">
            <c:param name="action" value="view" />
        </c:url>
        <a class="cart-link" href="${viewCartUrl}">
            View cart (${cart.itemCount})
        </a>
    </header>

    <div class="table-wrapper">
        <table>
            <thead>
            <tr>
                <th>Description</th>
                <th class="money">Price</th>
                <th><span class="visually-hidden">Action</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="product" items="${products}">
                <tr>
                    <td><c:out value="${product.description}" /></td>
                    <td class="money">
                        $<fmt:formatNumber value="${product.price}" type="number"
                                           minFractionDigits="2" maxFractionDigits="2" />
                    </td>
                    <td class="action-cell">
                        <c:url var="cartUrl" value="/cart" />
                        <form method="post" action="${cartUrl}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productCode" value="${product.code}">
                            <button type="submit">Add To Cart</button>
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
