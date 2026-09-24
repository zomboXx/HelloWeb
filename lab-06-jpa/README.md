# Buổi 6 — Email List với JPA

Bài này giữ form Email List của buổi 5, nhưng thay tầng lưu dữ liệu JDBC bằng Jakarta Persistence (JPA). Chương 13 trong slide dùng `javax.persistence`; dự án chạy Tomcat 10.1 nên dùng namespace hiện nay là `jakarta.persistence`.

## Luồng xử lý

```text
Browser → EmailListServlet → UserRepository → EntityManager → Hibernate/JPA → HikariCP → H2
                         ↓
                     JSP view
```

- `User` là entity ánh xạ sang bảng `User`; `Email` có ràng buộc `UNIQUE`.
- `persistence.xml` khai báo persistence unit `email-list-jpa`, H2 trong bộ nhớ và HikariCP.
- `JpaLifecycle` tạo một `EntityManagerFactory` lúc ứng dụng khởi động, thêm ba user mẫu và đóng factory khi ứng dụng dừng.
- `UserRepository.findByEmail()` dùng JPQL với tham số có tên `:email`; `insertIfAbsent()` dùng `persist()` trong transaction. Mỗi thao tác tạo và đóng `EntityManager` của riêng nó.
- `EmailListServlet` kiểm tra dữ liệu form, gọi repository rồi chuyển tới JSP. Servlet không chứa JPQL hay câu SQL.

| Buổi 5: JDBC | Buổi 6: JPA |
| --- | --- |
| `DataSource.getConnection()` | `EntityManagerFactory.createEntityManager()` |
| `PreparedStatement` và `?` | JPQL, `TypedQuery` và `:email` |
| `executeUpdate()` | `persist()` rồi `commit()` |
| Tự tạo bảng bằng SQL | JPA tạo bảng từ annotation của entity |

Việc kiểm tra email trước khi thêm giúp hiển thị lỗi dễ hiểu, nhưng hai request đồng thời vẫn có thể vượt qua bước kiểm tra. Ràng buộc `UNIQUE` của database là lớp bảo vệ cuối; repository nhận ra lỗi trùng của H2 ở bước commit và trả về `false`.

## Chạy trên Tomcat 10.1

Từ gốc repository, chạy `mvn -f lab-06-jpa/pom.xml clean package`. Chép `lab-06-jpa/target/email-list-jpa.war` vào thư mục `webapps` của Tomcat, khởi động Tomcat rồi mở `http://localhost:8080/email-list-jpa/`.

Nếu deploy Dockerfile đi kèm, WAR được đổi tên thành `ROOT.war`, nên trang ở `/` thay vì `/email-list-jpa/`.

Thử gửi `jsmith@gmail.com` để thấy lỗi email trùng. Thử một email mới để thấy trang cảm ơn, rồi gửi lại email đó để kiểm tra ràng buộc duy nhất. H2 chạy trong bộ nhớ nên dữ liệu mất khi khởi động lại ứng dụng; chưa có MySQL hay lưu trữ bền vững.

## Câu hỏi cần giải thích được

1. Vì sao chia sẻ `EntityManagerFactory`, nhưng tạo `EntityManager` riêng cho từng thao tác?
2. `SELECT u FROM User u WHERE u.email = :email` là JPQL hay SQL? `User` và `email` trỏ tới entity/field nào?
3. `getSingleResult()` làm gì khi không tìm thấy user? Vì sao repository bắt `NoResultException`?
4. Vì sao cần `transaction.begin()`, `commit()` và `rollback()` khi gọi `persist()`?
5. HikariCP có biến bài này thành JDBC thuần không? Không: JPA/Hibernate vẫn dùng JDBC ở tầng dưới, còn ứng dụng thao tác với entity và `EntityManager`.
