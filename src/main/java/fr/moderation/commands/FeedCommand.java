package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FeedCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public FeedCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.feed")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            // Feed self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans argument.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Feed other player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        // Feed the player
        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        target.setExhaustion(0.0f);
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Vous avez été nourri !");
        } else {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " a été nourri !");
            target.sendMessage(ChatColor.GREEN + "Vous avez été nourri par " + sender.getName() + " !");
        }
        
        return true;
    }
}
