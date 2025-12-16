package fr.moderation.listeners;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.DeathWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReviveTotemListener implements Listener {
    
    private final ModerationSMP plugin;
    private final DeathWorldManager deathWorldManager;
    
    public ReviveTotemListener(ModerationSMP plugin, DeathWorldManager deathWorldManager) {
        this.plugin = plugin;
        this.deathWorldManager = deathWorldManager;
    }
    
    @EventHandler
    public void onTotemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem de Résurrection")) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        event.setCancelled(true);
        openDeadPlayersGUI(player);
    }
    
    private void openDeadPlayersGUI(Player player) {
        Set<UUID> deadPlayers = deathWorldManager.getGhostPlayers();
        
        if (deadPlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Aucun joueur à ressusciter !");
            return;
        }
        
        int size = Math.min(54, ((deadPlayers.size() + 8) / 9) * 9);
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "Choisir un joueur");
        
        for (UUID playerId : deadPlayers) {
            OfflinePlayer deadPlayer = Bukkit.getOfflinePlayer(playerId);
            
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(deadPlayer);
                skullMeta.setDisplayName(ChatColor.YELLOW + deadPlayer.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Cliquez pour ressusciter");
                lore.add(ChatColor.DARK_GRAY + "UUID: " + playerId.toString());
                skullMeta.setLore(lore);
                
                skull.setItemMeta(skullMeta);
            }
            
            gui.addItem(skull);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.DARK_PURPLE + "Choisir un joueur")) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()) {
            return;
        }
        
        List<String> lore = clicked.getItemMeta().getLore();
        if (lore.size() < 2) {
            return;
        }
        
        String uuidLine = lore.get(1);
        String uuidStr = ChatColor.stripColor(uuidLine).replace("UUID: ", "");
        
        try {
            UUID targetId = UUID.fromString(uuidStr);
            
            boolean hasTotem = false;
            int totemSlot = -1;
            
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        String name = item.getItemMeta().getDisplayName();
                        if (name.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem de Résurrection")) {
                            hasTotem = true;
                            totemSlot = i;
                            break;
                        }
                    }
                }
            }
            
            if (!hasTotem) {
                player.sendMessage(ChatColor.RED + "Vous n'avez plus de Totem de Résurrection !");
                player.closeInventory();
                return;
            }
            
            Location reviveLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            deathWorldManager.fullyRevivePlayer(targetId, reviveLocation);
            
            ItemStack totem = player.getInventory().getItem(totemSlot);
            if (totem.getAmount() > 1) {
                totem.setAmount(totem.getAmount() - 1);
            } else {
                player.getInventory().setItem(totemSlot, null);
            }
            
            player.closeInventory();
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
            player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " a été ressuscité !");
            
            if (target.isOnline()) {
                ((Player) target).sendMessage(ChatColor.GREEN + "Vous avez été ressuscité par " + player.getName() + " !");
            }
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Erreur lors de la résurrection !");
            player.closeInventory();
        }
    }
}
