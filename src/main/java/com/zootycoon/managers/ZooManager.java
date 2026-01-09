package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.Zoo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZooManager {

    private final ZooTycoon plugin;
    private final Map<UUID, Zoo> zooCache = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ZooManager(ZooTycoon plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadZoos();
    }

    public Zoo createZoo(Player player, String name) {
        if (hasZoo(player.getUniqueId())) {
            return getZoo(player.getUniqueId());
        }
        Zoo zoo = new Zoo(player.getUniqueId(), name, player.getLocation());
        zooCache.put(player.getUniqueId(), zoo);

        generateParkEnvironment(zoo);

        saveZoos();
        return zoo;
    }

    private void generateParkEnvironment(Zoo zoo) {
        Location center = zoo.getEntrance();
        Chunk chunk = center.getChunk();
        World world = center.getWorld();
        int cx = chunk.getX() * 16;
        int cz = chunk.getZ() * 16;
        int y = center.getBlockY();

        // 1. Perimeter Fence (Stone Brick Walls + Fancy Iron Bars)
        for (int i = 0; i < 16; i++) {
            // North & South
            org.bukkit.Material fenceType = org.bukkit.Material.STONE_BRICK_WALL;

            // Corners - Pillars
            if (i == 0 || i == 15) {
                world.getBlockAt(cx + i, y, cz).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                world.getBlockAt(cx + i, y + 1, cz).setType(org.bukkit.Material.CHISELED_STONE_BRICKS); // 2 high
                world.getBlockAt(cx + i, y, cz + 15).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                world.getBlockAt(cx + i, y + 1, cz + 15).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);

                world.getBlockAt(cx, y, cz + i).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                world.getBlockAt(cx, y + 1, cz + i).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                world.getBlockAt(cx + 15, y, cz + i).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                world.getBlockAt(cx + 15, y + 1, cz + i).setType(org.bukkit.Material.CHISELED_STONE_BRICKS);
                continue;
            }

            // Walls
            world.getBlockAt(cx + i, y, cz).setType(fenceType);
            world.getBlockAt(cx + i, y, cz + 15).setType(fenceType);
            world.getBlockAt(cx, y, cz + i).setType(fenceType);
            world.getBlockAt(cx + 15, y, cz + i).setType(fenceType);

            // Toppers (Iron Bars)
            world.getBlockAt(cx + i, y + 1, cz).setType(org.bukkit.Material.IRON_BARS);
            world.getBlockAt(cx + i, y + 1, cz + 15).setType(org.bukkit.Material.IRON_BARS);
            world.getBlockAt(cx, y + 1, cz + i).setType(org.bukkit.Material.IRON_BARS);
            world.getBlockAt(cx + 15, y + 1, cz + i).setType(org.bukkit.Material.IRON_BARS);
        }

        // 2. Entrance Arch
        // Simple arch at player location (snapped to nearest side?)
        // For simplicity, building it explicitly at the center of North Side (z=0)
        int archX = cx + 8;
        int archZ = cz; // North side

        // Clear fence at entrance
        world.getBlockAt(archX, y, archZ).setType(org.bukkit.Material.AIR);
        world.getBlockAt(archX + 1, y, archZ).setType(org.bukkit.Material.AIR);
        world.getBlockAt(archX - 1, y, archZ).setType(org.bukkit.Material.AIR);
        world.getBlockAt(archX, y + 1, archZ).setType(org.bukkit.Material.AIR);
        world.getBlockAt(archX + 1, y + 1, archZ).setType(org.bukkit.Material.AIR);
        world.getBlockAt(archX - 1, y + 1, archZ).setType(org.bukkit.Material.AIR);

        // Build Columns
        for (int h = 0; h < 4; h++) {
            world.getBlockAt(archX - 2, y + h, archZ).setType(org.bukkit.Material.MOSSY_STONE_BRICKS);
            world.getBlockAt(archX + 2, y + h, archZ).setType(org.bukkit.Material.MOSSY_STONE_BRICKS);
        }

        // Roof
        world.getBlockAt(archX - 1, y + 3, archZ).setType(org.bukkit.Material.STONE_BRICK_SLAB);
        world.getBlockAt(archX + 1, y + 3, archZ).setType(org.bukkit.Material.STONE_BRICK_SLAB);
        world.getBlockAt(archX, y + 4, archZ).setType(org.bukkit.Material.GLOWSTONE); // Light
        world.getBlockAt(archX, y + 5, archZ).setType(org.bukkit.Material.STONE_BRICK_WALL); // Spire
    }

    public Zoo getZoo(UUID ownerId) {
        return zooCache.get(ownerId);
    }

    public boolean hasZoo(UUID ownerId) {
        return zooCache.containsKey(ownerId);
    }

    public Zoo getZooAt(Chunk chunk) {
        for (Zoo zoo : zooCache.values()) {
            if (zoo.isChunkClaimed(chunk)) {
                return zoo;
            }
        }
        return null;
    }

    private void loadZoos() {
        if (!dataFile.exists())
            return;

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("zoos")) {
            for (String key : dataConfig.getConfigurationSection("zoos").getKeys(false)) {
                UUID ownerId = UUID.fromString(key);
                String name = dataConfig.getString("zoos." + key + ".name");
                Location entrance = dataConfig.getLocation("zoos." + key + ".entrance");
                Zoo zoo = new Zoo(ownerId, name, entrance);

                List<String> chunks = dataConfig.getStringList("zoos." + key + ".chunks");
                for (String c : chunks) {
                    String[] parts = c.split(":");
                    // Manually adding back to list to bypass auto-entrance logic if needed,
                    // but simplify by just updating the list directly
                    zoo.getClaimedChunks().add(c);
                }

                zooCache.put(ownerId, zoo);
            }
        }
    }

    public void saveZoos() {
        dataConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Zoo> entry : zooCache.entrySet()) {
            String key = "zoos." + entry.getKey().toString();
            Zoo zoo = entry.getValue();
            dataConfig.set(key + ".name", zoo.getName());
            dataConfig.set(key + ".entrance", zoo.getEntrance());
            dataConfig.set(key + ".chunks", zoo.getClaimedChunks());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
