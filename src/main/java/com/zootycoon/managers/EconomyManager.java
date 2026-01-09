package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final ZooTycoon plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File ecoFile;
    private FileConfiguration ecoConfig;

    public EconomyManager(ZooTycoon plugin) {
        this.plugin = plugin;
        this.ecoFile = new File(plugin.getDataFolder(), "economy.yml");
        loadEconomy();
    }

    public double getBalance(Player player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void depositPlayer(Player player, double amount) {
        double current = getBalance(player);
        balances.put(player.getUniqueId(), current + amount);
        saveEconomy();
    }

    public void withdrawPlayer(Player player, double amount) {
        double current = getBalance(player);
        balances.put(player.getUniqueId(), current - amount);
        saveEconomy();
    }

    public boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    private void loadEconomy() {
        if (!ecoFile.exists())
            return;
        ecoConfig = YamlConfiguration.loadConfiguration(ecoFile);
        for (String key : ecoConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = ecoConfig.getDouble(key);
                balances.put(uuid, balance);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveEconomy() {
        ecoConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            ecoConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            ecoConfig.save(ecoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
