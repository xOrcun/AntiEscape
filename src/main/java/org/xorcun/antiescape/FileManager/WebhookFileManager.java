package org.xorcun.antiescape.FileManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.xorcun.antiescape.AntiEscape;

import java.io.File;
import java.io.IOException;
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

    public void saveWebhookFile() {
        try {
            webhookConfig.save(webhookFile);
        } catch (IOException e) {
            plugin.debug("Webhook file could not be saved: " + e.getMessage());
        }
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

    // Eski sistem metodları (geriye uyumluluk için)
    public String getControlStartTitle() {
        return webhookConfig.getString("messages.control.start.title", "🔒 Control Started");
    }

    public String getControlStartDescription() {
        return webhookConfig.getString("messages.control.start.description", "**%target%** was put under control by **%controller%**");
    }

    public String getControlEndTitle() {
        return webhookConfig.getString("messages.control.end.title", "✅ Released");
    }

    public String getControlEndDescription() {
        return webhookConfig.getString("messages.control.end.description", "**%target%** was released by **%controller%**");
    }

    public String getControlBanTitle() {
        return webhookConfig.getString("messages.control.ban.title", "🔨 Banned");
    }

    public String getControlBanDescription() {
        return webhookConfig.getString("messages.control.ban.description", "**%target%** was banned by **%controller%**");
    }

    public String getControlEscapeTitle() {
        return webhookConfig.getString("messages.control.escape.title", "🚨 Control Escape");
    }

    public String getControlEscapeDescription() {
        return webhookConfig.getString("messages.control.escape.description", "**%player%** escaped control from **%controller%**");
    }

    public String getMovementTitle() {
        return webhookConfig.getString("messages.movement.title", "🚶 Player Movement");
    }

    public String getMovementDescription() {
        return webhookConfig.getString("messages.movement.description", "**%player%** moved from `%from%` to `%to%`");
    }

    public String getCommandTitle() {
        return webhookConfig.getString("messages.commands.title", "⌨️ Command Executed");
    }

    public String getCommandDescription() {
        return webhookConfig.getString("messages.commands.description", "**%player%** executed: `%command%`");
    }

    public String getChatTitle() {
        return webhookConfig.getString("messages.chat.title", "💬 Chat Message");
    }

    public String getChatDescription() {
        return webhookConfig.getString("messages.chat.description", "**%player%**: %message%");
    }

    public String getDamageTitle() {
        return webhookConfig.getString("messages.damage.title", "⚔️ Damage Event");
    }

    public String getDamageDescription() {
        return webhookConfig.getString("messages.damage.description", "**%attacker%** attacked **%victim%** for **%damage%** damage");
    }

    public String getItemTitle() {
        return webhookConfig.getString("messages.items.title", "📦 Item Drop");
    }

    public String getItemDescription() {
        return webhookConfig.getString("messages.items.description", "**%player%** dropped: **%item%**");
    }

    public String getTestTitle() {
        return webhookConfig.getString("messages.test.title", "✅ Discord Webhook Test");
    }

    public String getTestDescription() {
        return webhookConfig.getString("messages.test.description", "Discord webhook system is working correctly!");
    }

    public String getTestContent() {
        return webhookConfig.getString("messages.test.content", "🎉 **Discord webhook test successful!**");
    }

    // Footer ayarları (eski sistem)
    public String getFooterText() {
        return webhookConfig.getString("footer.text", "AntiEscape Control System");
    }

    public String getFooterIconUrl() {
        return webhookConfig.getString("footer.icon_url", "");
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