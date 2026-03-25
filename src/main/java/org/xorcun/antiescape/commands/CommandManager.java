package org.xorcun.antiescape.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;

import java.util.ArrayList;
import java.util.List;
import org.xorcun.antiescape.commands.subcommands.*;

public class CommandManager implements CommandExecutor, TabCompleter {
    
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final AntiEscape plugin;

    public CommandManager(AntiEscape plugin) {
        this.plugin = plugin;
        
        subcommands.add(new ChatCommand(plugin));
        subcommands.add(new DeleteCommand(plugin));
        subcommands.add(new DiscordCommand(plugin));
        subcommands.add(new EndCommand(plugin));
        subcommands.add(new HelpCommand(plugin));
        subcommands.add(new HistoryCommand(plugin));
        subcommands.add(new IpCommand(plugin));
        subcommands.add(new LogsCommand(plugin));
        subcommands.add(new NotesCommand(plugin));
        subcommands.add(new ReloadCommand(plugin));
        subcommands.add(new SetCommand(plugin));
        subcommands.add(new StatsCommand(plugin));
        subcommands.add(new SuspiciousCommand(plugin));
        subcommands.add(new TakeCommand(plugin));
        subcommands.add(new VersionCommand(plugin));
        subcommands.add(new ViolationsCommand(plugin));
        subcommands.add(new WhitelistCommand(plugin));
    }

    public void registerCommand(SubCommand command) {
        subcommands.add(command);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§8[§eAntiEscape§8] §cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission(plugin.getConfig().getString("permissions.general", "antiescape.admin"))) {
            player.sendMessage(plugin.getMessageFileManager().getLangMessage("no-permission"));
            return true;
        }

        if (args.length > 0) {
            for (SubCommand subCommand : subcommands) {
                List<String> aliases = plugin.getMessageFileManager().getLangMessageListNoPrefix("command-aliases." + subCommand.getName());
                if (aliases == null) aliases = new ArrayList<>();
                boolean match = args[0].equalsIgnoreCase(subCommand.getName());
                if (!match) {
                    for (String alias : aliases) {
                        if (args[0].equalsIgnoreCase(alias)) {
                            match = true;
                            break;
                        }
                    }
                }

                if (match) {
                    if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
                        player.sendMessage(plugin.getMessageFileManager().getLangMessage("no-permission"));
                        return true;
                    }
                    subCommand.perform(player, args);
                    return true;
                }
            }
        }
        
        // Show help command if no arguments match or zero arguments
        for (SubCommand subCommand : subcommands) {
            if (subCommand.getName().equalsIgnoreCase("help")) {
                subCommand.perform(player, args);
                return true;
            }
        }
        
        // Fallback if no help command registered
        List<String> helpMessages = plugin.getMessageFileManager().getLangMessageList("commands-help");
        for (String helpMessage : helpMessages) {
            player.sendMessage(helpMessage);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        if (!player.hasPermission(plugin.getConfig().getString("permissions.general", "antiescape.admin"))) {
            return completions;
        }

        if (args.length == 1) {
            for (SubCommand subCommand : subcommands) {
                if (subCommand.getPermission() == null || player.hasPermission(subCommand.getPermission())) {
                    if (subCommand.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(subCommand.getName());
                    }
                    List<String> aliases = plugin.getMessageFileManager().getLangMessageListNoPrefix("command-aliases." + subCommand.getName());
                    if (aliases != null) {
                        for (String cmdAlias : aliases) {
                            if (cmdAlias.toLowerCase().startsWith(args[0].toLowerCase()) && !completions.contains(cmdAlias)) {
                                completions.add(cmdAlias);
                            }
                        }
                    }
                }
            }
        } else if (args.length > 1) {
            for (SubCommand subCommand : subcommands) {
                List<String> aliases = plugin.getMessageFileManager().getLangMessageListNoPrefix("command-aliases." + subCommand.getName());
                if (aliases == null) aliases = new ArrayList<>();
                boolean match = args[0].equalsIgnoreCase(subCommand.getName());
                if (!match) {
                    for (String cmdAlias : aliases) {
                        if (args[0].equalsIgnoreCase(cmdAlias)) {
                            match = true;
                            break;
                        }
                    }
                }

                if (match) {
                    if (subCommand.getPermission() == null || player.hasPermission(subCommand.getPermission())) {
                        List<String> subArgs = subCommand.getSubcommandArguments(player, args);
                        if (subArgs != null) {
                            completions.addAll(subArgs);
                        }
                    }
                }
            }
        }

        return completions;
    }
}
