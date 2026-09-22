package com.example.jdbc.data;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlGatewayRepository {
    private final DataSource dataSource;

    public SqlGatewayRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Result execute(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(5);
            statement.setMaxRows(100);

            if (!statement.execute(sql)) {
                return new Result(List.of(), List.of(), statement.getUpdateCount());
            }

            try (ResultSet resultSet = statement.getResultSet()) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                List<String> columns = new ArrayList<>();
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    columns.add(metadata.getColumnLabel(column));
                }

                List<List<String>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    List<String> row = new ArrayList<>();
                    for (int column = 1; column <= metadata.getColumnCount(); column++) {
                        Object value = resultSet.getObject(column);
                        row.add(value == null ? "" : value.toString());
                    }
                    rows.add(row);
                }
                return new Result(columns, rows, -1);
            }
        }
    }

    public record Result(List<String> columns, List<List<String>> rows, int affectedRows) {
        public boolean isQuery() {
            return affectedRows < 0;
        }
    }
}
