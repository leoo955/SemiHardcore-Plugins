package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PvpCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public PvpCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.pvp")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return true;
        }
        
        boolean currentState = plugin.getConfig().getBoolean("pvp.enabled", true);
        boolean newState;
        
        if (args.length == 0) {
            // Toggle
            newState = !currentState;
        } else {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("true") || arg.equals("enable")) {
                newState = true;
            } else if (arg.equals("off") || arg.equals("false") || arg.equals("disable")) {
                newState = false;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /pvp [on|off]");
                return true;
            }
        }
        
        plugin.getConfig().set("pvp.enabled", newState);
        plugin.saveConfig();
        
        String status = newState ? ChatColor.GREEN + "ACTIVÉ" : ChatColor.RED + "DÉSACTIVÉ";
        String message = ChatColor.GOLD + "PvP " + status + ChatColor.GOLD + " sur le serveur !";
        
        Bukkit.broadcastMessage(message);
        
        return true;
    }
}
