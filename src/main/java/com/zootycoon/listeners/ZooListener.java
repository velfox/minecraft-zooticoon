package com.zootycoon.listeners;

import com.zootycoon.ZooTycoon;
import com.zootycoon.managers.ZooManager;
import com.zootycoon.objects.Zoo;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class ZooListener implements Listener {

    private final ZooTycoon plugin;

    public ZooListener(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        ZooManager manager = plugin.getZooManager();
        Zoo zoo = manager.getZooAt(chunk);

        if (zoo != null) {
            // If the player doing the breaking is NOT the owner
            if (!zoo.getOwner().equals(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot build in " + zoo.getName());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        ZooManager manager = plugin.getZooManager();
        Zoo zoo = manager.getZooAt(chunk);

        if (zoo != null) {
            if (!zoo.getOwner().equals(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot build in " + zoo.getName());
            }
        }
    }
}
