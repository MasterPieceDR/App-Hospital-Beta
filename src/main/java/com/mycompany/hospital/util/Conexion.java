package com.mycompany.hospital.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BDD;encrypt=true;trustServerCertificate=true";
    private static final String USER = "proyectobdd";
    private static final String PASSWORD = "root";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}