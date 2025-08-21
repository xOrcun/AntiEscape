package org.xorcun.antiescape;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.xorcun.antiescape.FileManager.ConfigFileManager;
import org.xorcun.antiescape.FileManager.MessageFileManager;
import org.xorcun.antiescape.UpdateSystem.JoinEvent;
import org.xorcun.antiescape.UpdateSystem.UpdateChecker;
import org.xorcun.antiescape.LogManager;
import org.xorcun.antiescape.DiscordManager;
import org.xorcun.antiescape.AdvancedControlManager;
import org.xorcun.antiescape.AdvancedSecurityManager;

import java.util.*;
import java.util.Arrays;

public class AntiEscape extends JavaPlugin implements Listener, TabCompleter {
    private Location kontrolAlani = null;
    private Location kontrolSpawn = null;
    private final Map<UUID, Boolean> kontrolDurumu = new HashMap<>();
    private Map<UUID, Boolean> kontrolSohbet = new HashMap<>();
    private final Map<UUID, UUID> kontrolEdenKisi = new HashMap<>(); // Kontrol eden kişiyi takip etmek için
    private final Map<UUID, Boolean> banlandi = new HashMap<>(); // Oyuncunun banlanıp banlanmadığını takip etmek için

    private ConfigFileManager configFileManager;
    private MessageFileManager messageFileManager;
    private JoinEvent joinEvent;
    private LogManager logManager;
    private DiscordManager discordManager;
    private AdvancedControlManager advancedControlManager;
    private AdvancedSecurityManager advancedSecurityManager;

    // Debug mesajı yazdırma metodu
    public void debug(String message) {
        if (getConfig().getBoolean("debug", false)) {
            String language = getConfig().getString("language", "en");
            Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §f[" + language.toUpperCase() + "] " + message);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §aPlugin actived!");
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §fDiscord Server: §bhttps://orcunozturk.com/discord");

        // Önce config dosyasını kaydet ve yükle
        saveDefaultConfig();
        reloadConfig();

        // Event listener'ları register et
        getServer().getPluginManager().registerEvents(this, this);

        // TabCompleter'ı register et
        getCommand("control").setTabCompleter(this);

        joinEvent = new JoinEvent(this);
        getServer().getPluginManager().registerEvents(joinEvent, this);

        // Güvenlik sistemi için event listener
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                advancedSecurityManager.onPlayerJoin(event.getPlayer());
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                advancedSecurityManager.onPlayerQuit(event.getPlayer());
            }
        }, this);

        // Dil dosyasını kaydet ve yükle
        configFileManager = new ConfigFileManager(this);
        messageFileManager = new MessageFileManager(configFileManager);

        messageFileManager.saveLangFile();
        messageFileManager.loadLangFile();

        // Log sistemi başlat
        logManager = new LogManager(this);

        // Discord webhook sistemi başlat
        discordManager = new DiscordManager(this);

        // Gelişmiş kontrol sistemi başlat
        advancedControlManager = new AdvancedControlManager(this);

        // Gelişmiş güvenlik sistemi başlat
        advancedSecurityManager = new AdvancedSecurityManager(this);

        // Güncelleme sistemi
        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates();

        // Kontrol alanını yükle
        String locString = getConfig().getString("control-location");
        if (locString != null && !locString.equals("none")) {
            kontrolAlani = stringToLocation(locString);
        }

        // Kontrol spawn alanını yükle
        String spawnLocString = getConfig().getString("control-spawn-location");
        if (spawnLocString != null && !spawnLocString.equals("none")) {
            kontrolSpawn = stringToLocation(spawnLocString);
        }

        int pluginId = 23459; // bStats dashboard'dan aldığınız plugin ID'yi buraya girin
        new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        // Gelişmiş sistemleri kapat
        if (advancedControlManager != null) {
            advancedControlManager.onDisable();
        }
        if (advancedSecurityManager != null) {
            advancedSecurityManager.onDisable();
        }
        
        // Flag'leri temizle
        banlandi.clear();
        
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §cPlugin deactivated!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        
        // Permission kontrolü
        if (!player.hasPermission(getConfig().getString("permissions.general"))) {
            return completions;
        }
        
        if (cmd.getName().equalsIgnoreCase("control")) {
            if (args.length == 1) {
                // İlk argüman için öneriler (komut adı yazıldıktan sonra)
                if (args[0].equalsIgnoreCase("")) {
                    // Boş string ise tüm komutları öner
                    List<String> firstArgs = Arrays.asList(
                        "help", "version", "reload", "chat", "take", "end", "set", "delete", "discord", "logs", "history", "notes", "stats", "whitelist", "suspicious", "ip", "violations"
                    );
                    
                    for (String arg : firstArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("chat")) {
                    List<String> chatArgs = Arrays.asList("join", "leave");
                    for (String arg : chatArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("end")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        completions.add(onlinePlayer.getName());
                    }
                } else if (args[0].equalsIgnoreCase("set")) {
                    List<String> setArgs = Arrays.asList("area", "return");
                    for (String arg : setArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    List<String> deleteArgs = Arrays.asList("area", "spawn");
                    for (String arg : deleteArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("discord")) {
                    List<String> discordArgs = Arrays.asList("test");
                    for (String arg : discordArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("logs")) {
                    List<String> logsArgs = Arrays.asList("list", "clear", "clear-all");
                    for (String arg : logsArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("notes")) {
                    List<String> notesArgs = Arrays.asList("add", "clear", "list");
                    for (String arg : notesArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("stats")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        completions.add(onlinePlayer.getName());
                    }
                } else if (args[0].equalsIgnoreCase("whitelist")) {
                    List<String> whitelistArgs = Arrays.asList("add", "remove");
                    for (String arg : whitelistArgs) {
                        completions.add(arg);
                    }
                } else if (args[0].equalsIgnoreCase("suspicious") || args[0].equalsIgnoreCase("violations")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        completions.add(onlinePlayer.getName());
                    }
                } else if (args[0].equalsIgnoreCase("ip")) {
                    // IP adresi için öneri yok, boş bırak
                } else {
                    // Eğer yazılan komut bilinmiyorsa, tüm komutları öner
                    List<String> firstArgs = Arrays.asList(
                        "help", "version", "reload", "chat", "take", "end", "set", "delete", "discord", "logs", "history", "notes", "stats", "whitelist", "suspicious", "ip", "violations"
                    );
                    
                    for (String arg : firstArgs) {
                        if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                }
            } else if (args.length == 2) {
                // Üçüncü argüman için öneriler
                if (args[0].equalsIgnoreCase("chat")) {
                    List<String> chatArgs = Arrays.asList("join", "leave");
                    for (String arg : chatArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("end")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("set")) {
                    List<String> setArgs = Arrays.asList("area", "return");
                    for (String arg : setArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    List<String> deleteArgs = Arrays.asList("area", "spawn");
                    for (String arg : deleteArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("discord")) {
                    List<String> discordArgs = Arrays.asList("test");
                    for (String arg : discordArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("logs")) {
                    List<String> logsArgs = Arrays.asList("list", "clear", "clear-all");
                    for (String arg : logsArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("notes")) {
                    // args.length == 2 durumunda oyuncu adları öner
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("history") || args[0].equalsIgnoreCase("stats")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("whitelist")) {
                    List<String> whitelistArgs = Arrays.asList("add", "remove");
                    for (String arg : whitelistArgs) {
                        if (arg.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("suspicious") || args[0].equalsIgnoreCase("violations")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("ip")) {
                    // IP adresi için öneri yok, boş bırak
                }
            } else if (args.length == 3) {
                // Üçüncü argüman için öneriler
                if (args[0].equalsIgnoreCase("end")) {
                    List<String> endArgs = Arrays.asList("ban");
                    for (String arg : endArgs) {
                        if (arg.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("logs") && args[1].equalsIgnoreCase("clear")) {
                    List<String> logTypes = Arrays.asList("moves", "commands", "chat", "damage", "items", "control", "general");
                    for (String arg : logTypes) {
                        if (arg.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(arg);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("notes") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("list"))) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("whitelist") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("suspicious") || args[0].equalsIgnoreCase("violations")) {
                    // Clear önerisi ekle
                    if ("clear".startsWith(args[2].toLowerCase())) {
                        completions.add("clear");
                    }
                } else if (args[0].equalsIgnoreCase("ip")) {
                    // Online oyuncuları listele
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                }
            } else if (args.length == 4) {
                // Dördüncü argüman için öneriler
                if (args[0].equalsIgnoreCase("whitelist") && args[1].equalsIgnoreCase("add")) {
                    // Sadece add komutu için reason önerisi
                    if (args[2].toLowerCase().startsWith(args[3].toLowerCase())) {
                        completions.add("Security violation");
                    }
                }
            }
        }
        
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§8[§eAntiEscape§8] §cThis command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission(getConfig().getString("permissions.general"))) {
            player.sendMessage(messageFileManager.getLangMessage("no-permission"));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("control")) {
            if (args.length == 0) {
                // Yardım mesajlarını göster
                List<String> helpMessages = messageFileManager.getLangMessageList("commands-help");
                for (String helpMessage : helpMessages) {
                    player.sendMessage(helpMessage);
                }
                return true;
            }

            // Help komutu kontrolü
            if (args[0].equalsIgnoreCase("help")) {
                List<String> helpMessages = messageFileManager.getLangMessageList("commands-help");
                for (String helpMessage : helpMessages) {
                    player.sendMessage(helpMessage);
                }
                return true;
            }

            // Version komutu kontrolü
            if (args[0].equalsIgnoreCase("version")) {
                PluginDescriptionFile data = this.getDescription();
                player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
                player.sendMessage("");
                player.sendMessage("§fVersion: §a" + configFileManager.getConfigMessage("version"));
                player.sendMessage("§fLanguage: §e" + messageFileManager.getCurrentLanguage());
                player.sendMessage("§fLogging: §e" + (getConfig().getBoolean("logging.enabled", true) ? "Enabled" : "Disabled"));
                player.sendMessage("§fLog Files: §e" + logManager.getAllLogFilePaths().size() + " files");
                player.sendMessage("§fActive Controls: §e" + kontrolDurumu.size());
                player.sendMessage("§fDiscord Webhook: §e" + (discordManager.isDiscordEnabled() ? "Enabled" : "Disabled"));
                player.sendMessage("§fAuthors: §e" + data.getAuthors());
                player.sendMessage("§fWebsite: §e" + data.getWebsite());
                player.sendMessage("§fDiscord Server: §bhttps://orcunozturk.com/discord");
                player.sendMessage("");
                joinEvent.checkForUpdatesPlayer(player);
                return true;
            }

            // Discord test komutu kontrolü
            if (args[0].equalsIgnoreCase("discord")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("discord-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("test")) {
                    if (!discordManager.isDiscordEnabled()) {
                        player.sendMessage(messageFileManager.getLangMessage("discord-disabled"));
                        return true;
                    }
                    
                    if (discordManager.getWebhookUrl().equals("YOUR_WEBHOOK_URL_HERE")) {
                        player.sendMessage(messageFileManager.getLangMessage("discord-url-not-set"));
                        return true;
                    }
                    
                    player.sendMessage(messageFileManager.getLangMessage("discord-sending"));
                    discordManager.sendTestMessage();
                    player.sendMessage(messageFileManager.getLangMessage("discord-sent"));
                    return true;
                }
            }

            // Log komutları kontrolü
            if (args[0].equalsIgnoreCase("logs")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("logs-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("list")) {
                    player.sendMessage(messageFileManager.getLangMessage("log-files-title"));
                    Map<String, String> logPaths = logManager.getAllLogFilePaths();
                    for (Map.Entry<String, String> entry : logPaths.entrySet()) {
                        String status = getConfig().getBoolean("logging.log-" + entry.getKey(), true) ? 
                            messageFileManager.getLangMessage("log-files-enabled") : 
                            messageFileManager.getLangMessage("log-files-disabled");
                        player.sendMessage(status.replace("%type%", entry.getKey()));
                    }
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("clear")) {
                    if (args.length < 3) {
                        player.sendMessage(messageFileManager.getLangMessage("log-files-usage"));
                        player.sendMessage(messageFileManager.getLangMessage("log-files-types"));
                        return true;
                    }
                    
                    String logType = args[2].toLowerCase();
                    logManager.clearLogs(logType);
                    player.sendMessage(messageFileManager.getLangMessage("log-files-cleared").replace("%type%", logType));
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("clear-all")) {
                    logManager.clearAllLogs();
                    player.sendMessage(messageFileManager.getLangMessage("log-files-all-cleared"));
                    return true;
                }
            }

            // Gelişmiş kontrol komutları
            if (args[0].equalsIgnoreCase("history")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-history"));
                    return true;
                }
                
                String targetName = args[1];
                List<AdvancedControlManager.ControlHistory> history = advancedControlManager.getControlHistory(targetName);
                
                // Liste formatında mesajları göster
                List<String> historyMessages = messageFileManager.getLangMessageList("advanced-control-history-list");
                for (String message : historyMessages) {
                    String formattedMessage = message
                        .replace("%player%", targetName)
                        .replace("%count%", String.valueOf(history.size()));
                    player.sendMessage(formattedMessage);
                }
                
                if (history.isEmpty()) {
                    player.sendMessage(messageFileManager.getLangMessageNoPrefix("no-prefix.no-history"));
                } else {
                    for (int i = Math.max(0, history.size() - 5); i < history.size(); i++) {
                        AdvancedControlManager.ControlHistory record = history.get(i);
                        String status = record.isBanned() ? 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.banned") : 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.released");
                        
                        String message = messageFileManager.getLangMessageNoPrefix("advanced-control-history-record")
                            .replace("%index%", String.valueOf(i + 1))
                            .replace("%controller%", record.getController())
                            .replace("%target%", record.getTarget())
                            .replace("%start_date%", record.getStartTime())
                            .replace("%end_date%", record.getEndTime())
                            .replace("%status%", status);
                        
                        player.sendMessage(message);
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("notes")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-notes"));
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 4) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-notes-add"));
                        return true;
                    }
                    
                    String targetName = args[2];
                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-player-not-found").replace("%player%", targetName));
                        return true;
                    }
                    
                    StringBuilder note = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        note.append(args[i]).append(" ");
                    }
                    
                    advancedControlManager.addNote(targetName, note.toString().trim());
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-note-added"));
                } else if (args[1].equalsIgnoreCase("clear")) {
                    if (args.length < 3) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-notes"));
                        return true;
                    }
                    
                    String targetName = args[2];
                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-player-not-found").replace("%player%", targetName));
                        return true;
                    }
                    
                    advancedControlManager.clearNotes(targetName);
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-notes-cleared"));
                } else if (args[1].equalsIgnoreCase("list")) {
                    if (args.length < 3) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-notes"));
                        return true;
                    }
                    
                    String targetName = args[2];
                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-player-not-found").replace("%player%", targetName));
                        return true;
                    }
                    
                    List<String> notes = advancedControlManager.getNotes(targetName);
                    
                    // Liste formatında mesajları göster
                    List<String> notesMessages = messageFileManager.getLangMessageList("advanced-control-notes-list");
                    for (String message : notesMessages) {
                        String formattedMessage = message.replace("%player%", targetName);
                        player.sendMessage(formattedMessage);
                    }
                    
                    if (notes.isEmpty()) {
                        player.sendMessage(messageFileManager.getLangMessageNoPrefix("no-prefix.no-notes"));
                    } else {
                        for (int i = 0; i < notes.size(); i++) {
                            player.sendMessage("§7" + (i + 1) + ". §f" + notes.get(i));
                        }
                    }
                } else {
                    // Varsayılan olarak notları listele
                    String targetName = args[1];
                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(messageFileManager.getLangMessage("advanced-control-player-not-found").replace("%player%", targetName));
                        return true;
                    }
                    
                    List<String> notes = advancedControlManager.getNotes(targetName);
                    
                    // Liste formatında mesajları göster
                    List<String> notesMessages = messageFileManager.getLangMessageList("advanced-control-notes-list");
                    for (String message : notesMessages) {
                        String formattedMessage = message.replace("%player%", targetName);
                        player.sendMessage(formattedMessage);
                    }
                    
                    if (notes.isEmpty()) {
                        player.sendMessage(messageFileManager.getLangMessageNoPrefix("no-prefix.no-notes"));
                    } else {
                        for (int i = 0; i < notes.size(); i++) {
                            player.sendMessage("§7" + (i + 1) + ". §f" + (i + 1) + ". §f" + notes.get(i));
                        }
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("stats")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-usage-stats"));
                    return true;
                }
                
                String targetName = args[1];
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    player.sendMessage(messageFileManager.getLangMessage("advanced-control-player-not-found").replace("%player%", targetName));
                    return true;
                }
                
                int escapeAttempts = advancedControlManager.getEscapeAttempts(target.getUniqueId());
                int suspiciousCount = advancedSecurityManager.getSuspiciousActivityCount(targetName);
                String ipAddress = advancedSecurityManager.getPlayerIP(targetName);
                boolean isWhitelisted = advancedSecurityManager.isWhitelisted(targetName);
                boolean isControlled = advancedControlManager.isPlayerControlled(target.getUniqueId());
                
                // Liste formatında mesajları göster
                List<String> statsMessages = messageFileManager.getLangMessageList("advanced-control-stats-list");
                for (String message : statsMessages) {
                    String formattedMessage = message
                        .replace("%player%", targetName)
                        .replace("%escape_attempts%", String.valueOf(escapeAttempts))
                        .replace("%suspicious_count%", String.valueOf(suspiciousCount))
                        .replace("%ip_address%", ipAddress != null ? ipAddress : messageFileManager.getLangMessageNoPrefix("no-prefix.unknown"))
                        .replace("%whitelist_status%", isWhitelisted ? 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.whitelisted") : 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.not-whitelisted"))
                        .replace("%control_status%", isControlled ? 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.controlled") : 
                            messageFileManager.getLangMessageNoPrefix("no-prefix.not-controlled"));
                    player.sendMessage(formattedMessage);
                }
                
                return true;
            }

            // Güvenlik komutları
            if (args[0].equalsIgnoreCase("whitelist")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("whitelist-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                
                if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 3) {
                        player.sendMessage(messageFileManager.getLangMessage("whitelist-usage-add"));
                        return true;
                    }
                    
                    String targetName = args[2];
                    String reason = args.length > 3 ? args[3] : "Added by admin";
                    advancedSecurityManager.addToWhitelist(targetName, reason, player.getName());
                    player.sendMessage(messageFileManager.getLangMessage("whitelist-added").replace("%player%", targetName));
                } else if (args[1].equalsIgnoreCase("remove")) {
                    if (args.length < 3) {
                        player.sendMessage(messageFileManager.getLangMessage("whitelist-usage-remove"));
                        return true;
                    }
                    
                    String targetName = args[2];
                    advancedSecurityManager.removeFromWhitelist(targetName, player.getName());
                    player.sendMessage(messageFileManager.getLangMessage("whitelist-removed").replace("%player%", targetName));
                } else {
                    // Whitelist durumunu göster
                    String targetName = args[1];
                    boolean isWhitelisted = advancedSecurityManager.isWhitelisted(targetName);
                    
                    // Liste formatında mesajları göster
                    List<String> whitelistMessages = messageFileManager.getLangMessageList("whitelist-status-list");
                    for (String message : whitelistMessages) {
                        String formattedMessage = message
                            .replace("%player%", targetName)
                            .replace("%status%", isWhitelisted ? 
                                messageFileManager.getLangMessageNoPrefix("no-prefix.whitelisted") : 
                                messageFileManager.getLangMessageNoPrefix("no-prefix.not-whitelisted"))
                            .replace("%admin%", isWhitelisted ? 
                                advancedSecurityManager.getWhitelistAdmin(targetName) : 
                                "N/A");
                        player.sendMessage(formattedMessage);
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("suspicious")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("suspicious-usage"));
                    return true;
                }
                
                String targetName = args[1];
                
                if (args.length > 2 && args[2].equalsIgnoreCase("clear")) {
                    advancedSecurityManager.clearSuspiciousActivity(targetName);
                    player.sendMessage(messageFileManager.getLangMessage("suspicious-cleared").replace("%player%", targetName));
                } else {
                    // Şüpheli aktivite durumunu göster
                    int suspiciousCount = advancedSecurityManager.getSuspiciousActivityCount(targetName);
                    
                    // Liste formatında mesajları göster
                    List<String> suspiciousMessages = messageFileManager.getLangMessageList("suspicious-status-list");
                    for (String message : suspiciousMessages) {
                        String formattedMessage = message
                            .replace("%player%", targetName)
                            .replace("%count%", String.valueOf(suspiciousCount));
                        player.sendMessage(formattedMessage);
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("violations")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("violations-usage"));
                    return true;
                }
                
                String targetName = args[1];
                
                if (args.length > 2 && args[2].equalsIgnoreCase("clear")) {
                    advancedSecurityManager.clearViolationCount(targetName);
                    player.sendMessage(messageFileManager.getLangMessage("violations-cleared").replace("%player%", targetName));
                } else {
                    // Violation count durumunu göster
                    int violationCount = advancedSecurityManager.getViolationCount(targetName);
                    String lastBanDuration = advancedSecurityManager.getLastBanDuration(targetName);
                    
                    // Liste formatında mesajları göster
                    List<String> violationMessages = messageFileManager.getLangMessageList("violations-status-list");
                    for (String message : violationMessages) {
                        String formattedMessage = message
                            .replace("%player%", targetName)
                            .replace("%count%", String.valueOf(violationCount))
                            .replace("%last_ban%", lastBanDuration);
                        player.sendMessage(formattedMessage);
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("ip")) {
                if (args.length < 2) {
                    player.sendMessage(messageFileManager.getLangMessage("ip-usage"));
                    return true;
                }
                
                String ipAddress = args[1];
                List<String> playersOnIP = advancedSecurityManager.getPlayersOnIP(ipAddress);
                
                // Liste formatında mesajları göster
                List<String> ipMessages = messageFileManager.getLangMessageList("ip-info-list");
                for (String message : ipMessages) {
                    String formattedMessage = message
                        .replace("%ip%", ipAddress)
                        .replace("%count%", String.valueOf(playersOnIP.size()));
                    player.sendMessage(formattedMessage);
                }
                
                if (playersOnIP.isEmpty()) {
                    player.sendMessage(messageFileManager.getLangMessage("ip-no-players"));
                } else {
                    for (String playerName : playersOnIP) {
                        player.sendMessage("§7- §f" + playerName);
                    }
                }
                return true;
            }

            // Reload komutu kontrolü
            if (args[0].equalsIgnoreCase("reload")) {
                // Config dosyasını yeniden yükle (dosyayı bozmadan)
                reloadConfig();
                
                // Seçilen dil dosyasını diske kaydet ve yükle
                messageFileManager.saveLangFile();
                messageFileManager.loadLangFile();
                
                // Webhook dosyasını yeniden yükle
                discordManager.reloadWebhookFile();
                
                // Location'ları yeniden yükle
                String locString = getConfig().getString("control-location");
                if (locString != null && !locString.equals("none")) {
                    kontrolAlani = stringToLocation(locString);
                } else {
                    kontrolAlani = null;
                }
                
                String spawnLocString = getConfig().getString("control-spawn-location");
                if (spawnLocString != null && !spawnLocString.equals("none")) {
                    kontrolSpawn = stringToLocation(spawnLocString);
                } else {
                    kontrolSpawn = null;
                }
                
                // Debug mesajı
                debug("Config reloaded successfully. Language: " + messageFileManager.getCurrentLanguage());
                
                sender.sendMessage(messageFileManager.getLangMessage("config-reloaded"));
                return true;
            }

            // Sohbet Komut işlemleri
            if (args[0].equalsIgnoreCase("chat")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("control-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }

                UUID playerUUID = player.getUniqueId();
                if (args[1].equalsIgnoreCase("join")) {
                    // Oyuncu zaten kontrol sohbetindeyse
                    if (kontrolSohbet.containsKey(playerUUID) && kontrolSohbet.get(playerUUID)) {
                        player.sendMessage(messageFileManager.getLangMessage("already-in-control-chat"));
                        return true;
                    }

                    // Oyuncuyu kontrol sohbetine ekle
                    kontrolSohbet.put(playerUUID, true); // UUID ve true değeri ekle
                    player.sendMessage(messageFileManager.getLangMessage("control-chat-join"));

                    // Diğer oyunculara mesaj gönder
                    for (UUID uuid : kontrolSohbet.keySet()) {
                        Player kontrolPlayer = Bukkit.getPlayer(uuid);
                        if (kontrolPlayer != null && kontrolPlayer.isOnline()) {
                            kontrolPlayer.sendMessage(messageFileManager.getLangMessage("player-joined-control-chat").replace("%player%", player.getName()));
                        }
                    }

                    return true;
                } else if (args[1].equalsIgnoreCase("leave")) {
                    // Oyuncu kontrol sohbetinde mi?
                    if (!kontrolSohbet.containsKey(playerUUID) || !kontrolSohbet.get(playerUUID)) {
                        player.sendMessage(messageFileManager.getLangMessage("not-in-control-chat"));
                        return true;
                    }

                    // Oyuncuyu kontrol sohbetinden çıkar
                    kontrolSohbet.remove(playerUUID); // Haritadan kaldır
                    player.sendMessage(messageFileManager.getLangMessage("control-chat-left"));

                    // Diğer oyunculara mesaj gönder
                    for (UUID uuid : kontrolSohbet.keySet()) {
                        Player kontrolPlayer = Bukkit.getPlayer(uuid);
                        if (kontrolPlayer != null && kontrolPlayer.isOnline()) {
                            kontrolPlayer.sendMessage(messageFileManager.getLangMessage("player-quit-control-chat").replace("%player%", player.getName()));
                        }
                    }

                    return true;
                }
            }

            // "al" veya "take" komutu kontrolü
            if (args[0].equalsIgnoreCase("take")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("control-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                Player hedef = Bukkit.getPlayer(args[1]);
                if (hedef == null) {
                    player.sendMessage(messageFileManager.getLangMessage("offline-player"));
                    return true;
                }

                // Oyuncu kendini kontrole alamaz
                if (hedef.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(messageFileManager.getLangMessage("cannot-control-yourself"));
                    return true;
                }

                if (kontrolAlani == null) {
                    player.sendMessage(messageFileManager.getLangMessage("control-area-not-set"));
                    return true;
                }

                if (kontrolSpawn == null) {
                    player.sendMessage(messageFileManager.getLangMessage("control-return-not-set"));
                    return true;
                }

                if (kontrolDurumu.containsKey(hedef.getUniqueId())) {
                    player.sendMessage(messageFileManager.getLangMessage("control-player-started").replace("%player%", hedef.getName()));
                    return true;
                }

                // Oyuncuyu kontrol altına alma
                kontrolDurumu.put(hedef.getUniqueId(), true);
                kontrolSohbet.put(hedef.getUniqueId(), true); // Kontrol edilen oyuncu
                kontrolSohbet.put(player.getUniqueId(), true); // Kontrol eden kişi
                kontrolEdenKisi.put(hedef.getUniqueId(), player.getUniqueId()); // Kontrol eden kişiyi kaydet
                
                // Gelişmiş kontrol sistemi
                advancedControlManager.startControlSession(player, hedef);
                
                // Log kaydı
                logManager.logControlStart(player, hedef);
                
                // Discord webhook
                discordManager.sendControlStart(player, hedef);
                
                // Debug mesajları
                debug("Control started for: " + hedef.getName());
                debug("Control chat added: " + hedef.getName() + " - " + kontrolSohbet.get(hedef.getUniqueId()));
                debug("Control chat added: " + player.getName() + " - " + kontrolSohbet.get(player.getUniqueId()));
                
                hedef.teleport(kontrolAlani);
                
                // Tüm oyunculara kontrol başladı mesajı
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(messageFileManager.getLangMessage("control-started").replace("%player%", hedef.getName()));
                }
                
                // Kontrol edilen oyuncuya mesaj
                hedef.sendMessage(messageFileManager.getLangMessage("control-message").replace("%player%", player.getName()));
                
                // Kontrol eden kişiye bilgi mesajı
                player.sendMessage(messageFileManager.getLangMessage("control-chat-join"));
                player.sendMessage(messageFileManager.getLangMessage("control-chat"));
                
                return true;
            }

            // "bitir" veya "end" komutu kontrolü
            if (args[0].equalsIgnoreCase("end")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("control-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                Player hedef = Bukkit.getPlayer(args[1]);
                if (hedef == null || !kontrolDurumu.containsKey(hedef.getUniqueId())) {
                    player.sendMessage(messageFileManager.getLangMessage("control-player-error"));
                    return true;
                }

                if (args.length == 3 && (args[2].equalsIgnoreCase("ban"))) {
                    // Oyuncuyu yasakla
                    String banCommand = getConfig().getString("ban-command");
                    if (banCommand != null) {
                        // Config'den NORMAL ban ayarlarını al
                        String banDuration = getConfig().getString("auto-ban.normal-ban.duration", "1d");
                        String banReason = getConfig().getString("auto-ban.normal-ban.reason", "Control violation");
                        
                        // Değişkenleri değiştir
                        banCommand = banCommand
                            .replace("%player%", hedef.getName())
                            .replace("%duration%", banDuration)
                            .replace("%reason%", banReason);
                        
                        // Debug mesajı
                        debug("Executing normal ban command: " + banCommand);
                        
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
                        
                        // Oyuncunun banlandığını işaretle
                        banlandi.put(hedef.getUniqueId(), true);
                    }
                    
                    // Kontrol durumunu temizle
                    kontrolDurumu.remove(hedef.getUniqueId());
                    kontrolSohbet.remove(hedef.getUniqueId());
                    kontrolSohbet.remove(player.getUniqueId());
                    
                    // Gelişmiş kontrol sistemi
                    advancedControlManager.endControlSession(hedef, true);
                    
                    // Log kaydı
                    logManager.logControlEnd(player, hedef, true);
                    
                    // Discord webhook
                    discordManager.sendControlEnd(player, hedef, true);
                    
                    // Kontrol eden kişiyi temizle
                    kontrolEdenKisi.remove(hedef.getUniqueId());
                    
                    // Kontrol eden kişiye chat'ten çıktı mesajı
                    player.sendMessage(messageFileManager.getLangMessage("control-chat-left"));
                    
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(messageFileManager.getLangMessage("control-finished-ban").replace("%player%", hedef.getName()));
                    }
                    
                    return true; // Burada return yaparak tekrar çalışmasını engelle
                } else {
                    // Normal kontrol sonlandırma (ban olmadan)
                    kontrolDurumu.remove(hedef.getUniqueId());
                    kontrolSohbet.remove(hedef.getUniqueId());
                    kontrolSohbet.remove(player.getUniqueId());
                    hedef.teleport(kontrolSpawn);
                    
                    // Gelişmiş kontrol sistemi
                    advancedControlManager.endControlSession(hedef, false);
                    
                    // Log kaydı
                    logManager.logControlEnd(player, hedef, false);
                    
                    // Discord webhook
                    discordManager.sendControlEnd(player, hedef, false);
                    
                    // Kontrol eden kişiyi temizle
                    kontrolEdenKisi.remove(hedef.getUniqueId());
                    
                    // Kontrol eden kişiye chat'ten çıktı mesajı
                    player.sendMessage(messageFileManager.getLangMessage("control-chat-left"));
                    
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(messageFileManager.getLangMessage("control-finished-clean").replace("%player%", hedef.getName()));
                    }
                    
                    return true;
                }
                
                // Bu satır artık gereksiz çünkü her iki durumda da return yapılıyor
                // kontrolDurumu.remove(hedef.getUniqueId());
                // return true;
            }

            // "set"  komutu kontrolü
            if (args[0].equalsIgnoreCase("set")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("location-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                if (args[1].equalsIgnoreCase("area")) {
                    if (kontrolAlani != null) {
                        player.sendMessage(messageFileManager.getLangMessage("control-area-already-set"));
                        return true;
                    }
                    kontrolAlani = player.getLocation();
                    getConfig().set("control-location", locationToString(kontrolAlani));
                    saveConfig();
                    player.sendMessage(messageFileManager.getLangMessage("control-area-set"));
                    return true;
                } else if (args[1].equalsIgnoreCase("return")) {
                    if (kontrolSpawn != null) {
                        player.sendMessage(messageFileManager.getLangMessage("control-return-already-set"));
                        return true;
                    }
                    kontrolSpawn = player.getLocation();
                    getConfig().set("control-spawn-location", locationToString(kontrolSpawn));
                    saveConfig();
                    player.sendMessage(messageFileManager.getLangMessage("control-return-set"));
                    return true;
                }
            }

            // "delete" komutu kontrolü
            if (args[0].equalsIgnoreCase("delete")) {
                if (args.length < 2) {
                    // Yeni help yapısını kullan
                    List<String> helpMessages = messageFileManager.getLangMessageList("location-help");
                    for (String helpMessage : helpMessages) {
                        player.sendMessage(helpMessage);
                    }
                    return true;
                }
                // Kontrol alanını silme
                if (args[1].equalsIgnoreCase("area")) {
                    if (kontrolAlani == null) {
                        player.sendMessage(messageFileManager.getLangMessage("control-area-not-set"));
                        return true;
                    }
                    kontrolAlani = null;
                    getConfig().set("control-location", "none");
                    saveConfig();
                    player.sendMessage(messageFileManager.getLangMessage("control-area-deleted"));
                    return true;
                }

                // Başlangıç alanını (spawn) silme
                if (args[1].equalsIgnoreCase("spawn")) {
                    if (kontrolSpawn == null) {
                        player.sendMessage(messageFileManager.getLangMessage("control-return-not-set"));
                        return true;
                    }
                    kontrolSpawn = null;
                    getConfig().set("control-spawn-location", "none");
                    saveConfig();
                    player.sendMessage(messageFileManager.getLangMessage("control-return-deleted"));
                    return true;
                }
            }


            // Bilinmeyen argüman durumunda
            player.sendMessage(messageFileManager.getLangMessage("invalid-argument"));
            return true;
        }
        return false;
    }
    // Control Chat
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Log kaydı
        logManager.logChat(player, event.getMessage());

        // Discord webhook
        discordManager.sendChatEvent(player, event.getMessage());

        // Güvenlik sistemi - şüpheli aktivite kontrolü
        advancedSecurityManager.onPlayerAction(player, "chat");

        // Debug mesajı
        debug("Chat Event: " + player.getName() + " - Control Chat: " + kontrolSohbet.getOrDefault(playerUUID, false));

        // Eğer oyuncu kontrol sohbetindeyse
        if (kontrolSohbet.getOrDefault(playerUUID, false)) {
            event.setCancelled(true); // Varsayılan chat olayını iptal et (konsola gitmesini engelle)

            // Debug mesajı
            debug("Cancelling chat for: " + player.getName());

            // Kontrol sohbetindeki oyunculara (kontrol eden ve kontrol edilen) mesajları göster
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (kontrolSohbet.getOrDefault(onlinePlayer.getUniqueId(), false)
                        || onlinePlayer.hasPermission(configFileManager.getConfigMessage("permissions.chat")) || onlinePlayer.hasPermission(configFileManager.getConfigMessage("permissions.general"))) {
                    // Sohbet formatını ayarla ve sadece ilgili oyunculara gönder
                    String message = configFileManager.getConfigMessageWithColors("control-prefix")
                            .replace("%player%", player.getName())
                            .replace("%message%", event.getMessage());
                    
                    onlinePlayer.sendMessage(message);
                    
                    // Debug mesajı
                    debug("Sending to: " + onlinePlayer.getName() + " - Message: " + message);
                }
            }
        }
    }


    // Control Move
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Debug mesajı
        debug("Move Event: " + player.getName() + " - Control Status: " + kontrolDurumu.getOrDefault(player.getUniqueId(), false));
        
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // Güvenlik sistemi - şüpheli aktivite kontrolü
            advancedSecurityManager.onPlayerAction(player, "movement");
            
            // Log kaydı
            logManager.logMove(player, from, to);
            
            // Discord webhook
            discordManager.sendMoveEvent(player, from, to);
            
            // Debug mesajı
            debug("Move Event: " + player.getName() + " - From: " + from.toString() + " - To: " + (to != null ? to.toString() : "null"));
            
            // Tüm hareketleri engelle (küçük hareketler dahil)
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ() || from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch())) {
                event.setCancelled(true);
                
                // Debug mesajı
                debug("Cancelling move for: " + player.getName());
                
                // Oyuncuyu eski konumuna geri gönder
                Bukkit.getScheduler().runTask(this, () -> {
                    player.teleport(from);
                    player.sendMessage(messageFileManager.getLangMessage("error-move"));
                    
                    // Debug mesajı
                    debug("Teleported back: " + player.getName());
                });
            }
        }
    }

    // Control Teleport
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            // Sadece plugin tarafından yapılan teleport'lara izin ver
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
                player.sendMessage(messageFileManager.getLangMessage("error-move"));
            }
        }
    }

    // Control Flight
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(messageFileManager.getLangMessage("error-move"));
        }
    }

    // Control Sneak
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(messageFileManager.getLangMessage("error-move"));
        }
    }

    // Control Sprint
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(messageFileManager.getLangMessage("error-move"));
        }
    }

    // Control Item Drop
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Log kaydı
        logManager.logItemDrop(player, event.getItemDrop().getItemStack().getType().name());
        
        // Discord webhook
        discordManager.sendItemEvent(player, event.getItemDrop().getItemStack().getType().name(), event.getItemDrop().getItemStack().getAmount(), "drop");
        
        // Debug mesajı
        debug("Drop Event: " + player.getName() + " - Control Status: " + kontrolDurumu.getOrDefault(player.getUniqueId(), false));
        
        if (kontrolDurumu.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(messageFileManager.getLangMessage("error-drop"));
            
            // Debug mesajı
            debug("Cancelling drop for: " + player.getName());
        }
    }

    // Control Damage
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player hedef = (Player) event.getEntity();
            Player saldirgan = (Player) event.getDamager();
            
            // Log kaydı
            logManager.logDamage(saldirgan, hedef, event.getDamage());
            
            // Discord webhook
            discordManager.sendDamageEvent(saldirgan, hedef, event.getDamage());
            
            // Debug mesajı
            debug("Damage Event: " + saldirgan.getName() + " -> " + hedef.getName() + " - Target Control: " + kontrolDurumu.getOrDefault(hedef.getUniqueId(), false) + " - Attacker Control: " + kontrolDurumu.getOrDefault(saldirgan.getUniqueId(), false));
            
            if (kontrolDurumu.getOrDefault(hedef.getUniqueId(), false)) {
                event.setCancelled(true);
                saldirgan.sendMessage(messageFileManager.getLangMessage("error-damage-victim").replace("%victim%", hedef.getName()));
                
                // Debug mesajı
                debug("Cancelling damage to controlled player: " + hedef.getName());
            } else if (kontrolDurumu.getOrDefault(saldirgan.getUniqueId(), false)) {
                event.setCancelled(true);
                saldirgan.sendMessage(messageFileManager.getLangMessage("error-damage-attacker-you").replace("%attacker%", saldirgan.getName()));
                
                // Debug mesajı
                debug("Cancelling damage from controlled player: " + saldirgan.getName());
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Log kaydı
        logManager.logCommand(player, event.getMessage());

        // Discord webhook
        discordManager.sendCommandEvent(player, event.getMessage());

        // Debug mesajı
        debug("Command Event: " + player.getName() + " - Command: " + event.getMessage() + " - Control Status: " + kontrolDurumu.getOrDefault(playerUUID, false));

        // Güvenlik sistemi - şüpheli aktivite kontrolü
        advancedSecurityManager.onPlayerAction(player, "command");

        // Eğer oyuncu kontrol durumundaysa komut kullanımını engelle
        if (kontrolDurumu.getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            player.sendMessage(messageFileManager.getLangMessage("error-command"));
            
            // Debug mesajı
            debug("Cancelling command for: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Kontrol durumunu kontrol et
        if (kontrolDurumu.containsKey(playerUUID)) {
            // Kontrol eden kişiyi bul
            UUID controllerUUID = kontrolEdenKisi.get(playerUUID);
            
            // Gelişmiş kontrol sistemi - kaçış denemesi kaydet
            advancedControlManager.recordEscapeAttempt(player);
            
            // Gelişmiş kontrol sistemi - history'ye kayıt ekle (oyuncu çıktığında)
            if (controllerUUID != null) {
                Player controller = Bukkit.getPlayer(controllerUUID);
                if (controller != null && controller.isOnline()) {
                    // Controller online ise normal şekilde history'ye ekle
                    advancedControlManager.endControlSession(player, true); // true = banned (çünkü kaçtı)
                } else {
                    // Controller offline ise manuel olarak history'ye ekle
                    advancedControlManager.addEscapeToHistory(controllerUUID, player.getName());
                }
            } else {
                // Controller bilinmiyorsa manuel olarak history'ye ekle
                advancedControlManager.addEscapeToHistory(null, player.getName());
            }
            
            // Log kaydı
            if (controllerUUID != null) {
                logManager.logControlEscape(player);
                discordManager.sendControlEscape(player, Bukkit.getPlayer(controllerUUID) != null ? Bukkit.getPlayer(controllerUUID).getName() : "Unknown");
            } else {
                logManager.logControlEscape(player);
                discordManager.sendControlEscape(player, "Unknown");
            }
            
            // Kontrol durumunu kaldır
            kontrolDurumu.remove(playerUUID);
            kontrolSohbet.remove(playerUUID);
            kontrolEdenKisi.remove(playerUUID);

            // Sadece oyuncu kendi ayrıldığında kaçış banı at (end komutunda ban atıldıysa tekrar atma)
            // Eğer oyuncu zaten banlandıysa tekrar ban atma
            if (!banlandi.getOrDefault(playerUUID, false)) {
                // Config'ten ban-command'ı al
                String banCommand = getConfig().getString("ban-command");

                // Komutu çalıştır
                if (banCommand != null && !banCommand.isEmpty()) {
                    // Config'den KAÇIŞ ban ayarlarını al
                    String banDuration = getConfig().getString("auto-ban.escape-ban.duration", "7d");
                    String banReason = getConfig().getString("auto-ban.escape-ban.reason", "Escape attempt during control");
                    
                    // Değişkenleri değiştir
                    banCommand = banCommand
                        .replace("%player%", player.getName())
                        .replace("%duration%", banDuration)
                        .replace("%reason%", banReason);
                    
                    // Debug mesajı
                    debug("Executing escape ban command: " + banCommand);
                    
                    // Komutu çalıştır
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
                }
            } else {
                // Oyuncu zaten banlandı, flag'i temizle
                banlandi.remove(playerUUID);
                debug("Player " + player.getName() + " already banned, skipping escape ban");
            }

            // Kontrol sohbetine mesaj gönder
            String kontrolSohbetMesaji = messageFileManager.getLangMessage("control-chat-player-quit");
            if (kontrolSohbetMesaji != null) {
                kontrolSohbetMesaji = kontrolSohbetMesaji
                        .replace("%player%", player.getName());

                // Kontrol sohbetine mesaj gönder
                for (UUID uuid : kontrolSohbet.keySet()) {
                    Player kontrolPlayer = Bukkit.getPlayer(uuid);
                    if (kontrolPlayer != null && kontrolPlayer.isOnline()) {
                        kontrolPlayer.sendMessage(kontrolSohbetMesaji);
                    }
                }
            }
        }
    }

    // Konum string'e çevirme
    public String locationToString(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public Location stringToLocation(String locString) {
        String[] parts = locString.split(",");
        if (parts.length != 6) return null;
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }

    // Süre formatını düzenleme yardımcı metodu
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}

