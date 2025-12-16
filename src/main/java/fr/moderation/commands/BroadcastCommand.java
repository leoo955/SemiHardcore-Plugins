package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public BroadcastCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.broadcast")) {
            sender.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("prefix") + "Usage: /broadcast <message>");
            return true;
        }
        
        String message = String.join(" ", args);
        String format = plugin.getMessageRaw("broadcast_format").replace("{message}", message);
        
        Bukkit.broadcastMessage(ColorUtils.colorize(format));
        
        return true;
    }
}
