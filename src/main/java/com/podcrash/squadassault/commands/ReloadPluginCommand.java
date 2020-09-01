package com.podcrash.squadassault.commands;

import com.podcrash.api.commands.CommandBase;
import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadPluginCommand extends CommandBase {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("invicta.admin")) {
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            Main.getInstance().getServer().getPluginManager().enablePlugin(Main.getInstance());
            commandSender.sendMessage("Reloaded successfully.");
        }
        return true;
    }

}

