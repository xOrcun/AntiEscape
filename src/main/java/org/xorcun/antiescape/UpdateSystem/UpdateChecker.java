package org.xorcun.antiescape.UpdateSystem;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final Plugin plugin;

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        // Config'ten check-update değerini al
        boolean checkUpdate = plugin.getConfig().getBoolean("check-update");

        // Eğer check-update false ise güncelleme kontrolünü yapma
        if (!checkUpdate) {
            return;
        }

        // Asenkron olarak güncelleme kontrolü yap
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
                        Bukkit.getConsoleSender().sendMessage("§8§m--------------------§r §8[ §eAntiEscape §8] §8§m--------------------§r");
                        Bukkit.getConsoleSender().sendMessage("");
                        Bukkit.getConsoleSender().sendMessage("§fCurrent Version: §c" + plugin.getDescription().getVersion());
                        Bukkit.getConsoleSender().sendMessage("§fAvailable version: §a" + latestVersion);
                        Bukkit.getConsoleSender().sendMessage("§fDownload available at link: §b" + downloadURL);
                        Bukkit.getConsoleSender().sendMessage("");
                        Bukkit.getConsoleSender().sendMessage("§8§m--------------------§r §8[ §eAntiEscape §8] §8§m--------------------§r");
                    } else {
                        Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape§8] §fNo new version available");
                    }
                }
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§8[§eAntiEscape WARN§8] §fAn error occurred while checking for updates: §c" + e.getMessage());
            }
        });
    }
}
