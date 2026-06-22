package autoservis.servis.util;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.logging.Logger;

public class MachineIdService {

    private static final Logger logger = Logger.getLogger(MachineIdService.class.getName());
    private static String cachedId = null;

    public static synchronized String getMachineId() {
        if (cachedId != null) return cachedId;
        String raw = getMacAddress() + "|" + getHostname() + "|" + getDiskSerial();
        cachedId = sha256(raw);
        return cachedId;
    }

    private static String getMacAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac == null) continue;
                StringBuilder sb = new StringBuilder();
                for (byte b : mac) sb.append(String.format("%02X", b));
                if (!sb.isEmpty()) return sb.toString();
            }
        } catch (Exception e) {
            logger.warning("getMacAddress failed: " + e.getMessage());
        }
        return "NOMAC";
    }

    private static String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "NOHOST";
        }
    }

    private static String getDiskSerial() {
        // Pokusaj wmic (stariji Windows)
        String[] wmicPaths = {
            "C:\\Windows\\System32\\wbem\\wmic.exe",
            "wmic"
        };
        for (String wmic : wmicPaths) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{wmic, "diskdrive", "get", "SerialNumber"});
                String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                for (String line : out.split("\\r?\\n")) {
                    String s = line.trim();
                    if (!s.isBlank() && !s.equalsIgnoreCase("SerialNumber")) return s;
                }
            } catch (Exception ignored) {}
        }
        // Fallback: PowerShell (Windows 11)
        try {
            Process p = Runtime.getRuntime().exec(new String[]{
                "powershell", "-NoProfile", "-Command",
                "(Get-WmiObject Win32_DiskDrive | Select-Object -First 1).SerialNumber"
            });
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (!out.isBlank()) return out;
        } catch (Exception e) {
            logger.warning("getDiskSerial fallback failed: " + e.getMessage());
        }
        return "NODISK";
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
