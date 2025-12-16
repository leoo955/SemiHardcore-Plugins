package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public SpecCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("moderation.spec")) {
            player.sendMessage(plugin.getMessage("no_permission"));
            return true;
        }
        
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
            player.sendMessage(plugin.getMessage("prefix") + "§aMode survie activé.");
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
            player.sendMessage(plugin.getMessage("prefix") + "§aMode spectateur (Xray Admin) activé.");
        }
        
        return true;
    }
}
