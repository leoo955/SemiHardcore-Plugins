package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public BalanceCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "  Votre solde: " + ChatColor.GREEN + plugin.getEconomyManager().format(balance));
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════");
        
        return true;
    }
}
