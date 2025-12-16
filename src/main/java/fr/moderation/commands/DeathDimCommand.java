package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import fr.moderation.managers.PuzzleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathDimCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    private final PuzzleManager puzzleManager;
    
    public DeathDimCommand(ModerationSMP plugin, DeathWorldManager deathWorldManager, PuzzleManager puzzleManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
        this.puzzleManager = puzzleManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "tp":
                handleTeleport(sender, args);
                break;
            case "setspawn":
                handleSetSpawn(sender);
                break;
            case "reset":
                handleReset(sender, args);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "toggle":
                handleToggle(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("moderation.deathdim.tp")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /deathdim tp <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        deathWorldManager.teleportToDeathWorld(target);
        sender.sendMessage(ChatColor.GREEN + "Téléportation de " + target.getName() + " dans le monde de la mort.");
    }
    
    private void handleSetSpawn(CommandSender sender) {
        if (!sender.hasPermission("moderation.deathdim.setspawn")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande ne peut être exécutée que par un joueur.");
            return;
        }
        
        Player player = (Player) sender;
        
        if (!deathWorldManager.isDeathWorld(player.getWorld())) {
            sender.sendMessage(ChatColor.RED + "Vous devez être dans le monde de la mort pour définir son spawn.");
            return;
        }
        
        deathWorldManager.setDeathSpawn(player.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Spawn du monde de la mort défini à votre position.");
    }
    
    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("moderation.deathdim.reset")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /deathdim reset <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        if (!deathWorldManager.isInDeathWorld(target)) {
            sender.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans le monde de la mort.");
            return;
        }
        
        puzzleManager.resetPuzzle(target);
        sender.sendMessage(ChatColor.GREEN + "Énigme de " + target.getName() + " réinitialisée.");
    }
    
    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission("moderation.deathdim.info")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }
        
        World deathWorld = deathWorldManager.getDeathWorld();
        int playersInDeathWorld = deathWorldManager.getPlayersInDeathWorld().size();
        boolean enabled = plugin.getConfig().getBoolean("death-dimension.enabled", true);
        
        sender.sendMessage(ChatColor.GOLD + "╔═══════════════════════════════════╗");
        sender.sendMessage(ChatColor.GOLD + "║  " + ChatColor.BOLD + "Death Dimension Info" + ChatColor.GOLD + "         ║");
        sender.sendMessage(ChatColor.GOLD + "╚═══════════════════════════════════╝");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Monde: " + ChatColor.WHITE + (deathWorld != null ? deathWorld.getName() : "N/A"));
        sender.sendMessage(ChatColor.YELLOW + "Status: " + (enabled ? ChatColor.GREEN + "Activé" : ChatColor.RED + "Désactivé"));
        sender.sendMessage(ChatColor.YELLOW + "Joueurs présents: " + ChatColor.WHITE + playersInDeathWorld);
        sender.sendMessage("");
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("moderation.deathdim.reload")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return;
        }
        
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration du monde de la mort rechargée !");
    }
    
    private void handleToggle(CommandSender sender) {
        boolean currentState = plugin.getConfig().getBoolean("death-dimension.enabled", true);
        boolean newState = !currentState;
        
        plugin.getConfig().set("death-dimension.enabled", newState);
        plugin.saveConfig();
        
        if (newState) {
            sender.sendMessage(ChatColor.GREEN + "Le système de monde de la mort a été " + ChatColor.BOLD + "ACTIVÉ" + ChatColor.GREEN + " !");
            sender.sendMessage(ChatColor.GRAY + "Les joueurs seront envoyés dans le death world à leur mort.");
        } else {
            sender.sendMessage(ChatColor.RED + "Le système de monde de la mort a été " + ChatColor.BOLD + "DÉSACTIVÉ" + ChatColor.RED + " !");
            sender.sendMessage(ChatColor.GRAY + "Les joueurs mourront normalement sans être téléportés.");
        }
        
        // Notifier tous les admins en ligne
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.hasPermission("moderation.deathdim") && !online.equals(sender)) {
                online.sendMessage(ChatColor.YELLOW + "[Death World] " + 
                                 (newState ? ChatColor.GREEN + "Activé" : ChatColor.RED + "Désactivé") + 
                                 ChatColor.YELLOW + " par " + sender.getName());
            }
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Death Dimension ---");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim tp <joueur>" + ChatColor.GRAY + " - Téléporter dans le death world");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim setspawn" + ChatColor.GRAY + " - Définir le spawn du death world");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim reset <joueur>" + ChatColor.GRAY + " - Reset les puzzles d'un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim info" + ChatColor.GRAY + " - Informations sur le death world");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim toggle" + ChatColor.GRAY + " - Activer/Désactiver le système");
        sender.sendMessage(ChatColor.YELLOW + "/deathdim reload" + ChatColor.GRAY + " - Recharger la config");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("tp", "setspawn", "reset", "info", "toggle", "reload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("reset")) {
                // Suggest player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
