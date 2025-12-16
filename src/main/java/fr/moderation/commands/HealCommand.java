package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public HealCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.heal")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            // Heal self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans argument.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Heal other player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        // Heal the player
        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        target.setHealth(maxHealth);
        target.setFoodLevel(20);
        target.setSaturation(20.0f);
        target.setFireTicks(0);
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Vous avez été soigné !");
        } else {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " a été soigné !");
            target.sendMessage(ChatColor.GREEN + "Vous avez été soigné par " + sender.getName() + " !");
        }
        
        return true;
    }
}
