package com.zootycoon.managers;

import com.zootycoon.ZooTycoon;
import com.zootycoon.objects.Zoo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AnimalManager implements Listener {

    private final ZooTycoon plugin;

    public AnimalManager(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getItem() == null || event.getItem().getType() == Material.AIR)
            return;

        ItemStack item = event.getItem();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
            return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        EntityType type = null;

        // Simple mapping based on shop items
        if (name.contains("Lion"))
            type = EntityType.CAT; // Minecraft doesn't have Lions, use Cat or Ocelot
        else if (name.contains("Elephant"))
            type = EntityType.RAVAGER; // Large mob approximation? Or Sniffer? Let's use Sniffer for peaceful vibe if
                                       // available, else Cow. Actually simple mapping:
        else if (name.contains("Zebra"))
            type = EntityType.HORSE;

        // Better mapping for 1.21
        // If 1.21 logic allows, we can maybe use more specific mobs if available or
        // just stick to basics.
        // Let's assume standard mobs for now.
        if (name.equals("Lion"))
            type = EntityType.OCELOT;
        if (name.equals("Elephant"))
            type = EntityType.SNIFFER; // Sniffer is large and peaceful, better fits an Elephant than a Ravager
        if (name.equals("Zebra"))
            type = EntityType.HORSE;

        if (type != null) {
            event.setCancelled(true); // Prevent default usage
            Player player = event.getPlayer();

            // Check if placing in own zoo
            ZooManager zooManager = plugin.getZooManager();
            Zoo zoo = zooManager.getZooAt(event.getClickedBlock().getChunk());

            if (zoo == null || !zoo.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You can only place animals in your own zoo!");
                return;
            }

            Block block = event.getClickedBlock();
            Location spawnLoc = block.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);

            Entity entity = block.getWorld().spawnEntity(spawnLoc, type);
            entity.setCustomName(ChatColor.GOLD + name);
            entity.setCustomNameVisible(true);
            entity.setPersistent(true);

            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).setRemoveWhenFarAway(false);
            }

            // Remove item from hand
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(ChatColor.GREEN + "You released a " + name + "!");
        }
    }
}
