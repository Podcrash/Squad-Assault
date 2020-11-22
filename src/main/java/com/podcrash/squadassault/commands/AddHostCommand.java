package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddHostCommand extends HostCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args)) {
            return true;
        }

        Main.getSAConfig().getHosts().add(args[0].toLowerCase());
        Bukkit.broadcastMessage(ChatColor.YELLOW + args[0] + ChatColor.AQUA + " has been added as a host.");
        return true;
    }
}
