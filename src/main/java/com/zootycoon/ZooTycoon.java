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
    private com.zootycoon.managers.AttractionManager attractionManager;
    private com.zootycoon.managers.GuestManager guestManager;
    private com.zootycoon.managers.StaffManager staffManager;
    private com.zootycoon.managers.AnimalManager animalManager;
    private com.zootycoon.managers.FacilityManager facilityManager;
    private com.zootycoon.managers.EnclosureManager enclosureManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        economyManager = new EconomyManager(this);
        zooManager = new ZooManager(this);
        guiManager = new GUIManager(this);
        attractionManager = new com.zootycoon.managers.AttractionManager(this);
        guestManager = new com.zootycoon.managers.GuestManager(this);
        animalManager = new com.zootycoon.managers.AnimalManager(this);
        staffManager = new com.zootycoon.managers.StaffManager(this);
        facilityManager = new com.zootycoon.managers.FacilityManager(this);

        getCommand("zoo").setExecutor(new ZooCommand(this));

        getServer().getPluginManager().registerEvents(new ZooListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(animalManager, this);
        getServer().getPluginManager().registerEvents(attractionManager, this);
        getServer().getPluginManager().registerEvents(guestManager, this);
        getServer().getPluginManager().registerEvents(facilityManager, this);

        enclosureManager = new com.zootycoon.managers.EnclosureManager(this);
        getServer().getPluginManager().registerEvents(enclosureManager, this);

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

    public com.zootycoon.managers.AttractionManager getAttractionManager() {
        return attractionManager;
    }

    public com.zootycoon.managers.GuestManager getGuestManager() {
        return guestManager;
    }

    public com.zootycoon.managers.AnimalManager getAnimalManager() {
        return animalManager;
    }

    public com.zootycoon.managers.StaffManager getStaffManager() {
        return staffManager;
    }

    public com.zootycoon.managers.FacilityManager getFacilityManager() {
        return facilityManager;
    }

    public com.zootycoon.managers.EnclosureManager getEnclosureManager() {
        return enclosureManager;
    }
}
