package com.zootycoon;

import com.zootycoon.commands.ZooCommand;
import com.zootycoon.gui.GUIManager;
import com.zootycoon.listeners.ZooListener;
import com.zootycoon.managers.AnimalManager;
import com.zootycoon.managers.EconomyManager;
import com.zootycoon.managers.ZooManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZooTycoon extends JavaPlugin {

    private static ZooTycoon instance;
    private ZooManager zooManager;
    private GUIManager guiManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        economyManager = new EconomyManager(this);
        zooManager = new ZooManager(this);
        guiManager = new GUIManager(this);

        getCommand("zoo").setExecutor(new ZooCommand(this));

        getServer().getPluginManager().registerEvents(new ZooListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(new AnimalManager(this), this);

        getLogger().info("ZooTycoon has been enabled!");
    }

    @Override
    public void onDisable() {
        if (zooManager != null) {
            zooManager.saveZoos();
        }
        if (economyManager != null) {
            economyManager.saveEconomy();
        }
        getLogger().info("ZooTycoon has been disabled!");
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

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
