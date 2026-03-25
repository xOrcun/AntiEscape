package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;
import java.util.stream.Collectors;

public class NotesCommand extends SubCommand {

public NotesCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "notes";
    }

    @Override
    public String getDescription() {
        return "Manage notes for players";
    }

    @Override
    public String getSyntax() {
        return "/control notes <add/clear/list> <player> [note...]";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-usage-notes"));
            return;
        }

        if (isArg(args[1], "add")) {
            if (args.length < 4) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-usage-notes-add"));
                return;
            }

            String targetName = args[2];

            StringBuilder note = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                note.append(args[i]).append(" ");
            }

            plugin.getAdvancedControlManager().addNote(targetName, note.toString().trim());
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-note-added"));

        } else if (isArg(args[1], "clear")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-usage-notes"));
                return;
            }

            String targetName = args[2];
            plugin.getAdvancedControlManager().clearNotes(targetName);
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-notes-cleared"));

        } else if (isArg(args[1], "list")) {
            if (args.length < 3) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessage("advanced-control-usage-notes"));
                return;
            }

            String targetName = args[2];
            List<String> notes = plugin.getAdvancedControlManager().getNotes(targetName);

            List<String> notesMessages = plugin.getMessageFileManager().getLangMessageList("advanced-control-notes-list");
            for (String message : notesMessages) {
                String formattedMessage = message.replace("%player%", targetName);
                player.sendMessage(formattedMessage);
            }

            if (notes.isEmpty()) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.no-notes"));
            } else {
                for (int i = 0; i < notes.size(); i++) {
                    player.sendMessage("§7" + (i + 1) + ". §f" + notes.get(i));
                }
            }
        } else {
            // Shortcut for list if no add/clear/list argument is specified
            String targetName = args[1];
            List<String> notes = plugin.getAdvancedControlManager().getNotes(targetName);

            List<String> notesMessages = plugin.getMessageFileManager().getLangMessageList("advanced-control-notes-list");
            for (String message : notesMessages) {
                String formattedMessage = message.replace("%player%", targetName);
                player.sendMessage(formattedMessage);
            }

            if (notes.isEmpty()) {
                player.sendMessage(plugin.getMessageFileManager().getLangMessageNoPrefix("no-prefix.no-notes"));
            } else {
                for (int i = 0; i < notes.size(); i++) {
                    player.sendMessage("§7" + (i + 1) + ". §f" + notes.get(i));
                }
            }
        }
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        if (args.length == 2) {
            return getTabCompletions(args[1], "add", "clear", "list");
        }
 else if (args.length == 3) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}





