package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class HelpCommand extends SubCommand {

public HelpCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows help message for plugin commands";
    }

    @Override
    public String getSyntax() {
        return "/control help";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("commands-help");
        for (String helpMessage : helpMessages) {
            player.sendMessage(helpMessage);
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null; // No sub-arguments
    }
}





