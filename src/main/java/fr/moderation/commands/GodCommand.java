package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public GodCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.god")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            // Toggle god mode for self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans argument.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Toggle god mode for other player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        // Toggle god mode
        boolean newGodState = plugin.getGodModeManager().toggleGodMode(target);
        
        String status = newGodState ? ChatColor.GREEN + "activé" : ChatColor.RED + "désactivé";
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.YELLOW + "Mode invincibilité " + status + ChatColor.YELLOW + " !");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Mode invincibilité " + status + ChatColor.YELLOW + " pour " + target.getName() + " !");
            target.sendMessage(ChatColor.YELLOW + "Mode invincibilité " + status + ChatColor.YELLOW + " par " + sender.getName() + " !");
        }
        
        return true;
    }
}
