package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = PostgresConnection.getConnection()) {
            System.out.println("Connected successfully to PostgreSQL!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}