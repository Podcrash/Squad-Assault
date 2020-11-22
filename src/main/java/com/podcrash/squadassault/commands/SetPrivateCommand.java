package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetPrivateCommand extends HostCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args)) {
            return true;
        }

        boolean setting = Boolean.parseBoolean(args[0]);

        Main.getSAConfig().setPrivateLobby(setting);
        Bukkit.broadcastMessage(ChatColor.AQUA + "Lobby Privacy set to " + ChatColor.YELLOW + setting);
        return true;
    }
}
