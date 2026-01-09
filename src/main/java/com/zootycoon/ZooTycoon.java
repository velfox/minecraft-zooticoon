package com.zootycoon;

import com.zootycoon.commands.ZooCommand;
import com.zootycoon.gui.GUIManager;
import com.zootycoon.listeners.ZooListener;
import com.zootycoon.managers.ZooManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZooTycoon extends JavaPlugin {

    private static ZooTycoon instance;
    private ZooManager zooManager;
    private GUIManager guiManager;
    private Economy economy = null;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().warning(String.format("[%s] - Vault not found or disabled! Economy features will be disabled.",
                    getDescription().getName()));
        }

        zooManager = new ZooManager(this);
        guiManager = new GUIManager(this);

        getCommand("zoo").setExecutor(new ZooCommand(this));

        getServer().getPluginManager().registerEvents(new ZooListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(new com.zootycoon.managers.AnimalManager(this), this);

        getLogger().info("ZooTycoon has been enabled!");
    }

    @Override
    public void onDisable() {
        if (zooManager != null) {
            zooManager.saveZoos();
        }
        getLogger().info("ZooTycoon has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static ZooTycoon getInstance() {
        return instance;
    }

    public ZooManager getZooManager() {
        return zooManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
