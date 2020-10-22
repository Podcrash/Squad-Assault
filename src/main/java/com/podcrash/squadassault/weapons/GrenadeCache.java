package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.nms.PhysicsItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GrenadeCache {

    private final SAGame game;
    private final Player player;
    private final long time;
    private long duration = -1;
    private final PhysicsItem grenade;
    private final List<Block> blocks;

    public GrenadeCache(SAGame game, Player player, long time, PhysicsItem grenade) {
        this.game = game;
        this.player = player;
        this.time = time;
        this.grenade = grenade;
        blocks = new ArrayList<>();
    }

    public SAGame getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public long getTime() {
        return time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public PhysicsItem getGrenade() {
        return grenade;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Player> getNearbyPlayers(double distance) {
        List<Player> list = new ArrayList<>();
        for (Player player : game.getTeamA().getPlayers()) {
            if (player.getLocation().getWorld() == grenade.getGrenadeLocation().getWorld() && player.getLocation().distance(grenade.getGrenadeLocation()) <= distance) {
                list.add(player);
            }
        }
        for (Player player : game.getTeamB().getPlayers()) {
            if (player.getLocation().getWorld() == grenade.getGrenadeLocation().getWorld() && player.getLocation().distance(grenade.getGrenadeLocation()) <= distance) { list.add(player);
            }
        }
        return list;
    }

    public List<Player> getNearbyToBlockPlayers() {
        List<Player> list = new ArrayList<>();
        for (final Player player : game.getTeamA().getPlayers()) {
            for (final Block block : blocks) {
                if (player.getLocation().getWorld() == block.getWorld() && player.getLocation().distance(block.getLocation()) <= 1.0) {
                    list.add(player);
                }
            }
        }
        for (Player player : game.getTeamB().getPlayers()) {
            for (Block block : blocks) {
                if (player.getLocation().getWorld() == block.getWorld() && player.getLocation().distance(block.getLocation()) <= 1.5) {
                    list.add(player);
                }
            }
        }
        return list;
    }

}
