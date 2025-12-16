package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpAllCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TpAllCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.tpall")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        List<String> teleportedPlayers = new ArrayList<>();
        
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                // Save their location before teleporting
                plugin.getLastLocationManager().setLastLocation(online, online.getLocation());
                
                online.teleport(location);
                teleportedPlayers.add(online.getName());
                online.sendMessage(ChatColor.YELLOW + "Vous avez été téléporté vers " + player.getName() + " !");
            }
        }
        
        if (teleportedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Aucun joueur à téléporter.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Tous les joueurs ont été téléportés vers vous ! (" + teleportedPlayers.size() + " joueur(s))");
        }
        
        return true;
    }
}
