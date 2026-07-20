package com.duckslavi.aiguard.tickets;

import com.duckslavi.aiguard.AIGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Listener for Ticket and History GUIs
 */
public class TicketGuiListener implements Listener {

    private final AIGuard plugin;
    private final TicketManager ticketManager;

    public TicketGuiListener(AIGuard plugin, TicketManager ticketManager) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory top = event.getView().getTopInventory();
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) {
            return;
        }

        InventoryHolder holder = top.getHolder();

        if (holder instanceof TicketInventoryHolder) {
            event.setCancelled(true);
            if (clickedInv != top) {
                return;
            }

            FileConfiguration cfg = plugin.getConfigManager().getConfig();
            int ticketSize = cfg.getInt("gui.ticket.size", 27);
            if (ticketSize <= 0 || ticketSize % 9 != 0) {
                ticketSize = 27;
            }
            int ticketBackSlot = cfg.getInt("gui.ticket.back_slot", 18);
            int ticketInfoSlot = cfg.getInt("gui.ticket.info_slot", 11);
            int ticketClaimSlot = cfg.getInt("gui.ticket.claim_slot", 15);
            int ticketCloseSlot = cfg.getInt("gui.ticket.close_slot", 26);
            boolean teleportServerEnabled = cfg.getBoolean("gui.ticket.teleport_server_enabled", true);
            int teleportServerSlot = cfg.getInt("gui.ticket.teleport_server_slot", 12);
            boolean teleportWorldEnabled = cfg.getBoolean("gui.ticket.teleport_world_enabled", true);
            int teleportWorldSlot = cfg.getInt("gui.ticket.teleport_world_slot", 14);
            if (ticketBackSlot < 0 || ticketBackSlot >= ticketSize)
                ticketBackSlot = 18;
            if (ticketInfoSlot < 0 || ticketInfoSlot >= ticketSize)
                ticketInfoSlot = 11;
            if (ticketClaimSlot < 0 || ticketClaimSlot >= ticketSize)
                ticketClaimSlot = 15;
            if (ticketCloseSlot < 0 || ticketCloseSlot >= ticketSize)
                ticketCloseSlot = 26;
            if (teleportServerSlot < 0 || teleportServerSlot >= ticketSize)
                teleportServerSlot = 12;
            if (teleportWorldSlot < 0 || teleportWorldSlot >= ticketSize)
                teleportWorldSlot = 14;

            TicketInventoryHolder tiHolder = (TicketInventoryHolder) holder;
            String id = tiHolder.getTicketId();
            int historyPage = tiHolder.getHistoryPage();
            Ticket ticket = ticketManager.getTicket(id);
            if (ticket == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_not_found"));
                player.closeInventory();
                return;
            }

            int slot = event.getSlot();

            // Back button
            if (slot == ticketBackSlot) {
                if (historyPage >= 0) {
                    new HistoryGUI(plugin, ticketManager).open(player, historyPage);
                } else {
                    player.closeInventory();
                }
                return;
            }

            // Teleport to Server button
            if (teleportServerEnabled && slot == teleportServerSlot) {
                if (plugin.getConfigManager().isBungeeCordEnabled() && plugin.getBungeeManager() != null) {
                    plugin.getBungeeManager().sendPlayerToServer(player, ticket.getServerName());
                } else {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.teleport_server_not_available"));
                }
                return;
            }

            // Teleport to World button
            if (teleportWorldEnabled && slot == teleportWorldSlot) {
                String worldName = ticket.getWorldName();
                if (worldName == null || worldName.isEmpty()) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.teleport_world_missing"));
                    return;
                }

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.teleport_world_not_found")
                            .replace("{world}", worldName));
                    return;
                }

                player.teleport(world.getSpawnLocation());
                player.sendMessage(plugin.getMessageManager().getMessage("messages.teleport_world_success")
                        .replace("{world}", worldName));
                return;
            }

            if (slot == ticketClaimSlot) {
                if (!player.hasPermission("aiwatch.alert")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.no_permission"));
                    return;
                }

                TicketManager.ClaimResult result = ticketManager.claimTicket(id, player);
                switch (result) {
                    case SUCCESS:
                        player.sendMessage(
                                plugin.getMessageManager().getMessage("messages.ticket_claim_you").replace("{id}", id));
                        String broadcast = plugin.getMessageManager().getMessage("messages.ticket_claim_broadcast")
                                .replace("{staff}", player.getName())
                                .replace("{id}", id);
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (online.hasPermission("aiwatch.alert")) {
                                online.sendMessage(broadcast);
                            }
                        }
                        new TicketGUI(plugin, ticketManager, ticket, historyPage).open(player);
                        break;
                    case NOT_FOUND:
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_not_found"));
                        player.closeInventory();
                        break;
                    case ALREADY_CLOSED:
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_closed_you")
                                .replace("{id}", id));
                        break;
                    case ALREADY_CLAIMED_BY_YOU:
                        player.sendMessage(
                                plugin.getMessageManager().getMessage("messages.ticket_claim_you").replace("{id}", id));
                        break;
                    case ALREADY_CLAIMED_BY_OTHER:
                        String staffName = ticket.getClaimedBy() == null ? "Unknown" : ticket.getClaimedBy();
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_already_claimed")
                                .replace("{staff}", staffName));
                        break;
                }
                return;
            }

            if (slot == ticketCloseSlot) {
                if (!player.hasPermission("aiwatch.alert")) {
                    player.sendMessage(plugin.getMessageManager().getMessage("messages.no_permission"));
                    return;
                }

                TicketManager.CloseResult result = ticketManager.closeTicket(id, player);
                switch (result) {
                    case SUCCESS:
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_closed_you")
                                .replace("{id}", id));
                        String broadcast = plugin.getMessageManager().getMessage("messages.ticket_closed_broadcast")
                                .replace("{staff}", player.getName())
                                .replace("{id}", id);
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (online.hasPermission("aiwatch.alert")) {
                                online.sendMessage(broadcast);
                            }
                        }
                        if (historyPage >= 0) {
                            new HistoryGUI(plugin, ticketManager).open(player, historyPage);
                        } else {
                            player.closeInventory();
                        }
                        break;
                    case NOT_FOUND:
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_not_found"));
                        player.closeInventory();
                        break;
                    case ALREADY_CLOSED:
                        player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_closed_you")
                                .replace("{id}", id));
                        break;
                }
                return;
            }

            return;
        }

        if (holder instanceof HistoryInventoryHolder) {
            event.setCancelled(true);
            if (clickedInv != top) {
                return;
            }

            HistoryInventoryHolder hHolder = (HistoryInventoryHolder) holder;
            int page = hHolder.getPage();
            int slot = event.getSlot();

            FileConfiguration cfg = plugin.getConfigManager().getConfig();
            int historySize = cfg.getInt("gui.history.size", 54);
            if (historySize < 9 || historySize % 9 != 0) {
                historySize = 54;
            }
            int backSlot = cfg.getInt("gui.history.back_slot", 49);
            int prevSlot = cfg.getInt("gui.history.prev_slot", 45);
            int nextSlot = cfg.getInt("gui.history.next_slot", 53);
            if (backSlot < 0 || backSlot >= historySize)
                backSlot = 49;
            if (prevSlot < 0 || prevSlot >= historySize)
                prevSlot = 45;
            if (nextSlot < 0 || nextSlot >= historySize)
                nextSlot = 53;

            // Back / Navigation
            if (slot == backSlot) {
                // Back - closes the GUI
                player.closeInventory();
                return;
            }
            if (slot == prevSlot) {
                new HistoryGUI(plugin, ticketManager).open(player, page - 1);
                return;
            }
            if (slot == nextSlot) {
                new HistoryGUI(plugin, ticketManager).open(player, page + 1);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) {
                return;
            }
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                return;
            }

            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name == null) {
                return;
            }

            int idx = name.lastIndexOf('#');
            if (idx == -1 || idx + 1 >= name.length()) {
                return;
            }

            String id = name.substring(idx + 1).trim();
            Ticket ticket = ticketManager.getTicket(id);
            if (ticket == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("messages.ticket_not_found"));
                return;
            }

            new TicketGUI(plugin, ticketManager, ticket, page).open(player);
        }
    }
}
