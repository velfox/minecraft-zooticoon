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
        saveZoos();
        return zoo;
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
        if (!dataFile.exists()) return;
        
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
