package utils;

import java.awt.Desktop;
import java.net.URI;

public class BrowserUtils {

    public static void open(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}