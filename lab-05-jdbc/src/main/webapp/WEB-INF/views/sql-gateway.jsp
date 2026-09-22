<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>The SQL Gateway</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page gateway">
    <p class="back"><a href="${pageContext.request.contextPath}/">All exercises</a></p>
    <h1>The SQL Gateway</h1>
    <p>Enter an SQL statement and click the Execute button.</p>
    <form method="post" action="${pageContext.request.contextPath}/sqlGateway">
        <label class="block-label" for="sql">SQL statement:</label>
        <textarea id="sql" name="sql" rows="8" maxlength="2000" spellcheck="false" required><c:out value="${sql}" /></textarea>
        <button type="submit">Execute</button>
    </form>

    <c:if test="${not empty error}">
        <h2>SQL result:</h2>
        <p class="error" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <c:if test="${hasResult}">
        <h2>SQL result:</h2>
        <c:choose>
            <c:when test="${query}">
                <table>
                    <thead><tr>
                        <c:forEach var="column" items="${columns}">
                            <th scope="col"><c:out value="${column}" /></th>
                        </c:forEach>
                    </tr></thead>
                    <tbody>
                        <c:forEach var="row" items="${rows}">
                            <tr><c:forEach var="cell" items="${row}">
                                <td><c:out value="${cell}" /></td>
                            </c:forEach></tr>
                        </c:forEach>
                    </tbody>
                </table>
                <p><c:out value="${rows.size()}" /> row(s) returned (maximum 100).</p>
            </c:when>
            <c:otherwise>
                <p>The statement executed successfully.<br>
                   <c:out value="${affectedRows}" /> row(s) affected.</p>
            </c:otherwise>
        </c:choose>
    </c:if>
    <p class="note">This gateway operates on a disposable classroom database. Never expose unrestricted SQL against a real database.</p>
</main>
</body>
</html>
