package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class DiscordCommand extends SubCommand {

public DiscordCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "discord";
    }

    @Override
    public String getDescription() {
        return "Discord webhook management";
    }

    @Override
    public String getSyntax() {
        return "/control discord <test>";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("discord-help");
            for (String helpMessage : helpMessages) {
                player.sendMessage(helpMessage);
            }
            return;
        }

        if (isArg(args[1], "test")) {
            if (!plugin.getDiscordManager().isDiscordEnabled()) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("discord-disabled"));
                return;
            }

            if (plugin.getDiscordManager().getWebhookUrl().equals("YOUR_WEBHOOK_URL_HERE")) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("discord-url-not-set"));
                return;
            }

            player.sendMessage(plugin.getMessageFileManager().getLangMessage("discord-sending"));
            plugin.getDiscordManager().sendTestMessage();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("discord-sent"));
        } else {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("invalid-argument"));
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "test");
        }
        return null;
    }
}





