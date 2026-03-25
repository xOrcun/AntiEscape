package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.managers.AdvancedControlManager;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.stream.Collectors;

public class HistoryCommand extends SubCommand {

public HistoryCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public String getDescription() {
        return "View a player's control history";
    }

    @Override
    public String getSyntax() {
        return "/control history <player>";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-usage-history"));
            return;
        }

        String targetName = args[1];
        List<AdvancedControlManager.ControlHistory> history = plugin.getAdvancedControlManager().getControlHistory(targetName);

        List<String> historyMessages = plugin.getMessageFileManager().getLangMessageList("advanced-control-history-list");
        for (String message : historyMessages) {
            String formattedMessage = message
                    .replace("%player%", targetName)
                    .replace("%count%", String.valueOf(history.size()));
            player.sendMessage(formattedMessage);
        }

        if (history.isEmpty()) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.no-history"));
        } else {
            for (int i = Math.max(0, history.size() - 5); i < history.size(); i++) {
                AdvancedControlManager.ControlHistory record = history.get(i);
                String status = record.isBanned() ?
                        plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.banned") :
                        plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.released");

                String message = plugin.getMessageFileManager().getLangMessageNoPrefix("advanced-control-history-record")
                        .replace("%index%", String.valueOf(i + 1))
                        .replace("%controller%", record.getController())
                        .replace("%target%", record.getTarget())
                        .replace("%start_date%", record.getStartTime())
                        .replace("%end_date%", record.getEndTime())
                        .replace("%duration%", plugin.formatDuration(record.getDuration()))
                        .replace("%status%", status);

                player.sendMessage(message);
            }
        }
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





