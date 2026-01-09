package com.zootycoon.gui;

import com.zootycoon.ZooTycoon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager implements Listener {

    private final ZooTycoon plugin;

    public GUIManager(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Zoo Tycoon Menu");

        // 1. Shop
        addItem(menu, 11, Material.EMERALD, "Zoo Shop", 0, "Buy animals, staff, and facilities!");

        // 2. Commands / Help
        addItem(menu, 13, Material.BOOK, "Commands & Help", 0,
                ChatColor.WHITE + "/zoo create <name>" + "\n" +
                        ChatColor.WHITE + "/zoo claim" + "\n" +
                        ChatColor.WHITE + "/zoo setprice <amount>" + "\n" +
                        ChatColor.WHITE + "/zoo visit <player>");

        // 3. Info (Placeholder)
        addItem(menu, 15, Material.PAPER, "My Zoo Info", 0, "View your zoo stats (Coming Soon)");

        player.openInventory(menu);
    }

    public void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Zoo Shop");

        // Example items
        addItem(shop, 11, Material.OCELOT_SPAWN_EGG, "Lion", 500, "Spawns a Lion");
        addItem(shop, 13, Material.SNIFFER_SPAWN_EGG, "Elephant", 1000, "Spawns an Elephant");
        addItem(shop, 15, Material.HORSE_SPAWN_EGG, "Zebra", 300, "Spawns a Zebra");

        // Attractions
        addItem(shop, 20, Material.BEACON, "Carousel", 5000, "Animated Carousel Ride");
        addItem(shop, 22, Material.MINECART, "Rollercoaster", 8000, "Looping Coaster");

        // Staff
        addItem(shop, 24, Material.VILLAGER_SPAWN_EGG, "Hire Zookeeper", 2000, "Auto-feeds animals nearby");

        // Facilities
        addItem(shop, 25, Material.OAK_FENCE, "Burger Stand", 500, "Guests buy food here");

        player.openInventory(shop);
    }

    private void addItem(Inventory inv, int slot, Material material, String name, double price, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);

        java.util.List<String> loreLines = new java.util.ArrayList<>();
        if (lore.contains("\n")) {
            String[] lines = lore.split("\n");
            for (String l : lines)
                loreLines.add(ChatColor.GRAY + l);
        } else {
            loreLines.add(ChatColor.GRAY + lore);
        }

        if (price > 0) {
            loreLines.add("");
            loreLines.add(ChatColor.YELLOW + "Price: $" + price);
        }

        meta.setLore(loreLines);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.BLUE + "Zoo Shop") && !title.equals(ChatColor.DARK_GREEN + "Zoo Tycoon Menu"))
            return;
        event.setCancelled(true); // Prevent taking items

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Main Menu Logic
        if (title.equals(ChatColor.DARK_GREEN + "Zoo Tycoon Menu")) {
            if (clicked.getType() == Material.EMERALD) {
                openShop(player);
            }
            return;
        }

        // Shop Logic
        if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().contains("Back")) {
            openMainMenu(player);
            return;
        }

        double price = getPriceFromLore(clicked);

        if (price > 0) {
            com.zootycoon.managers.EconomyManager econ = plugin.getEconomyManager();
            // Normal items
            if (clicked.getType() == Material.BEACON || clicked.getType() == Material.MINECART) {
                // Start Attraction Placement
                String type = clicked.getItemMeta().getDisplayName().contains("Carousel") ? "Carousel"
                        : "Rollercoaster";

                // Don't charge yet, give Wand first!
                player.closeInventory();
                plugin.getAttractionManager().giveWand(player, type);
            } else if (clicked.getType() == Material.OAK_FENCE
                    && clicked.getItemMeta().getDisplayName().contains("Burger Stand")) {
                // Facility
                if (econ.withdrawPlayer(player, price)) {
                    player.closeInventory();
                    plugin.getFacilityManager().givePlacementWand(player, "Burger Stand");
                    player.sendMessage(ChatColor.GREEN + "You bought a Burger Stand!");
                } else {
                    player.sendMessage(ChatColor.RED + "Cannot afford Burger Stand!");
                }
            } else {
                // Animals/Items
                if (econ.hasEnough(player, price)) {
                    econ.withdrawPlayer(player, price);
                    player.sendMessage(ChatColor.GREEN + "You bought " + clicked.getItemMeta().getDisplayName());

                    ItemStack boughtItem = new ItemStack(clicked.getType());
                    ItemMeta meta = boughtItem.getItemMeta();
                    meta.setDisplayName(clicked.getItemMeta().getDisplayName()); // Keep custom name for spawning logic
                    boughtItem.setItemMeta(meta);

                    player.getInventory().addItem(boughtItem);
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot afford this! Balance: " + econ.getBalance(player));
                }
            }
        }
    }

    private double getPriceFromLore(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line.contains("Price: $")) {
                    try {
                        return Double.parseDouble(ChatColor.stripColor(line).replace("Price: $", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }
}
