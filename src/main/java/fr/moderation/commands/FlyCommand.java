package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public FlyCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.fly")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            // Toggle fly for self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans argument.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Toggle fly for other player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        // Toggle fly
        boolean newFlyState = !target.getAllowFlight();
        target.setAllowFlight(newFlyState);
        
        if (!newFlyState && target.isFlying()) {
            target.setFlying(false);
        }
        
        String status = newFlyState ? ChatColor.GREEN + "activé" : ChatColor.RED + "désactivé";
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.YELLOW + "Mode vol " + status + ChatColor.YELLOW + " !");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Mode vol " + status + ChatColor.YELLOW + " pour " + target.getName() + " !");
            target.sendMessage(ChatColor.YELLOW + "Mode vol " + status + ChatColor.YELLOW + " par " + sender.getName() + " !");
        }
        
        return true;
    }
}
