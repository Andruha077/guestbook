
package com.example.guestbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
            "jdbc:h2:file:./data/guest;AUTO_SERVER=TRUE";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS comments (
                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    author VARCHAR(64) NOT NULL,
                    text VARCHAR(1000) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection connection = getConnection();
             var statement = connection.createStatement()) {

            statement.execute(sql);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
