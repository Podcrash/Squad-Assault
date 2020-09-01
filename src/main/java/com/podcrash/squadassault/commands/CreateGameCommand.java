package com.podcrash.squadassault.commands;

import com.podcrash.api.commands.CommandBase;
import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateGameCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("invicta.admin") && commandSender instanceof Player) {
            if(strings.length != 3) {
                commandSender.sendMessage("need 3 args");
                return true;
            }
            if(Main.getGameManager().getGame(strings[0]) == null) {
                if(Main.getGameManager().getSetup().get(commandSender) == null) {
                    Main.getGameManager().getSetup().put((Player) commandSender, new GameSetup(strings[0], strings[1], Integer.parseInt(strings[2])));
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