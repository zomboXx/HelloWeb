# Chapter 12 — JDBC, MVC, and HikariCP

This lab recreates the SQL Gateway and Email List examples from Chapter 12. It deliberately does **not** copy the code on slides 25–28, where the servlet opens a JDBC connection and runs SQL directly.

## Structure

```text
Browser → Servlet (controller) → Repository (data access) → HikariCP → H2 database
                  ↓
              JSP (view)
```

- `Database` creates one `HikariDataSource` when the web application starts, creates the sample `User` table, and closes the pool when the app stops.
- `EmailListServlet` validates request data and selects a view. `UserRepository` inserts with a `PreparedStatement`; a unique constraint on `Email` handles duplicate submissions safely.
- `SqlGatewayServlet` accepts a classroom SQL exercise, while `SqlGatewayRepository` executes it. The servlet does not contain JDBC connection code. The JSP renders the result; it does not run SQL.
- `try-with-resources` closes a borrowed `Connection`. With HikariCP, this returns it to the pool for reuse rather than destroying the physical connection.

The default H2 database lives **only in memory**. Three sample users are inserted on startup. Restarting Tomcat resets the sample data. It is suitable for learning without MySQL credentials, not persistent storage.

## Run locally

1. Build: `mvn -f lab-05-jdbc/pom.xml clean package` from the repository root.
2. Deploy `lab-05-jdbc/target/jdbc-mvc.war` to Tomcat 10.1. The context path is `/jdbc-mvc` if you keep that WAR name; rename it to `ROOT.war` to use `/`.
3. Open `/jdbc-mvc/` and choose either exercise.

To reproduce the screenshots, run these one at a time in SQL Gateway:

```sql
SELECT * FROM User
```

```sql
INSERT INTO User (Email, FirstName, LastName)
VALUES ('mike@murach.com', 'Mike', 'Murach')
```

Run `SELECT * FROM User` again to see Mike. In Email List, submit `jsmith@gmail.com` to see the duplicate-email message.

Do **not** attach this SQL Gateway to a real database or publish it as an unrestricted public service. Allowing visitors to submit SQL is intentionally a classroom exercise, not a production pattern. The gateway accepts only one SELECT/INSERT/UPDATE/DELETE statement, but that is not a complete SQL security boundary.

## Questions to be ready for

- **Why not call `DriverManager.getConnection()` inside a servlet?** It mixes request handling with database infrastructure and creates a new physical connection for each request. Here, the servlet asks a repository to do the data work, and the repository borrows a pooled connection.
- **What does `close()` do with HikariCP?** It returns the connection to the pool. The pool itself is closed once when the application stops.
- **Why `PreparedStatement` for the email form?** Form values are bound as data, not concatenated into SQL. This also prevents SQL injection in that normal application flow.
- **Why does SQL Gateway use `Statement`?** Its purpose is to demonstrate SQL entered by the learner. It is not the pattern used for normal user-input forms.
- **Where is MVC?** Model/data access: repository and database; view: JSP/CSS; controller: servlet. The controller does not generate HTML or run SQL.
- **Why does duplicate email stay blocked?** The database has a `UNIQUE` constraint on `Email`, so even simultaneous requests cannot insert the same email twice.

Chapter 11 supplies the SQL background. Chapter 13 introduces JPA as a separate persistence approach; this JDBC lab does not mix in JPA.
