package org.xorcun.antiescape.FileManager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class MessageFileManager {

    private final ConfigFileManager configFileManager;
    private FileConfiguration langConfig;
    private String currentLanguage;

    // Constructor'da ConfigFileManager'dan referans al
    public MessageFileManager(ConfigFileManager configFileManager) {
        this.configFileManager = configFileManager;
    }

    private String normalizeLanguage(String lang) {
        if (lang == null) return "en";
        return lang.trim().toLowerCase(Locale.ROOT);
    }

    // Dil dosyasını kaydetme
    public void saveLangFile() {
        String language = normalizeLanguage(configFileManager.getConfigMessage("language"));
        if (language.isEmpty()) language = "en"; // Varsayılan dil

        this.currentLanguage = language;

        // lang klasörünü oluştur
        File langFolder = new File(configFileManager.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Dil dosyasını kaydet
        File langFile = new File(langFolder, language + ".yml");
        if (!langFile.exists()) {
            try {
                configFileManager.saveResource("lang/" + language + ".yml", false);
            } catch (IllegalArgumentException ex) {
                // Kaynak yoksa en.yml'i kaydet
                try {
                    configFileManager.saveResource("lang/en.yml", false);
                } catch (IllegalArgumentException ignored) {
                    // hiçbir şey yapma; en.yml de yoksa sessizce geç
                }
            }
        }
    }

    // Lang dosyasını yükleme
    public void loadLangFile() {
        String language = normalizeLanguage(configFileManager.getConfigMessage("language"));
        if (language.isEmpty()) language = "en"; // Varsayılan dil

        this.currentLanguage = language;

        File desired = new File(configFileManager.getDataFolder(), "lang/" + language + ".yml");
        if (!desired.exists()) {
            // Eksikse oluşturmayı dene ve sonra tekrar kontrol et
            saveLangFile();
        }

        if (desired.exists()) {
            langConfig = YamlConfiguration.loadConfiguration(desired);
        } else {
            // Eğer dosya yoksa, varsayılan dil dosyasını yükle
            File fallback = new File(configFileManager.getDataFolder(), "lang/en.yml");
            langConfig = YamlConfiguration.loadConfiguration(fallback);
            this.currentLanguage = "en";
        }
    }

    // Lang'den mesaj alma metodu
    public String getLangMessage(String key) {
        String prefix = configFileManager.getConfigMessage("prefix");
        String message = langConfig.getString(key);

        if (prefix == null) prefix = "&6ᴀɴᴛɪᴇѕᴄᴀᴘᴇ &8▸ &7";
        if (message == null) message = "Message not found: " + key + " (Language: " + currentLanguage + ")";

        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    // Prefix olmadan mesaj alma metodu
    public String getLangMessageNoPrefix(String key) {
        String message = langConfig.getString(key);
        if (message == null) message = "Message not found: " + key + " (Language: " + currentLanguage + ")";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Prefix olmadan liste mesajları alma metodu
    public List<String> getLangMessageListNoPrefix(String key) {
        List<String> messages = langConfig.getStringList(key);
        messages.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
        return messages;
    }

    public List<String> getLangMessageList(String key) {
        List<String> messages = langConfig.getStringList(key);
        messages.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
        return messages;
    }

    // Mevcut dili alma
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
