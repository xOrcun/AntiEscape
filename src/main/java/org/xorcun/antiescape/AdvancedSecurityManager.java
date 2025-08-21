package org.xorcun.antiescape;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancedSecurityManager {
    private final AntiEscape plugin;
    private final Map<String, List<String>> ipToPlayers;
    private final Map<String, String> playerToIP;
    private final Map<String, Integer> suspiciousActivityCount;
    private final Map<String, List<Long>> playerActions;
    private final Map<String, Boolean> vpnCache;
    private final Map<String, String> whitelist;
    private final Map<String, String> whitelistAdmins; 
    
    // Ban escalation tracking
    private final Map<String, Integer> violationCount;
    private final Map<String, String> lastBanDuration;
    
    private File whitelistFile;
    private FileConfiguration whitelistConfig;
    private final SimpleDateFormat dateFormat;
    private final Random random;

    public AdvancedSecurityManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.ipToPlayers = new ConcurrentHashMap<>();
        this.playerToIP = new ConcurrentHashMap<>();
        this.suspiciousActivityCount = new ConcurrentHashMap<>();
        this.playerActions = new ConcurrentHashMap<>();
        this.vpnCache = new ConcurrentHashMap<>();
        this.whitelist = new ConcurrentHashMap<>();
        this.whitelistAdmins = new ConcurrentHashMap<>(); 
        this.violationCount = new ConcurrentHashMap<>();
        this.lastBanDuration = new ConcurrentHashMap<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.random = new Random();
        
        initializeWhitelistFile();
        loadWhitelist();
    }

    private void initializeWhitelistFile() {
        whitelistFile = new File(plugin.getDataFolder(), "data/whitelist.yml");
        
        File parentDir = whitelistFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (!whitelistFile.exists()) {
            try {
                whitelistFile.createNewFile();
            } catch (IOException e) {
                plugin.debug("Whitelist file could not be created: " + e.getMessage());
            }
        }
        
        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
    }

    private void loadWhitelist() {
        if (!plugin.getConfig().getBoolean("advanced-security.whitelist.enabled", true)) return;
        
        for (String playerName : whitelistConfig.getKeys(false)) {
            if (whitelistConfig.isConfigurationSection(playerName)) {
                boolean isWhitelisted = whitelistConfig.getBoolean(playerName + ".whitelist", false);
                if (isWhitelisted) {
                    whitelist.put(playerName.toLowerCase(), "Whitelisted");

                    String admin = whitelistConfig.getString(playerName + ".admin", "Console");
                    whitelistAdmins.put(playerName.toLowerCase(), admin);
                }
            } else {

                String reason = whitelistConfig.getString(playerName, "Whitelisted");
                whitelist.put(playerName.toLowerCase(), reason);
                whitelistAdmins.put(playerName.toLowerCase(), "Console");
            }
        }
        
        plugin.debug("Whitelist loaded: " + whitelist.size() + " players");
    }

    public void saveWhitelist() {
        if (!plugin.getConfig().getBoolean("advanced-security.whitelist.enabled", true)) return;
        
        for (Map.Entry<String, String> entry : whitelist.entrySet()) {
            String playerName = entry.getKey();
            String reason = entry.getValue();
            

            whitelistConfig.set(playerName + ".whitelist", true);
            whitelistConfig.set(playerName + ".date", dateFormat.format(new Date()));
            

            String admin = whitelistAdmins.getOrDefault(playerName, "Console");
            whitelistConfig.set(playerName + ".admin", admin);
        }
        
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            plugin.debug("Whitelist file could not be saved: " + e.getMessage());
        }
    }

    public void onPlayerJoin(Player player) {
        if (!plugin.getConfig().getBoolean("advanced-security.enabled", true)) return;
        
        String playerName = player.getName();
        String ipAddress = player.getAddress().getAddress().getHostAddress();
        

        playerToIP.put(playerName, ipAddress);
        

        ipToPlayers.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(playerName);
        

        if (isWhitelisted(playerName)) {
            plugin.debug("Whitelisted player: " + playerName);
            return;
        }
        

        if (plugin.getConfig().getBoolean("advanced-security.ip-control.check-on-join", true)) {
            checkIPAddress(player, ipAddress);
        }
        

        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.enabled", true)) {
            checkVPNAsync(player, ipAddress);
        }
        

        suspiciousActivityCount.put(playerName, 0);
        playerActions.put(playerName, new ArrayList<>());
        
        plugin.debug("Security check completed: " + playerName);
    }

    public void onPlayerQuit(Player player) {
        String playerName = player.getName();
        String ipAddress = playerToIP.get(playerName);
        
        if (ipAddress != null) {

            List<String> players = ipToPlayers.get(ipAddress);
            if (players != null) {
                players.remove(playerName);
                if (players.isEmpty()) {
                    ipToPlayers.remove(ipAddress);
                }
            }
        }
        

        playerToIP.remove(playerName);
        suspiciousActivityCount.remove(playerName);
        playerActions.remove(playerName);
    }

    public void onPlayerAction(Player player, String actionType) {
        if (!plugin.getConfig().getBoolean("advanced-security.enabled", true)) return;
        if (isWhitelisted(player.getName())) return;
        
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        

        List<Long> actions = playerActions.get(playerName);
        if (actions == null) {
            actions = new ArrayList<>();
            playerActions.put(playerName, actions);
        }
        
        actions.add(currentTime);
        

        int timeWindow = plugin.getConfig().getInt("advanced-security.suspicious-activity.time-window", 10) * 1000;
        actions.removeIf(time -> currentTime - time > timeWindow);
        

        int threshold = plugin.getConfig().getInt("advanced-security.suspicious-activity.threshold", 5);
        if (actions.size() >= threshold) {
            handleSuspiciousActivity(player, actionType, actions.size());
        }
    }

    private void checkIPAddress(Player player, String ipAddress) {
        if (!plugin.getConfig().getBoolean("advanced-security.ip-control.enabled", true)) return;
        

        List<String> playersOnIP = ipToPlayers.get(ipAddress);
        int maxAccountsPerIP = plugin.getConfig().getInt("advanced-security.ip-control.max-accounts-per-ip", 3);
        
        if (playersOnIP != null && playersOnIP.size() > maxAccountsPerIP) {
            handleSecurityViolation(player, "Too many accounts per IP: " + playersOnIP.size());
        }
        

        List<String> blockedCountries = plugin.getConfig().getStringList("advanced-security.ip-control.blocked-countries");
        List<String> allowedCountries = plugin.getConfig().getStringList("advanced-security.ip-control.allowed-countries");
        
        if (!blockedCountries.isEmpty() || !allowedCountries.isEmpty()) {

            plugin.debug("Country check: " + player.getName() + " - IP: " + ipAddress);
        }
    }

    private void checkVPNAsync(Player player, String ipAddress) {
        if (vpnCache.containsKey(ipAddress)) {
            boolean isVPN = vpnCache.get(ipAddress);
            if (isVPN) {
                handleVPNDetection(player, ipAddress);
            }
            return;
        }
        

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isVPN = checkVPNService(ipAddress);
            vpnCache.put(ipAddress, isVPN);
            
            if (isVPN) {
                Bukkit.getScheduler().runTask(plugin, () -> handleVPNDetection(player, ipAddress));
            }
        });
    }

    private boolean checkVPNService(String ipAddress) {
        String apiKey = plugin.getConfig().getString("advanced-security.vpn-detection.api-key", "");
        
        if (apiKey.isEmpty()) {

            return random.nextDouble() < 0.1; 
        }
        
        try {

            URL url = new URL("https://api.vpn-detection-service.com/check?ip=" + ipAddress + "&key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {

                return false; 
            }
        } catch (Exception e) {
            plugin.debug("VPN check error: " + e.getMessage());
        }
        
        return false;
    }

    private void handleVPNDetection(Player player, String ipAddress) {
        if (!plugin.getConfig().getBoolean("advanced-security.vpn-detection.enabled", true)) return;
        
        String playerName = player.getName();
        

        if (plugin.getConfig().getBoolean("advanced-security.security-logging.log-vpn", true)) {
            plugin.debug("VPN detected: " + playerName + " - IP: " + ipAddress);
        }
        
        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.warn-vpn", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                    onlinePlayer.sendMessage("§c[Security] VPN detected: " + playerName);
                }
            }
        }
        
        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.block-vpn", true)) {
            handleSecurityViolation(player, "VPN/Proxy usage detected");
        }
    }

    private void handleSuspiciousActivity(Player player, String actionType, int actionCount) {
        if (!plugin.getConfig().getBoolean("advanced-security.suspicious-activity.enabled", true)) return;
        
        String playerName = player.getName();
        

        int currentCount = suspiciousActivityCount.getOrDefault(playerName, 0) + 1;
        suspiciousActivityCount.put(playerName, currentCount);
        
        if (plugin.getConfig().getBoolean("advanced-security.security-logging.log-suspicious", true)) {
            plugin.debug("Suspicious activity: " + playerName + " - " + actionType + " (Count: " + actionCount + ")");
        }
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                onlinePlayer.sendMessage("§e[Security] Suspicious activity: " + playerName + " - " + actionType);
            }
        }
        
        int maxSuspicious = plugin.getConfig().getInt("advanced-security.auto-ban.suspicious-activity", 5);
        if (currentCount >= maxSuspicious) {
            handleSecurityViolation(player, "Suspicious activity threshold exceeded");
        }
    }

    private void handleSecurityViolation(Player player, String reason) {
        if (!plugin.getConfig().getBoolean("auto-ban.enabled", true)) return;
        
        String playerName = player.getName();
        
        plugin.debug("Security violation: " + playerName + " - Reason: " + reason);
        
        int currentViolations = violationCount.getOrDefault(playerName, 0) + 1;
        violationCount.put(playerName, currentViolations);
        
        String baseBanDuration = plugin.getConfig().getString("auto-ban.normal-ban.duration", "1d");
        String banReason = plugin.getConfig().getString("auto-ban.normal-ban.reason", "Security violation detected");
        
        String finalBanDuration = calculateBanDuration(playerName, baseBanDuration, currentViolations);
        
        String finalReason = banReason + " (Violation #" + currentViolations + ")";
        
        String banCommand = plugin.getConfig().getString("ban-command");
        if (banCommand != null && !banCommand.isEmpty()) {
            
            if (isPermanentDuration(finalBanDuration)) {
                String permanentFormat = getPermanentFormat(finalBanDuration);
                banCommand = banCommand
                    .replace("%player%", playerName)
                    .replace("%duration%", permanentFormat)
                    .replace("%reason%", finalReason);
            } else {
                banCommand = banCommand
                    .replace("%player%", playerName)
                    .replace("%duration%", finalBanDuration)
                    .replace("%reason%", finalReason);
            }
            
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), banCommand);
            plugin.debug("Auto-ban command executed: " + banCommand);
        } else {
            String defaultBanCommand;
            if (isPermanentDuration(finalBanDuration)) {
                String permanentFormat = getPermanentFormat(finalBanDuration);
                defaultBanCommand = "ban " + playerName + " " + finalReason + " (Violation #" + currentViolations + ")";
            } else {
                defaultBanCommand = "tempban " + playerName + " " + finalBanDuration + " " + finalReason + " (Violation #" + currentViolations + ")";
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), defaultBanCommand);
            plugin.debug("Default ban command executed: " + defaultBanCommand);
        }
        
        lastBanDuration.put(playerName, finalBanDuration);
        
        if (plugin.getConfig().getBoolean("auto-ban.log-to-console", true)) {
            Bukkit.getConsoleSender().sendMessage("§c[Security] Auto-ban: " + playerName + " - Reason: " + reason + " - Duration: " + finalBanDuration + " - Violation #" + currentViolations);
        }

        if (plugin.getConfig().getBoolean("auto-ban.notify-admins", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                    onlinePlayer.sendMessage("§c[Security] Automatic ban: " + playerName + " - Reason: " + reason + " - Duration: " + finalBanDuration + " - Violation #" + currentViolations);
                }
            }
        }
        
        if (plugin.getConfig().getBoolean("auto-ban.discord-webhook", true)) {
            try {
                Object discordManager = plugin.getClass().getDeclaredField("discordManager").get(plugin);
                if (discordManager != null) {
                    discordManager.getClass().getMethod("sendSecurityBan", Player.class, String.class)
                        .invoke(discordManager, player, reason + " (Violation #" + currentViolations + ")");
                }
            } catch (Exception e) {
                plugin.debug("Could not send Discord webhook for security ban: " + e.getMessage());
            }
        }
    }
    
    private String calculateBanDuration(String playerName, String baseDuration, int violationCount) {
        if (!plugin.getConfig().getBoolean("auto-ban.escalation.enabled", true)) {
            return baseDuration;
        }
        
        if (violationCount <= 1) {
            return baseDuration;
        }
        
        int multiplier = plugin.getConfig().getInt("auto-ban.escalation.multiplier", 2);
        String maxDuration = plugin.getConfig().getString("auto-ban.escalation.max-duration", "1m");
        
        String escalatedDuration = escalateDuration(baseDuration, violationCount, multiplier);
        
        if (isDurationLonger(escalatedDuration, maxDuration)) {
            return maxDuration;
        }
        
        return escalatedDuration;
    }
    
    private String escalateDuration(String baseDuration, int violationCount, int multiplier) {
        if (baseDuration.endsWith("d")) {
            int days = Integer.parseInt(baseDuration.substring(0, baseDuration.length() - 1));
            int escalatedDays = days * (int) Math.pow(multiplier, violationCount - 1);
            return escalatedDays + "d";
        } else if (baseDuration.endsWith("w")) {
            int weeks = Integer.parseInt(baseDuration.substring(0, baseDuration.length() - 1));
            int escalatedWeeks = weeks * (int) Math.pow(multiplier, violationCount - 1);
            return escalatedWeeks + "w";
        } else if (baseDuration.endsWith("m")) {
            int months = Integer.parseInt(baseDuration.substring(0, baseDuration.length() - 1));
            int escalatedMonths = months * (int) Math.pow(multiplier, violationCount - 1);
            return escalatedMonths + "m";
        } else if (baseDuration.endsWith("y")) {
            int years = Integer.parseInt(baseDuration.substring(0, baseDuration.length() - 1));
            int escalatedYears = years * (int) Math.pow(multiplier, violationCount - 1);
            return escalatedYears + "y";
        } else if (isPermanentDuration(baseDuration)) {
            return baseDuration; 
        }
        
        return baseDuration;
    }
    
    private boolean isPermanentDuration(String duration) {
        String lowerDuration = duration.toLowerCase();
        return lowerDuration.equals("permanent") || 
               lowerDuration.equals("perm") || 
               lowerDuration.equals("-1") || 
               lowerDuration.equals("0") ||
               lowerDuration.equals("infinity") ||
               lowerDuration.equals("forever");
    }
    
    private String getPermanentFormat(String baseDuration) {
        String permanentFormat = plugin.getConfig().getString("auto-ban.permanent-format", "permanent");
        
        if (isPermanentDuration(baseDuration)) {
            return permanentFormat;
        }
        
        return permanentFormat;
    }
    
    private boolean isDurationLonger(String duration1, String duration2) {
        if (duration1.equalsIgnoreCase("permanent")) return true;
        if (duration2.equalsIgnoreCase("permanent")) return false;
        
        try {
            int val1 = Integer.parseInt(duration1.substring(0, duration1.length() - 1));
            int val2 = Integer.parseInt(duration2.substring(0, duration2.length() - 1));
            
            char unit1 = duration1.charAt(duration1.length() - 1);
            char unit2 = duration2.charAt(duration2.length() - 1);
            
            int days1 = convertToDays(val1, unit1);
            int days2 = convertToDays(val2, unit2);
            
            return days1 > days2;
        } catch (Exception e) {
            return false;
        }
    }
    
    private int convertToDays(int value, char unit) {
        switch (unit) {
            case 'd': return value;
            case 'w': return value * 7;
            case 'm': return value * 30;
            case 'y': return value * 365;
            default: return value;
        }
    }

    public boolean isWhitelisted(String playerName) {
        if (!plugin.getConfig().getBoolean("advanced-security.whitelist.enabled", true)) return false;
        return whitelist.containsKey(playerName.toLowerCase());
    }

    public void addToWhitelist(String playerName, String reason, String adminName) {
        whitelist.put(playerName.toLowerCase(), reason);
        
        if (adminName == null || adminName.isEmpty()) {
            adminName = "Console";
        }
        whitelistAdmins.put(playerName.toLowerCase(), adminName);
        
        whitelistConfig.set(playerName + ".whitelist", true);
        whitelistConfig.set(playerName + ".date", dateFormat.format(new Date()));
        whitelistConfig.set(playerName + ".admin", adminName);
        
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            plugin.debug("Whitelist could not be saved: " + e.getMessage());
        }
        
        plugin.debug("Added to whitelist: " + playerName + " - Reason: " + reason + " - Admin: " + adminName);
    }

    public void removeFromWhitelist(String playerName, String adminName) {
        whitelist.remove(playerName.toLowerCase());
        
        if (adminName == null || adminName.isEmpty()) {
            adminName = "Console";
        }
        whitelistAdmins.remove(playerName.toLowerCase());
        
        whitelistConfig.set(playerName + ".whitelist", false);
        whitelistConfig.set(playerName + ".date", dateFormat.format(new Date()));
        whitelistConfig.set(playerName + ".admin", adminName);
        
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            plugin.debug("Whitelist could not be saved: " + e.getMessage());
        }
        
        plugin.debug("Removed from whitelist: " + playerName + " - Admin: " + adminName);
    }

    public Map<String, String> getWhitelist() {
        return new HashMap<>(whitelist);
    }

    public String getWhitelistAdmin(String playerName) {
        return whitelistAdmins.getOrDefault(playerName.toLowerCase(), "Console");
    }

    public Map<String, String> getWhitelistAdmins() {
        return new HashMap<>(whitelistAdmins);
    }

    public int getSuspiciousActivityCount(String playerName) {
        return suspiciousActivityCount.getOrDefault(playerName, 0);
    }

    public List<String> getPlayersOnIP(String ipAddress) {
        return ipToPlayers.getOrDefault(ipAddress, new ArrayList<>());
    }

    public String getPlayerIP(String playerName) {
        return playerToIP.get(playerName);
    }

    public void clearSuspiciousActivity(String playerName) {
        suspiciousActivityCount.remove(playerName);
        playerActions.remove(playerName);
    }
    
    public int getViolationCount(String playerName) {
        return violationCount.getOrDefault(playerName, 0);
    }
    
    public void clearViolationCount(String playerName) {
        violationCount.remove(playerName);
        lastBanDuration.remove(playerName);
        plugin.debug("Violation count cleared for: " + playerName);
    }
    
    public String getLastBanDuration(String playerName) {
        return lastBanDuration.getOrDefault(playerName, "None");
    }
    
    public Map<String, Integer> getAllViolationCounts() {
        return new HashMap<>(violationCount);
    }

    public void onDisable() {
        saveWhitelist();
        plugin.debug("Security system disabled");
    }
} 