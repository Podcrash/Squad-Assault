package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
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
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player) || strings.length > 1) {
            return true;
        }
        Player player;
        if(strings.length == 1) {
            player = Bukkit.getPlayer(strings[0]);
        } else {
            player = (Player) commandSender;
        }
        SAGame game;
        if((game = Main.getGameManager().getGame(player)) == null) {
            return true;
        }
        String adr = new DecimalFormat("##.#").format(game.getStats().get(player.getUniqueId()).getADR());
        player.sendMessage(ChatColor.AQUA + "ADR: " + ChatColor.YELLOW + adr);
        return true;
    }
}
