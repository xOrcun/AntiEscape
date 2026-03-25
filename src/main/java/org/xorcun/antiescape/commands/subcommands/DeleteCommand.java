package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class DeleteCommand extends SubCommand {

public DeleteCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Delete designated control locations";
    }

    @Override
    public String getSyntax() {
        return "/control delete <area/spawn>";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("location-help");
            for (String helpMessage : helpMessages) {
                player.sendMessage(helpMessage);
            }
            return;
        }

        if (isArg(args[1], "area")) {
            if (plugin.getControlArea() == null) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-area-not-set"));
                return;
            }
            plugin.setControlArea(null);
            plugin.getConfig().set("control-location", "none");
            plugin.saveConfig();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-area-deleted"));
            return;
        }

        if (isArg(args[1], "spawn")) {
            if (plugin.getControlReturnLocation() == null) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-return-not-set"));
                return;
            }
            plugin.setControlReturnLocation(null);
            plugin.getConfig().set("control-spawn-location", "none");
            plugin.saveConfig();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-return-deleted"));
            return;
        }

        player.sendMessage(plugin.getMessageFileManager().getLangMessage("invalid-argument"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "area", "spawn");
        }
        return null;
    }
}





