package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EndCommand extends SubCommand {

public EndCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "end";
    }

    @Override
    public String getDescription() {
        return "End a player's control session and optionally ban them";
    }

    @Override
    public String getSyntax() {
        return "/control end <player> [ban]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!plugin.getAdvancedControlManager().checkControlCooldown(player)) {
            return;
        }

        if (args.length < 2) {
            List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("commands-help");
            for (String helpMessage : helpMessages) {
                player.sendMessage(helpMessage);
            }
            return;
        }

        Player hedef = Bukkit.getPlayer(args[1]);
        if (hedef == null || !plugin.getControlStatusMap().containsKey(hedef.getUniqueId())) {
            String targetName = (hedef != null) ? hedef.getName() : args[1];
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-player-error")
                    .replace("%player%", targetName));
            return;
        }

        if (args.length == 3 && isArg(args[2], "ban")) {
            String banDuration = plugin.getConfig().getString("advanced-security.auto-ban.normal-ban.duration", "1d");
            String banReason = plugin.getConfig().getString("advanced-security.auto-ban.normal-ban.reason", "Control violation");

            plugin.getAdvancedControlManager().autoBanPlayer(hedef, banReason, banDuration);
            plugin.getBannedMap().put(hedef.getUniqueId(), true);

            cleanControlState(hedef, player, true);

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(plugin.getMessageFileManager().getLangMessage("control-finished-ban").replace("%player%", hedef.getName()));
            }

        } else {
            cleanControlState(hedef, player, false);
            
            // Teleport handled gracefully if spawn not null
            if (plugin.getControlReturnLocation() != null) {
                hedef.teleport(plugin.getControlReturnLocation());
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(plugin.getMessageFileManager().getLangMessage("control-finished-clean").replace("%player%", hedef.getName()));
            }
        }
    }

    private void cleanControlState(Player hedef, Player player, boolean banned) {
        UUID targetUUID = hedef.getUniqueId();
        plugin.getControlStatusMap().remove(targetUUID);
        
        // Clean up all participants in this session
        plugin.getControlChatMap().values().removeIf(sessionHost -> sessionHost.equals(targetUUID));

        plugin.getAdvancedControlManager().endControlSession(hedef, banned);
        plugin.getLogManager().logControlEnd(player, hedef, banned);
        plugin.getDiscordManager().sendControlEnd(player, hedef, banned);

        plugin.getControllerMap().remove(targetUUID);
        player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-left"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return getTabCompletions(args[2], "ban");
        }
        return null;
    }
}





