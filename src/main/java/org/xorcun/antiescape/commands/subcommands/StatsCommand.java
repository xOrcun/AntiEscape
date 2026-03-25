package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.managers.AdvancedControlManager.ControlHistory;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class StatsCommand extends SubCommand {

    public StatsCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "View control statistics for a staff member or global";
    }

    @Override
    public String getSyntax() {
        return "/control stats [player]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length == 1) {
            // Global stats based on total history
            int total = 0;
            int banned = 0;
            int clean = 0;

            for (List<ControlHistory> historyList : plugin.getAdvancedControlManager().getAllHistory().values()) {
                for (ControlHistory record : historyList) {
                    total++;
                    if (record.isBanned()) {
                        banned++;
                    } else {
                        clean++;
                    }
                }
            }

            List<String> statsMessages = plugin.getMessageFileManager()
                    .getLangMessageList("advanced-control-stats-global");
            for (String message : statsMessages) {
                String formattedMessage = message
                        .replace("%total%", String.valueOf(total))
                        .replace("%banned%", String.valueOf(banned))
                        .replace("%clean%", String.valueOf(clean))
                        .replace("%escaped%", "0"); // Escapes not explicitly tracked in Global History yet
                player.sendMessage(formattedMessage);
            }
        } else {
            // Player stats
            String targetName = args[1];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            // Basic history stats
            List<ControlHistory> historyList = plugin.getAdvancedControlManager().getControlHistory(targetName);
            if (historyList != null) {
                // We could calculate history-specific stats here if needed,
                // but currently we focus on the detailed stats list.
            }

            // Detailed stats
            int escapeAttempts = plugin.getAdvancedControlManager()
                    .getEscapeAttempts(targetPlayer != null ? targetPlayer.getUniqueId() : null);
            // Note: getEscapeAttempts currently only works for online players in manager.
            // We should ideally track this per player name if we want it to persist.

            int suspiciousCount = plugin.getAdvancedSecurityManager().getSuspiciousActivityCount(targetName);
            String ipAddress = plugin.getAdvancedSecurityManager().getPlayerIP(targetName);
            if (ipAddress == null && targetPlayer != null) {
                ipAddress = targetPlayer.getAddress().getAddress().getHostAddress();
            }

            String whitelistStatus = plugin.getAdvancedSecurityManager().isWhitelisted(targetName)
                    ? plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.whitelisted")
                    : plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.not-whitelisted");

            String controlStatus = plugin.getAdvancedControlManager()
                    .isPlayerControlled(targetPlayer != null ? targetPlayer.getUniqueId() : null)
                            ? plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.controlled")
                            : plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.not-controlled");

            List<String> statsMessages = plugin.getMessageFileManager()
                    .getLangMessageList("advanced-control-stats-list");
            for (String message : statsMessages) {
                String formattedMessage = message
                        .replace("%player%", targetName)
                        .replace("%escape_attempts%", String.valueOf(escapeAttempts))
                        .replace("%suspicious_count%", String.valueOf(suspiciousCount))
                        .replace("%ip_address%", ipAddress != null ? ipAddress : "N/A")
                        .replace("%whitelist_status%", whitelistStatus)
                        .replace("%control_status%", controlStatus);
                player.sendMessage(formattedMessage);
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return null;
    }
}
