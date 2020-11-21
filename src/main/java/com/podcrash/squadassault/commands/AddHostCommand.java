package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddHostCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player) || !Main.getSAConfig().getHosts().contains(commandSender.getName().toLowerCase())) {
            return true;
        }
        if(args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "You must supply one argument!");
            return true;
        } else if(Main.getGameManager().getGames().get(0).getState() != SAGameState.WAITING) {
            commandSender.sendMessage(ChatColor.RED + "You cannot do this when the game is live!");
            return true;
        }

        Main.getSAConfig().getHosts().add(args[0].toLowerCase());
        Bukkit.broadcastMessage(ChatColor.YELLOW + args[0] + ChatColor.AQUA + " has been added as a host.");
        return true;
    }
}
