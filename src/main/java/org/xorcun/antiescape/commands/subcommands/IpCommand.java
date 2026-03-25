package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.stream.Collectors;

public class IpCommand extends SubCommand {

public IpCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "ip";
    }

    @Override
    public String getDescription() {
        return "Check players associated with an IP address";
    }

    @Override
    public String getSyntax() {
        return "/control ip <ip>";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("ip-usage"));
            return;
        }

        String ipAddress = args[1];
        List<String> playersOnIP = plugin.getAdvancedSecurityManager().getPlayersOnIP(ipAddress);

        List<String> ipMessages = plugin.getMessageFileManager().getLangMessageList("ip-info-list");
        for (String message : ipMessages) {
            String formattedMessage = message
                    .replace("%ip%", ipAddress)
                    .replace("%count%", String.valueOf(playersOnIP.size()));
            player.sendMessage(formattedMessage);
        }

        if (playersOnIP.isEmpty()) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("ip-no-players"));
        } else {
            for (String playerName : playersOnIP) {
                player.sendMessage("§7- §f" + playerName);
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            // Note: Returning player names here as a fallback, but in reality, entering an IP is expected.
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}





