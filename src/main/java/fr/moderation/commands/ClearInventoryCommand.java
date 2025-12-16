package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearInventoryCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public ClearInventoryCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.clear")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            // Clear self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans argument.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Clear other player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        // Clear inventory
        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        target.getInventory().setExtraContents(null);
        target.updateInventory();
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Votre inventaire a été vidé !");
        } else {
            sender.sendMessage(ChatColor.GREEN + "L'inventaire de " + target.getName() + " a été vidé !");
            target.sendMessage(ChatColor.RED + "Votre inventaire a été vidé par " + sender.getName() + " !");
        }
        
        return true;
    }
}
