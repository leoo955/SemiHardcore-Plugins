package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public ScoreboardCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            // Toggle scoreboard on/off
            if (player.getScoreboard().getObjective("main") != null) {
                plugin.getScoreboardManager().removeScoreboard(player);
                player.sendMessage(ChatColor.YELLOW + "Scoreboard désactivé !");
            } else {
                plugin.getScoreboardManager().updateScoreboard(player);
                player.sendMessage(ChatColor.GREEN + "Scoreboard activé !");
            }
        } else {
            // Refresh scoreboard
            plugin.getScoreboardManager().refreshPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Scoreboard rafraîchi !");
        }
        
        return true;
    }
}
