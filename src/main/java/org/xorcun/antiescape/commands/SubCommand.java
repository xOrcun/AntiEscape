package org.xorcun.antiescape.commands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;

import java.util.List;

public abstract class SubCommand {
    
    protected final AntiEscape plugin;

    public SubCommand(AntiEscape plugin) {
        this.plugin = plugin;
    }

    public abstract String getName();
    
    public abstract String getDescription();

    public abstract String getSyntax();
    
    public abstract String getPermission();

    public abstract void perform(Player player, String[] args);
    
    public abstract List<String> getSubcommandArguments(Player player, String[] args);

    protected boolean isArg(String input, String key) {
        if (input == null) return false;
        if (input.equalsIgnoreCase(key)) return true;
        List<String> aliases = plugin.getMessageFileManager().getLangMessageListNoPrefix("sub-arguments." + key);
        if (aliases != null) {
            for (String alias : aliases) {
                if (input.equalsIgnoreCase(alias)) return true;
            }
        }
        return false;
    }

    protected List<String> getTabCompletions(String input, String... internalKeys) {
        List<String> completions = new java.util.ArrayList<>();
        for (String key : internalKeys) {
            // Check the internal key itself (e.g., "add")
            if (key.toLowerCase().startsWith(input.toLowerCase())) {
                completions.add(key.toLowerCase());
            }
            // Check localized aliases
            List<String> aliases = plugin.getMessageFileManager().getLangMessageListNoPrefix("sub-arguments." + key);
            if (aliases != null) {
                for (String alias : aliases) {
                    if (alias.toLowerCase().startsWith(input.toLowerCase()) && !completions.contains(alias.toLowerCase())) {
                        completions.add(alias.toLowerCase());
                    }
                }
            }
        }
        return completions;
    }
}
