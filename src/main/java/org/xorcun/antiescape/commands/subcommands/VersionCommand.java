package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.UpdateSystem.UpdateChecker;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class VersionCommand extends SubCommand {

    public VersionCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Check the current plugin version and updates";
    }

    @Override
    public String getSyntax() {
        return "/control version";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        org.bukkit.plugin.PluginDescriptionFile data = plugin.getDescription();
        player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
        player.sendMessage("§fVersion: §a" + data.getVersion());
        player.sendMessage("§fLanguage: §e" + plugin.getMessageFileManager().getCurrentLanguage());
        player.sendMessage(
                "§fLogging: §e" + (plugin.getConfig().getBoolean("logging.enabled", true) ? "Enabled" : "Disabled"));
        player.sendMessage("§fLog Files: §e" + plugin.getLogManager().getAllLogFilePaths().size() + " files");
        player.sendMessage("§fActive Controls: §e" + plugin.getControlStatusMap().size());
        player.sendMessage(
                "§fDiscord Webhook: §e" + (plugin.getDiscordManager().isDiscordEnabled() ? "Enabled" : "Disabled"));
        player.sendMessage("§fAuthors: §e" + data.getAuthors());
        player.sendMessage("§fWebsite: §e" + data.getWebsite());
        player.sendMessage("§fDiscord Server: §bhttps://orcunozturk.com/discord");

        plugin.getUpdateChecker().sendUpdateNotification(player);

        // If up to date, or ahead, notify explicitly since sendUpdateNotification only
        // sends for OUTDATED
        UpdateChecker.VersionStatus status = plugin.getUpdateChecker().getVersionStatus();
        if (status == UpdateChecker.VersionStatus.UP_TO_DATE) {
            player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
            player.sendMessage("§7§lUsing latest version.");
            player.sendMessage("§fCurrent: §e" + data.getVersion());
            player.sendMessage("§8§m--------------------------------§r");
        } else if (status == UpdateChecker.VersionStatus.AHEAD) {
            player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
            player.sendMessage("§e§lDevelopment version!");
            player.sendMessage("§fCurrent: §e" + data.getVersion());
            player.sendMessage("§8§m--------------------------------§r");
        } else if (status == UpdateChecker.VersionStatus.UNKNOWN) {
            player.sendMessage("§8§m----------§r §8[ §eAntiEscape §8] §8§m----------§r");
            player.sendMessage("§c§lCould not check version.");
            player.sendMessage("§fCurrent: §e" + data.getVersion());
            player.sendMessage("§8§m--------------------------------§r");
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
