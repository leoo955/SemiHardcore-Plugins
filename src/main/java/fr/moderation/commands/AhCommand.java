package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.auction.AuctionGUI;
import fr.moderation.auction.AuctionHouse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AhCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    
    public AhCommand(ModerationSMP plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // /ah - Ouvre le GUI
        if (args.length == 0) {
            AuctionGUI.openMainMenu(plugin, player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "sell":
                handleSell(player, args);
                break;
            case "listings":
            case "mes":
                AuctionGUI.openPlayerListings(plugin, player);
                break;
            case "expired":
            case "expires":
                AuctionGUI.openExpiredItems(plugin, player);
                break;
            default:
                AuctionGUI.openMainMenu(plugin, player);
                break;
        }
        
        return true;
    }
    
    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ah sell <prix>");
            return;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Vous devez tenir un item dans votre main !");
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Prix invalide !");
            return;
        }
        
        if (price < AuctionHouse.MIN_PRICE) {
            player.sendMessage(ChatColor.RED + "Le prix minimum est de " + 
                             plugin.getEconomyManager().format(AuctionHouse.MIN_PRICE));
            return;
        }
        
        if (price > AuctionHouse.MAX_PRICE) {
            player.sendMessage(ChatColor.RED + "Le prix maximum est de " + 
                             plugin.getEconomyManager().format(AuctionHouse.MAX_PRICE));
            return;
        }
        
        AuctionHouse ah = plugin.getAuctionHouse();
        
        if (ah.getPlayerListingCount(player.getUniqueId()) >= AuctionHouse.MAX_LISTINGS_PER_PLAYER) {
            player.sendMessage(ChatColor.RED + "Vous avez atteint la limite de " + 
                             AuctionHouse.MAX_LISTINGS_PER_PLAYER + " ventes !");
            return;
        }
        
        double commission = ah.calculateCommission(price);
        
        if (!plugin.getEconomyManager().hasBalance(player.getUniqueId(), commission)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent pour payer la commission !");
            player.sendMessage(ChatColor.GRAY + "Commission: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(commission) + 
                             ChatColor.GRAY + " (" + AuctionHouse.COMMISSION_PERCENT + "%)");
            return;
        }
        
        // Retirer l'item et la commission
        ItemStack toSell = item.clone();
        player.getInventory().setItemInMainHand(null);
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), commission);
        
        // Ajouter la vente
        if (ah.addListing(player.getUniqueId(), player.getName(), toSell, price)) {
            player.sendMessage(ChatColor.GREEN + "Item mis en vente pour " + 
                             ChatColor.GOLD + plugin.getEconomyManager().format(price));
            player.sendMessage(ChatColor.GRAY + "Commission payée: " + ChatColor.YELLOW + 
                             plugin.getEconomyManager().format(commission));
        } else {
            // Refund if failure
            player.getInventory().addItem(toSell);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), commission);
            player.sendMessage(ChatColor.RED + "Impossible de mettre l'item en vente !");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("sell", "listings", "expired"));
        }
        
        return completions;
    }
}
