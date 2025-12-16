package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {
    
    private final ModerationSMP plugin;
    
    public GamemodeCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.gamemode")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /gamemode <mode> [joueur]");
            sender.sendMessage(ChatColor.GRAY + "Modes: survival/0/s, creative/1/c, adventure/2/a, spectator/3/sp");
            return true;
        }
        
        GameMode gameMode = parseGameMode(args[0]);
        if (gameMode == null) {
            sender.sendMessage(ChatColor.RED + "Mode de jeu invalide.");
            sender.sendMessage(ChatColor.GRAY + "Modes: survival/0/s, creative/1/c, adventure/2/a, spectator/3/sp");
            return true;
        }
        
        Player target;
        
        if (args.length == 1) {
            // Change gamemode for self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande sans spécifier de joueur.");
                return true;
            }
            target = (Player) sender;
        } else {
            // Change gamemode for other player
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }
        }
        
        target.setGameMode(gameMode);
        
        String modeName = getGameModeName(gameMode);
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Mode de jeu défini sur " + modeName + " !");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Mode de jeu de " + target.getName() + " défini sur " + modeName + " !");
            target.sendMessage(ChatColor.GREEN + "Votre mode de jeu a été défini sur " + modeName + " par " + sender.getName() + " !");
        }
        
        return true;
    }
    
    private GameMode parseGameMode(String input) {
        String lower = input.toLowerCase();
        
        switch (lower) {
            case "survival":
            case "s":
            case "0":
                return GameMode.SURVIVAL;
            case "creative":
            case "c":
            case "1":
                return GameMode.CREATIVE;
            case "adventure":
            case "a":
            case "2":
                return GameMode.ADVENTURE;
            case "spectator":
            case "sp":
            case "3":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }
    
    private String getGameModeName(GameMode mode) {
        switch (mode) {
            case SURVIVAL: return ChatColor.GREEN + "Survie";
            case CREATIVE: return ChatColor.AQUA + "Créatif";
            case ADVENTURE: return ChatColor.GOLD + "Aventure";
            case SPECTATOR: return ChatColor.GRAY + "Spectateur";
            default: return mode.name();
        }
    }
}
