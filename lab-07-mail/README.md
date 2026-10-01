# Buổi 7: Email List gửi thư chào mừng

Bài thực hành chương 14, dựa vào phần EmailListServlet và MailUtil ở slide 21–28 của `Chapter 14 slides.pptx`. Người dùng đăng ký Email List, ứng dụng lưu user rồi gửi email chào mừng. Bài kế thừa JPA/HikariCP của buổi 6 và dùng Jakarta Mail với Eclipse Angus thay cho `javax.mail` trong slide cũ.

## Chức năng

- Form đăng ký và trang cảm ơn bằng JSP/CSS, không dựng HTML trong Java.
- JPA lưu thông tin, kiểm tra email trùng và ràng buộc `UNIQUE`.
- Gửi email dạng plain text, UTF-8, subject và lời chào theo slide 25.
- Hỗ trợ SMTP nội bộ không xác thực, SMTP xác thực qua STARTTLS hoặc SSL.
- Ưu tiên SMTP; hỗ trợ API HTTPS của Brevo hoặc Resend khi SMTP chưa cấu hình, bị tắt, hoặc không kết nối được trước khi gửi.
- Không có phương án gửi: vẫn chạy form, nhưng báo rõ email chưa gửi. Gửi lỗi: giữ đăng ký đã lưu và báo lỗi riêng.
- Thông báo phương án đã sử dụng và lý do chuyển sang API; không giả vờ SMTP lỗi khi chưa thử kết nối.
- Post/Redirect/Get tránh gửi lại thư khi refresh trang cảm ơn.

## Phân chia code

| File | Trách nhiệm |
| --- | --- |
| `web/EmailListServlet.java` | Nhận GET/POST, kiểm tra form, gọi service và chọn JSP |
| `service/EmailListService.java` | Lưu đăng ký rồi yêu cầu gửi thư, xử lý email trùng và trạng thái SMTP/API |
| `service/RegistrationResult.java` | Kết quả đăng ký và trạng thái gửi thư |
| `model/User.java` | JPA entity và thông tin người đăng ký |
| `data/UserRepository.java` | JPQL tìm user, transaction lưu user |
| `data/JpaLifecycle.java` | Mở/đóng EntityManagerFactory và dữ liệu mẫu |
| `mail/SmtpConfig.java` | Đọc và kiểm tra biến môi trường, che mật khẩu khi in cấu hình |
| `mail/WelcomeMailSender.java` | Interface để service không phụ thuộc cách gửi thư cụ thể |
| `mail/SmtpMailSender.java` | Tạo Properties, Session, MimeMessage và gửi qua Transport |
| `mail/WelcomeMessage.java` | Subject và body dùng chung cho SMTP và API |
| `mail/EmailApiConfig.java` | Kiểm tra provider, API key và người gửi; không in key |
| `mail/HttpApiMailSender.java` | Java 17 HttpClient gọi HTTPS API, Gson tạo JSON đúng định dạng từng provider |
| `mail/SmtpFirstMailSender.java` | Quyết định SMTP trước, API sau; không gửi lại khi kết quả SMTP không chắc chắn |
| `mail/MailSenders.java` | Tạo các sender từ môi trường, không đặt cấu hình gửi mail trong servlet |
| `mail/MailDelivery.java` | Phương án gửi, provider và lý do fallback sau khi server chấp nhận |
| `mail/SmtpUnavailableException.java` | Kết nối/xác thực SMTP thất bại trước khi gửi, có thể fallback an toàn |
| `mail/MailDeliveryUncertainException.java` | Chưa xác nhận được chấp nhận thư, không tự retry |
| `mail/DisabledMailSender.java` | Trạng thái chưa cấu hình gửi mail, không giả lập gửi thành công |
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
| `SMTP_ENABLED` | Mặc định `true`; đặt `false` trên hosting chặn SMTP để không phải chờ timeout |

Ví dụ SMTP cục bộ/Mailpit: host `127.0.0.1`, port `1025`, security `none`, from `welcome@example.test`, không username/password. Phải có SMTP server đang chạy trên port đó. Thư bị giữ trong hộp thư thử nghiệm, không gửi ra Internet.

Ví dụ Gmail: host `smtp.gmail.com`, port `587`, security `starttls`, username và from là Gmail của bạn. Với tài khoản đủ điều kiện, dùng app password thay cho mật khẩu đăng nhập. Google yêu cầu bật 2-Step Verification để tạo app password và một số tài khoản không có tùy chọn này. [Hướng dẫn chính thức của Google](https://support.google.com/accounts/answer/185833?hl=en).

Code yêu cầu STARTTLS nếu chọn chế độ đó, kiểm tra danh tính certificate và đặt timeout kết nối/đọc/ghi 5 giây. Không bật debug SMTP và không đưa credentials vào Git. SMTP server nhận thư không đồng nghĩa người nhận chắc chắn nhận được trong inbox; kiểm tra spam và chính sách sender của nhà cung cấp.

## Phương án HTTPS email API

Đây là phần mở rộng theo đề xuất của trợ giảng, không thay thế phần Jakarta Mail đã học. Brevo/Resend là **dịch vụ gửi thư**, không phải hosting chạy website. Web vẫn chạy trên Render; backend gọi dịch vụ qua HTTPS/443. Cần tạo tài khoản, xác minh người gửi theo chính sách provider, và tạo API key trước khi gửi thật.

| Biến | Ý nghĩa |
| --- | --- |
| `EMAIL_API_PROVIDER` | `brevo` hoặc `resend` |
| `EMAIL_API_KEY` | API key bí mật của dịch vụ đã chọn, không phải mật khẩu Gmail |
| `EMAIL_API_FROM` | Một địa chỉ email gửi được provider cho phép, không kèm tên hiển thị |

Đặt cả ba biến để bật API; bỏ cả ba nếu chỉ dùng SMTP. Cấu hình dở dang hoặc provider không hỗ trợ sẽ báo lỗi khi khởi động, không báo thành công giả. Không có endpoint tùy ý: production chỉ gọi URL HTTPS cố định của Brevo/Resend; không theo redirect để tránh lộ key. Timeout kết nối 5 giây, mỗi request API tối đa 8 giây. Không log key, body thư hoặc nội dung response lỗi. Chỉ HTTP 2xx **kèm message ID hợp lệ** được coi là provider đã chấp nhận, không phải bảo đảm inbox đã nhận.

### Luồng ưu tiên và tránh thư trùng

1. `SMTP_ENABLED=true` và có cấu hình SMTP: thử SMTP trước.
2. Kết nối, TLS hoặc xác thực SMTP thất bại **trước `sendMessage`**: gọi API nếu đã cấu hình.
3. SMTP bị tắt hoặc chưa cấu hình: gọi API ngay, thông báo đúng lý do.
4. Lỗi trong lúc gửi SMTP: có thể server đã nhận thư; **không fallback** để tránh gửi trùng. Trang cảm ơn báo chưa xác nhận được.
5. API lỗi/timeout: không tự retry; đăng ký vẫn được lưu. Resend nhận thêm `Idempotency-Key` riêng cho mỗi lần gửi. Đây không phải cơ chế exactly-once xuyên suốt restart hay giữa SMTP và API.

Đăng ký email đã tồn tại không gửi thêm thư. H2 vẫn là database trong bộ nhớ: restart sẽ mất đăng ký và thông tin chống trùng trước đó.

Tài liệu: [Brevo gửi transactional email](https://developers.brevo.com/reference/send-transac-email), [Resend Send Email](https://resend.com/docs/api-reference/emails/send-email), [Resend idempotency](https://resend.com/docs/dashboard/emails/idempotency-keys).

**Resend:** địa chỉ thử nghiệm `onboarding@resend.dev` chỉ gửi tới email của chính tài khoản Resend. Muốn gửi tới email bất kỳ được nhập trong form phải xác minh domain riêng và dùng địa chỉ gửi thuộc domain đó. [Giới hạn chính thức](https://resend.com/docs/knowledge-base/403-error-resend-dev-domain). Brevo cũng cần người gửi hợp lệ/xác minh và tài khoản được phép gửi transactional mail; tạo API key không tự bảo đảm đủ điều kiện gửi.

## Deploy và giới hạn Render Free

Dockerfile build/test rồi deploy `ROOT.war`, nên form ở `/emailList`. Khi tạo service riêng, dùng Root Directory `lab-07-mail`, Dockerfile `./Dockerfile` và cấu hình SMTP qua mục Environment.

**Render Free chặn outbound SMTP ở các port 25, 465 và 587.** Vì vậy cấu hình Gmail SMTP chuẩn sẽ không gửi được thư. Để demo SMTP chương 14, chạy local hoặc dùng môi trường cho phép outbound SMTP. Trên Render Free có thể dùng adapter HTTPS đã bổ sung. [Giới hạn chính thức của Render](https://render.com/docs/free#other-limitations).

Trong Render → service của buổi 7 → **Environment**, đặt:

```text
SMTP_ENABLED=false
EMAIL_API_PROVIDER=brevo
EMAIL_API_KEY=<nhập key bí mật của bạn trực tiếp trên Render>
EMAIL_API_FROM=<địa chỉ người gửi đã được cho phép>
```

Hoặc chọn `resend` cùng key/người gửi của Resend. Những chuỗi trong dấu `<...>` là hướng dẫn, không phải giá trị để copy nguyên. Lưu biến và redeploy phiên bản code mới. Không cần mật khẩu/tài khoản Gmail để gọi API; vẫn cần tài khoản **dịch vụ gửi email**. Không đưa key vào GitHub, JSP, Dockerfile, ảnh chụp hoặc chat. Nếu để SMTP bật và cấu hình trên host bị chặn, mỗi đăng ký có thể đợi tới timeout SMTP rồi mới gửi API; đặt `false` là cách rõ ràng để tránh chờ này.

Đây là bài thực hành, chưa có xác minh quyền sở hữu email, hạn mức gửi hoặc chống abuse. Không bật gửi mail công khai với tài khoản thật trước khi bổ sung các bảo vệ đó. Không nhập dữ liệu cá nhân thật vào demo.

## Kiểm thử

`mvn clean verify` kiểm thử cấu hình SMTP/API, xác thực, mail UTF-8, không hạ cấp TLS, lưu JPA trước gửi, email trùng không gửi lại, lỗi gửi giữ đăng ký, và chưa cấu hình không báo đã gửi. GreenMail và HTTP server giả lập chỉ chạy trên loopback với port tự chọn, không chuyển thư tới server ngoài, không cần tài khoản thật. API test kiểm tra format JSON của cả Brevo/Resend, header xác thực, idempotency của Resend, HTTP 400/401/403/429/500, redirect và response không có message ID. Router test xác nhận SMTP thành công không gọi API, lỗi kết nối gọi API đúng một lần, lỗi sau khi bắt đầu gửi không gọi API.

Test giả lập xác nhận code xử lý đúng giao thức, **không** chứng minh một API key thật hoặc hosting đã gửi được tới inbox. Sau khi cấu hình thật, đăng ký một email mới do bạn kiểm soát; kiểm tra cả trạng thái trên web, log/dashboard provider, inbox và spam.

Để demo thủ công:

1. Mở form khi chưa cấu hình SMTP: thấy thông báo chưa cấu hình.
2. Đăng ký email mới: thấy thông tin đã lưu nhưng thư chưa gửi.
3. Gửi lại email đó: thấy lỗi trùng.
4. Cấu hình SMTP thử nghiệm, restart và đăng ký email mới: kiểm tra subject, người nhận và body ở hộp thư thử nghiệm.
5. Dừng SMTP và đăng ký email khác: trang báo đã lưu nhưng gửi mail lỗi.
6. Refresh trang cảm ơn: không gửi thêm thư.
7. Bật API hợp lệ, tắt SMTP: thấy thông báo SMTP bị tắt, thư được API chấp nhận; kiểm tra hộp thư thật.
8. Nếu SMTP bật nhưng không kết nối được và API có cấu hình: thấy lý do fallback là không kết nối được, không phải SMTP bị tắt.

## Câu hỏi cần hiểu

- `jakarta.mail.Session` khác `HttpSession` thế nào? Mail Session giữ cấu hình SMTP, còn HTTP session giữ trạng thái giữa các request của một người dùng.
- `MimeMessage` là gì? Thư có headers, người gửi/nhận và nội dung MIME. `setText(..., "UTF-8")` tạo nội dung plain text.
- Vì sao dùng `Session.getInstance`? Mỗi ứng dụng có cấu hình riêng, tránh phụ thuộc default session dùng chung.
- SMTP khác POP/IMAP thế nào? Bài này dùng SMTP để gửi; POP/IMAP dùng để truy xuất thư.
- Vì sao không rollback đăng ký nếu mail lỗi? Transaction JPA chỉ kiểm soát database; gửi SMTP là thao tác bên ngoài, không rollback được bằng JPA.
- Vì sao đóng Transport trong `finally`? Đóng cả khi có exception; lỗi lúc đóng không được biến một lần gửi đã được chấp nhận thành gửi lại qua API.
- Vì sao không catch tất cả lỗi SMTP rồi gửi API? SMTP có thể đã nhận thư nhưng mất kết nối trước khi ứng dụng nhận phản hồi; retry có thể tạo thư trùng.
- API khác SMTP thế nào? SMTP sender dùng Jakarta Mail/Transport; API sender dùng HttpClient gửi JSON tới dịch vụ qua HTTPS. Service chỉ phụ thuộc interface, servlet không xử lý giao thức gửi thư.
- Thư được SMTP chấp nhận có chắc tới inbox không? Không, mail có thể bị lọc, trả lại hoặc trì hoãn sau đó.

Nguồn thư viện: [Eclipse Angus Mail](https://eclipse-ee4j.github.io/angus-mail/), [GreenMail](https://greenmail-mail-test.github.io/greenmail/).
