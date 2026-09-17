<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Downloads</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="page-shell narrow-shell">
    <a class="back-link" href="${pageContext.request.contextPath}/download">← View list of albums</a>

    <section class="panel">
        <div class="download-heading">
            <div>
                <p class="eyebrow">Downloads</p>
                <h1><c:out value="${sessionScope.selectedAlbum.title}" /></h1>
                <p class="lead"><c:out value="${sessionScope.selectedAlbum.artist}" /></p>
            </div>
            <div class="album-art small" aria-hidden="true">
                <span><c:out value="${sessionScope.selectedAlbum.code}" /></span>
            </div>
        </div>

        <p class="welcome-message">
            Welcome, <strong><c:out value="${sessionScope.user.firstName}" /></strong>.
            These are the sample tracks available for this album.
        </p>

        <div class="table-wrap">
            <table>
                <thead>
                <tr>
                    <th scope="col">Song title</th>
                    <th scope="col">Audio format</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="track" items="${sessionScope.selectedAlbum.tracks}">
                    <tr>
                        <td><c:out value="${track.title}" /></td>
                        <td><span class="format-badge"><c:out value="${track.format}" /></span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="actions">
            <a class="button" href="${pageContext.request.contextPath}/download">Choose another album</a>
            <a class="text-button" href="${pageContext.request.contextPath}/download?action=forgetUser">Forget my registration</a>
        </div>
    </section>
</main>
</body>
</html>
