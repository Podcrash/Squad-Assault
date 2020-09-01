package com.podcrash.squadassault.commands;

import com.podcrash.api.commands.CommandBase;
import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuitCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;
        SAGame game = Main.getGameManager().getGame(player);
        if(game != null) {
            Main.getGameManager().removePlayer(game, player, false, false);
        }
        return true;
    }
}
