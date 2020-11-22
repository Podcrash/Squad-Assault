package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetRoundsHalfCommand extends HostCommand {
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

        Main.getSAConfig().setRoundsPerHalf(setting);
        Bukkit.broadcastMessage(ChatColor.AQUA + "Rounds per half set to " + ChatColor.YELLOW + setting);
        return true;
    }
}
