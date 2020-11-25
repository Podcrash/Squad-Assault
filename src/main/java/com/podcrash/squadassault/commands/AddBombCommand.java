package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddBombCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender.hasPermission("podcrash.admin") && commandSender instanceof Player) {
            if(args.length != 1) {
                commandSender.sendMessage("arg 0 should be letter");
                return true;
            }
            GameSetup setup = Main.getGameManager().getSetup().get(commandSender);
            if(setup == null) {
                commandSender.sendMessage("use /creategame first");
                return true;
            }
            boolean isA = args[0].equalsIgnoreCase("a");
            if(isA) {
                setup.setBombA(((Player)commandSender).getLocation().clone());
            } else {
                setup.setBombB(((Player)commandSender).getLocation().clone());
            }
            commandSender.sendMessage("success, do /finishcreation when both sites selected");
            return true;
        }
        return true;
    }
}