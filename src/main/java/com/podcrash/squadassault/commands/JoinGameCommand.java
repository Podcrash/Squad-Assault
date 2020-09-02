package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player && strings.length == 1) {
            Main.getGameManager().addPlayer(Main.getGameManager().getGame(strings[0]), (Player) commandSender);
        }
        return true;
    }
}
