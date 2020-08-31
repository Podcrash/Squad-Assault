package com.podcrash.squadassault.game;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import com.podcrash.squadassault.weapons.Grenade;
import com.podcrash.squadassault.weapons.Gun;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameTask extends BukkitRunnable {

    private long ticks;
    private final Map<UUID, Integer> delay;

    public GameTask() {
        ticks = 0;
        delay = new HashMap<>();
        runTaskTimer(Main.getInstance(), 0, 0);
    }

    public Map<UUID, Integer> getDelay() {
        return delay;
    }

    @Override
    public void run() {
        for(Map.Entry<UUID, Integer> entry : delay.entrySet()) {
            if(entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            } else {
                delay.remove(entry.getKey());
            }
        }

        for(Gun gun : Main.getWeaponManager().getGuns()) {
            gun.tick();
        }

        for(Grenade grenade : Main.getWeaponManager().getGrenades()) {
            grenade.tick(ticks);
        }
        for(SAGame game : Main.getGameManager().getGames()) {
            if(ticks % 10 == 0) {
                game.getBar().update();
            }
            if(ticks % 20 == 0) {
                if(game.getTimer() > 0) {
                    for(SAScoreboard scoreboard : game.getScoreboards().values()) {
                        Main.getGameManager().updateStatus(game, scoreboard.getStatus());
                    }
                }
                try {
                    game.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    Main.getGameManager().stopGame(game, false);
                }
            }
        }
        ++ticks;
    }
}
