package com.example.jdbc.data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean insert(String email, String firstName, String lastName) throws SQLException {
        String sql = "INSERT INTO User (Email, FirstName, LastName) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            // The UNIQUE constraint also protects against two concurrent submissions.
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw e;
        }
    }
}
