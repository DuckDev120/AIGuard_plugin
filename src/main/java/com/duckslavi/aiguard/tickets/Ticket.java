package com.duckslavi.aiguard.tickets;

import java.util.UUID;

/**
 * Object representing a single report ticket
 */
public class Ticket {

    private final String id;
    private final String playerName;
    private final UUID playerUuid;
    private final String serverName;
    private final String worldName;
    private final long timestamp;
    private final String originalMessage;
    private final String bannedWord;
    private final int similarityPercentage;

    private TicketStatus status;
    private String claimedBy;
    private UUID claimedByUuid;

    public Ticket(String id,
            String playerName,
            UUID playerUuid,
            String serverName,
            String worldName,
            long timestamp,
            String originalMessage,
            String bannedWord,
            int similarityPercentage,
            TicketStatus status,
            String claimedBy,
            UUID claimedByUuid) {
        this.id = id;
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.serverName = serverName;
        this.worldName = worldName;
        this.timestamp = timestamp;
        this.originalMessage = originalMessage;
        this.bannedWord = bannedWord;
        this.similarityPercentage = similarityPercentage;
        this.status = status;
        this.claimedBy = claimedBy;
        this.claimedByUuid = claimedByUuid;
    }

    public String getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getServerName() {
        return serverName;
    }

    public String getWorldName() {
        return worldName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String getBannedWord() {
        return bannedWord;
    }

    public int getSimilarityPercentage() {
        return similarityPercentage;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public UUID getClaimedByUuid() {
        return claimedByUuid;
    }

    public void setClaimedByUuid(UUID claimedByUuid) {
        this.claimedByUuid = claimedByUuid;
    }
}
