package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand extends SubCommand {

public WhitelistCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public String getDescription() {
        return "Manage the plugin's security whitelist";
    }

    @Override
    public String getSyntax() {
        return "/control whitelist <add/remove/list> [player] [reason...]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-usage-whitelist"));
            return;
        }

        if (isArg(args[1], "add")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-usage-whitelist"));
                return;
            }

            String targetName = args[2];
            StringBuilder reason = new StringBuilder();
            if (args.length > 3) {
                for (int i = 3; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
            } else {
                reason.append(plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.no-reason"));
            }

            if (plugin.getAdvancedSecurityManager().getWhitelist().containsKey(targetName.toLowerCase())) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-whitelist-already").replace("%player%", targetName));
                return;
            }

            plugin.getAdvancedSecurityManager().addToWhitelist(targetName, reason.toString().trim(), player.getName());
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-whitelist-added").replace("%player%", targetName));
            return;
        }

        if (isArg(args[1], "remove")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-usage-whitelist"));
                return;
            }

            String targetName = args[2];
            if (!plugin.getAdvancedSecurityManager().getWhitelist().containsKey(targetName.toLowerCase())) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-whitelist-not-found").replace("%player%", targetName));
                return;
            }

            plugin.getAdvancedSecurityManager().removeFromWhitelist(targetName, player.getName());
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-whitelist-removed").replace("%player%", targetName));
            return;
        }

        if (isArg(args[1], "list")) {
            List<String> whitelist = new ArrayList<>(plugin.getAdvancedSecurityManager().getWhitelist().keySet());

            List<String> listMessages = plugin.getMessageFileManager().getLangMessageList("advanced-security-whitelist-list");
            for (String message : listMessages) {
                String formattedMessage = message.replace("%count%", String.valueOf(whitelist.size()));
                player.sendMessage(formattedMessage);
            }

            if (whitelist.isEmpty()) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.whitelist-empty"));
            } else {
                for (String wPlayer : whitelist) {
                    player.sendMessage("§7- §f" + wPlayer);
                }
            }
            return;
        }

        player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-usage-whitelist"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "add", "remove", "list");
        } else if (args.length == 3 && isArg(args[1], "remove")) {
            return new ArrayList<>(plugin.getAdvancedSecurityManager().getWhitelist().keySet()).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && isArg(args[1], "add")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}





