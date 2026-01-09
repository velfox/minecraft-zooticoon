package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.GuestData;
import com.zootycoon.objects.Zoo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuestManager implements Listener {
    private final ZooTycoon plugin;
    private final Map<UUID, GuestData> guests = new HashMap<>();

    public GuestManager(ZooTycoon plugin) {
        this.plugin = plugin;
        startGuestCycle();
    }

    public void spawnGuest(Location location) {
        Villager guest = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        guest.setCustomName(ChatColor.AQUA + "Guest");
        guest.setCustomNameVisible(true);
        guest.setProfession(Villager.Profession.NITWIT);
        guest.setAI(true); // Let them wander for now (Phase 2 simple AI)

        guests.put(guest.getUniqueId(), new GuestData(guest.getUniqueId()));
    }

    private void startGuestCycle() {
        // Spawner Task: Spawns a guest every 30 seconds at a random Zoo's entrance (if
        // set)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Simplified: Just spawn near a random player for demo purposes if they have a
                // zoo
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Zoo zoo = plugin.getZooManager().getZoo(p.getUniqueId());
                    if (zoo != null && Math.random() < 0.3) {
                        // Spawn guest near player
                        spawnGuest(p.getLocation().add(5, 0, 0));
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 600L); // Every 30s

        // AI Logic Task
        new BukkitRunnable() {
            @Override
            public void run() {
                guests.entrySet().removeIf(entry -> {
                    Entity entity = plugin.getServer().getEntity(entry.getKey());
                    if (entity == null || entity.isDead())
                        return true;

                    GuestData data = entry.getValue();
                    data.tick();

                    // Simple interaction: if entity is near an animal, maybe boost happiness?
                    // Future: Pathfinding

                    return false;
                });
            }
        }.runTaskTimer(plugin, 20L, 40L); // Every 2s
    }

    @EventHandler
    public void onGuestClick(PlayerInteractEntityEvent event) {
        if (guests.containsKey(event.getRightClicked().getUniqueId())) {
            GuestData data = guests.get(event.getRightClicked().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GOLD + "--- Guest Info ---");
            event.getPlayer().sendMessage(data.getStatus());
        }
    }
}
