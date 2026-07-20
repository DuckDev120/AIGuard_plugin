package com.duckslavi.aiguard.messages;

import com.duckslavi.aiguard.AIGuard;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Message Manager - Responsible for loading and accessing messages.yml
 */
public class MessageManager {

    private final AIGuard plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(AIGuard plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads (or creates) the messages.yml file
     */
    public void loadMessages() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Returns a message by path, including color support (& -> §)
     */
    public String getMessage(String path) {
        String raw = messagesConfig != null ? messagesConfig.getString(path, path) : path;
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    /**
     * Returns the raw message configuration (for internal use)
     */
    public FileConfiguration getRawConfig() {
        return messagesConfig;
    }

    /**
     * Updates a message in memory (for use with set command)
     */
    public void setMessage(String path, String value) {
        if (messagesConfig == null)
            return;
        messagesConfig.set(path, value);
    }

    /**
     * Saves the message file to disk
     */
    public void saveMessages() {
        if (messagesConfig == null || messagesFile == null)
            return;
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving messages.yml: " + e.getMessage());
        }
    }
}
