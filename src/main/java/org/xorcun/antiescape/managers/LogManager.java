package org.xorcun.antiescape.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogManager {
    private final AntiEscape plugin;
    private final Map<String, File> logFiles;
    private final SimpleDateFormat dateFormat;
    private final ExecutorService logExecutor;

    // Cached settings
    private boolean loggingEnabled;
    private boolean timestampEnabled;
    private final Map<String, Boolean> typeEnabledCache;

    public LogManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.logFiles = new HashMap<>();
        this.logExecutor = Executors.newSingleThreadExecutor();
        this.typeEnabledCache = new HashMap<>();
        
        initializeLogFiles();
        reloadConfig();
    }

    public void reloadConfig() {
        this.loggingEnabled = plugin.getConfig().getBoolean("logging.enabled", true);
        this.timestampEnabled = plugin.getConfig().getBoolean("logging.format.timestamp", true);
        
        String[] logTypes = {"moves", "commands", "chat", "damage", "items", "control", "general"};
        for (String logType : logTypes) {
            boolean enabled = plugin.getConfig().getBoolean("logging.log-" + logType, true);
            typeEnabledCache.put(logType, enabled);
        }
    }

    private void initializeLogFiles() {
        // Create the logs folder
        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        // Create files for each log type
        String[] logTypes = {"moves", "commands", "chat", "damage", "items", "control", "general"};
        
        for (String logType : logTypes) {
            String filePath = plugin.getConfig().getString("logging.files." + logType, "logs/antiescape-" + logType + ".log");
            File logFile = new File(plugin.getDataFolder(), filePath);
            
            // Create the parent directory of the file
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            logFiles.put(logType, logFile);
        }
    }

    private boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    private boolean isLogTypeEnabled(String logType) {
        return typeEnabledCache.getOrDefault(logType, true);
    }

    private void writeLogInternal(String logType, String message) {
        File logFile = logFiles.get(logType);
        if (logFile == null) return;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            String timestamp = "";
            if (timestampEnabled) {
                timestamp = "[" + dateFormat.format(new Date()) + "] ";
            }
            writer.println(timestamp + message);
        } catch (IOException e) {
            plugin.debug("Log writing error (" + logType + "): " + e.getMessage());
        }
    }

    // Hareket logları
    public void logMove(Player player, Location from, Location to) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("moves")) return;
        logExecutor.submit(() -> {
            String message = String.format("[MOVE] %s moved from %s to %s", 
                player.getName(), 
                formatLocation(from), 
                formatLocation(to));
            writeLogInternal("moves", message);
        });
    }

    // Komut logları
    public void logCommand(Player player, String command) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("commands")) return;
        logExecutor.submit(() -> {
            String message = String.format("[COMMAND] %s executed: %s", 
                player.getName(), 
                command);
            writeLogInternal("commands", message);
        });
    }

    // Chat logları
    public void logChat(Player player, String message) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("chat")) return;
        logExecutor.submit(() -> {
            String logMessage = String.format("[CHAT] %s: %s", 
                player.getName(), 
                message);
            writeLogInternal("chat", logMessage);
        });
    }

    // Hasar logları
    public void logDamage(Player attacker, Player victim, double damage) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("damage")) return;
        logExecutor.submit(() -> {
            String message = String.format("[DAMAGE] %s attacked %s for %.2f damage", 
                attacker.getName(), 
                victim.getName(), 
                damage);
            writeLogInternal("damage", message);
        });
    }

    // Eşya logları
    public void logItemDrop(Player player, String itemName) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("items")) return;
        Location location = player.getLocation().clone();
        logExecutor.submit(() -> {
            String message = String.format("[ITEM] %s dropped: %s at %s", 
                player.getName(), 
                itemName,
                formatLocation(location));
            writeLogInternal("items", message);
        });
    }

    // Kontrol logları
    public void logControlStart(Player controller, Player target) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("control")) return;
        logExecutor.submit(() -> {
            String message = String.format("[CONTROL] %s started controlling %s", 
                controller.getName(), 
                target.getName());
            writeLogInternal("control", message);
        });
    }

    public void logControlEnd(Player controller, Player target, boolean banned) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("control")) return;
        logExecutor.submit(() -> {
            String action = banned ? "banned" : "released";
            String message = String.format("[CONTROL] %s %s %s", 
                controller.getName(), 
                action, 
                target.getName());
            writeLogInternal("control", message);
        });
    }

    public void logControlEscape(Player player) {
        if (!isLoggingEnabled() || !isLogTypeEnabled("control")) return;
        logExecutor.submit(() -> {
            String message = String.format("[CONTROL] %s escaped control", 
                player.getName());
            writeLogInternal("control", message);
        });
    }


    // General log
    public void logGeneral(String message) {
        logExecutor.submit(() -> {
            writeLogInternal("general", "[GENERAL] " + message);
        });
    }

    // Detailed player information log
    public void logPlayerInfo(Player player, String action, String details) {
        logExecutor.submit(() -> {
            String message = String.format("[PLAYER] %s %s - Details: %s", 
                player.getName(), 
                action, 
                details);
            writeLogInternal("general", message);
        });
    }

    // Server events log
    public void logServerEvent(String event, String details) {
        logExecutor.submit(() -> {
            String message = String.format("[SERVER] %s - %s", 
                event, 
                details);
            writeLogInternal("general", message);
        });
    }

    // Error log
    public void logError(String error, String details) {
        logExecutor.submit(() -> {
            String message = String.format("[ERROR] %s - Details: %s", 
                error, 
                details);
            writeLogInternal("general", message);
        });
    }

    // Success log
    public void logSuccess(String action, String details) {
        logExecutor.submit(() -> {
            String message = String.format("[SUCCESS] %s - %s", 
                action, 
                details);
            writeLogInternal("general", message);
        });
    }

    // Get the log file path
    public String getLogFilePath(String logType) {
        File logFile = logFiles.get(logType);
        return logFile != null ? logFile.getAbsolutePath() : "Log file not found";
    }

    // Get paths for all log files
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
                writer.print(""); // Clear the file
            } catch (IOException e) {
                plugin.debug("Log clearing error (" + logType + "): " + e.getMessage());
            }
        }
    }

    // Clear all logs
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

    public void shutdown() {
        if (logExecutor != null && !logExecutor.isShutdown()) {
            logExecutor.shutdown();
        }
    }
} 