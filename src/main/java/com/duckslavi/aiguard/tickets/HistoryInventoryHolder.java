package com.duckslavi.aiguard.tickets;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * InventoryHolder for ticket history GUI
 */
public class HistoryInventoryHolder implements InventoryHolder {
    private final int page;

    public HistoryInventoryHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
