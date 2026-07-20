package com.duckslavi.aiguard;

import com.duckslavi.aiguard.config.ConfigManager;
import com.duckslavi.aiguard.listeners.ChatListener;
import com.duckslavi.aiguard.bungee.BungeeManager;
import com.duckslavi.aiguard.messages.MessageManager;
import com.duckslavi.aiguard.tickets.TicketManager;
import com.duckslavi.aiguard.tickets.TicketManager.TicketStatusCounts;
import com.duckslavi.aiguard.tickets.TicketGuiListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * AIGuard - Chat monitoring plugin with banned word detection
 * Main class of the plugin
 */
public class AIGuard extends JavaPlugin {

    private static AIGuard instance;
    private ConfigManager configManager;
    private BungeeManager bungeeManager;
    private MessageManager messageManager;
    private TicketManager ticketManager;
    private final Set<UUID> mutedStaff = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Load messages
        messageManager = new MessageManager(this);
        messageManager.loadMessages();

        // Initialize ticket manager
        ticketManager = new TicketManager(this);

        // Register chat listener
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register ticket GUI listener
        getServer().getPluginManager().registerEvents(new TicketGuiListener(this, ticketManager), this);

        // Initialize BungeeCord if enabled
        if (configManager.isBungeeCordEnabled()) {
            bungeeManager = new BungeeManager(this);
            bungeeManager.initialize();
        }

        getLogger().info("AIGuard enabled successfully!");
        getLogger().info("BungeeCord mode: " + (configManager.isBungeeCordEnabled() ? "Enabled" : "Disabled"));
    }

    @Override
    public void onDisable() {
        if (bungeeManager != null) {
            bungeeManager.shutdown();
        }
        getLogger().info("AIGuard disabled successfully!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("aiguard")) {
            return false;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // help - Show help
        if (sub.equals("help")) {
            sendHelp(sender);
            return true;
        }

        // reload - Reload config and messages
        if (sub.equals("reload")) {
            if (!sender.hasPermission("aiwatch.admin")) {
                sender.sendMessage(messageManager.getMessage("messages.no_permission"));
                return true;
            }

            configManager.loadConfig();
            messageManager.loadMessages();
            sender.sendMessage("§a[AIGuard] Configuration and messages reloaded successfully!");
            return true;
        }

        // status - Show ticket status
        if (sub.equals("status")) {
            if (!sender.hasPermission("aiwatch.admin")) {
                sender.sendMessage(messageManager.getMessage("messages.no_permission"));
                return true;
            }

            TicketStatusCounts counts = ticketManager.getStatusCounts();
            sender.sendMessage(messageManager.getMessage("messages.status_header"));
            sender.sendMessage(messageManager.getMessage("messages.status_line_open").replace("{open}",
                    String.valueOf(counts.getOpen())));
            sender.sendMessage(messageManager.getMessage("messages.status_line_claimed").replace("{claimed}",
                    String.valueOf(counts.getClaimed())));
            sender.sendMessage(messageManager.getMessage("messages.status_line_closed").replace("{closed}",
                    String.valueOf(counts.getClosed())));
            return true;
        }

        // history - Open ticket history GUI
        if (sub.equals("history")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("messages.only_players"));
                return true;
            }
            if (!sender.hasPermission("aiwatch.admin")) {
                sender.sendMessage(messageManager.getMessage("messages.no_permission"));
                return true;
            }

            Player player = (Player) sender;
            new com.duckslavi.aiguard.tickets.HistoryGUI(this, ticketManager).open(player);
            return true;
        }

        // alerts - Toggle staff alerts
        if (sub.equals("alerts")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("messages.only_players"));
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("aiwatch.alert")) {
                sender.sendMessage(messageManager.getMessage("messages.no_permission"));
                return true;
            }

            boolean muted = toggleStaffAlerts(player.getUniqueId());
            if (muted) {
                sender.sendMessage(messageManager.getMessage("messages.staff_alerts_muted_on"));
            } else {
                sender.sendMessage(messageManager.getMessage("messages.staff_alerts_muted_off"));
            }
            return true;
        }

        // ticket <id> - Open specific ticket GUI (for clickable messages)
        if (sub.equals("ticket")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("messages.only_players"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c[AIGuard] Usage: /aiwatch ticket <id>");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("aiwatch.alert")) {
                sender.sendMessage(messageManager.getMessage("messages.no_permission"));
                return true;
            }

            String id = args[1];
            com.duckslavi.aiguard.tickets.Ticket ticket = ticketManager.getTicket(id);
            if (ticket == null) {
                sender.sendMessage(messageManager.getMessage("messages.ticket_not_found"));
                return true;
            }

            new com.duckslavi.aiguard.tickets.TicketGUI(this, ticketManager, ticket).open(player);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messageManager.getMessage("messages.help_header"));
        sender.sendMessage(messageManager.getMessage("messages.help_reload"));
        sender.sendMessage(messageManager.getMessage("messages.help_status"));
        sender.sendMessage(messageManager.getMessage("messages.help_history"));
        sender.sendMessage(messageManager.getMessage("messages.help_ticket"));
        sender.sendMessage(messageManager.getMessage("messages.help_alerts"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("aiguard")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission("aiwatch.admin")) {
                options.add("help");
                options.add("reload");
                options.add("status");
                options.add("history");
            }
            if (sender.hasPermission("aiwatch.alert")) {
                options.add("ticket");
                options.add("alerts");
            }

            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String opt : options) {
                if (opt.startsWith(current)) {
                    result.add(opt);
                }
            }
            Collections.sort(result);
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("ticket")) {
            if (!sender.hasPermission("aiwatch.alert")) {
                return Collections.emptyList();
            }

            String current = args[1].toLowerCase();
            List<String> result = new ArrayList<>();
            for (com.duckslavi.aiguard.tickets.Ticket ticket : ticketManager.getActiveTickets()) {
                String id = ticket.getId();
                if (id.toLowerCase().startsWith(current)) {
                    result.add(id);
                }
            }
            Collections.sort(result);
            return result;
        }

        return Collections.emptyList();
    }

    public static AIGuard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BungeeManager getBungeeManager() {
        return bungeeManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public boolean isStaffAlertsMuted(UUID uuid) {
        return mutedStaff.contains(uuid);
    }

    public boolean toggleStaffAlerts(UUID uuid) {
        if (mutedStaff.contains(uuid)) {
            mutedStaff.remove(uuid);
            return false;
        } else {
            mutedStaff.add(uuid);
            return true;
        }
    }
}
