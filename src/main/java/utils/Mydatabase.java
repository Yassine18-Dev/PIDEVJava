package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    private static final String URL  = "jdbc:mysql://localhost:3306/javadb";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Mydatabase instance;
    private Connection cnx;

    private Mydatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Connexion à javadb établie.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion : " + e.getMessage());
        }
    }

    public static Mydatabase getInstance() {
        if (instance == null) instance = new Mydatabase();
        return instance;
    }

    public Connection getCnx() { return cnx; }
}