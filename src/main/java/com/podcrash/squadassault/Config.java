package com.podcrash.squadassault;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.shop.PlayerShopItem;
import com.podcrash.squadassault.util.GameUtils;
import com.podcrash.squadassault.util.Item;
import com.podcrash.squadassault.weapons.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Config {

    private FileConfiguration config;
    private YamlConfiguration guns;
    private YamlConfiguration grenades;
    private YamlConfiguration shop;
    private YamlConfiguration maps;

    public Config() {
        File dataFolder = Main.getInstance().getDataFolder();
        dataFolder.mkdirs();
        log("Loading config.yml");
        loadConfig();

        log("Loading maps.yml");
        File fileMaps = new File(dataFolder, "maps.yml");
        if(fileMaps.exists()) {
            maps = YamlConfiguration.loadConfiguration(fileMaps);
        } else {
            saveMaps(dataFolder);
        }
        loadMaps();

        log("Loading guns.yml");
        File fileGuns = new File(dataFolder, "guns.yml");
        if(!fileGuns.exists()) {
            Main.getInstance().saveResource("guns.yml", true);
        }
        guns = YamlConfiguration.loadConfiguration(fileGuns);
        loadGuns();

        log("Loading grenades.yml");
        File fileNades = new File(dataFolder, "grenades.yml");
        if(!fileNades.exists()) {
            Main.getInstance().saveResource("grenades.yml",true);
        }

        grenades = YamlConfiguration.loadConfiguration(fileNades);
        loadNades();

        log("loading shop.yml");
        File fileShop = new File(dataFolder, "shop.yml");
        if(!fileShop.exists()) {
            Main.getInstance().saveResource("shop.yml",true);
        }

        shop = YamlConfiguration.loadConfiguration(fileShop);
        loadShop();
    }

    private void loadShop() {
        WeaponManager manager = Main.getWeaponManager();
        for(String gun : shop.getConfigurationSection("ShopGuns").getKeys(false)) {
            if(manager.getGun(gun) == null) {
                log(gun + " in shop.yml doesn't exist in guns.yml");
                continue;
            }
            int price = shop.getInt("ShopGuns."+gun+".Price");
            SATeam.Team side = SATeam.Team.valueOf(shop.getString("ShopGuns."+gun+".Side"));
            String name = shop.getString("ShopGuns."+gun+".ItemName");
            String lore = shop.getString("ShopGuns."+gun+".ItemLore");
            int slot = shop.getInt("ShopGuns."+gun+".Slot");
            Main.getShopManager().addShop(new PlayerShopItem(gun, name, slot, price, lore, side));
        }
        for(String grenade : shop.getConfigurationSection("ShopGrenades").getKeys(false)) {
            if(manager.getGrenade(grenade) == null) {
                log(grenade + " in shop.yml doesn't exist in grenades.yml");
                continue;
            }
            Main.getShopManager().addShop(new PlayerShopItem(
                grenade, shop.getString("ShopGrenades."+grenade+".ItemName"), shop.getInt("ShopGrenades."+grenade+
                    ".Slot"), shop.getInt("ShopGrenades."+grenade+
                    ".Price"), shop.getString("ShopGrenades."+grenade+".ItemLore")
            ));
        }
        for(String item : shop.getConfigurationSection("ShopItems").getKeys(false)) {
            int slot = shop.getInt("ShopItems."+item+".Slot");
            int price = shop.getInt("ShopItems."+item+".Price");
            int slotPlace = shop.getInt("ShopItems."+item+".SlotPlace");
            String name = shop.getString("ShopItems."+item+".ItemName");
            String lore = shop.getString("ShopItems."+item+".ItemLore");
            SATeam.Team side = SATeam.Team.valueOf(shop.getString("ShopItems."+item+".Side"));
            Material material = Material.getMaterial(shop.getString("ShopItems."+item+".Material"));
            Main.getShopManager().addShop(new PlayerShopItem(slot, slotPlace, name, material, price, lore, side, item));
        }
    }

    private void loadNades() {
        for(String nade : grenades.getConfigurationSection("Grenades").getKeys(false)) {
            Main.getWeaponManager().addGrenade(
                new Grenade(nade, GrenadeType.valueOf(grenades.getString("Grenades."+nade+".ItemInfo.Type")),
                        new Item(Material.valueOf(grenades.getString("Grenades."+nade+".ItemInfo.ItemType")),
                                (byte)grenades.getInt("Grenades."+nade+".ItemInfo.Data"), grenades.getString(
                                        "Grenades."+nade+".ItemInfo.Name")), grenades.getInt("Grenades."+nade+
                        ".Properties.Delay"),grenades.getInt("Grenades."+nade+
                        ".Properties.Duration"), grenades.getDouble("Grenades."+nade+
                        ".Properties.ThrowSpeed"),grenades.getDouble("Grenades."+nade+
                        ".Properties.EffectPower")));
        }
    }

    private void loadMaps() {
        if(maps.getString("Game") != null && !maps.isString("Game")) {
            for(String id : maps.getConfigurationSection("Game").getKeys(false)) {
                try {
                    Main.getGameManager().addGame(new SAGame(id, maps.getString("Game." + id + ".Name"),
                            GameUtils.getDeserializedLocation(maps.getString("Game." + id + ".Lobby")), maps.getInt(
                            "Game." + id + ".Min"),
                            GameUtils.getDeserializedLocations(maps.getStringList("Game." + id + ".AlphaSpawns")),
                            GameUtils.getDeserializedLocations(maps.getStringList("Game." + id + ".OmegaSpawns")),
                            GameUtils.getDeserializedLocation("Game." + id + "BombA"),
                            GameUtils.getDeserializedLocation("Game." + id + "BombB")));
                } catch (Exception e) {
                    error("Error loading game with ID " + id);
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadGuns() {
        for(String gun : guns.getConfigurationSection("Guns").getKeys(false)) {
            Gun gunObj = new Gun(gun, new Item(Material.valueOf(guns.getString("Guns."+gun+".ItemInfo.Type")),
                    (byte)guns.getInt("Guns."+gun+".ItemInfo.Data"),guns.getString("Guns."+gun+".ItemInfo.Name")),
                    GunHotbarType.valueOf(guns.getString("Guns."+gun+".ItemInfo.HotbarType")),
                    guns.getBoolean("Guns."+gun+".Shoot.Projectile"), guns.getString("Guns."+gun+".Shoot.Sound"),
                    guns.getString("Guns."+gun+".Reload.Sound"));
            gunObj.setBulletsPerPitch(guns.getInt("Guns." + gun + ".Burst.BulletsPerPitch"));
            gunObj.setBulletsPerYaw(guns.getInt("Guns." + gun + ".Burst.BulletsPerYaw"));
            gunObj.setDelayBullets(guns.getInt("Guns." + gun + ".Burst.DelayBullets"));
            gunObj.setBulletsPerBurst(guns.getInt("Guns." + gun + ".Burst.BulletsPerBurst"));
            gunObj.setDropoffPerBlock(guns.getInt("Guns." + gun + ".Shoot.DropoffPerBlock"));
            gunObj.setAccuracy((float)guns.getDouble("Guns." + gun + ".Shoot.Accuracy"));
            gunObj.setScope(guns.getBoolean("Guns." + gun + ".Shoot.Scope"));
            gunObj.setDamage(guns.getDouble("Guns." + gun + ".Shoot.Damage"));
            gunObj.setReloadDuration(guns.getInt("Guns." + gun + ".Reload.Duration"));
            gunObj.setMagSize(guns.getInt("Guns." + gun + ".Reload.Amount"));
            gunObj.setTotalAmmoSize(guns.getInt("Guns." + gun + ".Reload.TotalAmount"));
            gunObj.setBulletsPerShot(guns.getInt("Guns." + gun + ".Shoot.BulletsPerShot"));
            gunObj.setDelayPerShot(guns.getInt("Guns." + gun + ".Shoot.Delay"));
            gunObj.setKillReward(guns.getInt("Guns."+gun+".ItemInfo.KillReward"));
            Main.getWeaponManager().addGun(gunObj);
        }
    }

    private void saveMaps(File dataFolder) {
        File file = new File(dataFolder, "maps.yml");
        try {
            if(!file.exists()) {
                file.createNewFile();
                maps = YamlConfiguration.loadConfiguration(file);
                if(maps.getString("Game") == null) {
                    maps.set("Game", "No games made yet");
                }
            }
            maps.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLobbyTime() {
        return 0;
    }

    public void loadConfig() {
        Main.getInstance().reloadConfig();
        config = Main.getInstance().getConfig();
        //todo replace hardcoded timing values with config
    }

    private void log(String msg) {
        Main.getInstance().getLogger().log(Level.INFO, msg);
    }

    private void error(String msg) {
        Main.getInstance().getLogger().log(Level.SEVERE, msg);
    }

}
