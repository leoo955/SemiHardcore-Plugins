package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public LoginCommand(ModerationSMP plugin) {
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
        
        if (!plugin.getAuthManager().isRegistered(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("auth_not_registered"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /login <mdp>");
            return true;
        }
        
        if (plugin.getAuthManager().login(player, args[0])) {
            player.sendMessage(plugin.getMessage("auth_login_success"));
        } else {
            player.sendMessage(plugin.getMessage("auth_login_failed"));
        }
        
        return true;
    }
}
