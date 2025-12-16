package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public FreezeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.freeze")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /freeze <joueur>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        
        boolean nowFrozen = plugin.getFreezeManager().toggleFreeze(target);
        
        if (nowFrozen) {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " a été gelé !");
            target.sendMessage(ChatColor.RED + "═══════════════════════════");
            target.sendMessage(ChatColor.RED + "    VOUS AVEZ ÉTÉ GELÉ !");
            target.sendMessage(ChatColor.YELLOW + " Ne vous déconnectez pas !");
            target.sendMessage(ChatColor.GRAY + "   Un modérateur souhaite");
            target.sendMessage(ChatColor.GRAY + "     vous parler.");
            target.sendMessage(ChatColor.RED + "═══════════════════════════");
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 0.5f);
        } else {
            sender.sendMessage(ChatColor.GREEN + target.getName() + " a été dégelé !");
            target.sendMessage(ChatColor.GREEN + "Vous avez été dégelé ! Vous pouvez bouger.");
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.0f);
        }
        
        return true;
    }
}
