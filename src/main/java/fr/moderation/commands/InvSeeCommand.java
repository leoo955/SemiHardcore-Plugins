package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvSeeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public InvSeeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.invsee")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(plugin.getMessage("prefix") + "Usage: /invsee <joueur>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("player_not_found"));
            return true;
        }
        
        player.openInventory(target.getInventory());
        player.sendMessage(plugin.getMessageRaw("invsee_opened").replace("{player}", target.getName()));
        
        return true;
    }
}
