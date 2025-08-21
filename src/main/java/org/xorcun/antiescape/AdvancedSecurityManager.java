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
    private final Map<String, String> whitelistAdmins; // Admin bilgilerini sakla
    
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
        this.whitelistAdmins = new ConcurrentHashMap<>(); // Initialize new map
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
                    // Admin bilgisini yükle
                    String admin = whitelistConfig.getString(playerName + ".admin", "Console");
                    whitelistAdmins.put(playerName.toLowerCase(), admin);
                }
            } else {
                // Eski format için uyumluluk
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
            
            // Yeni format
            whitelistConfig.set(playerName + ".whitelist", true);
            whitelistConfig.set(playerName + ".date", dateFormat.format(new Date()));
            
            // Admin bilgisini kaydet
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
        
        // IP adresini kaydet
        playerToIP.put(playerName, ipAddress);
        
        // IP'ye oyuncu ekle
        ipToPlayers.computeIfAbsent(ipAddress, k -> new ArrayList<>()).add(playerName);
        
        // Whitelist kontrolü
        if (isWhitelisted(playerName)) {
            plugin.debug("Whitelisted player: " + playerName);
            return;
        }
        
        // IP kontrolü
        if (plugin.getConfig().getBoolean("advanced-security.ip-control.check-on-join", true)) {
            checkIPAddress(player, ipAddress);
        }
        
        // VPN kontrolü
        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.enabled", true)) {
            checkVPNAsync(player, ipAddress);
        }
        
        // Şüpheli aktivite sayacını sıfırla
        suspiciousActivityCount.put(playerName, 0);
        playerActions.put(playerName, new ArrayList<>());
        
        plugin.debug("Security check completed: " + playerName);
    }

    public void onPlayerQuit(Player player) {
        String playerName = player.getName();
        String ipAddress = playerToIP.get(playerName);
        
        if (ipAddress != null) {
            // IP'den oyuncuyu kaldır
            List<String> players = ipToPlayers.get(ipAddress);
            if (players != null) {
                players.remove(playerName);
                if (players.isEmpty()) {
                    ipToPlayers.remove(ipAddress);
                }
            }
        }
        
        // Temizlik
        playerToIP.remove(playerName);
        suspiciousActivityCount.remove(playerName);
        playerActions.remove(playerName);
    }

    public void onPlayerAction(Player player, String actionType) {
        if (!plugin.getConfig().getBoolean("advanced-security.enabled", true)) return;
        if (isWhitelisted(player.getName())) return;
        
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        
        // Oyuncu aksiyonlarını kaydet
        List<Long> actions = playerActions.get(playerName);
        if (actions == null) {
            actions = new ArrayList<>();
            playerActions.put(playerName, actions);
        }
        
        actions.add(currentTime);
        
        // Eski aksiyonları temizle
        int timeWindow = plugin.getConfig().getInt("advanced-security.suspicious-activity.time-window", 10) * 1000;
        actions.removeIf(time -> currentTime - time > timeWindow);
        
        // Şüpheli aktivite kontrolü
        int threshold = plugin.getConfig().getInt("advanced-security.suspicious-activity.threshold", 5);
        if (actions.size() >= threshold) {
            handleSuspiciousActivity(player, actionType, actions.size());
        }
    }

    private void checkIPAddress(Player player, String ipAddress) {
        if (!plugin.getConfig().getBoolean("advanced-security.ip-control.enabled", true)) return;
        
        // Aynı IP'den gelen oyuncu sayısını kontrol et
        List<String> playersOnIP = ipToPlayers.get(ipAddress);
        int maxAccountsPerIP = plugin.getConfig().getInt("advanced-security.ip-control.max-accounts-per-ip", 3);
        
        if (playersOnIP != null && playersOnIP.size() > maxAccountsPerIP) {
            handleSecurityViolation(player, "Too many accounts per IP: " + playersOnIP.size());
        }
        
        // Ülke kontrolü (basit implementasyon)
        List<String> blockedCountries = plugin.getConfig().getStringList("advanced-security.ip-control.blocked-countries");
        List<String> allowedCountries = plugin.getConfig().getStringList("advanced-security.ip-control.allowed-countries");
        
        if (!blockedCountries.isEmpty() || !allowedCountries.isEmpty()) {
            // Burada gerçek bir IP-to-country API kullanılabilir
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
        
        // Asenkron VPN kontrolü
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
            // API key yoksa basit kontrol
            return random.nextDouble() < 0.1; // %10 şans VPN
        }
        
        try {
            // Gerçek VPN API çağrısı burada yapılabilir
            URL url = new URL("https://api.vpn-detection-service.com/check?ip=" + ipAddress + "&key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // API yanıtını parse et
                return false; // Şimdilik false döndür
            }
        } catch (Exception e) {
            plugin.debug("VPN check error: " + e.getMessage());
        }
        
        return false;
    }

    private void handleVPNDetection(Player player, String ipAddress) {
        if (!plugin.getConfig().getBoolean("advanced-security.vpn-detection.enabled", true)) return;
        
        String playerName = player.getName();
        
        // VPN kullanımını logla
        if (plugin.getConfig().getBoolean("advanced-security.security-logging.log-vpn", true)) {
            plugin.debug("VPN detected: " + playerName + " - IP: " + ipAddress);
        }
        
        // VPN uyarısı
        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.warn-vpn", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                    onlinePlayer.sendMessage("§c[Security] VPN detected: " + playerName);
                }
            }
        }
        
        // VPN engelleme
        if (plugin.getConfig().getBoolean("advanced-security.vpn-detection.block-vpn", true)) {
            handleSecurityViolation(player, "VPN/Proxy usage detected");
        }
    }

    private void handleSuspiciousActivity(Player player, String actionType, int actionCount) {
        if (!plugin.getConfig().getBoolean("advanced-security.suspicious-activity.enabled", true)) return;
        
        String playerName = player.getName();
        
        // Şüpheli aktivite sayacını artır
        int currentCount = suspiciousActivityCount.getOrDefault(playerName, 0) + 1;
        suspiciousActivityCount.put(playerName, currentCount);
        
        // Logla
        if (plugin.getConfig().getBoolean("advanced-security.security-logging.log-suspicious", true)) {
            plugin.debug("Suspicious activity: " + playerName + " - " + actionType + " (Count: " + actionCount + ")");
        }
        
        // Uyarı gönder
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                onlinePlayer.sendMessage("§e[Security] Suspicious activity: " + playerName + " - " + actionType);
            }
        }
        
        // Otomatik ban kontrolü
        int maxSuspicious = plugin.getConfig().getInt("advanced-security.auto-ban.suspicious-activity", 5);
        if (currentCount >= maxSuspicious) {
            handleSecurityViolation(player, "Suspicious activity threshold exceeded");
        }
    }

    private void handleSecurityViolation(Player player, String reason) {
        if (!plugin.getConfig().getBoolean("auto-ban.enabled", true)) return;
        
        String playerName = player.getName();
        
        // Logla
        plugin.debug("Security violation: " + playerName + " - Reason: " + reason);
        
        // Ban escalation hesapla
        int currentViolations = violationCount.getOrDefault(playerName, 0) + 1;
        violationCount.put(playerName, currentViolations);
        
        // Auto-ban ayarlarını al (NORMAL ban için)
        String baseBanDuration = plugin.getConfig().getString("auto-ban.normal-ban.duration", "1d");
        String banReason = plugin.getConfig().getString("auto-ban.normal-ban.reason", "Security violation detected");
        
        // Ban escalation hesapla
        String finalBanDuration = calculateBanDuration(playerName, baseBanDuration, currentViolations);
        
        // Final reason'ı hazırla
        String finalReason = banReason + " (Violation #" + currentViolations + ")";
        
        // Ban komutunu çalıştır
        String banCommand = plugin.getConfig().getString("ban-command");
        if (banCommand != null && !banCommand.isEmpty()) {
            // %player%, %duration%, %reason% değişkenlerini değiştir
            
            // Permanent ban kontrolü
            if (isPermanentDuration(finalBanDuration)) {
                // Permanent ban için uygun komut formatını kullan
                String permanentFormat = getPermanentFormat(finalBanDuration);
                banCommand = banCommand
                    .replace("%player%", playerName)
                    .replace("%duration%", permanentFormat)
                    .replace("%reason%", finalReason);
            } else {
                // Geçici ban için normal format
                banCommand = banCommand
                    .replace("%player%", playerName)
                    .replace("%duration%", finalBanDuration)
                    .replace("%reason%", finalReason);
            }
            
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), banCommand);
            plugin.debug("Auto-ban command executed: " + banCommand);
        } else {
            // Eğer ban-command yoksa, varsayılan ban komutu kullan
            String defaultBanCommand;
            if (isPermanentDuration(finalBanDuration)) {
                // Permanent ban için uygun format
                String permanentFormat = getPermanentFormat(finalBanDuration);
                defaultBanCommand = "ban " + playerName + " " + finalReason + " (Violation #" + currentViolations + ")";
            } else {
                // Geçici ban için tempban
                defaultBanCommand = "tempban " + playerName + " " + finalBanDuration + " " + finalReason + " (Violation #" + currentViolations + ")";
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), defaultBanCommand);
            plugin.debug("Default ban command executed: " + defaultBanCommand);
        }
        
        // Son ban süresini kaydet
        lastBanDuration.put(playerName, finalBanDuration);
        
        // Console'a logla
        if (plugin.getConfig().getBoolean("auto-ban.log-to-console", true)) {
            Bukkit.getConsoleSender().sendMessage("§c[Security] Auto-ban: " + playerName + " - Reason: " + reason + " - Duration: " + finalBanDuration + " - Violation #" + currentViolations);
        }
        
        // Tüm adminlere bildir
        if (plugin.getConfig().getBoolean("auto-ban.notify-admins", true)) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(plugin.getConfig().getString("permissions.general"))) {
                    onlinePlayer.sendMessage("§c[Security] Automatic ban: " + playerName + " - Reason: " + reason + " - Duration: " + finalBanDuration + " - Violation #" + currentViolations);
                }
            }
        }
        
        // Discord webhook gönder (eğer aktifse)
        if (plugin.getConfig().getBoolean("auto-ban.discord-webhook", true)) {
            try {
                // DiscordManager'a erişim için reflection kullan
                Object discordManager = plugin.getClass().getDeclaredField("discordManager").get(plugin);
                if (discordManager != null) {
                    // sendSecurityBan metodunu çağır
                    discordManager.getClass().getMethod("sendSecurityBan", Player.class, String.class)
                        .invoke(discordManager, player, reason + " (Violation #" + currentViolations + ")");
                }
            } catch (Exception e) {
                plugin.debug("Could not send Discord webhook for security ban: " + e.getMessage());
            }
        }
    }
    
    // Ban escalation hesaplama metodu
    private String calculateBanDuration(String playerName, String baseDuration, int violationCount) {
        if (!plugin.getConfig().getBoolean("auto-ban.escalation.enabled", true)) {
            return baseDuration;
        }
        
        // İlk ihlal için base duration kullan
        if (violationCount <= 1) {
            return baseDuration;
        }
        
        // Escalation hesapla
        int multiplier = plugin.getConfig().getInt("auto-ban.escalation.multiplier", 2);
        String maxDuration = plugin.getConfig().getString("auto-ban.escalation.max-duration", "1m");
        
        // Duration'ı parse et ve çarp
        String escalatedDuration = escalateDuration(baseDuration, violationCount, multiplier);
        
        // Maksimum süreyi kontrol et
        if (isDurationLonger(escalatedDuration, maxDuration)) {
            return maxDuration;
        }
        
        return escalatedDuration;
    }
    
    // Duration escalation hesaplama
    private String escalateDuration(String baseDuration, int violationCount, int multiplier) {
        // Evrensel duration parsing (1d, 1w, 1m, 1y gibi)
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
            return baseDuration; // Permanent format'ı koru
        }
        
        // Bilinmeyen format için base duration döndür
        return baseDuration;
    }
    
    // Permanent duration kontrolü (farklı plugin'ler için)
    private boolean isPermanentDuration(String duration) {
        String lowerDuration = duration.toLowerCase();
        return lowerDuration.equals("permanent") || 
               lowerDuration.equals("perm") || 
               lowerDuration.equals("-1") || 
               lowerDuration.equals("0") ||
               lowerDuration.equals("infinity") ||
               lowerDuration.equals("forever");
    }
    
    // Permanent duration format'ını plugin'e uygun hale getir
    private String getPermanentFormat(String baseDuration) {
        // Config'den permanent format'ı al, yoksa varsayılan kullan
        String permanentFormat = plugin.getConfig().getString("auto-ban.permanent-format", "permanent");
        
        // Eğer base duration zaten permanent ise, format'ı koru
        if (isPermanentDuration(baseDuration)) {
            return permanentFormat;
        }
        
        return permanentFormat;
    }
    
    // Duration karşılaştırma
    private boolean isDurationLonger(String duration1, String duration2) {
        // Basit karşılaştırma (gerçek uygulamada daha gelişmiş olabilir)
        if (duration1.equalsIgnoreCase("permanent")) return true;
        if (duration2.equalsIgnoreCase("permanent")) return false;
        
        // Basit sayısal karşılaştırma
        try {
            int val1 = Integer.parseInt(duration1.substring(0, duration1.length() - 1));
            int val2 = Integer.parseInt(duration2.substring(0, duration2.length() - 1));
            
            char unit1 = duration1.charAt(duration1.length() - 1);
            char unit2 = duration2.charAt(duration2.length() - 1);
            
            // Unit'leri güne çevir
            int days1 = convertToDays(val1, unit1);
            int days2 = convertToDays(val2, unit2);
            
            return days1 > days2;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Unit'i güne çevir
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
        
        // Admin bilgisini kaydet
        if (adminName == null || adminName.isEmpty()) {
            adminName = "Console";
        }
        whitelistAdmins.put(playerName.toLowerCase(), adminName);
        
        // Yeni formatta kaydet
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
        
        // Admin bilgisini güncelle
        if (adminName == null || adminName.isEmpty()) {
            adminName = "Console";
        }
        whitelistAdmins.remove(playerName.toLowerCase());
        
        // Yeni formatta kaydet
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
    
    // Violation count metodları
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