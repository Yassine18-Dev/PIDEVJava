package utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                System.out.println("✅ config.properties chargé");
            } else {
                System.err.println("⚠ config.properties introuvable");
            }
        } catch (Exception e) {
            System.err.println("⚠ Erreur chargement config : " + e.getMessage());
        }
    }

    public static String get(String key)             { return props.getProperty(key); }
    public static String get(String key, String def) { return props.getProperty(key, def); }
}