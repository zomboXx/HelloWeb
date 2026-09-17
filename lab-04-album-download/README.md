# Buổi 4 — Album Download (Chapter 7)

Ứng dụng minh họa cách Java Servlet/JSP duy trì trạng thái người dùng bằng `HttpSession` và cookie.

## Luồng sử dụng

1. `GET /download` hiển thị danh sách bốn album.
2. Chọn một album gửi `productCode` đến `DownloadServlet`.
3. Servlet lưu cả object `Album` vào session với tên `selectedAlbum`.
4. Người dùng chưa đăng ký được chuyển đến form đăng ký.
5. `POST /download` kiểm tra dữ liệu, lưu `User` vào session và ghi cookie trong 30 ngày.
6. Servlet redirect sang trang Downloads để tránh gửi lại form khi refresh.
7. Lần chọn album tiếp theo, cookie giúp người dùng bỏ qua form đăng ký.

Các JSP nằm trong `WEB-INF/views`, nên trình duyệt không thể truy cập trực tiếp mà phải đi qua servlet.

## Công nghệ

- Java 17
- Jakarta Servlet 6.0
- JSP và JSTL 3
- Maven WAR
- Tomcat 10.1
- Docker/Render

## Chạy local

Build WAR:

```powershell
mvn clean package
```

Deploy `target/album-download.war` lên Tomcat. Nếu giữ nguyên tên WAR, URL là:

```text
http://localhost:8080/album-download/
```

Nếu deploy bằng Dockerfile/Render, WAR được đổi thành `ROOT.war`, nên mở trực tiếp URL gốc của service.

## Deploy trên Render

Tạo một Web Service mới từ repository `HelloWeb` với cấu hình:

- Branch: `main`
- Language: `Docker`
- Root Directory: `lab-04-album-download`
- Instance Type: `Free`

Dockerfile tự lấy biến `PORT` do Render cấp và chạy ứng dụng tại context path `/`.
