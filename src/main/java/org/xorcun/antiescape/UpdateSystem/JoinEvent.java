package org.xorcun.antiescape.UpdateSystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.xorcun.antiescape.AntiEscape;

public class JoinEvent implements Listener {
    private final AntiEscape plugin;

    public JoinEvent(AntiEscape plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdatesPlayer(Player player) {
        // Check if update checking is enabled
        if (!plugin.getConfig().getBoolean("check-update", true)) {
            plugin.debug("Update checking is disabled in config for player: " + player.getName());
            return;
        }

        // Check if player has permission to receive update notifications
        if (!player.hasPermission(plugin.getConfig().getString("permissions.update-notify"))) {
            return;
        }

        // Use the main update checker to avoid duplicate API calls
        plugin.getUpdateChecker().checkForUpdatesPlayer(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if update checking is enabled
        if (!plugin.getConfig().getBoolean("check-update", true)) {
            plugin.debug("Update checking is disabled in config for player join: " + player.getName());
            return;
        }
        
        // Check if player has permission to receive update notifications
        if (!player.hasPermission(plugin.getConfig().getString("permissions.update-notify"))) {
            return;
        }

        // Check for updates using the main update checker
        plugin.getUpdateChecker().checkForUpdatesPlayer(player);
    }
}
