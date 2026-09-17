<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>List of albums</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page-shell">
    <header class="site-header">
        <div class="brand-mark" aria-hidden="true">M</div>
        <div>
            <p class="eyebrow">Murach Music Store</p>
            <h1>List of albums</h1>
            <p class="lead">Choose an album to view its available downloads.</p>
        </div>
    </header>

    <section class="album-grid" aria-label="Albums">
        <c:forEach var="album" items="${albums}">
            <c:url var="albumUrl" value="/download">
                <c:param name="action" value="checkUser" />
                <c:param name="productCode" value="${album.code}" />
            </c:url>
            <article class="album-card">
                <div class="album-art" aria-hidden="true">
                    <span>${album.code}</span>
                </div>
                <div class="album-details">
                    <p class="artist"><c:out value="${album.artist}" /></p>
                    <h2><c:out value="${album.title}" /></h2>
                    <p>${album.tracks.size()} sample tracks</p>
                    <a class="button" href="${albumUrl}">View downloads</a>
                </div>
            </article>
        </c:forEach>
    </section>

    <c:if test="${not empty sessionScope.user}">
        <footer class="account-bar">
            Remembered as <strong><c:out value="${sessionScope.user.fullName}" /></strong>
            · <a href="${pageContext.request.contextPath}/download?action=forgetUser">Forget me</a>
        </footer>
    </c:if>
</main>
</body>
</html>
