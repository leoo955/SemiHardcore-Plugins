package fr.moderation.utils;

import fr.moderation.managers.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeTabCompleter implements TabCompleter {
    
    private final HomeManager homeManager;
    
    public HomeTabCompleter(HomeManager homeManager) {
        this.homeManager = homeManager;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            Map<String, org.bukkit.Location> homes = homeManager.getHomes(player);
            if (homes == null || homes.isEmpty()) {
                return Collections.emptyList();
            }
            return filter(new ArrayList<>(homes.keySet()), args[0]);
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
