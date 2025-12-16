package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetJailCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SetJailCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.setjail")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        // /setjail <nom>
        if (args.length < 1) {
            player.sendMessage(plugin.getMessage("prefix") + "§cUsage: /setjail <nom>");
            return true;
        }
        
        String jailName = args[0];
        
        // Sauvegarder la prison
        plugin.getJailManager().saveJail(jailName, player.getLocation());
        
        player.sendMessage(plugin.getMessage("prefix") + "§aPrison §e" + jailName + " §adéfinie à votre position actuelle.");
        
        return true;
    }
}
