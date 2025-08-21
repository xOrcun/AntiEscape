package org.xorcun.antiescape;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogManager {
    private final AntiEscape plugin;
    private final Map<String, File> logFiles;
    private final SimpleDateFormat dateFormat;

    public LogManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.logFiles = new HashMap<>();
        
        initializeLogFiles();
    }

    private void initializeLogFiles() {
        // Log klasörünü oluştur
        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        // Her log türü için dosya oluştur
        String[] logTypes = {"moves", "commands", "chat", "damage", "items", "control", "general"};
        
        for (String logType : logTypes) {
            String filePath = plugin.getConfig().getString("logging.files." + logType, "logs/antiescape-" + logType + ".log");
            File logFile = new File(plugin.getDataFolder(), filePath);
            
            // Dosyanın üst klasörünü oluştur
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            logFiles.put(logType, logFile);
        }
    }

    private boolean isLoggingEnabled() {
        return plugin.getConfig().getBoolean("logging.enabled", true);
    }

    private boolean isLogTypeEnabled(String logType) {
        return plugin.getConfig().getBoolean("logging.log-" + logType, true);
    }

    private void writeLog(String logType, String message) {
        if (!isLoggingEnabled() || !isLogTypeEnabled(logType)) return;
        
        File logFile = logFiles.get(logType);
        if (logFile == null) return;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            String timestamp = "";
            if (plugin.getConfig().getBoolean("logging.format.timestamp", true)) {
                timestamp = "[" + dateFormat.format(new Date()) + "] ";
            }
            writer.println(timestamp + message);
        } catch (IOException e) {
            plugin.debug("Log writing error (" + logType + "): " + e.getMessage());
        }
    }

    // Hareket logları
    public void logMove(Player player, Location from, Location to) {
        String message = String.format("[MOVE] %s moved from %s to %s", 
            player.getName(), 
            formatLocation(from), 
            formatLocation(to));
        writeLog("moves", message);
    }

    // Komut logları
    public void logCommand(Player player, String command) {
        String message = String.format("[COMMAND] %s executed: %s", 
            player.getName(), 
            command);
        writeLog("commands", message);
    }

    // Chat logları
    public void logChat(Player player, String message) {
        String logMessage = String.format("[CHAT] %s: %s", 
            player.getName(), 
            message);
        writeLog("chat", logMessage);
    }

    // Hasar logları
    public void logDamage(Player attacker, Player victim, double damage) {
        String message = String.format("[DAMAGE] %s attacked %s for %.2f damage", 
            attacker.getName(), 
            victim.getName(), 
            damage);
        writeLog("damage", message);
    }

    // Eşya logları
    public void logItemDrop(Player player, String itemName) {
        String message = String.format("[ITEM] %s dropped: %s at %s", 
            player.getName(), 
            itemName,
            formatLocation(player.getLocation()));
        writeLog("items", message);
    }

    // Kontrol logları
    public void logControlStart(Player controller, Player target) {
        String message = String.format("[CONTROL] %s started controlling %s", 
            controller.getName(), 
            target.getName());
        writeLog("control", message);
    }

    public void logControlEnd(Player controller, Player target, boolean banned) {
        String action = banned ? "banned" : "released";
        String message = String.format("[CONTROL] %s %s %s", 
            controller.getName(), 
            action, 
            target.getName());
        writeLog("control", message);
    }

    public void logControlEscape(Player player) {
        String message = String.format("[CONTROL] %s escaped control", 
            player.getName());
        writeLog("control", message);
    }

    // Teleport logları
    public void logTeleport(Player player, Location from, Location to) {
        String message = String.format("[TELEPORT] %s teleported from %s to %s", 
            player.getName(), 
            formatLocation(from), 
            formatLocation(to));
        writeLog("moves", message);
    }

    // Genel log
    public void logGeneral(String message) {
        writeLog("general", "[GENERAL] " + message);
    }

    // Detaylı oyuncu bilgisi logu
    public void logPlayerInfo(Player player, String action, String details) {
        String message = String.format("[PLAYER] %s %s - Details: %s", 
            player.getName(), 
            action, 
            details);
        writeLog("general", message);
    }

    // Sunucu olayları logu
    public void logServerEvent(String event, String details) {
        String message = String.format("[SERVER] %s - %s", 
            event, 
            details);
        writeLog("general", message);
    }

    // Hata logu
    public void logError(String error, String details) {
        String message = String.format("[ERROR] %s - Details: %s", 
            error, 
            details);
        writeLog("general", message);
    }

    // Başarı logu
    public void logSuccess(String action, String details) {
        String message = String.format("[SUCCESS] %s - %s", 
            action, 
            details);
        writeLog("general", message);
    }

    // Log dosyası yolunu alma
    public String getLogFilePath(String logType) {
        File logFile = logFiles.get(logType);
        return logFile != null ? logFile.getAbsolutePath() : "Log file not found";
    }

    // Tüm log dosyalarının yollarını alma
    public Map<String, String> getAllLogFilePaths() {
        Map<String, String> paths = new HashMap<>();
        for (Map.Entry<String, File> entry : logFiles.entrySet()) {
            paths.put(entry.getKey(), entry.getValue().getAbsolutePath());
        }
        return paths;
    }

    // Log dosyalarını temizleme
    public void clearLogs(String logType) {
        File logFile = logFiles.get(logType);
        if (logFile != null && logFile.exists()) {
            try (PrintWriter writer = new PrintWriter(logFile)) {
                writer.print(""); // Dosyayı temizle
            } catch (IOException e) {
                plugin.debug("Log clearing error (" + logType + "): " + e.getMessage());
            }
        }
    }

    // Tüm logları temizleme
    public void clearAllLogs() {
        for (String logType : logFiles.keySet()) {
            clearLogs(logType);
        }
    }

    private String formatLocation(Location location) {
        if (location == null) return "null";
        return String.format("%s,%.2f,%.2f,%.2f", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ());
    }
} 