package org.xorcun.antiescape.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xorcun.antiescape.AntiEscape;

import java.io.File;
import java.util.List;
import java.util.Map;

public class WebhookFileManager {
    private final AntiEscape plugin;
    private File webhookFile;
    private FileConfiguration webhookConfig;

    public WebhookFileManager(AntiEscape plugin) {
        this.plugin = plugin;
        loadWebhookFile();
    }

    private void loadWebhookFile() {
        webhookFile = new File(plugin.getDataFolder(), "webhook.yml");
        
        if (!webhookFile.exists()) {
            plugin.saveResource("webhook.yml", false);
        }
        
        webhookConfig = YamlConfiguration.loadConfiguration(webhookFile);
    }

    public void reloadWebhookFile() {
        webhookConfig = YamlConfiguration.loadConfiguration(webhookFile);
    }


    // Discord ayarları
    public boolean isDiscordEnabled() {
        return webhookConfig.getBoolean("discord.enabled", false);
    }

    public String getWebhookUrl() {
        return webhookConfig.getString("discord.webhook-url", "");
    }

    public String getBotName() {
        return webhookConfig.getString("discord.bot-name", "AntiEscape Bot");
    }

    public String getBotAvatar() {
        return webhookConfig.getString("discord.bot-avatar", "");
    }

    // Genel embed ayarları
    public String getDefaultEmbedColor() {
        return webhookConfig.getString("embeds.general.color", "#FF6B6B");
    }

    public boolean isTimestampEnabled() {
        return webhookConfig.getBoolean("embeds.general.timestamp", true);
    }

    public boolean isFooterEnabled() {
        return webhookConfig.getBoolean("embeds.general.footer", true);
    }

    public boolean isThumbnailEnabled() {
        return webhookConfig.getBoolean("embeds.general.thumbnail", true);
    }

    // Olay ayarları
    public boolean isControlEnabled() {
        return webhookConfig.getBoolean("discord.events.control", true);
    }

    public boolean isMovementEnabled() {
        return webhookConfig.getBoolean("discord.events.movement", false);
    }

    public boolean isCommandsEnabled() {
        return webhookConfig.getBoolean("discord.events.commands", false);
    }

    public boolean isChatEnabled() {
        return webhookConfig.getBoolean("discord.events.chat", false);
    }

    public boolean isDamageEnabled() {
        return webhookConfig.getBoolean("discord.events.damage", false);
    }

    public boolean isItemsEnabled() {
        return webhookConfig.getBoolean("discord.events.items", false);
    }

    public boolean isSecurityEnabled() {
        return webhookConfig.getBoolean("discord.events.security", true);
    }

    // Yeni embed formatı metodları
    public String getEmbedColor(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".color";
        return webhookConfig.getString(path, getDefaultEmbedColor());
    }

    public String getEmbedTitle(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".title";
        return webhookConfig.getString(path, "Event");
    }

    public String getEmbedDescription(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".description";
        return webhookConfig.getString(path, "No description");
    }

    public String getEmbedThumbnail(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".thumbnail";
        return webhookConfig.getString(path, "");
    }

    public List<Map<?, ?>> getEmbedFields(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".fields";
        return webhookConfig.getMapList(path);
    }

    public String getEmbedFooterText(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".footer.text";
        return webhookConfig.getString(path, "AntiEscape System");
    }

    public String getEmbedFooterIcon(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".footer.icon_url";
        return webhookConfig.getString(path, "");
    }

    public String getEmbedContent(String eventType, String subType) {
        String path = "embeds." + eventType;
        if (subType != null) {
            path += "." + subType;
        }
        path += ".content";
        return webhookConfig.getString(path, "");
    }


    // Mesaj formatını değiştirme yardımcı metodu
    public String formatMessage(String message, String... replacements) {
        String formattedMessage = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formattedMessage = formattedMessage.replace(replacements[i], replacements[i + 1]);
            }
        }
        return formattedMessage;
    }
} 