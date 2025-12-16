package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public RegisterCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getAuthManager().isLoggedIn(player)) {
            player.sendMessage(plugin.getMessage("auth_already_logged"));
            return true;
        }
        
        if (plugin.getAuthManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("auth_already_registered"));
            return true;
        }
        
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /register <mdp> <mdp>");
            return true;
        }
        
        if (!args[0].equals(args[1])) {
            player.sendMessage(plugin.getMessage("auth_password_mismatch"));
            return true;
        }
        
        plugin.getAuthManager().register(player, args[0]);
        player.sendMessage(plugin.getMessage("auth_register_success"));
        
        return true;
    }
}
