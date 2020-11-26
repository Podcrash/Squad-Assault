package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RemoveHostCommand extends HostCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args) || !Main.getSAConfig().getHosts().get(0).equalsIgnoreCase(commandSender.getName())) {
           return true;
        }

        Main.getSAConfig().getHosts().remove(args[0].toLowerCase());
        Main.getSAConfig().getConfig().set("Hosts", Main.getSAConfig().getHosts());
        Bukkit.broadcastMessage(ChatColor.YELLOW + args[0] + ChatColor.AQUA + " has been removed as a host.");
        return true;
    }
}
