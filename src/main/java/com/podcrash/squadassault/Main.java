package com.podcrash.squadassault;

import com.podcrash.squadassault.game.GameListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private static Config config;

    public static Main getInstance() {
        return instance;
    }

    public static Config getSAConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new Config();
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
