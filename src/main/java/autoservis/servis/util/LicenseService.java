package autoservis.servis.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LicenseService {

    private static final Logger logger = Logger.getLogger(LicenseService.class.getName());

    public static final String API_BASE_URL = "http://167.233.81.220:8080";
    private static final String API_KEY     = "AUTO-SERVIS-2026-LKAK";
    private static final String APP_ID      = "auto-servis";
    private static final String PUBLIC_KEY_BASE64 =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0reGMM9ByJNlPtE/f9LU" +
        "GNeJGsRUQonAzI3zGj6TYCn/7LqtCLZMpgzS6zQXR126QKW4qtHo9hsS+q8EciAe" +
        "q/GzfQQ5lsvVR1PARRoQG9SdqRnLVpa0GnrQaLGCytjtyBrXfxtJJSda4z/jnXY5" +
        "dB8ygC2TMA25WN23WqChnq5uDzsS13PLNmucKuSJ9E2mp/LSUwd3dazTlLMJSpPO"  +
        "EYaVVL4pIA7UXFiv/HQ68cg7tSelwk+Kreu58SdyrFMS/EJDrXSTj8ZsVWcJJfjL" +
        "/Dmbm59ShlHGjQcKv4yEfngh00JaKGg98WJ4/+Z+C4mvRgU6kmxfHgeYNP4Mn4xY" +
        "iQIDAQAB";

    public enum LicenseStatus {
        OK, EXPIRING_SOON, GRACE_PERIOD, BLOCKED, NEEDS_ACTIVATION, OFFLINE_VALID, OFFLINE_EXPIRED
    }

    public static class PingResult {
        public final LicenseStatus status;
        public final String token;
        public final int daysLeft;

        public PingResult(LicenseStatus status, String token, int daysLeft) {
            this.status   = status;
            this.token    = token;
            this.daysLeft = daysLeft;
        }
    }

    private static LicenseService instance;
    private PingResult lastPingResult;

    private LicenseService() {}

    public static synchronized LicenseService getInstance() {
        if (instance == null) instance = new LicenseService();
        return instance;
    }

    public PingResult getLastPingResult() { return lastPingResult; }

    // ----------------------------------------------------------------
    // Startup
    // ----------------------------------------------------------------

    public LicenseStatus startupCheck() {
        String clientKey = DatabaseManager.getSetting("client_key");
        if (clientKey == null || clientKey.isBlank()) {
            logger.info("Licenca nije aktivirana");
            return LicenseStatus.NEEDS_ACTIVATION;
        }

        PingResult result = ping(clientKey);
        if (result != null) {
            if (result.status == LicenseStatus.BLOCKED) {
                logger.warning("Licenca blokirana");
                return LicenseStatus.BLOCKED;
            }
            if (result.token != null) saveToken(result.token);
            return result.status;
        }

        logger.warning("Server nedostupan — provjera keširanog tokena");
        LicenseStatus offline = checkCachedToken();
        logger.info("Offline status: " + offline);
        return offline;
    }

    // ----------------------------------------------------------------
    // Network
    // ----------------------------------------------------------------

    public PingResult ping(String clientKey) {
        try {
            String machineId = MachineIdService.getMachineId();
            String body = String.format(
                "{\"clientKey\":\"%s\",\"appId\":\"%s\",\"machineId\":\"%s\"}",
                clientKey, APP_ID, machineId
            );

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(8))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/api/license/ping"))
                .header("Content-Type", "application/json")
                .header("X-API-Key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.warning("Ping odbijen — HTTP " + response.statusCode());
                lastPingResult = new PingResult(LicenseStatus.BLOCKED, null, 0);
                return lastPingResult;
            }

            lastPingResult = parseResponse(response.body());
            logger.info("Ping OK — status: " + lastPingResult.status + ", daysLeft: " + lastPingResult.daysLeft);
            return lastPingResult;

        } catch (Exception e) {
            logger.warning("Ping failed: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------------
    // Activation
    // ----------------------------------------------------------------

    public PingResult activate(String clientKey) {
        PingResult result = ping(clientKey);
        if (result == null) {
            logger.warning("Aktivacija neuspješna — server nedostupan");
            return null;
        }
        if (result.status == LicenseStatus.BLOCKED) {
            logger.warning("Aktivacija odbijena — licenca blokirana");
            return result;
        }

        DatabaseManager.saveSetting("client_key", clientKey);
        if (result.token != null) saveToken(result.token);
        logger.info("Licenca uspješno aktivirana");
        return result;
    }

    // ----------------------------------------------------------------
    // Token
    // ----------------------------------------------------------------

    public void saveToken(String token) {
        DatabaseManager.saveSetting("jwt_token", token);
        try {
            FeatureFlagService.getInstance().applyFromClaims(getClaims(token));
        } catch (Exception e) {
            logger.warning("applyFromClaims failed: " + e.getMessage());
        }
    }

    public boolean verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY_BASE64);
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getUrlDecoder().decode(parts[2]));

        } catch (Exception e) {
            logger.warning("verifyToken failed: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) return Map.of();
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return parseJson(payload);
    }

    public LicenseStatus checkCachedToken() {
        String token = DatabaseManager.getSetting("jwt_token");
        if (token == null) return LicenseStatus.OFFLINE_EXPIRED;

        try {
            if (!verifyToken(token)) return LicenseStatus.OFFLINE_EXPIRED;

            Map<String, Object> claims = getClaims(token);

            Object expObj = claims.get("exp");
            if (expObj == null) return LicenseStatus.OFFLINE_EXPIRED;
            long exp = (long) Double.parseDouble(expObj.toString());
            if (Instant.now().getEpochSecond() > exp) return LicenseStatus.OFFLINE_EXPIRED;

            String tokenMachineId = (String) claims.get("machine_id");
            if (!MachineIdService.getMachineId().equals(tokenMachineId))
                return LicenseStatus.OFFLINE_EXPIRED;

            return LicenseStatus.OFFLINE_VALID;

        } catch (Exception e) {
            logger.warning("checkCachedToken failed: " + e.getMessage());
            return LicenseStatus.OFFLINE_EXPIRED;
        }
    }

    // ----------------------------------------------------------------
    // Periodic ping
    // ----------------------------------------------------------------

    public void startPeriodicPing() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(30), e -> {
            Thread t = new Thread(() -> {
                String clientKey = DatabaseManager.getSetting("client_key");
                if (clientKey == null) return;

                PingResult result = ping(clientKey);
                if (result == null) return;

                if (result.status == LicenseStatus.BLOCKED) {
                    Platform.exit();
                    return;
                }
                if (result.token != null) saveToken(result.token);
            });
            t.setDaemon(true);
            t.start();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private PingResult parseResponse(String json) {
        Map<String, Object> map = parseJson(json);

        String statusStr = (String) map.get("status");
        String token     = (String) map.get("token");

        int daysLeft = 0;
        if (token != null) {
            try {
                Map<String, Object> claims = getClaims(token);
                String licenseExpires = (String) claims.get("license_expires");
                if (licenseExpires != null) {
                    LocalDate expiry = LocalDate.parse(licenseExpires);
                    daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), expiry);
                    if (daysLeft < 0) daysLeft = 0;
                }
            } catch (Exception e) {
                logger.warning("daysLeft calc failed: " + e.getMessage());
            }
        }

        LicenseStatus status = switch (statusStr != null ? statusStr : "") {
            case "EXPIRING_SOON" -> LicenseStatus.EXPIRING_SOON;
            case "GRACE_PERIOD"  -> LicenseStatus.GRACE_PERIOD;
            case "BLOCKED"       -> LicenseStatus.BLOCKED;
            default              -> LicenseStatus.OK;
        };

        return new PingResult(status, token, daysLeft);
    }

    // Minimal flat-JSON parser — no library dependencies
    private Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}"))   json = json.substring(0, json.length() - 1);

        int i = 0;
        while (i < json.length()) {
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length() || json.charAt(i) == '}') break;

            if (json.charAt(i) != '"') { i++; continue; }
            i++;
            StringBuilder key = new StringBuilder();
            while (i < json.length() && json.charAt(i) != '"') key.append(json.charAt(i++));
            i++;

            while (i < json.length() && (json.charAt(i) == ':' || Character.isWhitespace(json.charAt(i)))) i++;
            if (i >= json.length()) break;

            char ch = json.charAt(i);
            if (ch == '"') {
                i++;
                StringBuilder val = new StringBuilder();
                while (i < json.length() && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') i++;
                    if (i < json.length()) val.append(json.charAt(i++));
                }
                i++;
                map.put(key.toString(), val.toString());
            } else if (ch == 't') {
                map.put(key.toString(), Boolean.TRUE);  i += 4;
            } else if (ch == 'f') {
                map.put(key.toString(), Boolean.FALSE); i += 5;
            } else if (ch == 'n') {
                map.put(key.toString(), null);          i += 4;
            } else {
                StringBuilder val = new StringBuilder();
                while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != '}')
                    val.append(json.charAt(i++));
                try { map.put(key.toString(), Double.parseDouble(val.toString().trim())); }
                catch (NumberFormatException e) { map.put(key.toString(), val.toString().trim()); }
            }

            while (i < json.length() && (json.charAt(i) == ',' || Character.isWhitespace(json.charAt(i)))) i++;
        }
        return map;
    }
}
