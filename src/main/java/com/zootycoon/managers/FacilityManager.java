package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FacilityManager implements Listener {

    private final ZooTycoon plugin;
    private final List<Location> stalls = new ArrayList<>(); // Store stall locations

    public FacilityManager(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    public List<Location> getStalls() {
        return stalls;
    }

    public void givePlacementWand(Player player, String type) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Place " + type);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click ground to place");
        meta.setLore(lore);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You received a placement wand for " + type);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        Block clickedParams = event.getClickedBlock();
        if (clickedParams == null)
            return;

        String name = item.getItemMeta().getDisplayName();
        if (name.contains("Place Burger Stand")) {
            event.setCancelled(true);
            buildBurgerStand(event.getPlayer(), clickedParams.getRelative(BlockFace.UP).getLocation());
            event.getPlayer().getInventory().getItemInMainHand().setAmount(0); // Remove wand
        }
    }

    private void buildBurgerStand(Player player, Location loc) {
        // Simple visual: Fence + Slab + Floating Burger
        loc.getBlock().setType(Material.OAK_FENCE);
        loc.clone().add(0, 1, 0).getBlock().setType(Material.OAK_SLAB);

        // Floating Item
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0.5, 0.5, 0.5),
                EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomName(ChatColor.GOLD + "Burger Stand");
        stand.setCustomNameVisible(true);
        stand.getEquipment().setHelmet(new ItemStack(Material.COOKED_BEEF));

        stalls.add(loc);
        player.sendMessage(ChatColor.GREEN + "Burger Stand built!");
    }
}
