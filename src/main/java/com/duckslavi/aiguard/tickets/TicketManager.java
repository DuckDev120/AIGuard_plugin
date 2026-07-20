package com.duckslavi.aiguard.tickets;

import com.duckslavi.aiguard.AIGuard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

/**
 * Ticket Manager - Responsible for creating, storing, and managing the status
 * of tickets
 */
public class TicketManager {

    private final AIGuard plugin;
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final List<Ticket> history = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idCounter = new AtomicLong(1);

    public TicketManager(AIGuard plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new ticket and records it in the lists
     */
    public Ticket createTicket(String playerName,
            UUID playerUuid,
            String serverName,
            String worldName,
            String message,
            String bannedWord,
            int similarityPercentage) {
        String id = String.valueOf(idCounter.getAndIncrement());
        Ticket ticket = new Ticket(
                id,
                playerName,
                playerUuid,
                serverName,
                worldName,
                System.currentTimeMillis(),
                message,
                bannedWord,
                similarityPercentage,
                TicketStatus.OPEN,
                null,
                null);

        activeTickets.put(id, ticket);
        history.add(ticket);

        return ticket;
    }

    /**
     * Returns an active ticket by ID
     */
    public Ticket getTicket(String id) {
        return activeTickets.get(id);
    }

    /**
     * Returns a list of all active tickets
     */
    public List<Ticket> getActiveTickets() {
        return new ArrayList<>(activeTickets.values());
    }

    /**
     * Returns a list of all tickets created (including closed ones)
     */
    public List<Ticket> getHistoryTickets() {
        return new ArrayList<>(history);
    }

    /**
     * Returns a count of tickets by status
     */
    public TicketStatusCounts getStatusCounts() {
        int open = 0;
        int claimed = 0;
        int closed = 0;

        synchronized (history) {
            for (Ticket ticket : history) {
                if (ticket.getStatus() == TicketStatus.OPEN) {
                    open++;
                } else if (ticket.getStatus() == TicketStatus.CLAIMED) {
                    claimed++;
                } else if (ticket.getStatus() == TicketStatus.CLOSED) {
                    closed++;
                }
            }
        }

        return new TicketStatusCounts(open, claimed, closed);
    }

    /**
     * Attempt to claim a ticket by a staff member
     */
    public ClaimResult claimTicket(String id, Player staff) {
        Ticket ticket = activeTickets.get(id);
        if (ticket == null) {
            return ClaimResult.NOT_FOUND;
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            return ClaimResult.ALREADY_CLOSED;
        }

        if (ticket.getStatus() == TicketStatus.CLAIMED) {
            if (staff.getUniqueId().equals(ticket.getClaimedByUuid())) {
                return ClaimResult.ALREADY_CLAIMED_BY_YOU;
            }
            return ClaimResult.ALREADY_CLAIMED_BY_OTHER;
        }

        ticket.setStatus(TicketStatus.CLAIMED);
        ticket.setClaimedBy(staff.getName());
        ticket.setClaimedByUuid(staff.getUniqueId());
        return ClaimResult.SUCCESS;
    }

    /**
     * Close a ticket by a staff member
     */
    public CloseResult closeTicket(String id, Player staff) {
        Ticket ticket = activeTickets.get(id);
        if (ticket == null) {
            return CloseResult.NOT_FOUND;
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            return CloseResult.ALREADY_CLOSED;
        }

        ticket.setStatus(TicketStatus.CLOSED);
        // Leaving it in activeTickets so it appears in GUI, but can be removed if
        // desired later
        return CloseResult.SUCCESS;
    }

    /**
     * Inner class representing ticket counts by status
     */
    public static class TicketStatusCounts {
        private final int open;
        private final int claimed;
        private final int closed;

        public TicketStatusCounts(int open, int claimed, int closed) {
            this.open = open;
            this.claimed = claimed;
            this.closed = closed;
        }

        public int getOpen() {
            return open;
        }

        public int getClaimed() {
            return claimed;
        }

        public int getClosed() {
            return closed;
        }
    }

    /**
     * Possible results of a Claim attempt
     */
    public enum ClaimResult {
        SUCCESS,
        NOT_FOUND,
        ALREADY_CLOSED,
        ALREADY_CLAIMED_BY_YOU,
        ALREADY_CLAIMED_BY_OTHER
    }

    /**
     * Possible results of a Close attempt
     */
    public enum CloseResult {
        SUCCESS,
        NOT_FOUND,
        ALREADY_CLOSED
    }
}
