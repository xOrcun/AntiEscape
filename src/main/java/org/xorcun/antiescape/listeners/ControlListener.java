package org.xorcun.antiescape.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.xorcun.antiescape.AntiEscape;

import java.util.Map;
import java.util.UUID;

public class ControlListener implements Listener {

    private final AntiEscape plugin;

    public ControlListener(AntiEscape plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        plugin.getLogManager().logChat(player, event.getMessage());
        plugin.getDiscordManager().sendChatEvent(player, event.getMessage());

        plugin.getAdvancedSecurityManager().onPlayerAction(player, "chat");

        // Session-based routing
        if (plugin.getControlChatMap().containsKey(playerUUID)) {
            event.setCancelled(true);
            UUID sessionHost = plugin.getControlChatMap().get(playerUUID);

            plugin.debug("Routing chat for " + player.getName() + " in session of " + sessionHost);

            String message = plugin.getConfigFileManager().getConfigMessageWithColors("control-prefix")
                    .replace("%player%", player.getName())
                    .replace("%message%", event.getMessage());

            String spyMessage = plugin.getConfigFileManager().getConfigMessageWithColors("control-spy-format")
                    .replace("%target%", Bukkit.getOfflinePlayer(sessionHost).getName())
                    .replace("%player%", player.getName())
                    .replace("%message%", event.getMessage());

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                UUID onlineUUID = onlinePlayer.getUniqueId();
                
                // Send to players in the same session
                if (plugin.getControlChatMap().containsKey(onlineUUID) && plugin.getControlChatMap().get(onlineUUID).equals(sessionHost)) {
                    onlinePlayer.sendMessage(message);
                } 
                // Send spy message to staff with permission who are NOT currently in any control chat session
                else if (onlinePlayer.hasPermission(plugin.getConfigFileManager().getConfigMessage("permissions.general")) 
                        && !plugin.getControlChatMap().containsKey(onlineUUID)) {
                    onlinePlayer.sendMessage(spyMessage);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isControlled = plugin.getControlStatusMap().getOrDefault(uuid, false);

        if (isControlled) {
            Location from = event.getFrom();
            Location to = event.getTo();

            plugin.getAdvancedSecurityManager().onPlayerAction(player, "movement");
            plugin.getLogManager().logMove(player, from, to);
            plugin.getDiscordManager().sendMoveEvent(player, from, to);

            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()
                    || from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch())) {
                event.setCancelled(true);

                plugin.debug("Cancelling move for: " + player.getName());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(from);
                    player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-move"));
                    plugin.debug("Teleported back: " + player.getName());
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false)) {
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-move"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-move"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-move"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        if (plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-move"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        plugin.getLogManager().logItemDrop(player, event.getItemDrop().getItemStack().getType().name());
        plugin.getDiscordManager().sendItemEvent(player, event.getItemDrop().getItemStack().getType().name(),
                event.getItemDrop().getItemStack().getAmount(), "drop");

        plugin.debug("Drop Event: " + player.getName() + " - Control Status: "
                + plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false));

        if (plugin.getControlStatusMap().getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-drop"));

            plugin.debug("Cancelling drop for: " + player.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            plugin.getAdvancedSecurityManager().onPlayerAction(player, "combat");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player target = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            plugin.getLogManager().logDamage(attacker, target, event.getDamage());
            plugin.getDiscordManager().sendDamageEvent(attacker, target, event.getDamage());

            plugin.debug("Damage Event: " + attacker.getName() + " -> " + target.getName() + " - Target Control: "
                    + plugin.getControlStatusMap().getOrDefault(target.getUniqueId(), false) + " - Attacker Control: "
                    + plugin.getControlStatusMap().getOrDefault(attacker.getUniqueId(), false));

            if (plugin.getControlStatusMap().getOrDefault(target.getUniqueId(), false)) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getMessageFileManager().getLangMessage("error-damage-victim")
                        .replace("%victim%", target.getName()));

                plugin.debug("Cancelling damage to controlled player: " + target.getName());
            } else if (plugin.getControlStatusMap().getOrDefault(attacker.getUniqueId(), false)) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getMessageFileManager().getLangMessage("error-damage-attacker-you")
                        .replace("%attacker%", attacker.getName()));

                plugin.debug("Cancelling damage from controlled player: " + attacker.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        plugin.getLogManager().logCommand(player, event.getMessage());
        plugin.getDiscordManager().sendCommandEvent(player, event.getMessage());
        plugin.debug("Command Event: " + player.getName() + " - Command: " + event.getMessage() + " - Control Status: "
                + plugin.getControlStatusMap().getOrDefault(playerUUID, false));
        plugin.getAdvancedSecurityManager().onPlayerAction(player, "command");

        if (plugin.getControlStatusMap().getOrDefault(playerUUID, false)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("error-command"));

            plugin.debug("Cancelling command for: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.getControlStatusMap().containsKey(playerUUID)) {
            UUID controllerUUID = plugin.getControllerMap().get(playerUUID);

            plugin.getAdvancedControlManager().recordEscapeAttempt(player);

            if (controllerUUID != null) {
                Player controller = Bukkit.getPlayer(controllerUUID);
                if (controller != null && controller.isOnline()) {
                    plugin.getAdvancedControlManager().endControlSession(player, true);
                } else {
                    plugin.getAdvancedControlManager().addEscapeToHistory(controllerUUID, player.getName());
                }
            } else {
                plugin.getAdvancedControlManager().addEscapeToHistory(null, player.getName());
            }

            if (controllerUUID != null) {
                plugin.getLogManager().logControlEscape(player);
                plugin.getDiscordManager().sendControlEscape(player,
                        Bukkit.getPlayer(controllerUUID) != null ? Bukkit.getPlayer(controllerUUID).getName()
                                : "Unknown");
            } else {
                plugin.getLogManager().logControlEscape(player);
                plugin.getDiscordManager().sendControlEscape(player, "Unknown");
            }

            plugin.getControlStatusMap().remove(playerUUID);
            plugin.getControlChatMap().remove(playerUUID);
            plugin.getControllerMap().remove(playerUUID);
            plugin.getBannedMap().remove(playerUUID);

            String controlChatMsg = plugin.getMessageFileManager().getLangMessage("control-chat-player-quit");
            if (controlChatMsg != null) {
                controlChatMsg = controlChatMsg.replace("%player%", player.getName());

                for (Map.Entry<UUID, UUID> entry : plugin.getControlChatMap().entrySet()) {
                    Player controlPlayer = Bukkit.getPlayer(entry.getKey());
                    if (controlPlayer != null && controlPlayer.isOnline()) {
                        controlPlayer.sendMessage(controlChatMsg);
                    }
                }
            }
        }
    }
}
