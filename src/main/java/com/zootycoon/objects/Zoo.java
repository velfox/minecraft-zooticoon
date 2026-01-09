package com.zootycoon.objects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Zoo {
    private final UUID owner;
    private String name;
    private Location entrance;
    private List<String> claimedChunks; // Stored as "world:x:z"
    private long visitCount;
    private double entranceFee = 10.0; // Default $10

    public Zoo(UUID owner, String name, Location entrance) {
        this.owner = owner;
        this.name = name;
        this.entrance = entrance;
        this.claimedChunks = new ArrayList<>();
        this.visitCount = 0;

        // Auto-claim the entrance chunk
        addChunk(entrance.getChunk());
    }

    public double getEntranceFee() {
        return entranceFee;
    }

    public void setEntranceFee(double fee) {
        this.entranceFee = fee;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getEntrance() {
        return entrance;
    }

    public void setEntrance(Location entrance) {
        this.entrance = entrance;
    }

    public List<String> getClaimedChunks() {
        return claimedChunks;
    }

    public void addChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        if (!claimedChunks.contains(key)) {
            claimedChunks.add(key);
        }
    }

    public boolean isChunkClaimed(Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        return claimedChunks.contains(key);
    }

    public long getVisitCount() {
        return visitCount;
    }

    public void addVisit() {
        this.visitCount++;
    }

    private List<Enclosure> enclosures = new ArrayList<>();

    public void addEnclosure(Enclosure enclosure) {
        enclosures.add(enclosure);
    }

    public List<Enclosure> getEnclosures() {
        return enclosures;
    }

    /**
     * Checks if a location is inside any of this zoo's enclosures.
     */
    public boolean isInsideEnclosure(Location loc) {
        for (Enclosure enc : enclosures) {
            if (enc.isInside(loc))
                return true;
        }
        return false;
    }
}
