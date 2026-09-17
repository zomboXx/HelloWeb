<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Download registration</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page-shell narrow-shell">
    <a class="back-link" href="${pageContext.request.contextPath}/download">← View list of albums</a>

    <section class="panel">
        <p class="eyebrow">One quick step</p>
        <h1>Download registration</h1>
        <p class="lead">
            Register to view tracks from
            <strong><c:out value="${sessionScope.selectedAlbum.displayName}" /></strong>.
        </p>

        <c:if test="${not empty error}">
            <div class="error-message" role="alert"><c:out value="${error}" /></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/download" method="post" class="registration-form">
            <input type="hidden" name="action" value="registerUser">

            <label for="email">Email</label>
            <input id="email" name="email" type="email" required autocomplete="email"
                   value="<c:out value="${formUser.email}" />">

            <label for="firstName">First name</label>
            <input id="firstName" name="firstName" type="text" required autocomplete="given-name"
                   value="<c:out value="${formUser.firstName}" />">

            <label for="lastName">Last name</label>
            <input id="lastName" name="lastName" type="text" required autocomplete="family-name"
                   value="<c:out value="${formUser.lastName}" />">

            <button type="submit">Register</button>
        </form>

        <p class="privacy-note">Your registration is remembered in this browser for 30 days.</p>
    </section>
</main>
</body>
</html>
