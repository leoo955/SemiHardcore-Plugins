package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FantomeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    public FantomeCommand(ModerationSMP plugin, DeathWorldManager deathWorldManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in death world
        if (!deathWorldManager.isInDeathWorld(player)) {
            player.sendMessage(ChatColor.RED + "Vous devez être dans le Death Dimension pour devenir fantôme !");
            return true;
        }
        
        // Switch to ghost mode (spectator in real world)
        deathWorldManager.setPlayerAsGhost(player);
        
        return true;
    }
}
