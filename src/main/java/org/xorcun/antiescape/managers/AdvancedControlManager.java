package org.xorcun.antiescape.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.xorcun.antiescape.AntiEscape;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdvancedControlManager {
    private final AntiEscape plugin;
    private final Map<UUID, ControlSession> controlSessions;
    private final Map<String, List<ControlHistory>> controlHistory; // String olarak oyuncu adı
    private final Map<UUID, List<String>> playerNotes;
    private final Map<UUID, Integer> escapeAttempts;
    private final Map<UUID, BukkitTask> warningTasks;
    private final Map<UUID, BukkitTask> autoReleaseTasks;
    private final Map<UUID, Long> controlCooldowns;
    
    private File historyFile;
    private FileConfiguration historyConfig;
    private File notesFile;
    private FileConfiguration notesConfig;
    private final SimpleDateFormat dateFormat;

    // Cached configuration values
    private boolean advancedControlEnabled;
    private boolean historyEnabled;
    private boolean notesEnabled;
    private int maxNotes;
    private int noteLength;
    private int escapeAttemptsLimit;
    private String escapeBanDuration;
    private String escapeBanReason;
    private boolean timeLimitsEnabled;
    private int warningTime;
    private int maxDuration;
    private boolean autoReleaseEnabled;
    private boolean notificationsEnabled;
    private boolean titlesEnabled;
    private boolean soundEnabled;
    private boolean actionBarEnabled;

    public AdvancedControlManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.controlSessions = new HashMap<>();
        this.controlHistory = new HashMap<>();
        this.playerNotes = new HashMap<>();
        this.escapeAttempts = new HashMap<>();
        this.warningTasks = new HashMap<>();
        this.autoReleaseTasks = new HashMap<>();
        this.controlCooldowns = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        initializeHistoryFile();
        initializeNotesFile();
        reloadConfig();
        loadHistory();
        loadNotes();
    }

    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.advancedControlEnabled = config.getBoolean("advanced-control.enabled", true);
        this.historyEnabled = config.getBoolean("advanced-control.history.enabled", true);
        this.notesEnabled = config.getBoolean("advanced-control.notes.enabled", true);
        this.maxNotes = config.getInt("advanced-control.notes.max-notes", 10);
        this.noteLength = config.getInt("advanced-control.notes.note-length", 200);
        this.escapeAttemptsLimit = config.getInt("advanced-security.auto-ban.escape-attempts", 3);
        this.escapeBanDuration = config.getString("advanced-security.auto-ban.escape-ban.duration", "7d");
        this.escapeBanReason = config.getString("advanced-security.auto-ban.escape-ban.reason", "Escape attempt during control");
        this.timeLimitsEnabled = config.getBoolean("advanced-control.time-limits.enabled", true);
        this.warningTime = config.getInt("advanced-control.time-limits.warning-time", 300);
        this.maxDuration = config.getInt("advanced-control.time-limits.max-duration", 3600);
        this.autoReleaseEnabled = config.getBoolean("advanced-control.time-limits.auto-release", true);
        this.notificationsEnabled = config.getBoolean("advanced-control.notifications.enabled", true);
        this.titlesEnabled = config.getBoolean("advanced-control.notifications.title", true);
        this.soundEnabled = config.getBoolean("advanced-control.notifications.sound", true);
        this.actionBarEnabled = config.getBoolean("advanced-control.notifications.action-bar", true);
    }

    public boolean checkControlCooldown(Player player) {
        if (player.hasPermission("antiescape.bypass.cooldown")) return true;
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        int cooldownSeconds = plugin.getConfig().getInt("advanced-security.suspicious-activity.control-spam-cooldown", 5);
        long cooldownMillis = cooldownSeconds * 1000L;
        
        if (controlCooldowns.containsKey(uuid)) {
            long lastUsed = controlCooldowns.get(uuid);
            long diff = currentTime - lastUsed;
            
            if (diff < cooldownMillis) {
                long remainingSeconds = (cooldownMillis - diff) / 1000 + 1;
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-cooldown-error")
                    .replace("%seconds%", String.valueOf(remainingSeconds)));
                return false;
            }
        }
        
        controlCooldowns.put(uuid, currentTime);
        return true;
    }

    private void initializeHistoryFile() {
        String historyPath = plugin.getConfig().getString("advanced-control.history.history-file", "data/control-history.yml");
        historyFile = new File(plugin.getDataFolder(), historyPath);
        
        File parentDir = historyFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                plugin.debug("History file could not be created: " + e.getMessage());
            }
        }
        
        historyConfig = YamlConfiguration.loadConfiguration(historyFile);
    }

    private void initializeNotesFile() {
        notesFile = new File(plugin.getDataFolder(), "data/notes.yml");
        
        File parentDir = notesFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        if (!notesFile.exists()) {
            try {
                notesFile.createNewFile();
            } catch (IOException e) {
                plugin.debug("Notes file could not be created: " + e.getMessage());
            }
        }
        
        notesConfig = YamlConfiguration.loadConfiguration(notesFile);
    }

    private void loadHistory() {
        if (!plugin.getConfig().getBoolean("advanced-control.history.enabled", true)) return;
        
        for (String playerName : historyConfig.getKeys(false)) {
            try {
                List<ControlHistory> history = new ArrayList<>();
                
                List<Map<?, ?>> historyList = historyConfig.getMapList(playerName + ".history");
                for (Map<?, ?> entry : historyList) {
                    ControlHistory controlHistory = new ControlHistory(
                        (String) entry.get("controller"),
                        (String) entry.get("target"),
                        (String) entry.get("startTime"),
                        (String) entry.get("endTime"),
                        (Long) entry.get("duration"),
                        (Boolean) entry.get("banned")
                    );
                    history.add(controlHistory);
                }
                
                controlHistory.put(playerName, history);
                
                List<String> notes = historyConfig.getStringList(playerName + ".notes");

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().equals(playerName)) {
                        playerNotes.put(onlinePlayer.getUniqueId(), notes);
                        break;
                    }
                }
                
            } catch (Exception e) {
                plugin.debug("History loading error: " + playerName + " - " + e.getMessage());
            }
        }
    }

    private void loadNotes() {
        if (notesConfig == null) return;
        
        for (String playerName : notesConfig.getKeys(false)) {
            try {
                List<String> notes = notesConfig.getStringList(playerName + ".notes");
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getName().equals(playerName)) {
                        playerNotes.put(onlinePlayer.getUniqueId(), notes);
                        break;
                    }
                }
                
            } catch (Exception e) {
                plugin.debug("Notes loading error: " + playerName + " - " + e.getMessage());
            }
        }
    }

    public void saveHistory() {
        if (historyConfig == null) return;
        
        try {
            for (Map.Entry<String, List<ControlHistory>> entry : controlHistory.entrySet()) {
                String playerName = entry.getKey();
                List<ControlHistory> history = entry.getValue();
                
                List<Map<String, Object>> historyList = new ArrayList<>();
                for (ControlHistory controlHistory : history) {
                    Map<String, Object> historyEntry = new HashMap<>();
                    historyEntry.put("controller", controlHistory.getController());
                    historyEntry.put("target", controlHistory.getTarget());
                    historyEntry.put("startTime", controlHistory.getStartTime());
                    historyEntry.put("endTime", controlHistory.getEndTime());
                    historyEntry.put("duration", controlHistory.getDuration());
                    historyEntry.put("banned", controlHistory.isBanned());
                    historyList.add(historyEntry);
                }
                
                historyConfig.set(playerName + ".history", historyList);
            }
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    historyConfig.save(historyFile);
                } catch (IOException e) {
                    plugin.debug("History saving error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.debug("History processing error: " + e.getMessage());
        }
    }

    public void saveNotes() {
        if (notesConfig == null) return;
        
        try {
            for (Map.Entry<UUID, List<String>> entry : playerNotes.entrySet()) {
                UUID playerUUID = entry.getKey();
                List<String> notes = entry.getValue();
                
                String playerName = null;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getUniqueId().equals(playerUUID)) {
                        playerName = onlinePlayer.getName();
                        break;
                    }
                }
                
                if (playerName == null) {
                    playerName = playerUUID.toString();
                }
                
                notesConfig.set(playerName + ".notes", notes);
            }
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    notesConfig.save(notesFile);
                } catch (IOException e) {
                    plugin.debug("Notes saving error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            plugin.debug("Notes processing error: " + e.getMessage());
        }
    }

    public void startControlSession(Player controller, Player target) {
        if (!advancedControlEnabled) return;
        
        UUID targetUUID = target.getUniqueId();
        long startTime = System.currentTimeMillis();
        
        ControlSession session = new ControlSession(controller.getUniqueId(), startTime);
        controlSessions.put(targetUUID, session);
        
        escapeAttempts.put(targetUUID, 0);
        
        if (timeLimitsEnabled) {
            startWarningTask(target);
            startAutoReleaseTask(target);
        }
        
        showNotifications(target, "control-start", controller.getName());
        showNotifications(controller, "control-start-controller", target.getName());
        
        plugin.debug("Advanced control session started: " + target.getName());
    }

    public void endControlSession(Player target, boolean banned) {
        if (!advancedControlEnabled) return;
        
        UUID targetUUID = target.getUniqueId();
        ControlSession session = controlSessions.get(targetUUID);
        
        if (session != null) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - session.getStartTime();
            
            addToHistory(session.getController(), target.getName(), session.getStartTime(), endTime, duration, banned);
            
            controlSessions.remove(targetUUID);
            escapeAttempts.remove(targetUUID);
            
            
            cancelTasks(targetUUID);
            
            Player controller = Bukkit.getPlayer(session.getController());
            showNotifications(target, "control-end", controller != null ? controller.getName() : "Staff");
            
            plugin.debug("Advanced control session ended: " + target.getName());
        }
    }

    public void recordEscapeAttempt(Player player) {
        if (!advancedControlEnabled) return;
        
        UUID playerUUID = player.getUniqueId();
        int attempts = escapeAttempts.getOrDefault(playerUUID, 0) + 1;
        escapeAttempts.put(playerUUID, attempts);
        
        if (plugin.getConfig().getBoolean("advanced-control.statistics.track-escapes", true)) {
            plugin.debug("Escape attempt recorded: " + player.getName() + " (Attempt: " + attempts + ")");
        }
        
        if (attempts >= escapeAttemptsLimit) {
            autoBanPlayer(player, escapeBanReason, escapeBanDuration);
        }
    }

    public void addNote(Player player, String note) {
        if (!notesEnabled) return;
        
        UUID playerUUID = player.getUniqueId();
        List<String> notes = playerNotes.getOrDefault(playerUUID, new ArrayList<>());
        
        if (notes.size() >= maxNotes) {
            notes.remove(0); 
        }
        
        if (note.length() > noteLength) {
            note = note.substring(0, noteLength);
        }
        
        String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
        notes.add(timestampedNote);
        playerNotes.put(playerUUID, notes);
        
        saveNotes();
        
        plugin.debug("Note added: " + player.getName() + " - " + note);
    }

    public void addNote(String playerName, String note) {
        if (!plugin.getConfig().getBoolean("advanced-control.notes.enabled", true)) return;
        
        UUID playerUUID = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                playerUUID = onlinePlayer.getUniqueId();
                break;
            }
        }
        
        if (playerUUID == null) {

            if (notesConfig != null) {
                List<String> notes = notesConfig.getStringList(playerName + ".notes");
                
                int maxNotes = plugin.getConfig().getInt("advanced-control.notes.max-notes", 10);
                int maxLength = plugin.getConfig().getInt("advanced-control.notes.note-length", 200);
                
                if (notes.size() >= maxNotes) {
                    notes.remove(0); 
                }
                
                if (note.length() > maxLength) {
                    note = note.substring(0, maxLength);
                }
                
                String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
                notes.add(timestampedNote);
                notesConfig.set(playerName + ".notes", notes);
                
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        notesConfig.save(notesFile);
                    } catch (IOException e) {
                        plugin.debug("Error adding note: " + e.getMessage());
                    }
                });
                
                plugin.debug("Note added (offline): " + playerName + " - " + note);
            }
            return;
        }
        
        List<String> notes = playerNotes.getOrDefault(playerUUID, new ArrayList<>());
        
        int maxNotes = plugin.getConfig().getInt("advanced-control.notes.max-notes", 10);
        int maxLength = plugin.getConfig().getInt("advanced-control.notes.note-length", 200);
        
        if (notes.size() >= maxNotes) {
            notes.remove(0);
        }
        
        if (note.length() > maxLength) {
            note = note.substring(0, maxLength);
        }
        
        String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
        notes.add(timestampedNote);
        playerNotes.put(playerUUID, notes);
        
        saveNotes();
        
        plugin.debug("Note added: " + playerName + " - " + note);
    }

    public List<String> getNotes(Player player) {
        return playerNotes.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public List<String> getNotes(String playerName) {

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                return playerNotes.getOrDefault(onlinePlayer.getUniqueId(), new ArrayList<>());
            }
        }
        

        if (notesConfig != null) {
            return notesConfig.getStringList(playerName + ".notes");
        }
        
        return new ArrayList<>();
    }

    public void clearNotes(Player player) {
        playerNotes.remove(player.getUniqueId());
        

        saveNotes();
        
        plugin.debug("Notes cleared: " + player.getName());
    }

    public void clearNotes(String playerName) {

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                playerNotes.remove(onlinePlayer.getUniqueId());
                break;
            }
        }
        

        if (notesConfig != null) {
            notesConfig.set(playerName + ".notes", new ArrayList<>());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    notesConfig.save(notesFile);
                } catch (IOException e) {
                    plugin.debug("Error clearing notes: " + e.getMessage());
                }
            });
        }
        
        plugin.debug("Notes cleared: " + playerName);
    }

    public boolean isPlayerControlled(UUID playerUUID) {
        return controlSessions.containsKey(playerUUID);
    }

    public ControlSession getControlSession(UUID playerUUID) {
        return controlSessions.get(playerUUID);
    }

    public List<ControlHistory> getControlHistory(UUID playerUUID) {

        return controlHistory.getOrDefault(playerUUID.toString(), new ArrayList<>());
    }
    
    public List<ControlHistory> getControlHistory(String playerName) {

        return controlHistory.getOrDefault(playerName, new ArrayList<>());
    }

    public Map<String, List<ControlHistory>> getAllHistory() {
        return controlHistory;
    }

    public int getEscapeAttempts(UUID playerUUID) {
        return escapeAttempts.getOrDefault(playerUUID, 0);
    }

    public void addEscapeToHistory(UUID controllerUUID, String targetName) {
        if (!historyEnabled) return;
        
        String controllerName = "Unknown";
        if (controllerUUID != null) {
            Player controller = Bukkit.getPlayer(controllerUUID);
            if (controller != null) {
                controllerName = controller.getName();
            }
        }
        
        long currentTime = System.currentTimeMillis();
        

        ControlHistory escapeHistory = new ControlHistory(
            controllerName,
            targetName,
            dateFormat.format(new Date(currentTime - 60000)), 
            dateFormat.format(new Date(currentTime)),
            60000, 
            true 
        );
        

        List<ControlHistory> playerHistory = controlHistory.getOrDefault(targetName, new ArrayList<>());
        playerHistory.add(escapeHistory);
        

        int maxEntries = plugin.getConfig().getInt("advanced-control.history.max-entries", 1000);
        if (playerHistory.size() > maxEntries) {
            playerHistory.remove(0); 
        }
        
        controlHistory.put(targetName, playerHistory);
        

        saveHistory();
        

        plugin.debug("Escape history added: " + controllerName + " -> " + targetName + " (Player quit while controlled)");
    }


    private void startWarningTask(Player player) {
        if (warningTime > 0 && warningTime < maxDuration) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    showNotifications(player, "control-warning", "");
                }
            }.runTaskLater(plugin, (maxDuration - warningTime) * 20L);
            
            warningTasks.put(player.getUniqueId(), task);
        }
    }

    private void startAutoReleaseTask(Player player) {
        int maxDuration = plugin.getConfig().getInt("advanced-control.time-limits.max-duration", 3600);
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (autoReleaseEnabled) {

                    ControlSession session = controlSessions.get(player.getUniqueId());
                    if (session != null) {
                        Player controller = Bukkit.getPlayer(session.getController());
                        if (controller != null && controller.isOnline()) {

                            plugin.getServer().dispatchCommand(controller, "control end " + player.getName());
                        }
                    }
                }
            }
        }.runTaskLater(plugin, maxDuration * 20L);
        
        autoReleaseTasks.put(player.getUniqueId(), task);
    }

    private void cancelTasks(UUID playerUUID) {
        BukkitTask warningTask = warningTasks.remove(playerUUID);
        if (warningTask != null) {
            warningTask.cancel();
        }
        
        BukkitTask autoReleaseTask = autoReleaseTasks.remove(playerUUID);
        if (autoReleaseTask != null) {
            autoReleaseTask.cancel();
        }
    }

    private void showNotifications(Player player, String type, String otherPlayerName) {
        if (!notificationsEnabled) return;

        switch (type) {
            case "control-start":
                if (titlesEnabled) {
                    player.sendTitle(
                        plugin.getMessageFileManager().getLangMessage("title-control-start"),
                        plugin.getMessageFileManager().getLangMessage("subtitle-control-start")
                            .replace("%player%", otherPlayerName),
                        10, 70, 20
                    );
                }
                if (actionBarEnabled) {
                    player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-message")
                        .replace("%player%", otherPlayerName));
                }
                if (soundEnabled) {
                    player.playSound(player.getLocation(), "entity.enderdragon.growl", 1.0f, 0.5f);
                }
                break;
                
            case "control-start-controller":
                if (titlesEnabled) {
                    player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-player-started-controller")
                        .replace("%player%", otherPlayerName));
                }
                break;
                
            case "control-warning":
                if (titlesEnabled) {
                    player.sendTitle("§e§lWARNING", "§7Control will end soon", 10, 70, 20);
                }
                if (soundEnabled) {
                    player.playSound(player.getLocation(), "block.note_block.pling", 1.0f, 1.0f);
                }
                break;
                
            case "control-end":
                if (titlesEnabled) {
                    player.sendTitle(
                        plugin.getMessageFileManager().getLangMessage("title-control-end"),
                        plugin.getMessageFileManager().getLangMessage("subtitle-control-end")
                            .replace("%player%", otherPlayerName),
                        10, 70, 20
                    );
                }
                if (soundEnabled) {
                    player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                }
                break;
        }
    }

    private void addToHistory(UUID controllerUUID, String targetName, long startTime, long endTime, long duration, boolean banned) {
        if (!historyEnabled) return;
        
        Player controller = Bukkit.getPlayer(controllerUUID);
        String controllerName = controller != null ? controller.getName() : "Unknown";
        
        ControlHistory history = new ControlHistory(
            controllerName,
            targetName,
            dateFormat.format(new Date(startTime)),
            dateFormat.format(new Date(endTime)),
            duration,
            banned
        );
        

        List<ControlHistory> playerHistory = controlHistory.getOrDefault(targetName, new ArrayList<>());
        playerHistory.add(history);
        

        int maxEntries = plugin.getConfig().getInt("advanced-control.history.max-entries", 1000);
        if (playerHistory.size() > maxEntries) {
            playerHistory.remove(0); 
        }
        
        controlHistory.put(targetName, playerHistory);
        

        saveHistory();
        

        plugin.debug("History added: " + controllerName + " -> " + targetName + " (" + dateFormat.format(new Date(startTime)) + " - " + dateFormat.format(new Date(endTime)) + ")");
    }

    public void autoBanPlayer(Player player, String reason, String duration) {
        if (!plugin.getConfig().getBoolean("advanced-security.auto-ban.enabled", true)) return;
        
        String banCommand = plugin.getConfig().getString("advanced-security.auto-ban.ban-command");
        if (banCommand != null && !banCommand.isEmpty()) {
            banCommand = banCommand.replace("%player%", player.getName())
                                   .replace("%reason%", reason)
                                   .replace("%duration%", duration);
            
            // Remove leading slash if present
            if (banCommand.startsWith("/")) {
                banCommand = banCommand.substring(1);
            }
            
            plugin.debug("Executing ban command: " + banCommand);
            try {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), banCommand);
                plugin.debug("Auto ban: " + player.getName() + " - Reason: " + reason + " - Duration: " + duration);
            } catch (Exception e) {
                plugin.debug("Error executing ban command: " + e.getMessage());
            }
        }
    }

    public void onDisable() {
        saveHistory();
        saveNotes(); 
        

        

        for (BukkitTask task : warningTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : autoReleaseTasks.values()) {
            task.cancel();
        }
    }

    // İç sınıflar
    public static class ControlSession {
        private final UUID controller;
        private final long startTime;

        public ControlSession(UUID controller, long startTime) {
            this.controller = controller;
            this.startTime = startTime;
        }

        public UUID getController() { return controller; }
        public long getStartTime() { return startTime; }
    }

    public static class ControlHistory {
        private final String controller;
        private final String target;
        private final String startTime;
        private final String endTime;
        private final long duration;
        private final boolean banned;

        public ControlHistory(String controller, String target, String startTime, String endTime, long duration, boolean banned) {
            this.controller = controller;
            this.target = target;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
            this.banned = banned;
        }

        public String getController() { return controller; }
        public String getTarget() { return target; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public long getDuration() { return duration; }
        public boolean isBanned() { return banned; }
    }
} 