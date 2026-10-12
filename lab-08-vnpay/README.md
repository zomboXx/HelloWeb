# Lab 08 — Giỏ hàng CD và VNPay Sandbox

Ứng dụng Java 17 / Servlet / JSP độc lập. Luồng: chọn CD → giỏ hàng → tạo đơn hàng bằng **giá VND do server tính** → chuyển sang VNPay Sandbox → IPN xác nhận trạng thái → Return URL hiển thị kết quả.

Giao diện dùng bốn ảnh bìa **minh họa tự tạo** (không phải artwork chính thức của album), lưu tại `src/main/webapp/images/albums`. Popup cảm ơn chỉ xuất hiện khi trạng thái đơn hàng là `PAID` sau khi máy chủ xử lý IPN hợp lệ; chỉ quay về Return URL chưa đủ để hiện thông báo thành công.

> Chỉ dùng sandbox. Không nhập thẻ thật. Đơn hàng hiện lưu **trong RAM** để phục vụ bài thực hành; khi Render khởi động lại, trạng thái sẽ mất. Muốn dùng cho giao dịch thật phải thay `OrderStore` bằng cơ sở dữ liệu bền vững và xử lý đối soát.

Trang `/orders` cho người dùng cùng phiên trình duyệt xem các đơn đã tạo, album, số lượng và trạng thái. Đây **không phải trang admin** hay lịch sử bền vững: muốn quản lý mọi giao dịch phải dùng database cùng xác thực/phân quyền. Giỏ hàng chỉ được xóa sau khi đơn có trạng thái `PAID` do IPN xác nhận.

## Chạy local

```powershell
mvn clean verify
```

Deploy `target/vnpay-cart.war` vào Tomcat 10.1. Mở `/vnpay-cart/` nếu deploy theo tên WAR; mở `/` nếu deploy là `ROOT.war`.

Các biến môi trường cần điền **trên máy chủ**, không đưa vào Git:

| Tên | Ý nghĩa |
| --- | --- |
| `VNPAY_TMN_CODE` | Mã website của VNPay Sandbox |
| `VNPAY_HASH_SECRET` | Khóa bí mật ký HMAC-SHA512 |
| `VNPAY_RETURN_URL` | URL tuyệt đối, ví dụ `https://<ten-service>.onrender.com/vnpay/return` |

Đăng ký sandbox tại [cổng dành cho nhà phát triển VNPay](https://sandbox.vnpayment.vn/devreg/). Trên môi trường VNPay, thiết lập **IPN URL** là `https://<ten-service>.onrender.com/vnpay/ipn` (khác Return URL). Không cấu hình đủ 3 biến thì vẫn xem được giỏ hàng nhưng nút thanh toán bị khóa.

Nếu trang kết quả cứ ở `PENDING`, hãy kiểm tra VNPay đã được khai báo IPN URL trên chưa. Endpoint có thể truy cập công khai bằng HTTPS, nhưng chỉ xem trang Return URL không khiến đơn chuyển sang `PAID`. Trang kết quả tự kiểm tra trạng thái khoảng một phút; Render Logs ghi `VNPay IPN response code=...` khi có yêu cầu IPN. Không ghi Hash Secret vào log hoặc repository.

## Deploy trên Render

Tạo **Web Service** từ repo `HelloWeb`:

- Language: Docker
- Branch: `main`
- Root Directory: `lab-08-vnpay`
- Instance: Free nếu tài khoản cho phép
- Environment: ba biến ở bảng trên

Chọn URL service trước khi điền `VNPAY_RETURN_URL`, sau đó Save, rebuild và deploy. Nếu VNPay yêu cầu khai báo domain/Return URL/IPN URL trong trang sandbox, dùng URL của service này.

## Vì sao cần IPN?

`/vnpay/return` là đường **trình duyệt** quay về, có thể bị đóng hoặc người dùng tự truy cập, nên endpoint này chỉ hiển thị. `/vnpay/ipn` là thông báo **server-to-server**; code kiểm tra chữ ký, mã merchant, mã đơn, số tiền và trạng thái trước khi cập nhật đơn. IPN được xử lý idempotent: lần gọi lặp không tạo thêm giao dịch.

Tham khảo [tài liệu PAY v2.1.0 chính thức](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html).
