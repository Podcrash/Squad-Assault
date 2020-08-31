package com.podcrash.squadassault;

import com.podcrash.squadassault.game.GameListener;
import com.podcrash.squadassault.game.SAGameManager;
import com.podcrash.squadassault.weapons.WeaponManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private static Config config;
    private static SAGameManager gameManager;
    private static WeaponManager weaponManager;

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

    @Override
    public void onEnable() {
        instance 
          this;
        gameManager = new SAGameManager();
        config = new Config();
        weaponManager = new WeaponManager();
        registerCommands();
        getServer().getPluginManager().registerEvents(new GameListener(), this);

    private void registerCommands() {
    }
}
