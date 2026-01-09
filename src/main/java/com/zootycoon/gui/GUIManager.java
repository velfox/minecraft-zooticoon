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

    public void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Zoo Shop");

        // Example items
        addItem(shop, 11, Material.OCELOT_SPAWN_EGG, "Lion", 500, "Spawns a Lion");
        addItem(shop, 13, Material.SNIFFER_SPAWN_EGG, "Elephant", 1000, "Spawns an Elephant");
        addItem(shop, 15, Material.HORSE_SPAWN_EGG, "Zebra", 300, "Spawns a Zebra");

        // Attractions
        addItem(shop, 20, Material.BEACON, "Carousel", 5000, "Animated Carousel Ride");
        addItem(shop, 22, Material.MINECART, "Rollercoaster", 8000, "Looping Coaster");

        player.openInventory(shop);
    }

    private void addItem(Inventory inv, int slot, Material material, String name, double price, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + lore,
                "",
                ChatColor.YELLOW + "Price: $" + price));
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.BLUE + "Zoo Shop"))
            return;
        event.setCancelled(true); // Prevent taking items

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
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
