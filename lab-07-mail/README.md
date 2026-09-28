# Buổi 7: Email List gửi thư chào mừng

Bài thực hành chương 14, dựa vào phần EmailListServlet và MailUtil ở slide 21–28 của `Chapter 14 slides.pptx`. Người dùng đăng ký Email List, ứng dụng lưu user rồi gửi email chào mừng. Bài kế thừa JPA/HikariCP của buổi 6 và dùng Jakarta Mail với Eclipse Angus thay cho `javax.mail` trong slide cũ.

## Chức năng

- Form đăng ký và trang cảm ơn bằng JSP/CSS, không dựng HTML trong Java.
- JPA lưu thông tin, kiểm tra email trùng và ràng buộc `UNIQUE`.
- Gửi email dạng plain text, UTF-8, subject và lời chào theo slide 25.
- Hỗ trợ SMTP nội bộ không xác thực, SMTP xác thực qua STARTTLS hoặc SSL.
- Không có SMTP: vẫn chạy form, nhưng báo rõ email chưa gửi. SMTP lỗi: giữ đăng ký đã lưu và báo lỗi riêng.
- Post/Redirect/Get tránh gửi lại thư khi refresh trang cảm ơn.

## Phân chia code

| File | Trách nhiệm |
| --- | --- |
| `web/EmailListServlet.java` | Nhận GET/POST, kiểm tra form, gọi service và chọn JSP |
| `service/EmailListService.java` | Lưu đăng ký rồi yêu cầu gửi thư, xử lý email trùng và lỗi SMTP |
| `service/RegistrationResult.java` | Kết quả đăng ký và trạng thái gửi thư |
| `model/User.java` | JPA entity và thông tin người đăng ký |
| `data/UserRepository.java` | JPQL tìm user, transaction lưu user |
| `data/JpaLifecycle.java` | Mở/đóng EntityManagerFactory và dữ liệu mẫu |
| `mail/SmtpConfig.java` | Đọc và kiểm tra biến môi trường, che mật khẩu khi in cấu hình |
| `mail/WelcomeMailSender.java` | Interface để service không phụ thuộc cách gửi thư cụ thể |
| `mail/SmtpMailSender.java` | Tạo Properties, Session, MimeMessage và gửi qua Transport |
| `mail/DisabledMailSender.java` | Trạng thái chưa cấu hình SMTP, không giả lập gửi thành công |
| `META-INF/persistence.xml` | Cấu hình persistence unit, Hibernate, HikariCP và H2 |
| `WEB-INF/views/*.jsp`, `css/style.css` | Hiển thị form, lỗi và trang cảm ơn, escape dữ liệu người dùng |

Luồng POST: servlet kiểm tra dữ liệu, service gọi repository để commit đăng ký, sau đó gọi mail sender. Servlet lưu kết quả ngắn hạn trong session và trả HTTP 303 tới trang cảm ơn. GET trang cảm ơn lấy kết quả và xóa nó khỏi session. Đây là flash message, không phải bảng dữ liệu.

## Build và chạy local

Từ gốc repository:

```powershell
mvn -f .\lab-07-mail\pom.xml clean verify
```

WAR nằm tại `lab-07-mail/target/email-list-mail.war`. Chép WAR vào `webapps` của Tomcat 10.1, khởi động Tomcat và mở `http://localhost:8080/email-list-mail/emailList`.

Trong IntelliJ: mở **Run → Edit Configurations**, tạo/chọn **Tomcat Server → Local**, dùng Tomcat đã cài, rồi thêm artifact của bài này trong **Deployment**. Đặt **Application context** thành `/email-list-mail`. Nếu chưa có artifact, có thể chọn **External Source** và trỏ tới WAR đã build. Khi sửa code nhớ build lại WAR trước khi chạy.

H2 hiện chạy trong bộ nhớ và tạo lại schema khi ứng dụng khởi động. Các user mẫu là `jsmith@gmail.com`, `andi@murach.com`, `joel.murach@yahoo.com`. Không có lưu trữ bền vững và không tự gửi thư cho dữ liệu mẫu.

## Cấu hình SMTP

Ứng dụng đọc biến môi trường của **tiến trình Tomcat**, không tự đọc file `.env`. Trong cấu hình Tomcat của IntelliJ, tìm trường **Environment variables** (có thể phải bật qua **Modify options**) và nhập từng tên/giá trị. Không chọn lưu cấu hình chứa mật khẩu thành project file hoặc chia sẻ ảnh có mật khẩu. Nếu chạy Tomcat ngoài IDE, đặt biến trong môi trường của terminal dùng để khởi động Tomcat. Khởi động lại Tomcat sau khi thay đổi.

| Biến | Ý nghĩa |
| --- | --- |
| `SMTP_HOST` | Host SMTP, ví dụ `smtp.gmail.com` |
| `SMTP_FROM` | Email gửi, phải được nhà cung cấp cho phép |
| `SMTP_PORT` | Mặc định 587 với STARTTLS, 465 với SSL |
| `SMTP_SECURITY` | `starttls` (mặc định), `ssl`, hoặc `none` chỉ cho loopback |
| `SMTP_USERNAME` | Tài khoản SMTP; bỏ trống nếu server cục bộ không cần xác thực |
| `SMTP_PASSWORD` | Mật khẩu SMTP hoặc app password; nhập riêng trong môi trường chạy |

Ví dụ SMTP cục bộ/Mailpit: host `127.0.0.1`, port `1025`, security `none`, from `welcome@example.test`, không username/password. Phải có SMTP server đang chạy trên port đó. Thư bị giữ trong hộp thư thử nghiệm, không gửi ra Internet.

Ví dụ Gmail: host `smtp.gmail.com`, port `587`, security `starttls`, username và from là Gmail của bạn. Với tài khoản đủ điều kiện, dùng app password thay cho mật khẩu đăng nhập. Google yêu cầu bật 2-Step Verification để tạo app password và một số tài khoản không có tùy chọn này. [Hướng dẫn chính thức của Google](https://support.google.com/accounts/answer/185833?hl=en).

Code yêu cầu STARTTLS nếu chọn chế độ đó, kiểm tra danh tính certificate và đặt timeout kết nối/đọc/ghi 5 giây. Không bật debug SMTP và không đưa credentials vào Git. SMTP server nhận thư không đồng nghĩa người nhận chắc chắn nhận được trong inbox; kiểm tra spam và chính sách sender của nhà cung cấp.

## Deploy và giới hạn Render Free

Dockerfile build/test rồi deploy `ROOT.war`, nên form ở `/emailList`. Khi tạo service riêng, dùng Root Directory `lab-07-mail`, Dockerfile `./Dockerfile` và cấu hình SMTP qua mục Environment.

**Render Free chặn outbound SMTP ở các port 25, 465 và 587.** Vì vậy form và JPA có thể chạy trên Render Free, nhưng cấu hình Gmail SMTP chuẩn sẽ không gửi được thư. Để demo đúng chương 14, chạy local với SMTP thử nghiệm hoặc dùng môi trường có outbound SMTP được phép. Nếu cần gửi thư trên hosting miễn phí qua email API HTTPS, cần bổ sung sender khác; bài này chưa triển khai API đó. [Giới hạn chính thức của Render](https://render.com/docs/free).

Đây là bài thực hành, chưa có xác minh quyền sở hữu email, hạn mức gửi hoặc chống abuse. Không bật gửi mail công khai với tài khoản thật trước khi bổ sung các bảo vệ đó. Không nhập dữ liệu cá nhân thật vào demo.

## Kiểm thử

`mvn clean verify` chạy 13 test: cấu hình SMTP, xác thực, mail UTF-8, không hạ cấp TLS, lưu JPA trước gửi, email trùng không gửi lại, lỗi SMTP giữ đăng ký, chưa cấu hình không báo đã gửi. GreenMail chỉ chạy trong test trên loopback với port tự chọn, không chuyển thư tới server ngoài và không cần tài khoản Gmail.

Để demo thủ công:

1. Mở form khi chưa cấu hình SMTP: thấy thông báo chưa cấu hình.
2. Đăng ký email mới: thấy thông tin đã lưu nhưng thư chưa gửi.
3. Gửi lại email đó: thấy lỗi trùng.
4. Cấu hình SMTP thử nghiệm, restart và đăng ký email mới: kiểm tra subject, người nhận và body ở hộp thư thử nghiệm.
5. Dừng SMTP và đăng ký email khác: trang báo đã lưu nhưng gửi mail lỗi.
6. Refresh trang cảm ơn: không gửi thêm thư.

## Câu hỏi cần hiểu

- `jakarta.mail.Session` khác `HttpSession` thế nào? Mail Session giữ cấu hình SMTP, còn HTTP session giữ trạng thái giữa các request của một người dùng.
- `MimeMessage` là gì? Thư có headers, người gửi/nhận và nội dung MIME. `setText(..., "UTF-8")` tạo nội dung plain text.
- Vì sao dùng `Session.getInstance`? Mỗi ứng dụng có cấu hình riêng, tránh phụ thuộc default session dùng chung.
- SMTP khác POP/IMAP thế nào? Bài này dùng SMTP để gửi; POP/IMAP dùng để truy xuất thư.
- Vì sao không rollback đăng ký nếu mail lỗi? Transaction JPA chỉ kiểm soát database; gửi SMTP là thao tác bên ngoài, không rollback được bằng JPA.
- Vì sao dùng try-with-resources với Transport? Đóng kết nối cả khi gửi thành công lẫn khi có exception.
- Thư được SMTP chấp nhận có chắc tới inbox không? Không, mail có thể bị lọc, trả lại hoặc trì hoãn sau đó.

Nguồn thư viện: [Eclipse Angus Mail](https://eclipse-ee4j.github.io/angus-mail/), [GreenMail](https://greenmail-mail-test.github.io/greenmail/).
