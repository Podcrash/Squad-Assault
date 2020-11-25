package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadPluginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission("podcrash.admin")) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            Main.getInstance().getServer().getPluginManager().enablePlugin(Main.getInstance());
            commandSender.sendMessage("Reloaded successfully.");
        }
        return true;
    }

}

