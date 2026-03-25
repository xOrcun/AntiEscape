package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class SetCommand extends SubCommand {

    public SetCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set the control area or return spawn";
    }

    @Override
    public String getSyntax() {
        return "/control set <area/return>";
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
            if (plugin.getControlArea() != null) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-area-already-set"));
                return;
            }
            plugin.setControlArea(player.getLocation());
            plugin.getConfig().set("control-location", plugin.locationToString(plugin.getControlArea()));
            plugin.saveConfig();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-area-set"));
            return;

        } else if (isArg(args[1], "return")) {
            if (plugin.getControlReturnLocation() != null) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-return-already-set"));
                return;
            }
            plugin.setControlReturnLocation(player.getLocation());
            plugin.getConfig().set("control-spawn-location", plugin.locationToString(plugin.getControlReturnLocation()));
            plugin.saveConfig();
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("control-return-set"));
            return;
        }

        player.sendMessage(plugin.getMessageFileManager().getLangMessage("invalid-argument"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "area", "return");
        }
        return null;
    }
}
