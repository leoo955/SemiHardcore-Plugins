package fr.moderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class GetHeadCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }
        
        Player admin = (Player) sender;
        
        if (args.length < 1) {
            admin.sendMessage(ChatColor.RED + "Usage: /gethead <joueur>");
            return true;
        }
        
        String targetName = args[0];
        
        // Chercher le joueur (en ligne ou hors ligne)
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
        
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            admin.sendMessage(ChatColor.RED + "Joueur introuvable: " + targetName);
            return true;
        }
        
        // Create player head with UUID in lore
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(targetPlayer);
            meta.setDisplayName(ChatColor.GOLD + "Tête de " + targetPlayer.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Utilisez cette tête pour");
            lore.add(ChatColor.GRAY + "ressusciter ce joueur");
            lore.add(ChatColor.DARK_GRAY + "" + targetPlayer.getUniqueId()); // Hidden UUID
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        // Give head to admin
        admin.getInventory().addItem(head);
        admin.sendMessage(ChatColor.GREEN + "Vous avez reçu la tête de " + ChatColor.YELLOW + targetPlayer.getName());
        
        return true;
    }
}
