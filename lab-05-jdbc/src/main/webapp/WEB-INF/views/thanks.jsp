<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thank you</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page">
    <p class="back"><a href="${pageContext.request.contextPath}/">All exercises</a></p>
    <h1>Thank you for joining!</h1>
    <p>We saved <strong><c:out value="${email}" /></strong> for
       <c:out value="${firstName}" /> <c:out value="${lastName}" />.</p>
    <p><a href="${pageContext.request.contextPath}/emailList">Add another address</a></p>
</main>
</body>
</html>
