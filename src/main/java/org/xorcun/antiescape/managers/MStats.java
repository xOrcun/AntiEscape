package org.xorcun.antiescape.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Moduvon Software Telemetry Protocol v2.5
 * Architecture: High-Fidelity Signal Ingestion
 */
public class MStats {
    private final JavaPlugin plugin;
    private final int pluginId = 1;
    private static final String API_ENDPOINT = "https://mstats.moduvon.com/api/v1/submit.php";

    public MStats(JavaPlugin plugin) {
        this.plugin = plugin;
        startSubmitting();
    }

    private void startSubmitting() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // High-density signal pulse every 2 minutes for real-time analytics
        scheduler.scheduleAtFixedRate(this::submitData, 1, 2, TimeUnit.MINUTES);
    }

    private void submitData() {
        try {
            int cores = Runtime.getRuntime().availableProcessors();
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            String javaVer = System.getProperty("java.version");
            String mcVer = Bukkit.getBukkitVersion().split("-")[0];
            String software = Bukkit.getName();
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            
            // Using World UUID directly as the server_hash
            String serverHash = Bukkit.getWorlds().isEmpty() ? "unknown" : Bukkit.getWorlds().get(0).getUID().toString();

            String json = String.format(
                "{\"plugin_id\": %d, \"server_hash\": \"%s\", \"players_count\": %d, \"version\": \"%s\", \"mc_version\": \"%s\", \"os\": \"%s\", \"arch\": \"%s\", \"cores\": %d, \"java_version\": \"%s\", \"software_name\": \"%s\"}",
                pluginId, serverHash, onlinePlayers, plugin.getDescription().getVersion(), mcVer, osName, osArch, cores, javaVer, software
            );

            HttpURLConnection con = (HttpURLConnection) new URL(API_ENDPOINT).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "Moduvon-Software/2.5");
            con.setDoOutput(true);
            
            try (OutputStream os = con.getOutputStream()) { 
                os.write(json.getBytes(StandardCharsets.UTF_8)); 
            }
            
            con.getResponseCode();
            con.disconnect();
        } catch (Exception ignored) {
            // Passive fail-safe protocol
        }
    }
}