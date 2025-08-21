package org.xorcun.antiescape;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xorcun.antiescape.FileManager.WebhookFileManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DiscordManager {
    private final AntiEscape plugin;
    private final WebhookFileManager webhookFileManager;

    public DiscordManager(AntiEscape plugin) {
        this.plugin = plugin;
        this.webhookFileManager = new WebhookFileManager(plugin);
    }

    public boolean isDiscordEnabled() {
        return webhookFileManager.isDiscordEnabled();
    }

    public String getWebhookUrl() {
        return webhookFileManager.getWebhookUrl();
    }

    public void reloadWebhookFile() {
        webhookFileManager.reloadWebhookFile();
    }

    // Asenkron webhook gönderme metodu
    private void sendWebhookAsync(JSONObject payload) {
        if (!isDiscordEnabled() || getWebhookUrl().equals("YOUR_WEBHOOK_URL_HERE")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(getWebhookUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "AntiEscape-Plugin");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);

                // JSON payload'ı gönder
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toJSONString().getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    plugin.debug("Discord webhook sent successfully");
                } else {
                    plugin.debug("Discord webhook failed with response code: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                plugin.debug("Discord webhook error: " + e.getMessage());
            }
        });
    }

    // Kontrol başlatma webhook'u
    public void sendControlStart(Player controller, Player target) {
        if (!webhookFileManager.isControlEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("control", "start", 
            "%target%", target.getName(), 
            "%controller%", controller.getName());

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("control", "start");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%target%", target.getName(), 
                "%controller%", controller.getName());
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Kontrol bitirme webhook'u
    public void sendControlEnd(Player controller, Player target, boolean banned) {
        if (!webhookFileManager.isControlEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        String subType = banned ? "ban" : "end";
        
        // Embed oluştur
        JSONObject embed = createEmbed("control", subType, 
            "%target%", target.getName(), 
            "%controller%", controller.getName());

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("control", subType);
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%target%", target.getName(), 
                "%controller%", controller.getName());
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Kontrol kaçış webhook'u
    public void sendControlEscape(Player player, String controllerName) {
        if (!webhookFileManager.isControlEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("control", "escape", 
            "%player%", player.getName(), 
            "%controller%", controllerName);

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("control", "escape");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%controller%", controllerName);
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Hareket webhook'u
    public void sendMoveEvent(Player player, org.bukkit.Location from, org.bukkit.Location to) {
        if (!webhookFileManager.isMovementEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        String fromStr = formatLocation(from);
        String toStr = formatLocation(to);

        // Embed oluştur
        JSONObject embed = createEmbed("movement", "", 
            "%player%", player.getName(), 
            "%from%", fromStr, 
            "%to%", toStr);

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("movement", "");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%from%", fromStr, 
                "%to%", toStr);
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Komut webhook'u
    public void sendCommandEvent(Player player, String command) {
        if (!webhookFileManager.isCommandsEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("commands", "", 
            "%player%", player.getName(), 
            "%command%", command);

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("commands", "");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%command%", command);
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Chat webhook'u
    public void sendChatEvent(Player player, String message) {
        if (!webhookFileManager.isChatEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("chat", "", 
            "%player%", player.getName(), 
            "%message%", message);

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("chat", "");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%message%", message);
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Hasar webhook'u
    public void sendDamageEvent(Player attacker, Player victim, double damage) {
        if (!webhookFileManager.isDamageEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("damage", "", 
            "%attacker%", attacker.getName(), 
            "%victim%", victim.getName(), 
            "%damage%", String.valueOf(damage));

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("damage", "");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%attacker%", attacker.getName(), 
                "%victim%", victim.getName(), 
                "%damage%", String.valueOf(damage));
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Eşya webhook'u
    public void sendItemEvent(Player player, String itemName, int amount, String action) {
        if (!webhookFileManager.isItemsEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("item", action, 
            "%player%", player.getName(), 
            "%item%", itemName,
            "%amount%", String.valueOf(amount));

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("item", action);
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%item%", itemName,
                "%amount%", String.valueOf(amount));
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Güvenlik ban webhook'u
    public void sendSecurityBan(Player player, String reason) {
        if (!webhookFileManager.isSecurityEnabled()) return;

        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("security", "ban", 
            "%player%", player.getName(), 
            "%reason%", reason);

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("security", "ban");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%player%", player.getName(), 
                "%reason%", reason);
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    // Test webhook'u
    public void sendTestMessage() {
        JSONObject payload = new JSONObject();
        
        // Bot bilgileri
        if (!webhookFileManager.getBotName().isEmpty()) {
            payload.put("username", webhookFileManager.getBotName());
        }
        if (!webhookFileManager.getBotAvatar().isEmpty()) {
            payload.put("avatar_url", webhookFileManager.getBotAvatar());
        }

        // Embed oluştur
        JSONObject embed = createEmbed("test", "", 
            "%version%", plugin.getDescription().getVersion());

        // Content varsa ekle
        String content = webhookFileManager.getEmbedContent("test", "");
        if (content != null && !content.isEmpty()) {
            content = webhookFileManager.formatMessage(content, 
                "%version%", plugin.getDescription().getVersion());
            payload.put("content", content);
        }

        JSONArray embeds = new JSONArray();
        embeds.add(embed);
        payload.put("embeds", embeds);

        sendWebhookAsync(payload);
    }

    private JSONObject createEmbed(String eventType, String subType, String... replacements) {
        JSONObject embed = new JSONObject();
        
        String title = webhookFileManager.getEmbedTitle(eventType, subType);
        embed.put("title", title);
        
        String description = webhookFileManager.getEmbedDescription(eventType, subType);
        description = webhookFileManager.formatMessage(description, replacements);
        embed.put("description", description);
        
        String color = webhookFileManager.getEmbedColor(eventType, subType);
        embed.put("color", Integer.parseInt(color.substring(1), 16));
        
        if (webhookFileManager.isThumbnailEnabled()) {
            String thumbnail = webhookFileManager.getEmbedThumbnail(eventType, subType);
            if (!thumbnail.isEmpty()) {
                JSONObject thumbnailObj = new JSONObject();
                thumbnailObj.put("url", thumbnail);
                embed.put("thumbnail", thumbnailObj);
            }
        }
        
        List<Map<?, ?>> fields = webhookFileManager.getEmbedFields(eventType, subType);
        if (fields != null && !fields.isEmpty()) {
            JSONArray fieldsArray = new JSONArray();
            for (Map<?, ?> field : fields) {
                JSONObject fieldObj = new JSONObject();
                fieldObj.put("name", field.get("name"));
                String value = (String) field.get("value");
                value = webhookFileManager.formatMessage(value, replacements);
                fieldObj.put("value", value);
                fieldObj.put("inline", field.get("inline"));
                fieldsArray.add(fieldObj);
            }
            embed.put("fields", fieldsArray);
        }
        
        if (webhookFileManager.isFooterEnabled()) {
            JSONObject footer = new JSONObject();
            footer.put("text", webhookFileManager.getEmbedFooterText(eventType, subType));
            String footerIcon = webhookFileManager.getEmbedFooterIcon(eventType, subType);
            if (!footerIcon.isEmpty()) {
                footer.put("icon_url", footerIcon);
            }
            embed.put("footer", footer);
        }
        
        if (webhookFileManager.isTimestampEnabled()) {
            embed.put("timestamp", new Date().toInstant().toString());
        }
        
        return embed;
    }

    private String formatLocation(org.bukkit.Location location) {
        if (location == null) return "null";
        return String.format("%s,%.2f,%.2f,%.2f", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ());
    }
} 