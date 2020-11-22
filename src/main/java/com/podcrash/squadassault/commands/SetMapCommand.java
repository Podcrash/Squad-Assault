package com.podcrash.squadassault.commands;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class SetMapCommand extends HostCommand {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!checkPermission(commandSender, command, s, args)) {
            return true;
        }
        String map = args[0];

        for(SAGame game : Main.getGameManager().getGames()) {
            game.stop();
            for(Player player : game.getTeamA().getPlayers()) {
                Main.getGameManager().removePlayer(game, player, true, false);
            }
            for(Player player : game.getTeamB().getPlayers()) {
                Main.getGameManager().removePlayer(game, player, true, false);
            }
        }
        Main.getGameManager().getGames().clear();

        YamlConfiguration maps = Main.getSAConfig().getMaps();
        for(String id : maps.getConfigurationSection("Game").getKeys(false)) {
            if(!id.equalsIgnoreCase(map)) {
                continue;
            }
            World world = Bukkit.getServer().getWorld("Game."+id+".Name");
            world.getLivingEntities().stream().filter(e -> e.getType() != EntityType.PLAYER).forEach(Entity::remove);
            world.setStorm(false);
            try {
                Main.getGameManager().addGame(new SAGame(id, maps.getString("Game." + id + ".Name"),
                        Utils.getDeserializedLocation(maps.getString("Game." + id + ".Lobby")), maps.getInt(
                        "Game." + id + ".Min"),
                        Utils.getDeserializedLocations(maps.getStringList("Game." + id + ".AlphaSpawns")),
                        Utils.getDeserializedLocations(maps.getStringList("Game." + id + ".OmegaSpawns")),
                        Utils.getDeserializedLocation(maps.getString("Game." + id + ".BombA")),
                        Utils.getDeserializedLocation(maps.getString("Game." + id + ".BombB"))));
            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Could not load map with id " + id);
                e.printStackTrace();
            }
            for(Player player : Bukkit.getOnlinePlayers()) {
                Main.getGameManager().addPlayer(Main.getGameManager().getGame(id), player);
            }
            break;
        }

        Bukkit.broadcastMessage(ChatColor.AQUA + "Map set to " + ChatColor.YELLOW + map);
        return true;
    }
}
