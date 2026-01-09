package com.zootycoon.objects;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class AnimalData {
    private final UUID entityId;
    private final String type; // e.g., "Lion", "Elephant"
    private double hunger; // 0-100 (100 = full)
    private double thirst; // 0-100 (100 = quenched)
    private double happiness; // 0-100 (100 = happy)

    public AnimalData(UUID entityId, String type) {
        this.entityId = entityId;
        this.type = type;
        this.hunger = 100.0;
        this.thirst = 100.0;
        this.happiness = 100.0;
    }

    public void tick() {
        // Decrease stats over time
        // Adjust decay rate as needed
        this.hunger = Math.max(0, this.hunger - 0.5);
        this.thirst = Math.max(0, this.thirst - 0.7);

        // Happiness calculation
        if (hunger < 30 || thirst < 30) {
            happiness = Math.max(0, happiness - 1.0);
        } else {
            happiness = Math.min(100, happiness + 0.5);
        }
    }

    public String getStatusString() {
        ChatColor hungerColor = hunger > 50 ? ChatColor.GREEN : (hunger > 20 ? ChatColor.YELLOW : ChatColor.RED);
        ChatColor thirstColor = thirst > 50 ? ChatColor.GREEN : (thirst > 20 ? ChatColor.YELLOW : ChatColor.RED);
        ChatColor happyColor = happiness > 50 ? ChatColor.GREEN : (happiness > 20 ? ChatColor.YELLOW : ChatColor.RED);

        return String.format("%s H: %s%.0f%% %s T: %s%.0f%% %s Happy: %s%.0f%%",
                type,
                hungerColor, hunger, ChatColor.RESET,
                thirstColor, thirst, ChatColor.RESET,
                happyColor, happiness);
    }

    // Getters / Setters
    public UUID getEntityId() {
        return entityId;
    }

    public String getType() {
        return type;
    }

    public double getHunger() {
        return hunger;
    }

    public void setHunger(double hunger) {
        this.hunger = Math.min(100, Math.max(0, hunger));
    }

    public double getThirst() {
        return thirst;
    }

    public void setThirst(double thirst) {
        this.thirst = Math.min(100, Math.max(0, thirst));
    }

    public double getHappiness() {
        return happiness;
    }

    public void setHappiness(double happiness) {
        this.happiness = Math.min(100, Math.max(0, happiness));
    }
}
