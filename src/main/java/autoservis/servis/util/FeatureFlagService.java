package autoservis.servis.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class FeatureFlagService {

    private static final Logger logger = Logger.getLogger(FeatureFlagService.class.getName());

    private static final String HMAC_KEY = "psi-lic-7f3K#2026";
    private static final String SIG_KEY  = "feature_flags_sig";
    private static final String[] FLAGS  = {
        "feature_drive", "feature_email", "feature_popis", "feature_statistike"
    };

    private static FeatureFlagService instance;
    private boolean signatureValid;

    private FeatureFlagService() {
        signatureValid = verifySignature();
        if (!signatureValid) logger.warning("Feature flags signature invalid — all features disabled");
    }

    public static synchronized FeatureFlagService getInstance() {
        if (instance == null) instance = new FeatureFlagService();
        return instance;
    }

    public boolean isEnabled(String flag) {
        if (!signatureValid) return false;
        String val = DatabaseManager.getSetting(flag);
        return "true".equals(val);
    }

    public void applyFromClaims(Map<String, Object> claims) {
        for (String flag : FLAGS) {
            Object val = claims.get(flag);
            boolean enabled = Boolean.TRUE.equals(val) || "true".equals(String.valueOf(val));
            DatabaseManager.saveSetting(flag, enabled ? "true" : "false");
        }
        String sig = computeSignature();
        if (sig != null) DatabaseManager.saveSetting(SIG_KEY, sig);
        signatureValid = true;
    }

    private boolean verifySignature() {
        try {
            String stored = DatabaseManager.getSetting(SIG_KEY);
            if (stored == null) return false;
            String computed = computeSignature();
            return computed != null && computed.equals(stored);
        } catch (Exception e) {
            return false;
        }
    }

    private String computeSignature() {
        try {
            TreeMap<String, String> sorted = new TreeMap<>();
            for (String flag : FLAGS) {
                String val = DatabaseManager.getSetting(flag);
                sorted.put(flag, val != null ? val : "false");
            }
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : sorted.entrySet())
                sb.append(e.getKey()).append("=").append(e.getValue()).append(";");
            return hmacSha256(sb.toString());
        } catch (Exception e) {
            logger.warning("computeSignature failed: " + e.getMessage());
            return null;
        }
    }

    private String hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(HMAC_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
