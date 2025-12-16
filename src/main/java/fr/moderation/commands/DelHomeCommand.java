package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public DelHomeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.delhome")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /delhome <nom>");
            return true;
        }
        
        String homeName = args[0];
        
        if (plugin.getHomeManager().getHome(player, homeName) == null) {
            player.sendMessage(plugin.getMessageRaw("home_not_found").replace("{home}", homeName));
            return true;
        }
        
        plugin.getHomeManager().deleteHome(player, homeName);
        player.sendMessage(plugin.getMessageRaw("home_deleted").replace("{home}", homeName));
        
        return true;
    }
}
