import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SignSample {
    private static final String APP_KEY = "your-app-key";
    private static final String APP_SECRET = "your-app-secret";
    private static final String BASE_URL = "http://localhost:8088";

    public static void main(String[] args) throws Exception {
        String body = "{\"queryType\":\"medical_all\",\"queryParams\":{\"name\":\"张三\",\"idCard\":\"430102199001011234\"}}";
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String sign = sign(timestamp, nonce, body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/v1/medical/query"))
                .header("Content-Type", "application/json;charset=utf-8")
                .header("X-App-Key", APP_KEY)
                .header("X-Timestamp", timestamp)
                .header("X-Nonce", nonce)
                .header("X-Sign", sign)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }

    private static String sign(String timestamp, String nonce, String body) throws Exception {
        String payload = timestamp + "\n" + nonce + "\n" + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
