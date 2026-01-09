package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.Zoo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AttractionManager implements Listener {

    private final ZooTycoon plugin;
    private final Map<UUID, String> pendingAttraction = new HashMap<>(); // Player -> AttractionType

    public AttractionManager(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    public void giveWand(Player player, String attractionType) {
        pendingAttraction.put(player.getUniqueId(), attractionType);

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Attraction Wand: " + ChatColor.AQUA + attractionType);
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Right-click ground to PREVIEW",
                ChatColor.GRAY + "Left-click preview to CONFIRM build",
                ChatColor.RED + "Drop to CANCEL"));
        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.YELLOW + "You received the Attraction Wand! Right click to preview.");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!pendingAttraction.containsKey(player.getUniqueId()))
            return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD || !item.hasItemMeta())
            return;
        if (!item.getItemMeta().getDisplayName().startsWith(ChatColor.GOLD + "Attraction Wand"))
            return;

        event.setCancelled(true);
        String type = pendingAttraction.get(player.getUniqueId());

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // PREVIEW
            Block clicked = event.getClickedBlock();
            Location origin = clicked.getLocation().add(0, 1, 0);
            showPreview(player, origin, type);
            player.sendMessage(
                    ChatColor.AQUA + "Previewing " + type + ". Left click the center block to confirm purchase.");
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // BUILD
            Block clicked = event.getClickedBlock();
            // Ideally we check if it matches preview, simplified here

            // Check Money
            double price = type.equals("Carousel") ? 5000 : 8000;
            if (!plugin.getEconomyManager().hasEnough(player, price)) {
                player.sendMessage(ChatColor.RED + "You need $" + price + "!");
                return;
            }

            plugin.getEconomyManager().withdrawPlayer(player, price);
            buildAttraction(player, clicked.getLocation().add(0, 1, 0), type);

            player.getInventory().remove(item);
            pendingAttraction.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + type + " constructed!");
        }
    }

    private void showPreview(Player player, Location origin, String type) {
        int radius = type.equals("Carousel") ? 3 : 5;
        BlockData ghost = Bukkit.createBlockData(Material.BLUE_STAINED_GLASS);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location loc = origin.clone().add(x, 0, z);
                player.sendBlockChange(loc, ghost);
            }
        }
    }

    private void buildAttraction(Player player, Location origin, String type) {
        if (type.equals("Carousel")) {
            buildCarousel(origin);
        } else if (type.equals("Rollercoaster")) {
            buildRollercoaster(origin);
        }
    }

    private void buildCarousel(Location origin) {
        int radius = 3;
        // Platform
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Circle check
                if (Math.sqrt(x * x + z * z) <= radius) {
                    origin.getWorld().getBlockAt(origin.clone().add(x, 0, z)).setType(Material.STRIPPED_OAK_WOOD);
                    origin.getWorld().getBlockAt(origin.clone().add(x, 1, z)).setType(Material.OAK_FENCE); // Roof
                                                                                                           // support
                    origin.getWorld().getBlockAt(origin.clone().add(x, 3, z)).setType(Material.WHITE_WOOL); // Roof
                }
            }
        }

        // Animated Entity
        org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) origin.getWorld()
                .spawnEntity(origin.clone().add(0, 1, 2), EntityType.HORSE);
        horse.setAI(false);
        horse.setTamed(true);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.setCustomName("Carousel Horse");
        horse.setCustomNameVisible(true);
        horse.setColor(org.bukkit.entity.Horse.Color.WHITE);
        horse.setStyle(org.bukkit.entity.Horse.Style.WHITEFIELD);

        // Animation Task
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (horse.isDead()) {
                    this.cancel();
                    return;
                }
                angle += 0.1;
                double x = Math.cos(angle) * 2;
                double z = Math.sin(angle) * 2;
                Location loc = origin.clone().add(x, 1.5, z);
                loc.setYaw((float) Math.toDegrees(-angle)); // Face direction

                // Teleport horse (and passenger)
                horse.teleport(loc);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void buildRollercoaster(Location origin) {
        // Simple loop
        int size = 6;
        for (int x = 0; x < size; x++) {
            origin.getWorld().getBlockAt(origin.clone().add(x, 0, 0)).setType(Material.RED_WOOL);
            origin.getWorld().getBlockAt(origin.clone().add(x, 1, 0)).setType(Material.POWERED_RAIL);
            // Power it
            origin.getWorld().getBlockAt(origin.clone().add(x, 0, 0)).getRelative(BlockFace.DOWN)
                    .setType(Material.REDSTONE_BLOCK);
        }
        // Spawn cart
        origin.getWorld().spawnEntity(origin.clone().add(0, 1.5, 0), EntityType.MINECART);
    }
}
