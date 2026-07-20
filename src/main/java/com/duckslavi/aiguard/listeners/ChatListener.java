package com.duckslavi.aiguard.listeners;

import com.duckslavi.aiguard.AIGuard;
import com.duckslavi.aiguard.utils.FuzzyMatcher;
import com.duckslavi.aiguard.tickets.Ticket;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat event listener - checks messages against the banned words list
 */
public class ChatListener implements Listener {

    private final AIGuard plugin;
    private final Map<UUID, Integer> playerDailyAlerts = new ConcurrentHashMap<>();
    private LocalDate alertDay = LocalDate.now();

    public ChatListener(AIGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if this world is enabled for filtering (Multiverse/Core support)
        if (!plugin.getConfigManager().isWorldEnabled(player.getWorld().getName())) {
            return;
        }

        // Check if async processing is enabled
        if (plugin.getConfigManager().isAsyncProcessingEnabled()) {
            // Async processing
            processMessageAsync(player, message);
        } else {
            // Synchronous processing
            processMessage(player, message);
        }
    }

    private boolean incrementAlertCount(Player player) {
        int maxPerDay = plugin.getConfigManager().getMaxAlertsPerPlayerPerDay();
        if (maxPerDay <= 0) {
            return true;
        }

        LocalDate today = LocalDate.now();
        if (!today.equals(alertDay)) {
            alertDay = today;
            playerDailyAlerts.clear();
        }

        UUID uuid = player.getUniqueId();
        int current = playerDailyAlerts.getOrDefault(uuid, 0);
        if (current >= maxPerDay) {
            return false;
        }

        playerDailyAlerts.put(uuid, current + 1);
        return true;
    }

    /**
     * Process message asynchronously
     */
    private void processMessageAsync(Player player, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                processMessage(player, message);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Process message and check against banned words
     */
    private void processMessage(Player player, String message) {
        List<String> bannedWords = plugin.getConfigManager().getBannedWords();
        double threshold = plugin.getConfigManager().getSimilarityThreshold();

        // Check every banned word
        for (String bannedWord : bannedWords) {
            FuzzyMatcher.MatchResult result = FuzzyMatcher.checkSimilarity(message, bannedWord, threshold);

            if (result.isMatch()) {
                // Match found - send alert
                sendAlert(player, message, result);

                // Break loop after first match
                break;
            }
        }
    }

    /**
     * Sends alert to all players with permission
     */
    private void sendAlert(Player player, String originalMessage, FuzzyMatcher.MatchResult matchResult) {
        if (!incrementAlertCount(player)) {
            return;
        }

        String serverName = plugin.getConfigManager().getConfig().getString("server_name");
        if (serverName == null || serverName.isEmpty()) {
            serverName = plugin.getServer().getName();
        }

        Ticket ticket = plugin.getTicketManager().createTicket(
                player.getName(),
                player.getUniqueId(),
                serverName,
                player.getWorld().getName(),
                originalMessage,
                matchResult.getBannedWord(),
                matchResult.getSimilarityPercentage());

        Ticket finalTicket = ticket;
        String finalServerName = serverName;

        new BukkitRunnable() {
            @Override
            public void run() {
                String baseText = plugin.getMessageManager().getMessage("staff.click_alert");
                String hoverText = plugin.getMessageManager().getMessage("staff.click_alert_hover");

                TextComponent component = new TextComponent(TextComponent.fromLegacyText(baseText));
                component.setClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aiwatch ticket " + finalTicket.getId()));
                component.setHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("aiwatch.alert")
                            && !plugin.isStaffAlertsMuted(onlinePlayer.getUniqueId())) {
                        onlinePlayer.spigot().sendMessage(component);
                    }
                }

                if (plugin.getConfigManager().isLogSuspiciousMessagesEnabled()) {
                    plugin.getLogger()
                            .warning("Suspicious message detected: " + player.getName() + " [" + finalServerName
                                    + "] -> " + originalMessage +
                                    " | Banned word: " + matchResult.getBannedWord() +
                                    " | Similarity: " + matchResult.getSimilarityPercentage() + "%");
                }

                if (plugin.getConfigManager().isBungeeCordEnabled() && plugin.getBungeeManager() != null) {
                    plugin.getBungeeManager().sendAlert(player.getName(), originalMessage, matchResult);
                }
            }
        }.runTask(plugin);
    }
}
