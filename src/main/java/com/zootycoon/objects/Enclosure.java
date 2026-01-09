package com.zootycoon.objects;

import org.bukkit.Location;

public class Enclosure {
    private final Location center;
    private final int radius;
    private final String type; // e.g., "Savannah", "Jungle"

    public Enclosure(Location center, int radius, String type) {
        this.center = center;
        this.radius = radius;
        this.type = type;
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public String getType() {
        return type;
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(center.getWorld()))
            return false;
        double distSq = loc.distanceSquared(center);
        return distSq <= (radius * radius);
    }
}
