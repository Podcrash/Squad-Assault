package com.podcrash.squadassault;

import com.podcrash.squadassault.commands.*;
import com.podcrash.squadassault.game.GameListener;
import com.podcrash.squadassault.game.GameTask;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SAGameManager;
import com.podcrash.squadassault.nms.BulletSnowball;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.shop.ShopManager;
import com.podcrash.squadassault.util.Randomizer;
import com.podcrash.squadassault.weapons.WeaponManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class Main extends JavaPlugin {
    private static Main instance;
    private static Config config;
    private static SAGameManager gameManager;
    private static WeaponManager weaponManager;
    private static ShopManager shopManager;
    private static GameListener listener;
    private static GameTask task;

    public static Main getInstance() {
        return instance;
    }

    public static Config getSAConfig() {
        return config;
    }

    public static SAGameManager getGameManager() {
        return gameManager;
    }

    public static WeaponManager getWeaponManager(){
        return weaponManager;
    }

    public static ShopManager getShopManager() {
        return shopManager;
    }

    public static GameTask getUpdateTask() {
        return task;
    }

    public static GameListener getListener() {
        return listener;
    }

    @Override
    public void onLoad() {
        NmsUtils.injectEntity("bullet_snowball", 104, BulletSnowball.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        config = new Config();
        gameManager = new SAGameManager();
        task = new GameTask();
        weaponManager = new WeaponManager();
        shopManager = new ShopManager();

        config.startConfig();
        registerCommands();
        List<SAGame> gameList = gameManager.getGames();
        SAGame game = gameList.get(Randomizer.randomInt(gameList.size()));

        getServer().getPluginManager().registerEvents(listener = new GameListener(game), this);
        for(Player player : Bukkit.getOnlinePlayers()) {
            gameManager.addPlayer(game, player);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        for(SAGame game : gameManager.getGames()) {
            gameManager.stopGame(game, false);
            for(Entity entity : game.getBombA().getWorld().getEntities()) {
                if(entity.getType() == EntityType.DROPPED_ITEM) {
                    entity.remove();
                }
            }
        }

        saveConfig();

        gameManager = null;
        HandlerList.unregisterAll(listener);
        listener = null;
        task.cancel();
        task = null;
    }

    private void registerCommands() {
        getCommand("advancedstats").setExecutor(new AdvancedStatsCommand());
        getCommand("stopgame").setExecutor(new StopGame());
        getCommand("addhost").setExecutor(new AddHostCommand());
        getCommand("blacklistplayer").setExecutor(new BlacklistPlayerCommand());
        getCommand("whitelistplayer").setExecutor(new WhitelistPlayerCommand());
        getCommand("setroundshalf").setExecutor(new SetRoundsHalfCommand());
        getCommand("setroundswin").setExecutor(new SetRoundsWinCommand());
        getCommand("setshutdown").setExecutor(new SetShutdownCommand());
        getCommand("setexportstats").setExecutor(new SetExportStatsCommand());
        getCommand("setmap").setExecutor(new SetMapCommand());
        getCommand("setprivate").setExecutor(new SetPrivateCommand());
        getCommand("setrandomizeside").setExecutor(new SetRandomizeSideCommand());
        getCommand("setmaxplayers").setExecutor(new SetMaxPlayersCommand());
        getCommand("setminplayers").setExecutor(new SetMinPlayersCommand());
    }
}
