package com.podcrash.squadassault.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnnounceCommand implements CommandExecutor {


    //Todo delete this later, this is just for communicating to players on the server via console
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        StringBuilder builder = new StringBuilder();
        for(String string : args) {
            builder.append(string).append(" ");
        }
        Bukkit.broadcastMessage(builder.toString());
        return true;
    }
}
