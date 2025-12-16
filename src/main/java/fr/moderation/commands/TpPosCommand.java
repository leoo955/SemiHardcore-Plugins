package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpPosCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TpPosCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.tppos")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /tppos <x> <y> <z> [world]");
            return true;
        }
        
        double x, y, z;
        
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Les coordonnées doivent être des nombres.");
            return true;
        }
        
        World world = player.getWorld();
        
        // Check if world is specified
        if (args.length >= 4) {
            World specifiedWorld = Bukkit.getWorld(args[3]);
            if (specifiedWorld == null) {
                sender.sendMessage(ChatColor.RED + "Monde introuvable: " + args[3]);
                return true;
            }
            world = specifiedWorld;
        }
        
        // Save current location
        plugin.getLastLocationManager().setLastLocation(player, player.getLocation());
        
        Location targetLocation = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
        player.teleport(targetLocation);
        
        sender.sendMessage(ChatColor.GREEN + "Téléportation vers " + String.format("%.1f, %.1f, %.1f", x, y, z) + " dans " + world.getName() + " !");
        
        return true;
    }
}
