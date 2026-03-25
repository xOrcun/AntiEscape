package org.xorcun.antiescape;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.xorcun.antiescape.managers.ConfigFileManager;
import org.xorcun.antiescape.managers.MessageFileManager;
import org.xorcun.antiescape.managers.LogManager;
import org.xorcun.antiescape.managers.DiscordManager;
import org.xorcun.antiescape.managers.AdvancedControlManager;
import org.xorcun.antiescape.managers.AdvancedSecurityManager;
import org.xorcun.antiescape.UpdateSystem.JoinEvent;
import org.xorcun.antiescape.UpdateSystem.UpdateChecker;
import org.xorcun.antiescape.commands.CommandManager;
import org.xorcun.antiescape.listeners.ControlListener;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.PluginCommand;
import java.lang.reflect.Field;

import java.util.*;

public class AntiEscape extends JavaPlugin {
    private boolean debugEnabled = false;
    private Location controlArea = null;
    private Location controlReturnLocation = null;
    private final Map<UUID, Boolean> controlStatusMap = new HashMap<>();
    private final Map<UUID, UUID> controlChatMap = new HashMap<>();
    private final Map<UUID, UUID> controllerMap = new HashMap<>();
    private final Map<UUID, Boolean> bannedMap = new HashMap<>(); 

    private ConfigFileManager configFileManager;
    private MessageFileManager messageFileManager;
    private JoinEvent joinEvent;
    private LogManager logManager;
    private DiscordManager discordManager;
    private AdvancedControlManager advancedControlManager;
    private AdvancedSecurityManager advancedSecurityManager;
    private UpdateChecker updateChecker;

    // --- GETTERS & SETTERS FOR MODULAR COMMANDS --- //
    public Location getControlArea() { return controlArea; }
    public void setControlArea(Location loc) { this.controlArea = loc; }

    public Location getControlReturnLocation() { return controlReturnLocation; }
    public void setControlReturnLocation(Location loc) { this.controlReturnLocation = loc; }

    public Map<UUID, Boolean> getControlStatusMap() { return controlStatusMap; }
    public Map<UUID, UUID> getControlChatMap() { return controlChatMap; }
    public Map<UUID, UUID> getControllerMap() { return controllerMap; }
    public Map<UUID, Boolean> getBannedMap() { return bannedMap; }

    public ConfigFileManager getConfigFileManager() { return configFileManager; }
    public MessageFileManager getMessageFileManager() { return messageFileManager; }
    public JoinEvent getJoinEvent() { return joinEvent; }
    public LogManager getLogManager() { return logManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public AdvancedControlManager getAdvancedControlManager() { return advancedControlManager; }
    public AdvancedSecurityManager getAdvancedSecurityManager() { return advancedSecurityManager; }
    // ---------------------------------------------- //

    public void reloadPluginSettings() {
        reloadConfig();
        this.debugEnabled = getConfig().getBoolean("debug", false);
        
        if (advancedSecurityManager != null) advancedSecurityManager.reloadConfig();
        if (logManager != null) logManager.reloadConfig();
        if (discordManager != null) discordManager.reloadWebhookFile();
        if (messageFileManager != null) messageFileManager.loadLangFile();
    }

    public void debug(String message) {
        if (debugEnabled) {
            String language = getConfig().getString("language", "en");
            Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §f[" + language.toUpperCase() + "] " + message);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    // Getter method for UpdateChecker
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §aPlugin actived!");
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §fDiscord Server: §bhttps://orcunozturk.com/discord");

        saveDefaultConfig();
        reloadConfig();
        this.debugEnabled = getConfig().getBoolean("debug", false);

        CommandManager commandManager = new CommandManager(this);
        getCommand("control").setExecutor(commandManager);
        getCommand("control").setTabCompleter(commandManager);

        getServer().getPluginManager().registerEvents(new ControlListener(this), this);

        joinEvent = new JoinEvent(this);
        getServer().getPluginManager().registerEvents(joinEvent, this);

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

        configFileManager = new ConfigFileManager(this);
        messageFileManager = new MessageFileManager(configFileManager);

        messageFileManager.saveLangFile();
        messageFileManager.loadLangFile();
        
        registerMainCommandAliases();

        logManager = new LogManager(this);

        discordManager = new DiscordManager(this);

        advancedControlManager = new AdvancedControlManager(this);

        advancedSecurityManager = new AdvancedSecurityManager(this);

        updateChecker = new UpdateChecker(this);
        updateChecker.checkForUpdates();

        String locString = getConfig().getString("control-location");
        if (locString != null && !locString.equals("none")) {
            controlArea = stringToLocation(locString);
        }

        String spawnLocString = getConfig().getString("control-spawn-location");
        if (spawnLocString != null && !spawnLocString.equals("none")) {
            controlReturnLocation = stringToLocation(spawnLocString);
        }

        int pluginId = 23459; 
        new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        if (advancedSecurityManager != null) {
            advancedSecurityManager.onDisable();
        }
        
        bannedMap.clear();
        
        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §cPlugin deactivated!");
    }

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

    private void registerMainCommandAliases() {
        List<String> aliases = messageFileManager.getLangMessageListNoPrefix("main-command-aliases");
        if (aliases == null || aliases.isEmpty()) return;

        try {
            PluginCommand controlCommand = getCommand("control");
            if (controlCommand == null) return;

            // Set aliases on the command itself (optional but good for metadata)
            controlCommand.setAliases(aliases);

            // Access CommandMap
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());

            // Register each alias to the CommandMap
            for (String alias : aliases) {
                if (alias.equalsIgnoreCase("control")) continue;
                
                // Register alias pointing to the main command
                commandMap.register(getDescription().getName(), new Command(alias) {
                    @Override
                    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                        return controlCommand.execute(sender, commandLabel, args);
                    }

                    @Override
                    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                        return controlCommand.tabComplete(sender, alias, args);
                    }
                });
            }
            
            debug("Dynamic main command aliases registered: " + String.join(", ", aliases));
        } catch (Exception e) {
            debug("Failed to register dynamic aliases: " + e.getMessage());
        }
    }

    public String formatDuration(long duration) {
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


