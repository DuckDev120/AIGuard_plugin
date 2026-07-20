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
 * GUI for displaying ticket history
 */
public class HistoryGUI {

    private final AIGuard plugin;
    private final TicketManager ticketManager;

    public HistoryGUI(AIGuard plugin, TicketManager ticketManager) {
        this.plugin = plugin;
        this.ticketManager = ticketManager;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        String title = plugin.getMessageManager().getMessage("history.gui_title");
        FileConfiguration msgCfg = plugin.getMessageManager().getRawConfig();
        FileConfiguration mainCfg = plugin.getConfigManager().getConfig();
        List<Ticket> tickets = ticketManager.getHistoryTickets();

        int size = mainCfg.getInt("gui.history.size", 54);
        if (size < 9 || size % 9 != 0) {
            size = 54;
        }

        int pageSize = mainCfg.getInt("gui.history.page_size", 45);
        if (pageSize < 1 || pageSize > size - 9) {
            pageSize = Math.min(45, size - 9);
        }
        if (pageSize < 1) {
            pageSize = 1;
        }

        int backSlot = mainCfg.getInt("gui.history.back_slot", 49);
        int prevSlot = mainCfg.getInt("gui.history.prev_slot", 45);
        int nextSlot = mainCfg.getInt("gui.history.next_slot", 53);
        if (backSlot < 0 || backSlot >= size)
            backSlot = 49;
        if (prevSlot < 0 || prevSlot >= size)
            prevSlot = 45;
        if (nextSlot < 0 || nextSlot >= size)
            nextSlot = 53;

        int maxPage = tickets.isEmpty() ? 0 : (tickets.size() - 1) / pageSize;
        if (page < 0)
            page = 0;
        if (page > maxPage)
            page = maxPage;

        Inventory inv = Bukkit.createInventory(new HistoryInventoryHolder(page), size, title);

        int start = page * pageSize;
        int end = Math.min(start + pageSize, tickets.size());
        int slot = 0;
        for (int i = start; i < end && slot < pageSize; i++) {
            Ticket ticket = tickets.get(i);

            Material mat;
            String nameKey;
            if (ticket.getStatus() == TicketStatus.OPEN) {
                mat = Material.LIME_WOOL;
                nameKey = "history.item_name_open";
            } else if (ticket.getStatus() == TicketStatus.CLAIMED) {
                mat = Material.YELLOW_WOOL;
                nameKey = "history.item_name_claimed";
            } else {
                mat = Material.RED_WOOL;
                nameKey = "history.item_name_closed";
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            String displayName = plugin.getMessageManager().getMessage(nameKey).replace("{id}", ticket.getId());
            meta.setDisplayName(displayName);

            List<String> rawLore = msgCfg.getStringList("history.item_lore");
            List<String> lore = new ArrayList<>();
            String timeStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(ticket.getTimestamp()));
            for (String line : rawLore) {
                line = line
                        .replace("{player}", ticket.getPlayerName())
                        .replace("{server}", ticket.getServerName())
                        .replace("{world}", ticket.getWorldName() == null ? "-" : ticket.getWorldName())
                        .replace("{time}", timeStr)
                        .replace("{banned_word}", ticket.getBannedWord())
                        .replace("{similarity}", String.valueOf(ticket.getSimilarityPercentage()));
                lore.add(color(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(plugin.getMessageManager().getMessage("history.back_button_name"));
        List<String> backLore = new ArrayList<>();
        for (String line : msgCfg.getStringList("history.back_button_lore")) {
            backLore.add(color(line));
        }
        backMeta.setLore(backLore);
        backItem.setItemMeta(backMeta);
        inv.setItem(backSlot, backItem);

        // Previous page
        if (page > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            prevMeta.setDisplayName(plugin.getMessageManager().getMessage("history.prev_page_name"));
            List<String> prevLore = new ArrayList<>();
            for (String line : msgCfg.getStringList("history.prev_page_lore")) {
                prevLore.add(color(line));
            }
            prevMeta.setLore(prevLore);
            prevItem.setItemMeta(prevMeta);
            inv.setItem(prevSlot, prevItem);
        }

        // Next page
        if (page < maxPage) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            nextMeta.setDisplayName(plugin.getMessageManager().getMessage("history.next_page_name"));
            List<String> nextLore = new ArrayList<>();
            for (String line : msgCfg.getStringList("history.next_page_lore")) {
                nextLore.add(color(line));
            }
            nextMeta.setLore(nextLore);
            nextItem.setItemMeta(nextMeta);
            inv.setItem(nextSlot, nextItem);
        }

        player.openInventory(inv);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
