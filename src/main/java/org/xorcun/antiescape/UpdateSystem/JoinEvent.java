package org.xorcun.antiescape.UpdateSystem;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.xorcun.antiescape.FileManager.MessageFileManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class JoinEvent implements Listener {

    private final Plugin plugin;
    private MessageFileManager messageFileManager;

    public JoinEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission(Objects.requireNonNull(plugin.getConfig().getString("permissions.update-notify")))) {
            checkForUpdatesPlayer(player);
        }
    }

    public void checkForUpdatesPlayer(Player player) {
        boolean checkUpdate = plugin.getConfig().getBoolean("check-update");

        if (!checkUpdate) {
            return;
        }

        // Asenkron görevle güncelleme kontrolü yap
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // URL'den güncel versiyonu al
                URL url = new URL("https://api.orcunozturk.com/antiescape/version.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // JSON verisini işleme
                    JsonObject jsonResponse = (JsonObject) new JsonParser().parse(response.toString());
                    String latestVersion = jsonResponse.get("version").getAsString();
                    String downloadURL = jsonResponse.get("download_url").getAsString();

                    // Mevcut versiyon ile karşılaştır
                    if (!plugin.getDescription().getVersion().equals(latestVersion)) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(String.format("§8[§eAntiEscape§8] §7An update for AntiEscape is available. You are running version §c%s§7, the latest version is §a%s.", plugin.getConfig().getString("version"), latestVersion));
                            player.sendMessage(String.format("§8[§eAntiEscape§8] §7Update at §b%s", downloadURL));
                        });
                    }
                }
            } catch (Exception e) {
                player.sendMessage("§8[§eAntiEscape WARN§8] Update system failed, check the console.");
                Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape WARN§8] §fAn error occurred while checking for updates: §c" + e.getMessage());
            }
        });
    }
}
