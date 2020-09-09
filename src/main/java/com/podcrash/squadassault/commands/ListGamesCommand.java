package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListGamesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        StringBuilder builder = new StringBuilder();
        for(SAGame game : Main.getGameManager().getGames()) {
            builder.append("id ").append(game.getId()).append(" playing on ").append(game.getMapName()).append(".\n");
        }
        commandSender.sendMessage(builder.toString());
        return true;
    }
}
