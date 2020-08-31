package com.podcrash.squadassault;

import com.podcrash.squadassault.game.GameListener;
import com.podcrash.squadassault.game.SAGameManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private static Config config;
    private static SAGameManager manager;

    public static Main getInstance() {
        return instance;
    }

    public static Config getSAConfig() {
        return config;
    }

    public static SAGameManager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new Config();
        manager = new SAGameManager();
        registerCommands();
        getServer().getPluginManager().registerEvents(new GameListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void registerCommands() {
    }


}
