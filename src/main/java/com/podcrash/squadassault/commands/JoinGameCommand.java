package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player && args.length == 1) {
            Main.getGameManager().addPlayer(Main.getGameManager().getGame(args[0]), (Player) commandSender);
        }
        return true;
    }
}
