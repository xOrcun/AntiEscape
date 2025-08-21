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
    private long lastCheckTime = 0;
    private static final long CHECK_COOLDOWN = 300000; // 5 minutes

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
            } else {
                plugin.debug("GitHub API returned error code: " + responseCode);
            }

        } catch (Exception e) {
            plugin.debug("Update check failed: " + e.getMessage());
        }
    }

    private void parseGitHubResponse(String jsonResponse) {
        try {
            plugin.debug("Parsing GitHub response...");
            plugin.debug("Response length: " + jsonResponse.length());
            
            // Extract version information
            String tagName = extractJsonValue(jsonResponse, "tag_name");
            String htmlUrl = extractJsonValue(jsonResponse, "html_url");
            String body = extractJsonValue(jsonResponse, "body");
            String publishedAt = extractJsonValue(jsonResponse, "published_at");

            plugin.debug("Extracted tag_name: " + tagName);
            plugin.debug("Extracted html_url: " + htmlUrl);
            plugin.debug("Extracted body length: " + (body != null ? body.length() : "null"));
            plugin.debug("Extracted published_at: " + publishedAt);

            if (tagName != null && !tagName.isEmpty()) {
                latestVersion = tagName;
                latestVersionUrl = htmlUrl != null ? htmlUrl : DOWNLOAD_URL;
                
                // Check if this is a newer version
                String currentVersion = plugin.getDescription().getVersion();
                updateAvailable = isNewerVersion(currentVersion, latestVersion);
                
                plugin.debug("Latest version: " + latestVersion);
                plugin.debug("Current version: " + currentVersion);
                plugin.debug("Update available: " + updateAvailable);
                
                if (updateAvailable && body != null && !body.isEmpty()) {
                    plugin.debug("Release notes: " + body.substring(0, Math.min(body.length(), 100)) + "...");
                }
            } else {
                plugin.debug("Failed to extract tag_name from response");
                plugin.debug("Response preview: " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)) + "...");
            }
        } catch (Exception e) {
            plugin.debug("Failed to parse GitHub response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            
            startIndex += searchKey.length();
            
            // Skip whitespace and quotes
            while (startIndex < json.length() && (json.charAt(startIndex) == ' ' || json.charAt(startIndex) == '"')) {
                startIndex++;
            }
            
            int endIndex = startIndex;
            if (json.charAt(startIndex - 1) == '"') {
                // Find closing quote
                endIndex = json.indexOf("\"", startIndex);
                if (endIndex == -1) return null;
            } else {
                // Find next comma or closing brace
                while (endIndex < json.length() && json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}') {
                    endIndex++;
                }
            }
            
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            plugin.debug("Failed to extract JSON value for " + key + ": " + e.getMessage());
            return null;
        }
    }

    private boolean isNewerVersion(String currentVersion, String latestVersion) {
        try {
            plugin.debug("Comparing versions - Current: '" + currentVersion + "' vs Latest: '" + latestVersion + "'");
            
            // Remove "v" prefix if present
            currentVersion = currentVersion.replaceAll("^v", "");
            latestVersion = latestVersion.replaceAll("^v", "");
            
            plugin.debug("After removing 'v' prefix - Current: '" + currentVersion + "' vs Latest: '" + latestVersion + "'");
            
            // Handle beta/alpha/rc versions
            currentVersion = normalizeVersion(currentVersion);
            latestVersion = normalizeVersion(latestVersion);
            
            plugin.debug("After normalization - Current: '" + currentVersion + "' vs Latest: '" + latestVersion + "'");
            
            // Split version into parts
            String[] currentParts = currentVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");
            
            plugin.debug("Version parts - Current: " + java.util.Arrays.toString(currentParts) + " vs Latest: " + java.util.Arrays.toString(latestParts));
            
            // Compare version parts
            int maxLength = Math.max(currentParts.length, latestParts.length);
            for (int i = 0; i < maxLength; i++) {
                int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
                
                plugin.debug("Comparing part " + i + " - Current: " + currentPart + " vs Latest: " + latestPart);
                
                if (latestPart > currentPart) {
                    plugin.debug("Latest version is newer at part " + i);
                    return true;
                } else if (latestPart < currentPart) {
                    plugin.debug("Current version is newer at part " + i);
                    return false;
                }
            }
            
            plugin.debug("Versions are equal");
            return false; // Versions are equal
        } catch (Exception e) {
            plugin.debug("Version comparison failed: " + e.getMessage());
            return false;
        }
    }
    
    private String normalizeVersion(String version) {
        // Convert beta/alpha/rc to comparable format
        version = version.toLowerCase();
        
        // Replace beta with .999
        version = version.replaceAll("beta", ".999");
        version = version.replaceAll("alpha", ".998");
        version = version.replaceAll("rc", ".997");
        version = version.replaceAll("snapshot", ".996");
        
        // Remove any remaining non-numeric characters except dots
        version = version.replaceAll("[^0-9.]", "");
        
        // Ensure proper format
        version = version.replaceAll("\\.+", ".");
        if (version.startsWith(".")) version = version.substring(1);
        if (version.endsWith(".")) version = version.substring(0, version.length() - 1);
        
        return version;
    }
    
    private int parseVersionPart(String part) {
        try {
            // Handle decimal parts like "999" from beta
            if (part.contains(".")) {
                String[] subParts = part.split("\\.");
                if (subParts.length > 0) {
                    return Integer.parseInt(subParts[0]);
                }
            }
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            plugin.debug("Failed to parse version part: " + part + " - Error: " + e.getMessage());
            return 0;
        }
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
        Bukkit.getConsoleSender().sendMessage("§aNew version available!");
        Bukkit.getConsoleSender().sendMessage("§fCurrent: §e" + plugin.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§fLatest: §a" + latestVersion);
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("§7Download: §b" + latestVersionUrl);
        Bukkit.getConsoleSender().sendMessage("§7All releases: §b" + RELEASES_URL);
        Bukkit.getConsoleSender().sendMessage("§8§m--------------------------------§r");
    }

    public void sendUpdateNotification(Player player) {
        if (!updateAvailable || latestVersion == null) {
            return;
        }

        player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
        player.sendMessage("§a🔄 New version available!");
        player.sendMessage("§fCurrent: §e" + plugin.getDescription().getVersion());
        player.sendMessage("§fLatest: §a" + latestVersion);
        player.sendMessage("");
        player.sendMessage("§7Download: §b" + latestVersionUrl);
        player.sendMessage("§7All releases: §b" + RELEASES_URL);
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
                plugin.debug("Player " + player.getName() + " is using the latest version");
            }
            return;
        }

        // Perform fresh update check
        player.sendMessage("§8[§eAntiEscape§8] §7Checking for updates...");
        
        CompletableFuture.runAsync(() -> {
            try {
                performUpdateCheck();
                
                // Send result to player
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (updateAvailable) {
                        sendUpdateNotification(player);
                    } else {
                        plugin.debug("Player " + player.getName() + " is using the latest version");
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§8[§eAntiEscape§8] §cUpdate check failed: " + e.getMessage());
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
}
