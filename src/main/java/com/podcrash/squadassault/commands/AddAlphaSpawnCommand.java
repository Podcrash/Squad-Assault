package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddAlphaSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("podcrash.admin") && commandSender instanceof Player) {
            GameSetup setup = Main.getGameManager().getSetup().get(commandSender);
            if(setup == null) {
                commandSender.sendMessage("use /creategame first");
                return true;
            }
            setup.getAlphaSpawns().add(((Player)commandSender).getLocation().clone());
            commandSender.sendMessage("spawn " + setup.getAlphaSpawns().size() + " added");
            return true;
        }
        return true;
    }
}