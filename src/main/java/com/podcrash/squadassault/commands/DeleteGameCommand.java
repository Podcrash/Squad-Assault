package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeleteGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission("podcrash.admin") && args.length == 1) {
            SAGame game = Main.getGameManager().getGame(args[0]);
            if(game != null) {
                Main.getGameManager().stopGame(game,false);
                Main.getGameManager().removeGame(game);
                commandSender.sendMessage("success");
                Main.getSAConfig().getMaps().set("Game."+args[0],null);
                Main.getSAConfig().saveMaps(Main.getInstance().getDataFolder());
            } else {
                commandSender.sendMessage("Game doesn't exist");
            }
        } else {
            commandSender.sendMessage("must specify id");
        }
        return true;
    }
}
