package org.xorcun.antiescape.commands.subcommands;

import org.bukkit.entity.Player;
import org.xorcun.antiescape.AntiEscape;
import org.xorcun.antiescape.commands.SubCommand;

import java.util.List;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(AntiEscape plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the plugin configurations";
    }

    @Override
    public String getSyntax() {
        return "/control reload";
    }

    @Override
    public String getPermission() {
        return plugin.getConfig().getString("permissions.general", "antiescape.admin");
    }

    @Override
    public void perform(Player player, String[] args) {
        plugin.reloadConfig();
        plugin.getMessageFileManager().loadLangFile();

        // Wait for next steps - loadWhitelist missing? Let's verify
        // advancedSecurityManager soon
        // plugin.getAdvancedSecurityManager().loadWhitelist();
        player.sendMessage(plugin.getMessageFileManager().getLangMessage("config-reloaded"));
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
