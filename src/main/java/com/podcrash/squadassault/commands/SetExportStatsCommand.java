package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetExportStatsCommand extends HostCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args)) {
            return true;
        }

        boolean setting = Boolean.parseBoolean(args[0]);

        Main.getSAConfig().setExportStatsAtEnd(setting);
        Bukkit.broadcastMessage(ChatColor.AQUA + "Export Stats at End set to " + ChatColor.YELLOW + setting);
        return true;
    }
}
