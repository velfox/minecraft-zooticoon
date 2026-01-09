package com.zootycoon.objects;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import java.util.UUID;

public class GuestData {
    public enum State {
        WANDERING, QUEUING, RIDING
    }

    private final UUID entityId;
    private State currentState = State.WANDERING;
    private Location targetLocation;

    // ... existing stats ...
    private double budget;
    private double happiness;
    private double hunger;
    private double thirst;
    private String thinking; // "I'm hungry"

    public GuestData(UUID entityId) {
        this.entityId = entityId;
        this.budget = 50 + Math.random() * 100; // $50 - $150
        this.happiness = 80;
        this.hunger = 100; // Full
        this.thirst = 100; // Full
        this.thinking = "Just arrived!";
    }

    public void tick() {
        hunger = Math.max(0, hunger - 0.2);
        thirst = Math.max(0, thirst - 0.3);

        if (hunger < 20 || thirst < 20) {
            happiness = Math.max(0, happiness - 0.5);
            thinking = "I need food or drink!";
        } else if (happiness > 90) {
            thinking = "What a great zoo!";
        } else {
            thinking = "Exploring...";
        }
    }

    public String getStatus() {
        return String.format("%sHappy: %.0f%% %sHunger: %.0f%% %sThirst: %.0f%% \n%sThought: %s",
                ChatColor.YELLOW, happiness,
                ChatColor.GREEN, hunger,
                ChatColor.AQUA, thirst,
                ChatColor.GRAY, thinking);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    public double getBudget() {
        return budget;
    }

    public void spend(double amount) {
        this.budget -= amount;
    }
}
