package com.duckslavi.aiguard.config;

import com.duckslavi.aiguard.AIGuard;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Configuration Manager - Responsible for loading and saving plugin settings
 */
public class ConfigManager {

    private final AIGuard plugin;
    private FileConfiguration config;

    // Default settings
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.75;
    private static final boolean DEFAULT_BUNGEE_ENABLED = false;
    private static final String DEFAULT_ALERT_MESSAGE = "§c[AIGuard] §e{player} §fwrote a suspicious message: §7{message} §f| Banned Word: §c{banned_word} §f| Similarity: §6{similarity}%";

    public ConfigManager(AIGuard plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads configuration from file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Ensure all required values exist
        ensureDefaults();

        plugin.getLogger().info("Configuration loaded successfully!");
        plugin.getLogger().info("Similarity threshold: " + getSimilarityThreshold());
        plugin.getLogger().info("Banned words count: " + getBannedWords().size());
    }

    /**
     * Ensures all required values exist in configuration
     */
    private void ensureDefaults() {
        boolean changed = false;

        if (!config.contains("similarity_threshold")) {
            config.set("similarity_threshold", DEFAULT_SIMILARITY_THRESHOLD);
            changed = true;
        }

        if (!config.contains("bungee.enabled")) {
            config.set("bungee.enabled", DEFAULT_BUNGEE_ENABLED);
            changed = true;
        }

        if (!config.contains("bungee.channel")) {
            config.set("bungee.channel", "aiguard:alerts");
            changed = true;
        }

        if (!config.contains("messages.alert")) {
            config.set("messages.alert", DEFAULT_ALERT_MESSAGE);
            changed = true;
        }

        if (!config.contains("async_processing")) {
            config.set("async_processing", true);
            changed = true;
        }

        if (!config.contains("banned_words")) {
            config.set("banned_words", List.of("idiot", "dumb", "stupid"));
            changed = true;
        }

        if (!config.contains("advanced.check_private_messages")) {
            config.set("advanced.check_private_messages", false);
            changed = true;
        }

        if (!config.contains("advanced.log_suspicious_messages")) {
            config.set("advanced.log_suspicious_messages", true);
            changed = true;
        }

        if (!config.contains("advanced.max_alerts_per_player_per_day")) {
            config.set("advanced.max_alerts_per_player_per_day", 10);
            changed = true;
        }

        // GUI settings for ticket and history (editable in config.yml)
        if (!config.contains("gui.ticket.size")) {
            config.set("gui.ticket.size", 27);
            changed = true;
        }
        if (!config.contains("gui.ticket.info_slot")) {
            config.set("gui.ticket.info_slot", 11);
            changed = true;
        }
        if (!config.contains("gui.ticket.claim_slot")) {
            config.set("gui.ticket.claim_slot", 15);
            changed = true;
        }
        if (!config.contains("gui.ticket.close_slot")) {
            config.set("gui.ticket.close_slot", 26);
            changed = true;
        }
        if (!config.contains("gui.ticket.back_slot")) {
            config.set("gui.ticket.back_slot", 18);
            changed = true;
        }
        if (!config.contains("gui.ticket.teleport_server_enabled")) {
            config.set("gui.ticket.teleport_server_enabled", true);
            changed = true;
        }
        if (!config.contains("gui.ticket.teleport_server_slot")) {
            config.set("gui.ticket.teleport_server_slot", 12);
            changed = true;
        }
        if (!config.contains("gui.ticket.teleport_world_enabled")) {
            config.set("gui.ticket.teleport_world_enabled", true);
            changed = true;
        }
        if (!config.contains("gui.ticket.teleport_world_slot")) {
            config.set("gui.ticket.teleport_world_slot", 14);
            changed = true;
        }

        if (!config.contains("gui.history.size")) {
            config.set("gui.history.size", 54);
            changed = true;
        }
        if (!config.contains("gui.history.page_size")) {
            config.set("gui.history.page_size", 45);
            changed = true;
        }
        if (!config.contains("gui.history.back_slot")) {
            config.set("gui.history.back_slot", 49);
            changed = true;
        }
        if (!config.contains("gui.history.prev_slot")) {
            config.set("gui.history.prev_slot", 45);
            changed = true;
        }
        if (!config.contains("gui.history.next_slot")) {
            config.set("gui.history.next_slot", 53);
            changed = true;
        }

        if (changed) {
            plugin.saveConfig();
        }
    }

    /**
     * Returns the similarity threshold required to detect a banned word
     */
    public double getSimilarityThreshold() {
        return config.getDouble("similarity_threshold", DEFAULT_SIMILARITY_THRESHOLD);
    }

    /**
     * Returns the list of banned words
     */
    public List<String> getBannedWords() {
        return config.getStringList("banned_words");
    }

    /**
     * Checks if BungeeCord mode is enabled
     */
    public boolean isBungeeCordEnabled() {
        return config.getBoolean("bungee.enabled", DEFAULT_BUNGEE_ENABLED);
    }

    /**
     * Returns the BungeeCord channel name
     */
    public String getBungeeChannel() {
        return config.getString("bungee.channel", "aiguard:alerts");
    }

    /**
     * Returns the alert message
     */
    public String getAlertMessage() {
        return config.getString("messages.alert", DEFAULT_ALERT_MESSAGE);
    }

    /**
     * Checks if async processing is enabled
     */
    public boolean isAsyncProcessingEnabled() {
        return config.getBoolean("async_processing", true);
    }

    public int getMaxAlertsPerPlayerPerDay() {
        return config.getInt("advanced.max_alerts_per_player_per_day", 10);
    }

    public boolean isLogSuspiciousMessagesEnabled() {
        return config.getBoolean("advanced.log_suspicious_messages", true);
    }

    public boolean isCheckPrivateMessagesEnabled() {
        return config.getBoolean("advanced.check_private_messages", false);
    }

    /**
     * Returns the raw configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Checks if the given world is enabled for message scanning
     */
    public boolean isWorldEnabled(String worldName) {
        List<String> enabledWorlds = config.getStringList("worlds.enabled");
        if (enabledWorlds == null || enabledWorlds.isEmpty()) {
            return true;
        }
        return enabledWorlds.contains(worldName);
    }
}
