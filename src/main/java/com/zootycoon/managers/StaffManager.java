package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.AnimalData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffManager {

    private final ZooTycoon plugin;
    private final List<UUID> zookeepers = new ArrayList<>();

    public StaffManager(ZooTycoon plugin) {
        this.plugin = plugin;
        startStaffAI();
    }

    public void hireZookeeper(Location location) {
        Villager keeper = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        keeper.setCustomName(ChatColor.GREEN + "Zookeeper");
        keeper.setCustomNameVisible(true);
        keeper.setProfession(Villager.Profession.FARMER);
        keeper.setAI(true);

        zookeepers.add(keeper.getUniqueId());
    }

    private void startStaffAI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // For each zookeeper
                zookeepers.removeIf(uuid -> {
                    Entity entity = plugin.getServer().getEntity(uuid);
                    if (entity == null || entity.isDead())
                        return true;

                    LivingEntity keeper = (LivingEntity) entity;

                    // Look for hungry animals nearby
                    plugin.getAnimalManager().getAnimals().forEach((animId, animData) -> {
                        Entity animInfo = plugin.getServer().getEntity(animId);
                        if (animInfo != null && !animInfo.isDead()) {
                            if (animInfo.getLocation().getWorld().equals(keeper.getWorld())) {
                                if (animInfo.getLocation().distance(keeper.getLocation()) < 10) {
                                    // Check Needs
                                    if (animData.getHunger() < 50) {
                                        // Feed
                                        animData.setHunger(animData.getHunger() + 30);
                                        keeper.getWorld().spawnParticle(org.bukkit.Particle.HEART,
                                                animInfo.getLocation().add(0, 1, 0), 5);
                                        keeper.getWorld().playSound(animInfo.getLocation(), Sound.ENTITY_GENERIC_EAT, 1,
                                                1);
                                    }
                                    if (animData.getThirst() < 50) {
                                        // Water
                                        animData.setThirst(animData.getThirst() + 30);
                                        keeper.getWorld().spawnParticle(org.bukkit.Particle.SPLASH,
                                                animInfo.getLocation().add(0, 1, 0), 10);
                                    }
                                }
                            }
                        }
                    });

                    return false;
                });
            }
        }.runTaskTimer(plugin, 40L, 100L); // Check every 5 seconds
    }
}
