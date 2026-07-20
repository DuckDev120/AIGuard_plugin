package com.duckslavi.aiguard.tickets;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * InventoryHolder for a single ticket - allows identifying the GUI and
 * retrieving the ticket ID
 */
public class TicketInventoryHolder implements InventoryHolder {

    private final String ticketId;
    private final int historyPage;

    public TicketInventoryHolder(String ticketId) {
        this(ticketId, -1);
    }

    public TicketInventoryHolder(String ticketId, int historyPage) {
        this.ticketId = ticketId;
        this.historyPage = historyPage;
    }

    public String getTicketId() {
        return ticketId;
    }

    public int getHistoryPage() {
        return historyPage;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
