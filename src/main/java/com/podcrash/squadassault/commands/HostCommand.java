package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGameState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class HostCommand implements CommandExecutor {
    public boolean checkPermission(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player) || !Main.getSAConfig().getHosts().contains(commandSender.getName().toLowerCase())) {
            return false;
        }
        if(args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "You must supply one argument!");
            return false;
        } else if(Main.getGameManager().getGames().get(0).getState() != SAGameState.WAITING) {
            commandSender.sendMessage(ChatColor.RED + "You cannot do this when the game is live!");
            return false;
        }
        return true;
    }
}
