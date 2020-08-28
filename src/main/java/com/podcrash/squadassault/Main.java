package com.podcrash.squadassault;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void registerCommands() {
       // getCommand("deleteworld").setExecutor(new DeleteWorldCommand());
    }

    private void registerListeners() {
    }

}
