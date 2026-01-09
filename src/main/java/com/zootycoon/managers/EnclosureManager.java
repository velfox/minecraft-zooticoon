package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.Enclosure;
import com.zootycoon.objects.Zoo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class EnclosureManager implements Listener {

    private final ZooTycoon plugin;

    public EnclosureManager(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    public void giveWand(Player player, String type) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Enclosure Wand: " + type);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click ground to place center.",
                ChatColor.GRAY + "Creates a 10x10 fence area."));
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "Received Enclosure Wand for " + type);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.contains("Enclosure Wand"))
            return;

        Player player = event.getPlayer();
        if (!plugin.getZooManager().hasZoo(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must own a zoo to place enclosures!");
            return;
        }

        String type = ChatColor.stripColor(displayName).replace("Enclosure Wand: ", "");
        Location center = event.getClickedBlock().getLocation().add(0, 1, 0); // Above clicked block

        Zoo zoo = plugin.getZooManager().getZoo(player.getUniqueId());

        // Build Structure
        buildEnclosure(center, 10, Material.OAK_FENCE); // 10 radius

        // Save Data
        Enclosure enclosure = new Enclosure(center, 10, type);
        zoo.addEnclosure(enclosure);

        player.sendMessage(ChatColor.GREEN + "Built " + type + " Enclosure!");
        item.setAmount(item.getAmount() - 1); // Consume wand
        event.setCancelled(true);
    }

    private void buildEnclosure(Location center, int radius, Material fenceType) {
        // Simple square fence
        int y = center.getBlockY();
        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // Perimeter only
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    loc.getBlock().setType(fenceType);
                    // Clear block above to ensure 2-high or just visibility? Let's just do 1 high
                    // for now.
                }
            }
        }
    }

    public boolean isLocationValid(Location loc, UUID playerId) {
        if (!plugin.getZooManager().hasZoo(playerId))
            return false;
        return plugin.getZooManager().getZoo(playerId).isInsideEnclosure(loc);
    }
}
