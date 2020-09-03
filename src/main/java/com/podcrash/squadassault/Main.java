package com.podcrash.squadassault;

import com.podcrash.squadassault.commands.*;
import com.podcrash.squadassault.game.GameListener;
import com.podcrash.squadassault.game.GameTask;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SAGameManager;
import com.podcrash.squadassault.shop.ShopManager;
import com.podcrash.squadassault.weapons.WeaponManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


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
        getServer().getPluginManager().registerEvents(listener = new GameListener(), this);
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
        gameManager = null;
        HandlerList.unregisterAll(listener);
        listener = null;
        task.cancel();
        task = null;
    }

    private void registerCommands() {
        getCommand("quit").setExecutor(new QuitCommand());
        getCommand("addalphaspawn").setExecutor(new AddAlphaSpawnCommand());
        getCommand("addomegaspawn").setExecutor(new AddOmegaSpawnCommand());
        getCommand("addbomb").setExecutor(new AddBombCommand());
        getCommand("creategame").setExecutor(new CreateGameCommand());
        getCommand("disablemap").setExecutor(new DisableMapCommand());
        getCommand("enablemap").setExecutor(new EnableMapCommand());
        getCommand("finishcreation").setExecutor(new FinishCreationCommand());
        getCommand("reloadplugin").setExecutor(new ReloadPluginCommand());
        getCommand("setlobby").setExecutor(new SetLobbyCommand());
        getCommand("deletegame").setExecutor(new DeleteGameCommand());
        getCommand("joingame").setExecutor(new JoinGameCommand());
    }
}
