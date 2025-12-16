package fr.moderation.commands;

import fr.moderation.ModerationSMP;
import fr.moderation.managers.TeamManager;
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
import java.util.Set;
import java.util.UUID;

public class TeamCommand implements CommandExecutor, TabCompleter {
    
    private final ModerationSMP plugin;
    private final TeamManager teamManager;
    
    public TeamCommand(ModerationSMP plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut utiliser cette commande.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "setleader":
                handleSetLeader(player, args);
                break;
            case "add":
                handleAdd(player, args);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "sethome":
                handleSetHome(player);
                break;
            case "home":
                handleHome(player);
                break;
            case "delhome":
                handleDelHome(player);
                break;
            case "resign":
                handleResign(player);
                break;
            case "list":
                handleList(player);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "deny":
                handleDeny(player);
                break;
            case "chat":
                handleChat(player, args);
                break;
            case "transfer":
                handleTransfer(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleSetHome(Player sender) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'équipe peuvent définir le home d'équipe.");
            return;
        }
        
        String team = teamManager.getLeaderTeam(sender.getUniqueId());
        teamManager.setTeamHome(team, sender.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Home d'équipe défini !");
    }
    
    private void handleDelHome(Player sender) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'équipe peuvent supprimer le home d'équipe.");
            return;
        }
        
        String team = teamManager.getLeaderTeam(sender.getUniqueId());
        if (teamManager.getTeamHome(team) == null) {
            sender.sendMessage(ChatColor.RED + "Aucun home d'équipe défini.");
            return;
        }
        
        teamManager.deleteTeamHome(team);
        sender.sendMessage(ChatColor.GREEN + "Home d'équipe supprimé !");
    }
    
    private void handleHome(Player sender) {
        // Check if player is in death world
        if (plugin.getDeathWorldManager().isInDeathWorld(sender)) {
            if (!sender.hasPermission("moderation.deathdim.bypass")) {
                sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous téléporter depuis le monde de la mort !");
                return;
            }
        }
        
        String team = teamManager.getPlayerTeam(sender.getUniqueId());
        if (team == null) {
            sender.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune équipe.");
            return;
        }
        
        org.bukkit.Location home = teamManager.getTeamHome(team);
        if (home == null) {
            sender.sendMessage(ChatColor.RED + "Aucun home d'équipe défini.");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "Téléportation dans 3 secondes...");
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (sender.isOnline()) {
                sender.teleport(home);
                sender.sendMessage(ChatColor.GREEN + "Téléportation réussie !");
            }
        }, 60L); // 3 secondes = 60 ticks
    }
    
    private void handleResign(Player sender) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'êtes pas chef d'équipe.");
            return;
        }
        
        String team = teamManager.getLeaderTeam(sender.getUniqueId());
        teamManager.resignLeader(team);
        sender.sendMessage(ChatColor.GREEN + "Vous avez démissionné de votre poste de chef.");
        sender.sendMessage(ChatColor.YELLOW + "L'équipe " + teamManager.getTeamDisplayName(team) + ChatColor.YELLOW + " n'a plus de chef !");
    }
    
    private void handleSetLeader(Player sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Seuls les OP peuvent définir un chef d'équipe.");
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /team setleader <joueur> <vert|bleu|rouge>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        String colorArg = args[2].toLowerCase();
        String teamColor;
        
        if (colorArg.equals("vert") || colorArg.equals("green")) {
            teamColor = TeamManager.TEAM_GREEN;
        } else if (colorArg.equals("bleu") || colorArg.equals("blue")) {
            teamColor = TeamManager.TEAM_BLUE;
        } else if (colorArg.equals("rouge") || colorArg.equals("red")) {
            teamColor = TeamManager.TEAM_RED;
        } else {
            sender.sendMessage(ChatColor.RED + "Couleur invalide. Utilisez: vert, bleu, rouge.");
            return;
        }
        
        teamManager.setLeader(teamColor, target);
        sender.sendMessage(ChatColor.GREEN + target.getName() + " est maintenant chef de l'équipe " + teamManager.getTeamDisplayName(teamColor));
        target.sendMessage(ChatColor.GREEN + "Vous avez été promu chef de l'équipe " + teamManager.getTeamDisplayName(teamColor));
    }
    
    private void handleAdd(Player sender, String[] args) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'équipe peuvent recruter.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /team add <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        // Check if target is a Guister
        if (plugin.getGuisterManager() != null && plugin.getGuisterManager().isGuister(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + target.getName() + " est un Guister et ne peut pas rejoindre de team.");
            return;
        }
        
        String teamColor = teamManager.getLeaderTeam(sender.getUniqueId());
        teamManager.addMember(teamColor, target);
        
        sender.sendMessage(ChatColor.GREEN + target.getName() + " a rejoint votre équipe !");
        target.sendMessage(ChatColor.GREEN + "Vous avez rejoint l'équipe " + teamManager.getTeamDisplayName(teamColor));
    }

    
    private void handleKick(Player sender, String[] args) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'équipe peuvent exclure.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /team kick <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        String leaderTeam = teamManager.getLeaderTeam(sender.getUniqueId());
        String targetTeam = teamManager.getPlayerTeam(target.getUniqueId());
        
        if (targetTeam == null || !targetTeam.equals(leaderTeam)) {
            sender.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans votre équipe.");
            return;
        }
        
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous exclure vous-même.");
            return;
        }
        
        teamManager.removePlayer(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + target.getName() + " a été exclu de l'équipe.");
        target.sendMessage(ChatColor.RED + "Vous avez été exclu de l'équipe.");
    }
    
    private void handleLeave(Player sender) {
        String team = teamManager.getPlayerTeam(sender.getUniqueId());
        if (team == null) {
            sender.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune équipe.");
            return;
        }
        
        // Allow leaders to leave (implicitly resigns)
        teamManager.removePlayer(sender.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Vous avez quitté l'équipe.");
    }
    
    private void handleInfo(Player sender) {
        String team = teamManager.getPlayerTeam(sender.getUniqueId());
        if (team == null) {
            sender.sendMessage(ChatColor.YELLOW + "Vous n'êtes dans aucune équipe.");
            return;
        }
        
        sender.sendMessage(ChatColor.GRAY + "Vous êtes dans l'équipe " + teamManager.getTeamDisplayName(team));
        if (teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "Vous êtes le CHEF de cette équipe.");
        }
    }
    
    private void handleList(Player sender) {
        sender.sendMessage(ChatColor.GOLD + "=== √Čquipes du Serveur ===");
        
        for (String teamColor : new String[]{TeamManager.TEAM_GREEN, TeamManager.TEAM_BLUE, TeamManager.TEAM_RED}) {
            Set<UUID> members = teamManager.getTeamMembers(teamColor);
            UUID leaderId = teamManager.getTeamLeader(teamColor);
            
            String teamName = teamManager.getTeamDisplayName(teamColor);
            sender.sendMessage(teamName + ChatColor.YELLOW + " (" + members.size() + " membre(s)):");
            
            if (leaderId != null) {
                Player leader = Bukkit.getPlayer(leaderId);
                String leaderName = leader != null ? leader.getName() : "Inconnu";
                sender.sendMessage(ChatColor.GRAY + "  Chef: " + ChatColor.GOLD + leaderName);
            } else {
                sender.sendMessage(ChatColor.GRAY + "  Chef: " + ChatColor.RED + "Aucun");
            }
            
            if (!members.isEmpty()) {
                StringBuilder membersList = new StringBuilder(ChatColor.GRAY + "  Membres: ");
                int count = 0;
                for (UUID memberId : members) {
                    if (!memberId.equals(leaderId)) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null) {
                            if (count > 0) membersList.append(", ");
                            membersList.append(ChatColor.WHITE).append(member.getName());
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    sender.sendMessage(membersList.toString());
                }
            }
            sender.sendMessage("");
        }
    }
    
    private void handleInvite(Player sender, String[] args) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'√©quipe peuvent inviter.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /team invite <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous inviter vous-m√™me !");
            return;
        }
        
        // Check if target is a Guister
        if (plugin.getGuisterManager() != null && plugin.getGuisterManager().isGuister(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + target.getName() + " est un Guister et ne peut pas rejoindre de team.");
            return;
        }
        
        // Check if target is already in a team
        if (teamManager.getPlayerTeam(target.getUniqueId()) != null) {
            sender.sendMessage(ChatColor.RED + target.getName() + " est d√©j√† dans une √©quipe !");
            return;
        }
        
        String teamColor = teamManager.getLeaderTeam(sender.getUniqueId());
        String teamName = teamManager.getTeamDisplayName(teamColor);
        
        teamManager.sendInvite(sender.getUniqueId(), target.getUniqueId());
        
        sender.sendMessage(ChatColor.GREEN + "Invitation envoy√©e √† " + target.getName() + " !");
        target.sendMessage(ChatColor.YELLOW + "Vous avez re√ßu une invitation pour rejoindre l'√©quipe " + teamName + ChatColor.YELLOW + " !");
        target.sendMessage(ChatColor.GRAY + "Utilisez " + ChatColor.GREEN + "/team accept" + ChatColor.GRAY + " ou " + ChatColor.RED + "/team deny");
    }
    
    private void handleAccept(Player sender) {
        if (!teamManager.hasPendingInvite(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez aucune invitation en attente.");
            return;
        }
        
        UUID inviterId = teamManager.getInviter(sender.getUniqueId());
        Player inviter = Bukkit.getPlayer(inviterId);
        
        if (inviter == null || !teamManager.isLeader(inviterId)) {
            sender.sendMessage(ChatColor.RED + "Cette invitation n'est plus valide.");
            teamManager.removeInvite(sender.getUniqueId());
            return;
        }
        
        String teamColor = teamManager.getLeaderTeam(inviterId);
        teamManager.addMember(teamColor, sender);
        teamManager.removeInvite(sender.getUniqueId());
        
        String teamName = teamManager.getTeamDisplayName(teamColor);
        sender.sendMessage(ChatColor.GREEN + "Vous avez rejoint l'√©quipe " + teamName + ChatColor.GREEN + " !");
        inviter.sendMessage(ChatColor.GREEN + sender.getName() + " a accept√© l'invitation !");
    }
    
    private void handleDeny(Player sender) {
        if (!teamManager.hasPendingInvite(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez aucune invitation en attente.");
            return;
        }
        
        UUID inviterId = teamManager.getInviter(sender.getUniqueId());
        teamManager.removeInvite(sender.getUniqueId());
        
        sender.sendMessage(ChatColor.YELLOW + "Vous avez refus√© l'invitation.");
        
        Player inviter = Bukkit.getPlayer(inviterId);
        if (inviter != null) {
            inviter.sendMessage(ChatColor.RED + sender.getName() + " a refus√© l'invitation.");
        }
    }
    
    private void handleChat(Player sender, String[] args) {
        String team = teamManager.getPlayerTeam(sender.getUniqueId());
        if (team == null) {
            sender.sendMessage(ChatColor.RED + "Vous n'√™tes dans aucune √©quipe.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /team chat <message>");
            return;
        }
        
        // Build the message from args
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        
        String message = sender.getName() + ": " + messageBuilder.toString();
        teamManager.sendTeamMessage(team, message);
    }
    
    private void handleTransfer(Player sender, String[] args) {
        if (!teamManager.isLeader(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Seuls les chefs d'√©quipe peuvent transf√©rer le leadership.");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /team transfer <joueur>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return;
        }
        
        String teamColor = teamManager.getLeaderTeam(sender.getUniqueId());
        String targetTeam = teamManager.getPlayerTeam(target.getUniqueId());
        
        if (targetTeam == null || !targetTeam.equals(teamColor)) {
            sender.sendMessage(ChatColor.RED + "Ce joueur n'est pas dans votre √©quipe.");
            return;
        }
        
        if (target.equals(sender)) {
            sender.sendMessage(ChatColor.RED + "Vous √™tes d√©j√† le chef !");
            return;
        }
        
        teamManager.transferLeadership(teamColor, target.getUniqueId());
        
        String teamName = teamManager.getTeamDisplayName(teamColor);
        sender.sendMessage(ChatColor.GREEN + "Vous avez transf√©r√© le leadership √† " + target.getName() + " !");
        target.sendMessage(ChatColor.GOLD + "Vous √™tes maintenant le chef de l'√©quipe " + teamName + ChatColor.GOLD + " !");
        
        // Notify all team members
        for (UUID memberId : teamManager.getTeamMembers(teamColor)) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !member.equals(sender) && !member.equals(target)) {
                member.sendMessage(ChatColor.YELLOW + target.getName() + " est maintenant le chef de l'√©quipe !");
            }
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Commandes d'Équipe ---");
        if (player.isOp()) {
            player.sendMessage(ChatColor.YELLOW + "/team setleader <joueur> <vert|bleu|rouge> " + ChatColor.GRAY + "- Définir un chef");
        }
        player.sendMessage(ChatColor.YELLOW + "/team invite <joueur> " + ChatColor.GRAY + "- Inviter un membre (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team accept " + ChatColor.GRAY + "- Accepter une invitation");
        player.sendMessage(ChatColor.YELLOW + "/team deny " + ChatColor.GRAY + "- Refuser une invitation");
        player.sendMessage(ChatColor.YELLOW + "/team add <joueur> " + ChatColor.GRAY + "- Forcer l'ajout (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team kick <joueur> " + ChatColor.GRAY + "- Exclure un membre (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team sethome " + ChatColor.GRAY + "- Définir le home d'équipe (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team delhome " + ChatColor.GRAY + "- Supprimer le home d'équipe (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team home " + ChatColor.GRAY + "- TP au home d'équipe");
        player.sendMessage(ChatColor.YELLOW + "/team transfer <joueur> " + ChatColor.GRAY + "- Transférer le leadership (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team resign " + ChatColor.GRAY + "- Démissionner (Chef)");
        player.sendMessage(ChatColor.YELLOW + "/team leave " + ChatColor.GRAY + "- Quitter l'équipe");
        player.sendMessage(ChatColor.YELLOW + "/team list " + ChatColor.GRAY + "- Lister toutes les équipes");
        player.sendMessage(ChatColor.YELLOW + "/team chat <message> " + ChatColor.GRAY + "- Message d'équipe");
        player.sendMessage(ChatColor.YELLOW + "/team info " + ChatColor.GRAY + "- Infos équipe");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.isOp()) {
                subcommands.add("setleader");
            }
            subcommands.add("invite");
            subcommands.add("accept");
            subcommands.add("deny");
            subcommands.add("add");
            subcommands.add("kick");
            subcommands.add("transfer");
            subcommands.add("leave");
            subcommands.add("info");
            subcommands.add("list");
            subcommands.add("chat");
            subcommands.add("sethome");
            subcommands.add("delhome");
            subcommands.add("home");
            subcommands.add("resign");
            return filter(subcommands, args[0]);
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setleader") || args[0].equalsIgnoreCase("add") || 
                args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("invite") ||
                args[0].equalsIgnoreCase("transfer")) {
                return null; // Return null to show online players
            }
        }
        
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setleader")) {
                return filter(Arrays.asList("vert", "bleu", "rouge"), args[2]);
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
