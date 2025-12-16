package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.GuisterManager;
import org.bukkit.Bukkit;
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
import java.util.UUID;

public class GuisterCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    private final GuisterManager guisterManager;
    
    public GuisterCommand(ModerationSMP plugin, GuisterManager guisterManager) {
        this.plugin = plugin;
        this.guisterManager = guisterManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Seuls les OP peuvent utiliser cette commande.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                handleAdd(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /guister add <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        if (guisterManager.isGuister(target.getUniqueId())) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " est déjà un Guister.");
            return;
        }
        
        guisterManager.addGuister(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + target.getName() + " est maintenant un Guister.");
        target.sendMessage(ChatColor.GOLD + "Vous avez reçu le rôle Guister. Vous ne pouvez pas rejoindre de team.");
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /guister remove <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        if (!guisterManager.isGuister(target.getUniqueId())) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " n'est pas un Guister.");
            return;
        }
        
        guisterManager.removeGuister(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Rôle Guister retiré à " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "Votre rôle Guister a été retiré. Vous pouvez maintenant rejoindre une team.");
    }
    
    private void handleList(CommandSender sender) {
        if (guisterManager.getAllGuisters().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Aucun Guister actuellement.");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "--- Liste des Guisters ---");
        for (UUID uuid : guisterManager.getAllGuisters()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                sender.sendMessage(ChatColor.YELLOW + "- " + player.getName() + ChatColor.GRAY + " (en ligne)");
            } else {
                sender.sendMessage(ChatColor.GRAY + "- " + Bukkit.getOfflinePlayer(uuid).getName() + " (hors ligne)");
            }
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Commandes Guister ---");
        sender.sendMessage(ChatColor.YELLOW + "/guister add <joueur> " + ChatColor.GRAY + "- Assigner le rôle Guister");
        sender.sendMessage(ChatColor.YELLOW + "/guister remove <joueur> " + ChatColor.GRAY + "- Retirer le rôle Guister");
        sender.sendMessage(ChatColor.YELLOW + "/guister list " + ChatColor.GRAY + "- Lister les Guisters");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("add", "remove", "list");
            return filter(subcommands, args[0]);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return null; // Return null to show online players
            }
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
