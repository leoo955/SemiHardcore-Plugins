package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcoCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    
    public EcoCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moderation.eco")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
            case "add":
                handleGive(sender, args);
                break;
            case "take":
            case "remove":
                handleTake(sender, args);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "reset":
                handleReset(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco give <joueur> <montant>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Montant invalide !");
            return;
        }
        
        plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
        sender.sendMessage(ChatColor.GREEN + "Ajouté " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount) + ChatColor.GREEN + 
                         " à " + ChatColor.YELLOW + target.getName());
        target.sendMessage(ChatColor.GREEN + "Vous avez reçu " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount));
    }
    
    private void handleTake(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco take <joueur> <montant>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Montant invalide !");
            return;
        }
        
        if (plugin.getEconomyManager().removeBalance(target.getUniqueId(), amount)) {
            sender.sendMessage(ChatColor.GREEN + "Retiré " + ChatColor.GOLD + 
                             plugin.getEconomyManager().format(amount) + ChatColor.GREEN + 
                             " à " + ChatColor.YELLOW + target.getName());
            target.sendMessage(ChatColor.RED + "Vous avez perdu " + ChatColor.GOLD + 
                             plugin.getEconomyManager().format(amount));
        } else {
            sender.sendMessage(ChatColor.RED + "Le joueur n'a pas assez d'argent !");
        }
    }
    
    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco set <joueur> <montant>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Montant invalide !");
            return;
        }
        
        plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
        sender.sendMessage(ChatColor.GREEN + "Solde de " + ChatColor.YELLOW + target.getName() + 
                         ChatColor.GREEN + " défini à " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount));
        target.sendMessage(ChatColor.YELLOW + "Votre solde a été modifié à " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(amount));
    }
    
    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco reset <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        double startingBalance = plugin.getEconomyManager().getStartingBalance();
        plugin.getEconomyManager().setBalance(target.getUniqueId(), startingBalance);
        sender.sendMessage(ChatColor.GREEN + "Solde de " + ChatColor.YELLOW + target.getName() + 
                         ChatColor.GREEN + " réinitialisé à " + ChatColor.GOLD + 
                         plugin.getEconomyManager().format(startingBalance));
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "═══ Economy Admin ═══");
        sender.sendMessage(ChatColor.YELLOW + "/eco give <joueur> <montant>" + ChatColor.GRAY + " - Donner de l'argent");
        sender.sendMessage(ChatColor.YELLOW + "/eco take <joueur> <montant>" + ChatColor.GRAY + " - Retirer de l'argent");
        sender.sendMessage(ChatColor.YELLOW + "/eco set <joueur> <montant>" + ChatColor.GRAY + " - Définir le solde");
        sender.sendMessage(ChatColor.YELLOW + "/eco reset <joueur>" + ChatColor.GRAY + " - Réinitialiser le solde");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "take", "set", "reset"));
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        
        return completions;
    }
}
