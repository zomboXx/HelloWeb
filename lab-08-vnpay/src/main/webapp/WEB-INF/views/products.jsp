<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#102d31">
    <title>Khám phá CD · Murach Music Store</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<main class="shell">
    <header class="site-header">
        <a class="brand" href="${pageContext.request.contextPath}/cart" aria-label="Murach Music Store - trang chủ">
            <span class="brand-mark" aria-hidden="true">◉</span><span>Murach <strong>Music</strong></span>
        </a>
        <c:url var="cartUrl" value="/cart"><c:param name="action" value="view" /></c:url>
        <a class="button button-outline" href="${cartUrl}">Giỏ hàng <span aria-hidden="true">↗</span></a>
    </header>

    <section class="hero" aria-labelledby="page-title">
        <div class="hero-copy">
            <span class="eyebrow">Bộ sưu tập âm nhạc</span>
            <h1 id="page-title">Những chiếc CD<br><em>đáng nghe lại.</em></h1>
            <p>Chọn album bạn thích, thêm vào giỏ và trải nghiệm thanh toán qua VNPay Sandbox.</p>
            <span class="hero-note"><span aria-hidden="true">✦</span> Demo học tập · Không dùng tiền thật</span>
        </div>
        <div class="hero-record" aria-hidden="true"><div class="record-label"><span>MADE FOR<br>THE MOMENT</span></div></div>
    </section>

    <section class="catalog" aria-labelledby="catalog-title">
        <div class="section-heading">
            <div><span class="eyebrow">The collection</span><h2 id="catalog-title">Danh sách album</h2></div>
            <span class="item-count">04 album</span>
        </div>
        <div class="product-grid">
            <c:forEach var="product" items="${products}">
                <article class="product-card">
                    <img class="album-cover" src="${pageContext.request.contextPath}${product.coverPath}"
                         alt="Ảnh bìa minh họa cho album ${product.title} của ${product.artist}" loading="lazy" width="480" height="480">
                    <div class="product-meta">
                        <span class="product-artist"><c:out value="${product.artist}" /></span>
                        <h3><c:out value="${product.title}" /></h3>
                        <div class="product-bottom">
                            <strong class="amount"><fmt:formatNumber value="${product.priceVnd}" pattern="#,##0" /> ₫</strong>
                            <c:url var="postUrl" value="/cart" />
                            <form method="post" action="${postUrl}">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="productCode" value="${product.code}">
                                <button type="submit" class="add-button" aria-label="Thêm ${product.title} vào giỏ hàng">Thêm <span aria-hidden="true">＋</span></button>
                            </form>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>
    <footer class="site-footer"><span>Murach Music Store</span><span>VNPay Sandbox · Bài thực hành</span></footer>
</main>
</body>
</html>
