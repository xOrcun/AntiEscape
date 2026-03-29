package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.stream.Collectors;

public class ChatCommand extends SubCommand {

public ChatCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getDescription() {
        return "Join or leave the control chat";
    }

    @Override
    public String getSyntax() {
        return "/control chat [join <player> | leave]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        // Just "/control chat" - join own session or notify
        if (args.length == 1) {
            // If player is a target in an active control, they are already in their own session
            if (plugin.getControlStatusMap().containsKey(player.getUniqueId())) {
                plugin.getControlChatMap().put(player.getUniqueId(), player.getUniqueId());
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-join")
                        .replace("%player%", player.getName()));
                return;
            }

            // If player is a controller (admin), toggling chat
            if (plugin.getControlChatMap().containsKey(player.getUniqueId())) {
                plugin.getControlChatMap().remove(player.getUniqueId());
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-left"));
            } else {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("chat-usage"));
            }
            return;
        }

        // "/control chat join <player>"
        if (args.length >= 3 && isArg(args[1], "join")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null || !plugin.getControlStatusMap().containsKey(target.getUniqueId())) {
                String targetName = (target != null) ? target.getName() : args[2];
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-player-error")
                        .replace("%player%", targetName));
                return;
            }

            // Join the target's session (target UUID is host)
            plugin.getControlChatMap().put(player.getUniqueId(), target.getUniqueId());
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-join")
                    .replace("%player%", target.getName()));
            return;
        }

        // "/control chat leave"
        if (args.length >= 2 && isArg(args[1], "leave")) {
            if (plugin.getControlChatMap().containsKey(player.getUniqueId())) {
                plugin.getControlChatMap().remove(player.getUniqueId());
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-left"));
            } else {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("not-in-chat"));
            }
            return;
        }

        player.sendMessage(plugin.getMessageFileManager().getLangMessage("chat-usage"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "join", "leave");
        }
        if (args.length == 3 && isArg(args[1], "join")) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> plugin.getControlStatusMap().containsKey(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}





