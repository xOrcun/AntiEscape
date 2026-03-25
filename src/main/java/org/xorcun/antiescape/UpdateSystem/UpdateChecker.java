package org.xorcun.antiescape.UpdateSystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    private final AntiEscape plugin;
    private static final String API_URL = "https://api.github.com/repos/xOrcun/AntiEscape/releases/latest";
    private static final String DOWNLOAD_URL = "https://github.com/xOrcun/AntiEscape/releases/latest";
    private static final String RELEASES_URL = "https://github.com/xOrcun/AntiEscape/releases";

    private String latestVersion;
    private String latestVersionUrl;
    private boolean updateAvailable = false;
    private VersionStatus versionStatus = VersionStatus.UNKNOWN;
    private long lastCheckTime = 0;
    private static final long CHECK_COOLDOWN = 300000; // 5 minutes

    public enum VersionStatus {
        OUTDATED,
        UP_TO_DATE,
        AHEAD,
        UNKNOWN
    }

    public UpdateChecker(AntiEscape plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        // Check if update checking is enabled
        if (!plugin.getConfig().getBoolean("check-update", true)) {
            plugin.debug("Update checking is disabled in config");
            return;
        }

        // Check cooldown to prevent spam
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_COOLDOWN) {
            plugin.debug("Update check skipped due to cooldown");
            return;
        }

        lastCheckTime = currentTime;
        plugin.debug("Starting update check...");

        // Check for updates asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                performUpdateCheck();
            } catch (Exception e) {
                plugin.debug("Update check failed: " + e.getMessage());
                versionStatus = VersionStatus.UNKNOWN;
            }
        });
    }

    private void performUpdateCheck() {
        try {
            // Create connection to GitHub API
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AntiEscape-UpdateChecker/1.0");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds

            int responseCode = connection.getResponseCode();
            plugin.debug("GitHub API response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                parseGitHubResponse(response.toString());

                // Notify online players if update is available
                if (updateAvailable) {
                    notifyOnlinePlayers();
                }
            } else if (responseCode == 429) { // HTTP 429 Too Many Requests (Rate Limited)
                plugin.debug("GitHub API rate limit exceeded");
                versionStatus = VersionStatus.UNKNOWN;
            } else {
                plugin.debug("GitHub API returned error code: " + responseCode);
                versionStatus = VersionStatus.UNKNOWN;
            }

        } catch (Exception e) {
            plugin.debug("Update check failed: " + e.getMessage());
            versionStatus = VersionStatus.UNKNOWN;
        }
    }

    private void parseGitHubResponse(String jsonResponse) {
        try {
            plugin.debug("Parsing GitHub response...");

            // Extract version information
            String tagName = extractJsonValue(jsonResponse, "tag_name");
            String htmlUrl = extractJsonValue(jsonResponse, "html_url");
            String body = extractJsonValue(jsonResponse, "body");

            if (tagName != null && !tagName.isEmpty()) {
                latestVersion = tagName;
                latestVersionUrl = htmlUrl != null ? htmlUrl : DOWNLOAD_URL;

                // Check version status
                String currentVersion = plugin.getDescription().getVersion();
                versionStatus = compareVersions(currentVersion, latestVersion);
                updateAvailable = (versionStatus == VersionStatus.OUTDATED);

                plugin.debug("Latest version: " + latestVersion);
                plugin.debug("Current version: " + currentVersion);
                plugin.debug("Version status: " + versionStatus);

                if (updateAvailable && body != null && !body.isEmpty()) {
                    plugin.debug("Release notes found");
                }
            } else {
                plugin.debug("Failed to extract tag_name from response");
                versionStatus = VersionStatus.UNKNOWN;
            }
        } catch (Exception e) {
            plugin.debug("Failed to parse GitHub response: " + e.getMessage());
            versionStatus = VersionStatus.UNKNOWN;
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1)
                return null;

            startIndex += searchKey.length();

            // Skip whitespace and quotes
            while (startIndex < json.length() && (json.charAt(startIndex) == ' ' || json.charAt(startIndex) == '"')) {
                startIndex++;
            }

            int endIndex = startIndex;
            if (json.charAt(startIndex - 1) == '"') {
                // Find closing quote
                endIndex = json.indexOf("\"", startIndex);
                if (endIndex == -1)
                    return null;
            } else {
                // Find next comma or closing brace
                while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                    endIndex++;
                }
            }

            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private VersionStatus compareVersions(String currentVersion, String latestVersion) {
        try {
            // Remove "v" prefix if present
            currentVersion = currentVersion.replaceAll("^v", "");
            latestVersion = latestVersion.replaceAll("^v", "");

            // Normalize and split versions
            String[] currentParts = normalizeAndSplit(currentVersion);
            String[] latestParts = normalizeAndSplit(latestVersion);

            // Compare version parts (major.minor.patch.weight)
            for (int i = 0; i < 4; i++) {
                int currentPart = Integer.parseInt(currentParts[i]);
                int latestPart = Integer.parseInt(latestParts[i]);

                if (latestPart > currentPart) {
                    return VersionStatus.OUTDATED;
                } else if (latestPart < currentPart) {
                    return VersionStatus.AHEAD;
                }
            }

            return VersionStatus.UP_TO_DATE;
        } catch (Exception e) {
            plugin.debug("Version comparison failed: " + e.getMessage());
            return VersionStatus.UNKNOWN;
        }
    }

    private String[] normalizeAndSplit(String version) {
        String lower = version.toLowerCase();
        int weight = 4; // Default: Final/Release

        if (lower.contains("snapshot")) weight = 0;
        else if (lower.contains("alpha")) weight = 1;
        else if (lower.contains("beta")) weight = 2;
        else if (lower.contains("rc")) weight = 3;

        // Extract numeric parts
        String numericOnly = version.replaceAll("[^0-9.]", " ").trim().replaceAll(" +", ".");
        String[] parts = numericOnly.split("\\.");
        
        String[] result = new String[4];
        result[0] = parts.length > 0 ? parts[0] : "0"; // Major
        result[1] = parts.length > 1 ? parts[1] : "0"; // Minor
        result[2] = parts.length > 2 ? parts[2] : "0"; // Patch
        result[3] = String.valueOf(weight);            // Weight

        return result;
    }

    private void notifyOnlinePlayers() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Console notification (only when plugin starts)
            sendConsoleUpdateNotification();

            // Player notifications
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(plugin.getConfig().getString("permissions.update-notify"))) {
                    sendUpdateNotification(player);
                }
            }
        });
    }

    private void sendConsoleUpdateNotification() {
        if (!updateAvailable || latestVersion == null) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
        Bukkit.getConsoleSender().sendMessage("§a§lNew version available!");
        Bukkit.getConsoleSender().sendMessage("§fCurrent: §e" + plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§fLatest: §a" + latestVersion);
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§fDownload:");
        Bukkit.getConsoleSender().sendMessage(" §b" + latestVersionUrl);
        Bukkit.getConsoleSender().sendMessage("§fAll releases: §b" + RELEASES_URL);
        Bukkit.getConsoleSender().sendMessage("§8§m--------------------------------§r");
    }

    public void sendUpdateNotification(Player player) {
        if (!updateAvailable || latestVersion == null) {
            return;
        }

        player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
        player.sendMessage("§a§lNew version available!");
        player.sendMessage("§fCurrent: §e" + plugin.getDescription().getVersion());
        player.sendMessage("§fLatest: §a" + latestVersion);
        player.sendMessage("");
        player.sendMessage("§fDownload:");
        player.sendMessage(" §b" + latestVersionUrl);
        player.sendMessage("§fAll releases: §b" + RELEASES_URL);
        player.sendMessage("§8§m--------------------------------§r");
    }

    public void checkForUpdatesPlayer(Player player) {
        // Check if update checking is enabled
        if (!plugin.getConfig().getBoolean("check-update", true)) {
            plugin.debug("Update checking is disabled in config for player: " + player.getName());
            return;
        }

        if (!player.hasPermission(plugin.getConfig().getString("permissions.update-notify"))) {
            return;
        }

        // Check if we have recent update info
        if (System.currentTimeMillis() - lastCheckTime < CHECK_COOLDOWN && latestVersion != null) {
            if (updateAvailable) {
                sendUpdateNotification(player);
            } else {
                plugin.debug("Player " + player.getName() + " check complete - status: " + versionStatus);
            }
            return;
        }

        // Perform fresh update check
        player.sendMessage(plugin.getMessageFileManager().getLangMessage("update-checking"));

        CompletableFuture.runAsync(() -> {
            try {
                performUpdateCheck();

                // Send result to player
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (updateAvailable) {
                        sendUpdateNotification(player);
                    } else {
                        plugin.debug("Player " + player.getName() + " check complete - status: " + versionStatus);
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(plugin.getMessageFileManager().getLangMessage("update-failed").replace("%error%",
                            e.getMessage()));
                });
            }
        });
    }

    // Getter methods
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getLatestVersionUrl() {
        return latestVersionUrl;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    public VersionStatus getVersionStatus() {
        return versionStatus;
    }
}
