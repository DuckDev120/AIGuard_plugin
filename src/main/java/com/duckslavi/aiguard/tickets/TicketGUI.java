package com.duckslavi.aiguard.tickets;

import com.duckslavi.aiguard.AIGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GUI for displaying a single ticket
 */
public class TicketGUI {

    private final AIGuard plugin;
    private final TicketManager ticketManager;
    private final Ticket ticket;
    private final int historyPage;

    public TicketGUI(AIGuard plugin, TicketManager ticketManager, Ticket ticket) {
        this(plugin, ticketManager, ticket, -1);
    }

    public TicketGUI(AIGuard plugin, TicketManager ticketManager, Ticket ticket, int historyPage) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
        this.ticket = ticket;
        this.historyPage = historyPage;
    }

    public void open(Player player) {
        String titleTemplate = plugin.getMessageManager().getMessage("ticket.title");
        String title = titleTemplate.replace("{id}", ticket.getId());

        FileConfiguration mainCfg = plugin.getConfigManager().getConfig();
        int size = mainCfg.getInt("gui.ticket.size", 27);
        if (size <= 0 || size % 9 != 0) {
            size = 27;
        }
        int infoSlot = mainCfg.getInt("gui.ticket.info_slot", 11);
        int claimSlot = mainCfg.getInt("gui.ticket.claim_slot", 15);
        int closeSlot = mainCfg.getInt("gui.ticket.close_slot", 26);
        int backSlot = mainCfg.getInt("gui.ticket.back_slot", 18);
        boolean teleportServerEnabled = mainCfg.getBoolean("gui.ticket.teleport_server_enabled", true);
        int teleportServerSlot = mainCfg.getInt("gui.ticket.teleport_server_slot", 12);
        boolean teleportWorldEnabled = mainCfg.getBoolean("gui.ticket.teleport_world_enabled", true);
        int teleportWorldSlot = mainCfg.getInt("gui.ticket.teleport_world_slot", 14);

        if (infoSlot < 0 || infoSlot >= size)
            infoSlot = 11;
        if (claimSlot < 0 || claimSlot >= size)
            claimSlot = 15;
        if (closeSlot < 0 || closeSlot >= size)
            closeSlot = 26;
        if (backSlot < 0 || backSlot >= size)
            backSlot = 18;
        if (teleportServerSlot < 0 || teleportServerSlot >= size)
            teleportServerSlot = 12;
        if (teleportWorldSlot < 0 || teleportWorldSlot >= size)
            teleportWorldSlot = 14;

        Inventory inv = Bukkit.createInventory(new TicketInventoryHolder(ticket.getId(), historyPage), size, title);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(plugin.getMessageManager().getMessage("ticket.info_item_name"));

        FileConfiguration cfg = plugin.getMessageManager().getRawConfig();
        List<String> rawLore = cfg.getStringList("ticket.info_item_lore");
        List<String> lore = new ArrayList<>();
        String timeStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ticket.getTimestamp()));
        for (String line : rawLore) {
            line = line
                    .replace("{player}", ticket.getPlayerName())
                    .replace("{server}", ticket.getServerName())
                    .replace("{world}", ticket.getWorldName() == null ? "-" : ticket.getWorldName())
                    .replace("{time}", timeStr)
                    .replace("{message}", ticket.getOriginalMessage())
                    .replace("{banned_word}", ticket.getBannedWord())
                    .replace("{similarity}", String.valueOf(ticket.getSimilarityPercentage()));
            lore.add(color(line));
        }
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        inv.setItem(infoSlot, info);

        ItemStack claimItem = new ItemStack(Material.LIME_DYE);
        ItemMeta claimMeta = claimItem.getItemMeta();
        if (ticket.getStatus() == TicketStatus.OPEN) {
            claimMeta.setDisplayName(plugin.getMessageManager().getMessage("ticket.claim_button_name"));
            List<String> raw = cfg.getStringList("ticket.claim_button_lore");
            List<String> lore2 = new ArrayList<>();
            for (String line : raw) {
                lore2.add(color(line));
            }
            claimMeta.setLore(lore2);
        } else if (ticket.getStatus() == TicketStatus.CLAIMED) {
            String name = plugin.getMessageManager().getMessage("ticket.claimed_button_name")
                    .replace("{staff}", ticket.getClaimedBy() == null ? "Unknown" : ticket.getClaimedBy());
            claimMeta.setDisplayName(name);
            List<String> raw = cfg.getStringList("ticket.claimed_button_lore");
            List<String> lore2 = new ArrayList<>();
            for (String line : raw) {
                lore2.add(color(line));
            }
            claimMeta.setLore(lore2);
            claimItem.setType(Material.YELLOW_DYE);
        } else {
            String name = plugin.getMessageManager().getMessage("ticket.claimed_button_name")
                    .replace("{staff}", ticket.getClaimedBy() == null ? "Closed" : ticket.getClaimedBy());
            claimMeta.setDisplayName(name);
            claimItem.setType(Material.GRAY_DYE);
        }
        claimItem.setItemMeta(claimMeta);
        inv.setItem(claimSlot, claimItem);

        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(plugin.getMessageManager().getMessage("ticket.close_button_name"));
        List<String> rawCloseLore = cfg.getStringList("ticket.close_button_lore");
        List<String> closeLore = new ArrayList<>();
        for (String line : rawCloseLore) {
            closeLore.add(color(line));
        }
        closeMeta.setLore(closeLore);
        closeItem.setItemMeta(closeMeta);
        inv.setItem(closeSlot, closeItem);

        // Server teleport button
        if (teleportServerEnabled) {
            ItemStack serverTeleportItem = new ItemStack(Material.ENDER_EYE);
            ItemMeta serverTeleportMeta = serverTeleportItem.getItemMeta();
            serverTeleportMeta
                    .setDisplayName(plugin.getMessageManager().getMessage("ticket.teleport_server_button_name"));
            List<String> serverLoreRaw = cfg.getStringList("ticket.teleport_server_button_lore");
            List<String> serverLore = new ArrayList<>();
            for (String line : serverLoreRaw) {
                line = line.replace("{server}", ticket.getServerName());
                serverLore.add(color(line));
            }
            serverTeleportMeta.setLore(serverLore);
            serverTeleportItem.setItemMeta(serverTeleportMeta);
            inv.setItem(teleportServerSlot, serverTeleportItem);
        }

        // World teleport button
        if (teleportWorldEnabled) {
            ItemStack worldTeleportItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta worldTeleportMeta = worldTeleportItem.getItemMeta();
            worldTeleportMeta
                    .setDisplayName(plugin.getMessageManager().getMessage("ticket.teleport_world_button_name"));
            List<String> worldLoreRaw = cfg.getStringList("ticket.teleport_world_button_lore");
            List<String> worldLore = new ArrayList<>();
            for (String line : worldLoreRaw) {
                line = line
                        .replace("{world}", ticket.getWorldName() == null ? "-" : ticket.getWorldName())
                        .replace("{server}", ticket.getServerName());
                worldLore.add(color(line));
            }
            worldTeleportMeta.setLore(worldLore);
            worldTeleportItem.setItemMeta(worldTeleportMeta);
            inv.setItem(teleportWorldSlot, worldTeleportItem);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(plugin.getMessageManager().getMessage("ticket.back_button_name"));
        List<String> rawBackLore = cfg.getStringList("ticket.back_button_lore");
        List<String> backLore = new ArrayList<>();
        for (String line : rawBackLore) {
            backLore.add(color(line));
        }
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        inv.setItem(backSlot, backItem);

        player.openInventory(inv);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
