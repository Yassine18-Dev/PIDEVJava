package services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CaptchaService {

    private static final String SECRET_KEY = System.getenv("RECAPTCHA_SECRET_KEY");

    public boolean verifyCaptcha(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
                throw new IllegalStateException("RECAPTCHA_SECRET_KEY manquante dans les variables d'environnement.");
            }

            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body =
                    "secret=" + URLEncoder.encode(SECRET_KEY, StandardCharsets.UTF_8) +
                            "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            String response = readResponse(con);
            System.out.println("reCAPTCHA response = " + response);

            return response.contains("\"success\": true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String readResponse(HttpURLConnection con) throws Exception {
        InputStream is = con.getResponseCode() >= 400
                ? con.getErrorStream()
                : con.getInputStream();

        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
}