package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    private static Mydatabase instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3307/pidevj?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Mydatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion base de données réussie.");
        } catch (SQLException e) {
            System.err.println("Erreur connexion : " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static Mydatabase getInstance() {
        if (instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }

    // Méthode principale
    public Connection getConnection() {
        return connection;
    }

    // Méthode pour la compatibilité (alias de getConnection)
    public Connection getCnx() {
        return connection;
    }
}