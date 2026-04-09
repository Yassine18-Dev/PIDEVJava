package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

<<<<<<< HEAD
    private static Mydatabase instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3306/pidevj?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Mydatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion base de données réussie.");
        } catch (SQLException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
=======
    private final String URL = "jdbc:mysql://localhost:3306/javadb?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static Mydatabase instance;

    private Mydatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException(e);
>>>>>>> origin/feature/team-player
        }
    }

    public static Mydatabase getInstance() {
        if (instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }

<<<<<<< HEAD
    public Connection getCnx() {
        return cnx;
=======
    public Connection getConnection() {
        return connection;
>>>>>>> origin/feature/team-player
    }
}