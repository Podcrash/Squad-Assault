package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.PlayerStats;
import com.podcrash.squadassault.game.SAGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class AdvancedStatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player) || args.length > 1) {
            return true;
        }
        Player player;
        if(args.length == 1) {
            player = Bukkit.getPlayer(args[0]);
        } else {
            player = (Player) commandSender;
        }
        SAGame game;
        if((game = Main.getGameManager().getGame(player)) == null) {
            return true;
        }
        PlayerStats stats = game.getStats().get(player.getUniqueId());
        String adr = new DecimalFormat("##.#").format(stats.getADR());
        player.sendMessage(ChatColor.AQUA + "ADR: " + ChatColor.YELLOW + adr);
        player.sendMessage(ChatColor.AQUA + "Opening Duels: " + ChatColor.YELLOW + stats.getOpenWins() + ChatColor.AQUA + "/" + ChatColor.YELLOW + stats.getOpenLosses());
        player.sendMessage(ChatColor.AQUA + "Headshots: " + ChatColor.YELLOW + stats.getHeadshots());
        player.sendMessage(ChatColor.AQUA + "Bomb Plants/Defuses: " + ChatColor.YELLOW + stats.getBombPlants() + ChatColor.AQUA + "/" + ChatColor.YELLOW + stats.getBombDefuses());
        return true;
    }
}
