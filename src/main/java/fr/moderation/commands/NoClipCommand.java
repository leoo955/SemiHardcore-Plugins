package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoClipCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public NoClipCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.isOp()) {
            player.sendMessage(plugin.getMessage("prefix") + "§cCette commande est réservée aux OPs !");
            return true;
        }
        
        boolean enabled = plugin.getNoClipManager().toggleNoClip(player);
        
        if (enabled) {
            player.sendMessage(plugin.getMessage("prefix") + "§aNoClip §2activé§a ! Vous pouvez traverser les blocs.");
        } else {
            player.sendMessage(plugin.getMessage("prefix") + "§cNoClip §4désactivé§c !");
        }
        
        return true;
    }
}
