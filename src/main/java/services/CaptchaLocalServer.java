package services;

import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class CaptchaLocalServer {

    private static HttpServer server;
    private static final int PORT = 9999;

    private static final String SITE_KEY = "6LevM8ssAAAAADQhsTLP6M9ZWy66-vz5mR5FTL5Q";

    public static void start() {
        try {
            if (server != null) {
                return;
            }

            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/recaptcha.html", exchange -> {
                String html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>reCAPTCHA</title>
                            <script src="https://www.google.com/recaptcha/api.js" async defer></script>
                        </head>
                        <body style="background:#111827; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; overflow:hidden;">
                            <form>
                                <div class="g-recaptcha"
                                     data-sitekey="%s"
                                     data-callback="captchaSuccess"
                                     data-expired-callback="captchaExpired"></div>
                            </form>

                            <script>
                                function captchaSuccess(token) {
                                    document.title = "TOKEN:" + token;
                                }

                                function captchaExpired() {
                                    document.title = "TOKEN_EXPIRED";
                                }
                            </script>
                        </body>
                        </html>
                        """.formatted(SITE_KEY);

                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });

            server.start();
            System.out.println("Captcha server started: http://localhost:" + PORT + "/recaptcha.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}