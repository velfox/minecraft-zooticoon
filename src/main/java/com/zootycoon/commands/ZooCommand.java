package com.zootycoon.commands;

import com.zootycoon.ZooTycoon;
import com.zootycoon.managers.ZooManager;
import com.zootycoon.objects.Zoo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class ZooCommand implements CommandExecutor {

    private final ZooTycoon plugin;

    public ZooCommand(ZooTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        Player player = (Player) sender;
        ZooManager zooManager = plugin.getZooManager();

        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Zoo Tycoon Help:");
            player.sendMessage("/zoo create <name> - Create your zoo");
            player.sendMessage("/zoo claim - Claim current chunk");
            player.sendMessage("/zoo info - Info about current zoo");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /zoo create <name>");
                return true;
            }
            if (zooManager.hasZoo(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You already have a zoo!");
                return true;
            }
            String name = args[1]; // simplified, ideally join args
            zooManager.createZoo(player, name);
            player.sendMessage(ChatColor.GREEN + "Zoo '" + name + "' created!");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            if (!zooManager.hasZoo(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You don't have a zoo! Use /zoo create first.");
                return true;
            }
            Zoo zoo = zooManager.getZoo(player.getUniqueId());
            Chunk chunk = player.getLocation().getChunk();

            if (zoo.isChunkClaimed(chunk)) {
                player.sendMessage(ChatColor.RED + "You already own this chunk.");
                return true;
            }

            Zoo existingZoo = zooManager.getZooAt(chunk);
            if (existingZoo != null) {
                player.sendMessage(ChatColor.RED + "This chunk is owned by " + existingZoo.getName());
                return true;
            }

            zoo.addChunk(chunk);
            player.sendMessage(ChatColor.GREEN + "Chunk claimed for your zoo!");
            zooManager.saveZoos();
            return true;
        }

        if (args[0].equalsIgnoreCase("visit")) {
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /zoo visit <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            UUID targetId;
            String targetName = args[1];

            if (target != null) {
                targetId = target.getUniqueId();
                targetName = target.getName();
            } else {
                // Try offline lookup (simplified, usually requires more complex lookup or just
                // cached UUIDs)
                // For now, only support online or if we iterate cache
                org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
                targetId = offline.getUniqueId();
            }

            if (!zooManager.hasZoo(targetId)) {
                player.sendMessage(ChatColor.RED + targetName + " does not have a zoo!");
                return true;
            }

            Zoo zoo = zooManager.getZoo(targetId);
            player.teleport(zoo.getEntrance());
            player.sendMessage(ChatColor.GREEN + "Teleported to " + zoo.getName() + "!");
            zoo.addVisit();
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            plugin.getGuiManager().openShop(player); // For now just open shop, can extend to main menu
            return true;
        }

        return true;
    }
}
