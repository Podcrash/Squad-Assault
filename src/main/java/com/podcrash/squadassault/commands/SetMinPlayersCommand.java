package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMinPlayersCommand extends HostCommand {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args)) {
            return true;
        }

        int setting;
        try {
            setting = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(ChatColor.RED + "Not a number!");
            return true;
        }

        Main.getSAConfig().setMinPlayers(setting);
        Main.getGameManager().getGame((Player) commandSender).setMinPlayers(setting);
        Bukkit.broadcastMessage(ChatColor.AQUA + "Minimum players set to " + ChatColor.YELLOW + setting);
        return true;
    }
}
