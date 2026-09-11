# Lab 03 - Shopping Cart với HttpSession

Ứng dụng mô phỏng giỏ hàng CD trong Chapter 7 của *Murach's Java Servlets and JSP*. Mỗi trình duyệt có một giỏ hàng riêng được lưu trong `HttpSession`.

Demo: [https://helloweb-1.onrender.com/cart?action=list](https://helloweb-1.onrender.com/cart?action=list)

## Chức năng

- Hiển thị danh sách CD.
- Thêm CD vào giỏ hàng.
- Tăng số lượng khi thêm lại cùng một CD.
- Cập nhật số lượng và tính lại thành tiền.
- Xóa sản phẩm.
- Xem tổng tiền và hoàn tất đơn hàng.

## Luồng xử lý chính

1. `index.jsp` chuyển request đầu tiên tới `/cart?action=list`.
2. `CartServlet` lấy hoặc tạo đối tượng `Cart` trong session.
3. Servlet xử lý hành động và chuyển dữ liệu sang JSP trong `WEB-INF/views`.
4. Sau các request `POST`, servlet redirect về một request `GET` để tránh lặp thao tác khi refresh.

Trình duyệt nhận cookie `JSESSIONID`. Tomcat dùng mã này để tìm lại session chứa giỏ hàng trong những request tiếp theo.

## Build

```powershell
mvn clean package
```

Kết quả được tạo tại:

```text
target/shopping-cart.war
```

Ứng dụng sử dụng Java 17, Jakarta Servlet 6.0, JSP 3.1, Jakarta Tags 3.0 và chạy trên Tomcat 10.1.
