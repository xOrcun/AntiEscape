package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.stream.Collectors;

public class TakeCommand extends SubCommand {

    public TakeCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getDescription() {
        return "Take a player into control";
    }

    @Override
    public String getSyntax() {
        return "/control take <player>";
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
            List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("control-help");
            for (String helpMessage : helpMessages) {
                player.sendMessage(helpMessage);
            }
            return;
        }

        Player hedef = Bukkit.getPlayer(args[1]);
        if (hedef == null) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("offline-player"));
            return;
        }

        if (hedef.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("cannot-control-yourself"));
            return;
        }

        if (plugin.getControlArea() == null) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-area-not-set"));
            return;
        }

        if (plugin.getControlReturnLocation() == null) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-return-not-set"));
            return;
        }

        if (plugin.getControlStatusMap().containsKey(hedef.getUniqueId())) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-player-started")
                    .replace("%player%", hedef.getName()));
            return;
        }

        // Start control Session
        plugin.getControlStatusMap().put(hedef.getUniqueId(), true);
        
        // Join session chat (Target UUID as session host)
        plugin.getControlChatMap().put(hedef.getUniqueId(), hedef.getUniqueId());
        plugin.getControlChatMap().put(player.getUniqueId(), hedef.getUniqueId());
        
        plugin.getControllerMap().put(hedef.getUniqueId(), player.getUniqueId());

        plugin.getAdvancedControlManager().startControlSession(player, hedef);
        plugin.getLogManager().logControlStart(player, hedef);
        plugin.getDiscordManager().sendControlStart(player, hedef);

        plugin.debug("Control started for: " + hedef.getName());
        hedef.teleport(plugin.getControlArea());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(plugin.getMessageFileManager().getLangMessage("control-started")
                    .replace("%player%", hedef.getName()));
        }

        hedef.sendMessage(
                plugin.getMessageFileManager().getLangMessage("control-message").replace("%player%", player.getName()));
        player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-chat-join")
                .replace("%player%", hedef.getName()));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
