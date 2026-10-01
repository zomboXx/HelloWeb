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
    <p class="back"><a href="${pageContext.request.contextPath}/">Home</a></p>
    <h1>Thanks for joining our email list</h1>
    <p>We saved your registration:</p>
    <dl>
        <dt>Email:</dt><dd><c:out value="${user.email}" /></dd>
        <dt>First Name:</dt><dd><c:out value="${user.firstName}" /></dd>
        <dt>Last Name:</dt><dd><c:out value="${user.lastName}" /></dd>
    </dl>
    <c:choose>
        <c:when test="${mailStatus == 'ACCEPTED_BY_SMTP'}">
            <p role="status">Your welcome email was accepted by the SMTP server. Please check your inbox and spam folder.</p>
        </c:when>
        <c:when test="${mailStatus == 'ACCEPTED_BY_API'}">
            <p class="note" role="status">
                SMTP is preferred, but
                <c:choose>
                    <c:when test="${fallbackReason == 'SMTP_DISABLED'}">it is disabled on this host.</c:when>
                    <c:when test="${fallbackReason == 'SMTP_NOT_CONFIGURED'}">it is not configured.</c:when>
                    <c:otherwise>its connection could not be established.</c:otherwise>
                </c:choose>
                We used the <c:out value="${mailProvider}" /> HTTPS email API instead.
            </p>
            <p role="status">The email provider accepted your welcome email. Please check your inbox and spam folder.</p>
        </c:when>
        <c:when test="${mailStatus == 'NOT_CONFIGURED'}">
            <p class="note" role="status">Your registration is saved, but the welcome email was not sent because neither SMTP nor an HTTPS email API is enabled and configured.</p>
        </c:when>
        <c:when test="${mailStatus == 'UNCONFIRMED'}">
            <p class="error" role="alert">Your registration is saved, but we could not confirm email acceptance. No automatic retry was made to avoid duplicate emails. Please check your inbox and contact the site administrator.</p>
        </c:when>
        <c:otherwise>
            <p class="error" role="alert">Your registration is saved, but email sending did not complete successfully. Please contact the site administrator.</p>
        </c:otherwise>
    </c:choose>
    <p><a href="${pageContext.request.contextPath}/emailList">Return to the registration form</a></p>
</main>
</body>
</html>
