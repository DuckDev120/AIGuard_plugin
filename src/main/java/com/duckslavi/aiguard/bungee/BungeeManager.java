package com.duckslavi.aiguard.bungee;

import com.duckslavi.aiguard.AIGuard;
import com.duckslavi.aiguard.tickets.Ticket;
import com.duckslavi.aiguard.utils.FuzzyMatcher;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collection;

/**
 * BungeeCord Communication Manager - Responsible for sending and receiving
 * alerts between servers
 */
public class BungeeManager implements PluginMessageListener {

    private final AIGuard plugin;
    private final String incomingChannel;
    private static final String BUNGEE_CHANNEL = "BungeeCord";

    public BungeeManager(AIGuard plugin) {
        this.plugin = plugin;
        this.incomingChannel = plugin.getConfigManager().getBungeeChannel();
    }

    /**
     * Initializes the BungeeCord manager
     */
    public void initialize() {
        // Register channel for outgoing messages to BungeeCord
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BUNGEE_CHANNEL);

        // Register channel for incoming messages from the network (custom channel)
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, incomingChannel, this);

        plugin.getLogger().info("BungeeCord Manager initialized successfully! Incoming channel: " + incomingChannel
                + ", Outgoing channel: " + BUNGEE_CHANNEL);
    }

    /**
     * Shuts down the BungeeCord manager
     */
    public void shutdown() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, BUNGEE_CHANNEL);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, incomingChannel);

        plugin.getLogger().info("BungeeCord Manager closed successfully!");
    }

    /**
     * Sends an alert to all servers in the BungeeCord network
     */
    public void sendAlert(String playerName, String message, FuzzyMatcher.MatchResult matchResult) {
        // Build message content for other servers
        ByteArrayDataOutput msgOut = ByteStreams.newDataOutput();
        msgOut.writeUTF("ALERT");
        msgOut.writeUTF(playerName);
        msgOut.writeUTF(message);
        msgOut.writeUTF(matchResult.getBannedWord());
        msgOut.writeInt(matchResult.getSimilarityPercentage());
        msgOut.writeUTF(getServerName());

        byte[] msgBytes = msgOut.toByteArray();

        // Build message to BungeeCord with Forward command
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(incomingChannel);
        out.writeShort(msgBytes.length);
        out.write(msgBytes);

        // Send message via any player (required for BungeeCord)
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (!onlinePlayers.isEmpty()) {
            Player sender = onlinePlayers.iterator().next();
            sender.sendPluginMessage(plugin, BUNGEE_CHANNEL, out.toByteArray());
        }
    }

    /**
     * Sends a player to another server in the BungeeCord network
     */
    public void sendPlayerToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, BUNGEE_CHANNEL, out.toByteArray());
    }

    /**
     * Handles incoming messages from BungeeCord
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(incomingChannel)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String action = in.readUTF();

        if ("ALERT".equals(action)) {
            handleIncomingAlert(in);
        }
    }

    /**
     * Handles incoming alert from another server
     */
    private void handleIncomingAlert(ByteArrayDataInput in) {
        try {
            String playerName = in.readUTF();
            String originalMessage = in.readUTF();
            String bannedWord = in.readUTF();
            int similarityPercentage = in.readInt();
            String sourceServer = in.readUTF();

            Ticket ticket = plugin.getTicketManager().createTicket(
                    playerName,
                    null,
                    sourceServer,
                    null,
                    originalMessage,
                    bannedWord,
                    similarityPercentage);

            String baseText = plugin.getMessageManager().getMessage("staff.click_alert");
            String hoverText = plugin.getMessageManager().getMessage("staff.click_alert_hover");

            TextComponent component = new TextComponent(TextComponent.fromLegacyText(baseText));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aiwatch ticket " + ticket.getId()));
            component.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("aiwatch.alert")
                        && !plugin.isStaffAlertsMuted(onlinePlayer.getUniqueId())) {
                    onlinePlayer.spigot().sendMessage(component);
                }
            }

            plugin.getLogger().warning("Network alert: " + playerName + " [" + sourceServer + "] -> " +
                    originalMessage + " | Banned word: " + bannedWord + " | Similarity: " + similarityPercentage + "%");

        } catch (Exception e) {
            plugin.getLogger().severe("Error processing BungeeCord alert: " + e.getMessage());
        }
    }

    /**
     * Returns the current server name
     */
    private String getServerName() {
        // Try to get server name from config
        String serverName = plugin.getConfigManager().getConfig().getString("server_name");
        if (serverName != null && !serverName.isEmpty()) {
            return serverName;
        }

        // If not defined, use default name
        return "Server-" + System.currentTimeMillis() % 1000;
    }
}
