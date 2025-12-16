package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.XrayManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TopLuckCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TopLuckCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        openTopLuckGui(player);
        return true;
    }
    
    private void openTopLuckGui(Player viewer) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8Top Luck (Session)");
        XrayManager manager = plugin.getXrayManager();
        
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= 54) break;
            
            UUID uuid = target.getUniqueId();
            int totalMined = manager.getTotalMined(uuid);
            
            // Only show players who mined at least one block
            if (totalMined > 0) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(target);
                meta.setDisplayName("§e" + target.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Total miné: §f" + totalMined);
                lore.add("");
                
                addOreStat(lore, manager, uuid, Material.DIAMOND_ORE, "Diamant");
                addOreStat(lore, manager, uuid, Material.ANCIENT_DEBRIS, "Netherite");
                addOreStat(lore, manager, uuid, Material.GOLD_ORE, "Or");
                
                meta.setLore(lore);
                head.setItemMeta(meta);
                
                gui.setItem(slot++, head);
            }
        }
        
        viewer.openInventory(gui);
    }
    
    private void addOreStat(List<String> lore, XrayManager manager, UUID uuid, Material ore, String name) {
        int count = manager.getOreMined(uuid, ore);
        // Ajouter aussi la version Deepslate pour le diamant/or
        if (ore == Material.DIAMOND_ORE) {
            count += manager.getOreMined(uuid, Material.DEEPSLATE_DIAMOND_ORE);
        } else if (ore == Material.GOLD_ORE) {
            count += manager.getOreMined(uuid, Material.DEEPSLATE_GOLD_ORE);
        }
        
        double percentage = manager.getPercentage(uuid, ore); // Note: getPercentage should ideally combine types too
        
        // Manual recalculation of combined percentage for precise display
        int total = manager.getTotalMined(uuid);
        double realPercentage = total > 0 ? (double) count / total * 100.0 : 0.0;
        
        String color = "§a";
        if (realPercentage > 5.0) color = "§c"; // Suspect si > 5%
        else if (realPercentage > 2.0) color = "§6"; // Un peu chanceux
        
        lore.add("§7" + name + ": " + color + String.format("%.2f", realPercentage) + "% §7(" + count + ")");
    }
}
