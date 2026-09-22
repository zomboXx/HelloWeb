<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Join our email list</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page">
    <p class="back"><a href="${pageContext.request.contextPath}/">All exercises</a></p>
    <h1>Join our email list</h1>
    <p>To join our email list, enter your name and email address below.</p>

    <c:if test="${not empty error}">
        <p class="error" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/emailList">
        <div class="form-row">
            <label for="email">Email:</label>
            <input id="email" name="email" type="email" maxlength="255" required
                   value="<c:out value='${email}' />">
        </div>
        <div class="form-row">
            <label for="firstName">First Name:</label>
            <input id="firstName" name="firstName" maxlength="100" required
                   value="<c:out value='${firstName}' />">
        </div>
        <div class="form-row">
            <label for="lastName">Last Name:</label>
            <input id="lastName" name="lastName" maxlength="100" required
                   value="<c:out value='${lastName}' />">
        </div>
        <div class="form-actions"><button type="submit">Join Now</button></div>
    </form>
</main>
</body>
</html>
