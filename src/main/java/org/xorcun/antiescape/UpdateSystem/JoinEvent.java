package org.xorcun.antiescape.UpdateSystem;

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

        // Special join message for plugin owner (supports online/offline mode)
        String onlineUUID = "f7b6dd98-bbff-4620-8de1-217986d63ea9";
        String offlineUUID = "56e07bfa-b3fb-314d-bad0-2ed76b58896f";
        String playerUUID = player.getUniqueId().toString();

        if (playerUUID.equalsIgnoreCase(onlineUUID) ||
                playerUUID.equalsIgnoreCase(offlineUUID)) {
            String message = "§8[§eAntiEscape§8] §fPlugin owner §b" + player.getName()
                    + " §fjoined the server! Thanks for your support!";
            org.bukkit.Bukkit.broadcastMessage(message);
        }

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
