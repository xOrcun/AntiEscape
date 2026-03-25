package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class ViolationsCommand extends SubCommand {

public ViolationsCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "violations";
    }

    @Override
    public String getDescription() {
        return "List players with security rule violations";
    }

    @Override
    public String getSyntax() {
        return "/control violations";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length > 1 && isArg(args[1], "clear")) {
            if (args.length == 3) {
                plugin.getAdvancedSecurityManager().getAllViolationCounts().remove(args[2].toLowerCase());
            } else {
                plugin.getAdvancedSecurityManager().getAllViolationCounts().clear();
            }
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("violations-cleared").replace("%player%", args.length == 3 ? args[2] : "All Players"));
            return;
        }

        // Support /control violations <player>
        if (args.length == 2 && !isArg(args[1], "clear")) {
            String targetName = args[1];
            int count = plugin.getAdvancedSecurityManager().getViolationCount(targetName);
            
            List<String> violationMessages = plugin.getMessageFileManager().getLangMessageList("advanced-security-violations-player");
            for (String message : violationMessages) {
                String formattedMessage = message
                        .replace("%player%", targetName)
                        .replace("%count%", String.valueOf(count));
                player.sendMessage(formattedMessage);
            }
            
            if (count == 0) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-no-violations"));
            }
            return;
        }

        java.util.Map<String, Integer> violations = plugin.getAdvancedSecurityManager().getAllViolationCounts();

        List<String> violationMessages = plugin.getMessageFileManager().getLangMessageList("advanced-security-violations-list");
        for (String message : violationMessages) {
            String formattedMessage = message
                    .replace("%count%", String.valueOf(violations.size()));
            player.sendMessage(formattedMessage);
        }

        if (violations.isEmpty()) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-security-no-violations"));
        } else {
            for (java.util.Map.Entry<String, Integer> entry : violations.entrySet()) {
                String message = plugin.getMessageFileManager().getLangMessageNoPrefix("advanced-security-violation-record")
                        .replace("%player%", entry.getKey())
                        .replace("%count%", String.valueOf(entry.getValue()));
                player.sendMessage(message);
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





