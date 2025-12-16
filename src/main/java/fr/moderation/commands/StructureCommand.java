package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.StructureManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StructureCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    private final StructureManager structureManager;
    
    public StructureCommand(ModerationSMP plugin, StructureManager structureManager) {
        this.plugin = plugin;
        this.structureManager = structureManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "save":
                handleSave(player, args);
                break;
            case "load":
                handleLoad(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "setpuzzle":
                handleSetPuzzle(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /structure save <nom>");
            return;
        }
        
        String name = args[1];
        
        if (structureManager.saveStructure(player, name)) {
            player.sendMessage(ChatColor.GREEN + "Structure '" + name + "' sauvegardée avec succès !");
        } else {
            player.sendMessage(ChatColor.RED + "Erreur lors de la sauvegarde. Assurez-vous d'avoir une sélection WorldEdit.");
        }
    }
    
    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /structure load <nom>");
            return;
        }
        
        String name = args[1];
        
        if (!structureManager.structureExists(name)) {
            player.sendMessage(ChatColor.RED + "Structure '" + name + "' introuvable.");
            return;
        }
        
        if (structureManager.pasteStructure(name, player.getLocation())) {
            player.sendMessage(ChatColor.GREEN + "Structure '" + name + "' chargée à votre position !");
        } else {
            player.sendMessage(ChatColor.RED + "Erreur lors du chargement de la structure.");
        }
    }
    
    private void handleList(Player player) {
        List<String> structures = structureManager.listStructures();
        
        if (structures.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Aucune structure sauvegardée.");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Structures disponibles ===");
        for (String structure : structures) {
            player.sendMessage(ChatColor.YELLOW + "- " + structure);
        }
    }
    
    private void handleSetPuzzle(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /structure setpuzzle <star|parkour> <nom>");
            return;
        }
        
        String puzzleType = args[1].toLowerCase();
        String structureName = args[2];
        
        if (!puzzleType.equals("star") && !puzzleType.equals("parkour")) {
            player.sendMessage(ChatColor.RED + "Type de puzzle invalide. Utilisez: star ou parkour");
            return;
        }
        
        if (!structureManager.structureExists(structureName)) {
            player.sendMessage(ChatColor.RED + "Structure '" + structureName + "' introuvable.");
            return;
        }
        
        structureManager.setPuzzleStructure(puzzleType, structureName);
        player.sendMessage(ChatColor.GREEN + "Structure '" + structureName + "' définie pour le puzzle " + puzzleType.toUpperCase() + " !");
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Commandes Structure ===");
        player.sendMessage(ChatColor.YELLOW + "/structure save <nom> " + ChatColor.GRAY + "- Sauvegarder votre sélection WorldEdit");
        player.sendMessage(ChatColor.YELLOW + "/structure load <nom> " + ChatColor.GRAY + "- Charger une structure à votre position");
        player.sendMessage(ChatColor.YELLOW + "/structure list " + ChatColor.GRAY + "- Lister toutes les structures");
        player.sendMessage(ChatColor.YELLOW + "/structure setpuzzle <star|parkour> <nom> " + ChatColor.GRAY + "- Définir la structure d'un puzzle");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("save", "load", "list", "setpuzzle"), args[0]);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("load")) {
                return filter(structureManager.listStructures(), args[1]);
            }
            if (args[0].equalsIgnoreCase("setpuzzle")) {
                return filter(Arrays.asList("star", "parkour"), args[1]);
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("setpuzzle")) {
            return filter(structureManager.listStructures(), args[2]);
        }
        
        return Collections.emptyList();
    }
    
    private List<String> filter(List<String> list, String input) {
        if (input == null || input.isEmpty()) {
            return list;
        }
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
