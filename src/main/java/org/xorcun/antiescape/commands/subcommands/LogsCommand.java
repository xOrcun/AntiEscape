package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsCommand extends SubCommand {

public LogsCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public String getDescription() {
        return "Manage plugin log files";
    }

    @Override
    public String getSyntax() {
        return "/control logs <list/clear/clear-all> [type]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("logs-help");
            for (String helpMessage : helpMessages) {
                player.sendMessage(helpMessage);
            }
            return;
        }

        if (isArg(args[1], "list")) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("log-files-title"));
            Map<String, String> logPaths = plugin.getLogManager().getAllLogFilePaths();
            for (Map.Entry<String, String> entry : logPaths.entrySet()) {
                String status = plugin.getConfig().getBoolean("logging.log-" + entry.getKey(), true) ?
                        plugin.getMessageFileManager().getLangMessage("log-files-enabled") :
                        plugin.getMessageFileManager().getLangMessage("log-files-disabled");
                player.sendMessage(status.replace("%type%", entry.getKey()));
            }
            return;
        }

        if (isArg(args[1], "clear")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("log-files-usage"));
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("log-files-types"));
                return;
            }

            String logType = args[2].toLowerCase();
            plugin.getLogManager().clearLogs(logType);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("log-files-cleared").replace("%type%", logType));
            return;
        }

        if (isArg(args[1], "clear-all")) {
            plugin.getLogManager().clearAllLogs();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("log-files-all-cleared"));
            return;
        }
        
        player.sendMessage(plugin.getMessageFileManager().getLangMessage("invalid-argument"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "list", "clear", "clear-all");
        } else if (args.length == 3 && args[1].equalsIgnoreCase("clear")) {
            return Arrays.asList("moves", "commands", "chat", "damage", "items", "control", "general").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}





