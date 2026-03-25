package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class SuspiciousCommand extends SubCommand {

public SuspiciousCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "suspicious";
    }

    @Override
    public String getDescription() {
        return "Clear suspicious activity counts for all offline players";
    }

    @Override
    public String getSyntax() {
        return "/control suspicious <clear>";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-usage-suspicious"));
            return;
        }

        if (isArg(args[1], "clear")) {
            if (args.length == 3) {
                plugin.getAdvancedSecurityManager().clearSuspiciousActivity(args[2]);
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("suspicious-cleared").replace("%player%", args[2]));
            } else {
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    plugin.getAdvancedSecurityManager().clearSuspiciousActivity(p.getName());
                }
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("suspicious-cleared").replace("%player%", "All Online Players"));
            }
        } else {
            // Show status for a single player
            String targetName = args[1];
            int count = plugin.getAdvancedSecurityManager().getSuspiciousActivityCount(targetName);
            
            List<String> statusMessages = plugin.getMessageFileManager().getLangMessageList("suspicious-status-list");
            for (String message : statusMessages) {
                player.sendMessage(message
                        .replace("%player%", targetName)
                        .replace("%count%", String.valueOf(count)));
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            List<String> completions = getTabCompletions(args[1], "clear");
            completions.addAll(org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList()));
            return completions;
        }
        return null;
    }
}





