package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission("podcrash.admin") && commandSender instanceof Player) {
            if(args.length != 3) {
                commandSender.sendMessage("need 3 args");
                return true;
            }
            if(Main.getGameManager().getGame(args[0]) == null) {
                if(Main.getGameManager().getSetup().get(commandSender) == null) {
                    Main.getGameManager().getSetup().put((Player) commandSender, new GameSetup(args[0], args[1], Integer.parseInt(args[2])));
                    commandSender.sendMessage("complete. now use /setlobby");
                } else {
                    commandSender.sendMessage("already making a game");
                    return true;
                }
            } else {
                commandSender.sendMessage("id taken");
                return true;
            }
        }
        return true;
    }
}