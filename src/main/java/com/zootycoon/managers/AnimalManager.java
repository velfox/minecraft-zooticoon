package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.AnimalData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimalManager implements Listener {

    private final ZooTycoon plugin;
    // Map Entity UUID to AnimalData
    private final Map<UUID, AnimalData> animals = new HashMap<>();

    public Map<UUID, AnimalData> getAnimals() {
        return animals;
    }

    public AnimalManager(ZooTycoon plugin) {
        this.plugin = plugin;
        startSimulationTask();
    }

    private void startSimulationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Loop through all tracked animals
                animals.entrySet().removeIf(entry -> {
                    Entity entity = plugin.getServer().getEntity(entry.getKey());
                    if (entity == null || entity.isDead()) {
                        return true; // Remove dead animals
                    }

                    AnimalData data = entry.getValue();
                    data.tick();

                    // Death condition
                    if (data.getHunger() <= 0 || data.getThirst() <= 0) {
                        entity.remove();
                        return true;
                    }

                    return false;
                });
            }
        }.runTaskTimer(plugin, 20L, 100L); // Run every 5 seconds (100 ticks)
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta())
            return;

        String name = item.getItemMeta().getDisplayName();
        if (name == null)
            return;

        String type = null;
        EntityType entityType = null;

        if (name.contains("Lion") && item.getType() == Material.OCELOT_SPAWN_EGG) {
            type = "Lion";
            entityType = EntityType.OCELOT;
        } else if (name.contains("Elephant") && item.getType() == Material.SNIFFER_SPAWN_EGG) {
            type = "Elephant";
            entityType = EntityType.SNIFFER;
        } else if (name.contains("Zebra") && item.getType() == Material.HORSE_SPAWN_EGG) {
            type = "Zebra";
            entityType = EntityType.HORSE;
        }

        if (type != null) {
            event.setCancelled(true);

            // Checks if inside zoo (simplified for now, ideally check ZooManager)
            if (!plugin.getZooManager().hasZoo(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You need a zoo first! /zoo create <name>");
                return;
            }

            // Check if inside enclosure
            if (!plugin.getEnclosureManager().isLocationValid(event.getClickedBlock().getLocation(),
                    event.getPlayer().getUniqueId())) {
                event.getPlayer()
                        .sendMessage(ChatColor.RED + "You can only spawn animals inside a purchased Enclosure!");
                return;
            }

            LivingEntity entity = (LivingEntity) event.getClickedBlock().getWorld().spawnEntity(
                    event.getClickedBlock().getLocation().add(0, 1, 0), entityType);

            entity.setCustomName(ChatColor.GOLD + type);
            entity.setCustomNameVisible(true);
            entity.setRemoveWhenFarAway(false);

            // Register Data
            animals.put(entity.getUniqueId(), new AnimalData(entity.getUniqueId(), type));

            event.getPlayer().sendMessage(ChatColor.GREEN + "You placed a " + type + "!");

            // Consume egg
            if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                event.getPlayer().getInventory().getItemInMainHand().setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (animals.containsKey(event.getRightClicked().getUniqueId())) {
            AnimalData data = animals.get(event.getRightClicked().getUniqueId());
            Player player = event.getPlayer();

            // Display Stats
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(data.getStatusString()));

            // Feeding Logic (Simplified)
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.WHEAT || hand.getType() == Material.APPLE
                    || hand.getType() == Material.BEEF) {
                data.setHunger(data.getHunger() + 20);
                player.sendMessage(ChatColor.GREEN + "You fed the " + data.getType() + "!");
                hand.setAmount(hand.getAmount() - 1);
            } else if (hand.getType() == Material.WATER_BUCKET) {
                data.setThirst(data.getThirst() + 100);
                player.sendMessage(ChatColor.AQUA + "You gave water to the " + data.getType() + "!");
                // Keep bucket logic omitted for brevity
            }
        }
    }
}
