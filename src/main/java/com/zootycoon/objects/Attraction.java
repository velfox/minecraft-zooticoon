package com.zootycoon.objects;

import org.bukkit.Location;

public class Attraction {
    private final String type;
    private final Location location;
    private final Location queueStart;

    public Attraction(String type, Location location, Location queueStart) {
        this.type = type;
        this.location = location;
        this.queueStart = queueStart;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Location getQueueStart() {
        return queueStart;
    }
}
