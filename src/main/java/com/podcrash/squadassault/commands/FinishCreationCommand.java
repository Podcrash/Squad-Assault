package com.podcrash.squadassault.commands;

import com.podcrash.api.commands.CommandBase;
import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FinishCreationCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("podcrash.admin") && commandSender instanceof Player) {
            GameSetup setup = Main.getGameManager().getSetup().get(commandSender);
            if(setup == null) {
                commandSender.sendMessage("use /creategame first");
                return true;
            }
            if(setup.getBombA() == null || setup.getBombB() == null || setup.getLobby() == null || setup.getAlphaSpawns().size() < setup.getMinPlayers() || setup.getOmegaSpawns().size() < setup.getMinPlayers()) {
                commandSender.sendMessage("Unfinished, complete setup before entering command");
                return true;
            }
            Main.getGameManager().addGame(new SAGame(setup.getId(), setup.getMapName(), setup.getLobby(),
                    setup.getMinPlayers(), setup.getAlphaSpawns(), setup.getOmegaSpawns(), setup.getBombA(), setup.getBombB()));
            Main.getSAConfig().getMaps().set("Game."+setup.getId()+".Min", setup.getMinPlayers());
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".Name", setup.getMapName());
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".Lobby", Utils.getSerializedLocation(setup.getLobby()));
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".AlphaSpawns",
                    Utils.getSerializedLocations(setup.getAlphaSpawns()));
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".OmegaSpawns",
                    Utils.getSerializedLocations(setup.getOmegaSpawns()));
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".BombA", Utils.getSerializedLocation(setup.getBombA()));
            Main.getSAConfig().getMaps().set("Game." + setup.getId() + ".BombB", Utils.getSerializedLocation(setup.getBombB()));
            Main.getSAConfig().saveMaps(Main.getInstance().getDataFolder());
            Main.getGameManager().getSetup().remove(commandSender);
            commandSender.sendMessage("Success");
            return true;
        }
        return true;
    }
}
