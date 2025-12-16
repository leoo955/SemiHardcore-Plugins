package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SpeedCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.speed")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /speed <1-10>");
            return true;
        }
        
        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Veuillez entrer un nombre entre 1 et 10.");
            return true;
        }
        
        if (speed < 1 || speed > 10) {
            sender.sendMessage(ChatColor.RED + "La vitesse doit être entre 1 et 10.");
            return true;
        }
        
        // Convert 1-10 to Minecraft speed (0.0 to 1.0)
        // 1 = default (0.2 for walk, 0.1 for fly)
        // 10 = max (1.0)
        float walkSpeed = 0.2f * (speed / 1.0f);
        float flySpeed = 0.1f * (speed / 1.0f);
        
        // Cap at 1.0
        walkSpeed = Math.min(walkSpeed, 1.0f);
        flySpeed = Math.min(flySpeed, 1.0f);
        
        player.setWalkSpeed(walkSpeed);
        player.setFlySpeed(flySpeed);
        
        sender.sendMessage(ChatColor.GREEN + "Vitesse définie à " + speed + " !");
        
        return true;
    }
}
