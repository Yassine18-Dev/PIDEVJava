package services;

import com.sun.net.httpserver.HttpServer;
import entities.User;
import utils.PasswordUtils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class GoogleAuthService {

    private static final String CLIENT_ID = System.getenv("OAUTH_GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("OAUTH_GOOGLE_CLIENT_SECRET");

    private static final int PORT = 8888;
    private static final String REDIRECT_URI = "http://localhost:" + PORT + "/callback";

    public User loginWithGoogle() throws Exception {
        checkConfig();

        String code = waitForGoogleCode();

        if (code == null) {
            return null;
        }

        String tokenResponse = exchangeCodeForToken(code);
        String accessToken = extractJsonValue(tokenResponse, "access_token");

        if (accessToken == null || accessToken.isEmpty()) {
            return null;
        }

        String userInfo = getGoogleUserInfo(accessToken);

        String email = extractJsonValue(userInfo, "email");
        String name = extractJsonValue(userInfo, "name");

        if (email == null || email.isEmpty()) {
            return null;
        }

        UserService userService = new UserService();

        if (!userService.emailExiste(email)) {
            User newUser = new User(
                    email,
                    PasswordUtils.hashPassword("GOOGLE_AUTH"),
                    name == null || name.isEmpty() ? email.split("@")[0] : name,
                    "USER",
                    "ACTIVE",
                    "Compte créé via Google OAuth",
                    "Unknown",
                    new Timestamp(System.currentTimeMillis())
            );

            userService.ajouter(newUser);
        }

        return userService.getUserByEmail(email);
    }

    private void checkConfig() {
        if (CLIENT_ID == null || CLIENT_ID.isBlank()
                || CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            throw new IllegalStateException(
                    "Google OAuth non configuré. Ajoute OAUTH_GOOGLE_CLIENT_ID et OAUTH_GOOGLE_CLIENT_SECRET dans Environment variables."
            );
        }
    }

    private String waitForGoogleCode() throws Exception {
        final String[] codeHolder = new String[1];

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            codeHolder[0] = params.get("code");

            String response = """
                    <html>
                    <body style="font-family: Arial; background:#0f172a; color:white; display:flex; align-items:center; justify-content:center; height:100vh;">
                        <div style="text-align:center;">
                            <h2>Connexion Google réussie ✅</h2>
                            <p>Vous pouvez fermer cette fenêtre et retourner à ArenaMind.</p>
                        </div>
                    </body>
                    </html>
                    """;

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.start();

        String authUrl =
                "https://accounts.google.com/o/oauth2/v2/auth" +
                        "?client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                        "&response_type=code" +
                        "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8) +
                        "&access_type=offline" +
                        "&prompt=select_account";

        Desktop.getDesktop().browse(new URI(authUrl));

        int timeoutSeconds = 120;

        for (int i = 0; i < timeoutSeconds; i++) {
            if (codeHolder[0] != null) {
                server.stop(0);
                return codeHolder[0];
            }

            Thread.sleep(1000);
        }

        server.stop(0);
        return null;
    }

    private String exchangeCodeForToken(String code) throws Exception {
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body =
                "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                        "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                        "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8) +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                        "&grant_type=authorization_code";

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        return readResponse(con);
    }

    private String getGoogleUserInfo(String accessToken) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        return readResponse(con);
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

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return map;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");

            if (parts.length == 2) {
                map.put(
                        URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                );
            }
        }

        return map;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";

        int index = json.indexOf(pattern);

        if (index == -1) {
            return null;
        }

        int start = json.indexOf("\"", index + pattern.length());
        int end = json.indexOf("\"", start + 1);

        if (start == -1 || end == -1) {
            return null;
        }

        return json.substring(start + 1, end);
    }
}