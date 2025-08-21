package org.xorcun.antiescape;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private final Map<UUID, BossBar> bossBars;
    private final Map<UUID, BukkitTask> warningTasks;
    private final Map<UUID, BukkitTask> autoReleaseTasks;
    
    private File historyFile;
    private FileConfiguration historyConfig;
    private File notesFile;
    private FileConfiguration notesConfig;
    private final SimpleDateFormat dateFormat;

    public AdvancedControlManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.controlSessions = new HashMap<>();
        this.controlHistory = new HashMap<>();
        this.playerNotes = new HashMap<>();
        this.escapeAttempts = new HashMap<>();
        this.bossBars = new HashMap<>();
        this.warningTasks = new HashMap<>();
        this.autoReleaseTasks = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        initializeHistoryFile();
        initializeNotesFile();
        loadHistory();
        loadNotes();
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
                
                // Notes yükle
                List<String> notes = historyConfig.getStringList(playerName + ".notes");
                // Notes için UUID gerekli, oyuncu adından UUID bul
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
                
                // Notes için UUID gerekli, oyuncu adından UUID bul
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
            
            historyConfig.save(historyFile);
        } catch (IOException e) {
            plugin.debug("History saving error: " + e.getMessage());
        }
    }

    public void saveNotes() {
        if (notesConfig == null) return;
        
        try {
            for (Map.Entry<UUID, List<String>> entry : playerNotes.entrySet()) {
                UUID playerUUID = entry.getKey();
                List<String> notes = entry.getValue();
                
                // UUID'den oyuncu adını bul
                String playerName = null;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getUniqueId().equals(playerUUID)) {
                        playerName = onlinePlayer.getName();
                        break;
                    }
                }
                
                // Eğer oyuncu online değilse, UUID'yi string olarak kullan
                if (playerName == null) {
                    playerName = playerUUID.toString();
                }
                
                notesConfig.set(playerName + ".notes", notes);
            }
            
            notesConfig.save(notesFile);
        } catch (IOException e) {
            plugin.debug("Notes saving error: " + e.getMessage());
        }
    }

    public void startControlSession(Player controller, Player target) {
        if (!plugin.getConfig().getBoolean("advanced-control.enabled", true)) return;
        
        UUID targetUUID = target.getUniqueId();
        long startTime = System.currentTimeMillis();
        
        // Kontrol oturumu oluştur
        ControlSession session = new ControlSession(controller.getUniqueId(), startTime);
        controlSessions.put(targetUUID, session);
        
        // Kaçış denemelerini sıfırla
        escapeAttempts.put(targetUUID, 0);
        
        // Boss bar oluştur
        if (plugin.getConfig().getBoolean("advanced-control.notifications.boss-bar", true)) {
            createBossBar(target);
        }
        
        // Uyarı görevini başlat
        if (plugin.getConfig().getBoolean("advanced-control.time-limits.enabled", true)) {
            startWarningTask(target);
            startAutoReleaseTask(target);
        }
        
        // Bildirimleri göster
        showNotifications(target, "control-start");
        showNotifications(controller, "control-start-controller");
        
        plugin.debug("Advanced control session started: " + target.getName());
    }

    public void endControlSession(Player target, boolean banned) {
        if (!plugin.getConfig().getBoolean("advanced-control.enabled", true)) return;
        
        UUID targetUUID = target.getUniqueId();
        ControlSession session = controlSessions.get(targetUUID);
        
        if (session != null) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - session.getStartTime();
            
            // Geçmişe ekle
            addToHistory(session.getController(), target.getName(), session.getStartTime(), endTime, duration, banned);
            
            // Oturumu temizle
            controlSessions.remove(targetUUID);
            escapeAttempts.remove(targetUUID);
            
            // Boss bar'ı kaldır
            removeBossBar(target);
            
            // Görevleri iptal et
            cancelTasks(targetUUID);
            
            // Bildirimleri göster
            showNotifications(target, "control-end");
            
            plugin.debug("Advanced control session ended: " + target.getName());
        }
    }

    public void recordEscapeAttempt(Player player) {
        if (!plugin.getConfig().getBoolean("advanced-control.enabled", true)) return;
        
        UUID playerUUID = player.getUniqueId();
        int attempts = escapeAttempts.getOrDefault(playerUUID, 0) + 1;
        escapeAttempts.put(playerUUID, attempts);
        
        // İstatistiklere ekle
        if (plugin.getConfig().getBoolean("advanced-control.statistics.track-escapes", true)) {
            plugin.debug("Escape attempt recorded: " + player.getName() + " (Attempt: " + attempts + ")");
        }
        
        // Otomatik ban kontrolü
        int maxAttempts = plugin.getConfig().getInt("advanced-security.auto-ban.escape-attempts", 3);
        if (attempts >= maxAttempts) {
            autoBanPlayer(player, "Escape attempts exceeded");
        }
    }

    public void addNote(Player player, String note) {
        if (!plugin.getConfig().getBoolean("advanced-control.notes.enabled", true)) return;
        
        UUID playerUUID = player.getUniqueId();
        List<String> notes = playerNotes.getOrDefault(playerUUID, new ArrayList<>());
        
        int maxNotes = plugin.getConfig().getInt("advanced-control.notes.max-notes", 10);
        int maxLength = plugin.getConfig().getInt("advanced-control.notes.note-length", 200);
        
        if (notes.size() >= maxNotes) {
            notes.remove(0); // En eski notu kaldır
        }
        
        if (note.length() > maxLength) {
            note = note.substring(0, maxLength);
        }
        
        String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
        notes.add(timestampedNote);
        playerNotes.put(playerUUID, notes);
        
        // Notları kaydet
        saveNotes();
        
        plugin.debug("Note added: " + player.getName() + " - " + note);
    }

    public void addNote(String playerName, String note) {
        if (!plugin.getConfig().getBoolean("advanced-control.notes.enabled", true)) return;
        
        // Oyuncu adından UUID bul
        UUID playerUUID = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                playerUUID = onlinePlayer.getUniqueId();
                break;
            }
        }
        
        if (playerUUID == null) {
            // Oyuncu online değilse, notes.yml'ye direkt ekle
            if (notesConfig != null) {
                List<String> notes = notesConfig.getStringList(playerName + ".notes");
                
                int maxNotes = plugin.getConfig().getInt("advanced-control.notes.max-notes", 10);
                int maxLength = plugin.getConfig().getInt("advanced-control.notes.note-length", 200);
                
                if (notes.size() >= maxNotes) {
                    notes.remove(0); // En eski notu kaldır
                }
                
                if (note.length() > maxLength) {
                    note = note.substring(0, maxLength);
                }
                
                String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
                notes.add(timestampedNote);
                notesConfig.set(playerName + ".notes", notes);
                
                try {
                    notesConfig.save(notesFile);
                } catch (IOException e) {
                    plugin.debug("Error adding note: " + e.getMessage());
                }
                
                plugin.debug("Note added (offline): " + playerName + " - " + note);
            }
            return;
        }
        
        // Online oyuncu için normal işlem
        List<String> notes = playerNotes.getOrDefault(playerUUID, new ArrayList<>());
        
        int maxNotes = plugin.getConfig().getInt("advanced-control.notes.max-notes", 10);
        int maxLength = plugin.getConfig().getInt("advanced-control.notes.note-length", 200);
        
        if (notes.size() >= maxNotes) {
            notes.remove(0); // En eski notu kaldır
        }
        
        if (note.length() > maxLength) {
            note = note.substring(0, maxLength);
        }
        
        String timestampedNote = "[" + dateFormat.format(new Date()) + "] " + note;
        notes.add(timestampedNote);
        playerNotes.put(playerUUID, notes);
        
        // Notları kaydet
        saveNotes();
        
        plugin.debug("Note added: " + playerName + " - " + note);
    }

    public List<String> getNotes(Player player) {
        return playerNotes.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    public List<String> getNotes(String playerName) {
        // Oyuncu adından UUID bul
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                return playerNotes.getOrDefault(onlinePlayer.getUniqueId(), new ArrayList<>());
            }
        }
        
        // Eğer oyuncu online değilse, notes.yml'den yükle
        if (notesConfig != null) {
            return notesConfig.getStringList(playerName + ".notes");
        }
        
        return new ArrayList<>();
    }

    public void clearNotes(Player player) {
        playerNotes.remove(player.getUniqueId());
        
        // Notları kaydet
        saveNotes();
        
        plugin.debug("Notes cleared: " + player.getName());
    }

    public void clearNotes(String playerName) {
        // Oyuncu adından UUID bul
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(playerName)) {
                playerNotes.remove(onlinePlayer.getUniqueId());
                break;
            }
        }
        
        // notes.yml'den de temizle
        if (notesConfig != null) {
            notesConfig.set(playerName + ".notes", new ArrayList<>());
            try {
                notesConfig.save(notesFile);
            } catch (IOException e) {
                plugin.debug("Error clearing notes: " + e.getMessage());
            }
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
        // UUID'yi string'e çevir
        return controlHistory.getOrDefault(playerUUID.toString(), new ArrayList<>());
    }
    
    public List<ControlHistory> getControlHistory(String playerName) {
        // Oyuncu adına göre history getir
        return controlHistory.getOrDefault(playerName, new ArrayList<>());
    }

    public int getEscapeAttempts(UUID playerUUID) {
        return escapeAttempts.getOrDefault(playerUUID, 0);
    }

    public void addEscapeToHistory(UUID controllerUUID, String targetName) {
        if (!plugin.getConfig().getBoolean("advanced-control.history.enabled", true)) return;
        
        String controllerName = "Unknown";
        if (controllerUUID != null) {
            Player controller = Bukkit.getPlayer(controllerUUID);
            if (controller != null) {
                controllerName = controller.getName();
            }
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Kaçış kaydı oluştur
        ControlHistory escapeHistory = new ControlHistory(
            controllerName,
            targetName,
            dateFormat.format(new Date(currentTime - 60000)), // 1 dakika önce (tahmini başlangıç)
            dateFormat.format(new Date(currentTime)),
            60000, // 1 dakika (tahmini süre)
            true // banned = true (çünkü kaçtı)
        );
        
        // History'yi oyuncu adına göre sakla
        List<ControlHistory> playerHistory = controlHistory.getOrDefault(targetName, new ArrayList<>());
        playerHistory.add(escapeHistory);
        
        // Maksimum kayıt sayısını kontrol et
        int maxEntries = plugin.getConfig().getInt("advanced-control.history.max-entries", 1000);
        if (playerHistory.size() > maxEntries) {
            playerHistory.remove(0); // En eski kaydı kaldır
        }
        
        controlHistory.put(targetName, playerHistory);
        
        // History'yi dosyaya kaydet
        saveHistory();
        
        // Debug mesajı
        plugin.debug("Escape history added: " + controllerName + " -> " + targetName + " (Player quit while controlled)");
    }

    private void createBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(
            "§c§lCONTROLLED", 
            BarColor.RED, 
            BarStyle.SOLID
        );
        bossBar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bossBar);
    }

    private void removeBossBar(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    private void startWarningTask(Player player) {
        int warningTime = plugin.getConfig().getInt("advanced-control.time-limits.warning-time", 300);
        int maxDuration = plugin.getConfig().getInt("advanced-control.time-limits.max-duration", 3600);
        
        if (warningTime > 0 && warningTime < maxDuration) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    showNotifications(player, "control-warning");
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
                if (plugin.getConfig().getBoolean("advanced-control.time-limits.auto-release", true)) {
                    // Kontrol eden kişiyi bul
                    ControlSession session = controlSessions.get(player.getUniqueId());
                    if (session != null) {
                        Player controller = Bukkit.getPlayer(session.getController());
                        if (controller != null && controller.isOnline()) {
                            // Kontrolü bitir
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

    private void showNotifications(Player player, String type) {
        if (!plugin.getConfig().getBoolean("advanced-control.notifications.enabled", true)) return;
        
        switch (type) {
            case "control-start":
                if (plugin.getConfig().getBoolean("advanced-control.notifications.title", true)) {
                    player.sendTitle("§c§lCONTROLLED", "§7You are now under control", 10, 70, 20);
                }
                if (plugin.getConfig().getBoolean("advanced-control.notifications.action-bar", true)) {
                    // Action bar mesajı (1.8+ için)
                    player.sendMessage("§c§lYou are now under control!");
                }
                if (plugin.getConfig().getBoolean("advanced-control.notifications.sound", true)) {
                    player.playSound(player.getLocation(), "entity.enderdragon.growl", 1.0f, 0.5f);
                }
                break;
                
            case "control-warning":
                if (plugin.getConfig().getBoolean("advanced-control.notifications.title", true)) {
                    player.sendTitle("§e§lWARNING", "§7Control will end soon", 10, 70, 20);
                }
                if (plugin.getConfig().getBoolean("advanced-control.notifications.sound", true)) {
                    player.playSound(player.getLocation(), "block.note_block.pling", 1.0f, 1.0f);
                }
                break;
                
            case "control-end":
                if (plugin.getConfig().getBoolean("advanced-control.notifications.title", true)) {
                    player.sendTitle("§a§lRELEASED", "§7You are now free", 10, 70, 20);
                }
                if (plugin.getConfig().getBoolean("advanced-control.notifications.sound", true)) {
                    player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                }
                break;
        }
    }

    private void addToHistory(UUID controllerUUID, String targetName, long startTime, long endTime, long duration, boolean banned) {
        if (!plugin.getConfig().getBoolean("advanced-control.history.enabled", true)) return;
        
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
        
        // History'yi oyuncu adına göre sakla
        List<ControlHistory> playerHistory = controlHistory.getOrDefault(targetName, new ArrayList<>());
        playerHistory.add(history);
        
        // Maksimum kayıt sayısını kontrol et
        int maxEntries = plugin.getConfig().getInt("advanced-control.history.max-entries", 1000);
        if (playerHistory.size() > maxEntries) {
            playerHistory.remove(0); // En eski kaydı kaldır
        }
        
        controlHistory.put(targetName, playerHistory);
        
        // History'yi dosyaya kaydet
        saveHistory();
        
        // Debug mesajı
        plugin.debug("History added: " + controllerName + " -> " + targetName + " (" + dateFormat.format(new Date(startTime)) + " - " + dateFormat.format(new Date(endTime)) + ")");
    }

    private void autoBanPlayer(Player player, String reason) {
        if (!plugin.getConfig().getBoolean("advanced-security.auto-ban.enabled", true)) return;
        
        String banCommand = plugin.getConfig().getString("ban-command");
        if (banCommand != null && !banCommand.isEmpty()) {
            banCommand = banCommand.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), banCommand);
            
            plugin.debug("Auto ban: " + player.getName() + " - Reason: " + reason);
        }
    }

    public void onDisable() {
        saveHistory();
        saveNotes(); // Notları da kaydet
        
        // Tüm boss bar'ları kaldır
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
        }
        
        // Tüm görevleri iptal et
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