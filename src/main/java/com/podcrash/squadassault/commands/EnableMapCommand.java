package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SAGameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnableMapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("podcrash.admin")) {
            if(strings.length != 1) {
                commandSender.sendMessage("Must specify id!");
                return true;
            }
            String id = strings[0];
            SAGame game = Main.getGameManager().getGame(id);
            if(game != null) {
                if(game.getState() != SAGameState.DISABLED) {
                    commandSender.sendMessage("Already enabled!");
                    return true;
                }
                game.setState(SAGameState.WAITING);
                commandSender.sendMessage("Enabled!");
            } else {
                commandSender.sendMessage("Id invalid");
                return true;
            }
        }
        return true;
    }
}
