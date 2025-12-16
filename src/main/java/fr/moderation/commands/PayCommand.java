package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public PayCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /pay <joueur> <montant>");
            return true;
        }
        
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous payer vous-même !");
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Montant invalide !");
            return true;
        }
        
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Le montant doit être positif !");
            return true;
        }
        
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent !");
            player.sendMessage(ChatColor.GRAY + "Votre solde: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player.getUniqueId())));
            return true;
        }
        
        // Effectuer le transfert
        plugin.getEconomyManager().transfer(player.getUniqueId(), target.getUniqueId(), amount);
        
        // Messages
        player.sendMessage(ChatColor.GREEN + "Vous avez envoyé " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount) + ChatColor.GREEN + 
                         " à " + ChatColor.YELLOW + target.getName());
        
        target.sendMessage(ChatColor.GREEN + "Vous avez reçu " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount) + ChatColor.GREEN + 
                         " de " + ChatColor.YELLOW + player.getName());
        
        return true;
    }
}
