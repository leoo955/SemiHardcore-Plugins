package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpHereCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public TpHereCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.tphere")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /tphere <joueur>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player_not_found"));
            return true;
        }
        
        target.teleport(player);
        player.sendMessage(plugin.getMessageRaw("tphere_success").replace("{player}", target.getName()));
        target.sendMessage(plugin.getMessageRaw("tphere_target").replace("{player}", player.getName()));
        
        return true;
    }
}
