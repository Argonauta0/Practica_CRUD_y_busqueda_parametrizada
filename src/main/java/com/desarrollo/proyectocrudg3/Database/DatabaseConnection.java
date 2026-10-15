package com.desarrollo.proyectocrudg3.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/bdventas";
    private static final String USER = "postgres";
    private static final String PASSWORD = "aprendiendoPostgres2026*";

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}