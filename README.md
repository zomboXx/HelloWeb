# Bài tập Lập trình Web

Repository này tổng hợp các bài thực hành môn Lập trình Web. Mỗi thư mục `lab-*` là một Maven web application độc lập, sử dụng Java Servlet và được triển khai riêng.

## Danh sách bài tập

| Buổi | Nội dung | Mã nguồn | Demo |
| --- | --- | --- | --- |
| 1 | Murach Survey | [lab-01-survey](./lab-01-survey) | [Mở website](https://wepr-7m86.onrender.com/) |
| 2 | Join Our Email List | [lab-02-email-list](./lab-02-email-list) | [Mở website](https://helloweb-dfjj.onrender.com/) |
| 3 | Shopping Cart với `HttpSession` | [lab-03-cart](./lab-03-cart) | [Mở website](https://helloweb-1.onrender.com/cart?action=list) |
| 4 | Album Download với session và cookie | [lab-04-album-download](./lab-04-album-download) | [Mở website](https://helloweb-2.onrender.com/) |
| 5 | JDBC, MVC và HikariCP (SQL Gateway + Email List) | [lab-05-jdbc](./lab-05-jdbc) | SQL Gateway chỉ chạy trên localhost |
| 6 | Email List với JPA, Hibernate và HikariCP | [lab-06-jpa](./lab-06-jpa) | Chạy local; chưa triển khai |
| 7 | Email List gửi thư chào mừng bằng Jakarta Mail (chương 14) | [lab-07-mail](./lab-07-mail) | Chạy local; cần cấu hình SMTP để gửi thư |

## Build từng bài

Chạy Maven trong thư mục của bài cần kiểm tra:

```powershell
cd lab-01-survey
mvn clean package
```

Hoặc chạy từ thư mục gốc của repository:

```powershell
mvn -f .\lab-01-survey\pom.xml clean package
mvn -f .\lab-02-email-list\pom.xml clean package
mvn -f .\lab-03-cart\pom.xml clean package
mvn -f .\lab-04-album-download\pom.xml clean package
mvn -f .\lab-05-jdbc\pom.xml clean package
mvn -f .\lab-06-jpa\pom.xml clean package
mvn -f .\lab-07-mail\pom.xml clean verify
```

File WAR sau khi build nằm trong thư mục `target` của từng bài.
