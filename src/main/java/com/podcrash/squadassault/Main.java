package com.podcrash.squadassault;

import com.podcrash.squadassault.game.GameListener;
import com.podcrash.squadassault.game.SAGameManager;
import com.podcrash.squadassault.shop.ShopManager;
import com.podcrash.squadassault.weapons.WeaponManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private static Config config;
    private static SAGameManager gameManager;
    private static WeaponManager weaponManager;
    private static ShopManager shopManager;

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

    @Override
    public void onEnable() {
        instance = this;
        gameManager = new SAGameManager();
        weaponManager = new WeaponManager();
        shopManager = new ShopManager();
        config = new Config();
        registerCommands();
        getServer().getPluginManager().registerEvents(new GameListener(), this);
    }

    private void registerCommands() {
    }
}
