package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.Main;
import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.events.GunDamageEvent;
import com.podcrash.squadassault.nms.NmsUtils;
import com.podcrash.squadassault.util.Item;
import com.podcrash.squadassault.util.Randomizer;
import com.podcrash.squadassault.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Gun {

    private final Item item;
    private final String name;
    private final GunHotbarType type;
    private final boolean projectile;
    private final boolean isShotgun;
    private final String shootSound;
    private final String reloadSound;
    private int reloadDuration;
    private int magSize;
    private int totalAmmoSize;
    private double accuracy;
    private double damage;
    private int bulletsPerShot;
    private boolean scope;
    private int delayPerShot;
    private double dropoffPerBlock;
    private double armorPen;
    private int bulletsPerYaw;
    private int bulletsPerPitch;
    private int delayBullets;
    private int bulletsPerBurst;
    private int killReward;
    private double projectileConeMin;
    private double projectileConeMax;
    private double coneIncPerBullet;
    private double resetPerTick;
    private final Map<UUID, Long> delay;
    private final Map<UUID, GunCache> cache;
    private final Map<UUID, GunReload> reloading;

    public Gun(String name, Item item, GunHotbarType type, boolean projectile, String shootSound, String reloadSound,
     boolean isShotgun) {
        this.name = name;
        this.item = item;
        this.type = type;
        this.projectile = projectile;
        this.shootSound = shootSound;
        this.reloadSound = reloadSound;
        this.isShotgun = isShotgun;
        delay = new HashMap<>();
        reloading = new HashMap<>();
        cache = new HashMap<>();
    }

    public void setReloadDuration(int reloadDuration) {
        this.reloadDuration = reloadDuration;
    }

    public int getReloadDuration() {
        return reloadDuration;
    }

    public int getMagSize() {
        return magSize;
    }

    public void setMagSize(int magSize) {
        this.magSize = magSize;
    }

    public int getTotalAmmoSize() {
        return totalAmmoSize;
    }

    public void setTotalAmmoSize(int totalAmmoSize) {
        this.totalAmmoSize = totalAmmoSize;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getBulletsPerShot() {
        return bulletsPerShot;
    }

    public void setBulletsPerShot(int bulletsPerShot) {
        this.bulletsPerShot = bulletsPerShot;
    }

    public boolean hasScope() {
        return scope;
    }

    public void setScope(boolean scope) {
        this.scope = scope;
    }

    public int getDelayPerShot() {
        return delayPerShot;
    }

    public void setDelayPerShot(int delayPerShot) {
        this.delayPerShot = delayPerShot;
    }

    public double getDropoffPerBlock() {
        return dropoffPerBlock;
    }

    public void setDropoffPerBlock(double dropoffPerBlock) {
        this.dropoffPerBlock = dropoffPerBlock;
    }

    public int getBulletsPerYaw() {
        return bulletsPerYaw;
    }

    public void setBulletsPerYaw(int bulletsPerYaw) {
        this.bulletsPerYaw = bulletsPerYaw;
    }

    public int getBulletsPerPitch() {
        return bulletsPerPitch;
    }

    public void setBulletsPerPitch(int bulletsPerPitch) {
        this.bulletsPerPitch = bulletsPerPitch;
    }

    public int getDelayBullets() {
        return delayBullets;
    }

    public void setDelayBullets(int delayBullets) {
        this.delayBullets = delayBullets;
    }

    public int getBulletsPerBurst() {
        return bulletsPerBurst;
    }

    public void setBulletsPerBurst(int bulletsPerBurst) {
        this.bulletsPerBurst = bulletsPerBurst;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item;
    }

    public GunHotbarType getType() {
        return type;
    }

    public boolean isProjectile() {
        return projectile;
    }

    public String getShootSound() {
        return shootSound;
    }

    public String getReloadSound() {
        return reloadSound;
    }

    public void shoot(SAGame game, Player player) {
        if(player.getInventory().getHeldItemSlot() != type.ordinal()) {
            return;
        }
        long del = System.currentTimeMillis() / 49;
        if(delay.get(player.getUniqueId()) == null) {
            delay.put(player.getUniqueId(), del);
        } else if(del - delay.get(player.getUniqueId()) <= delayPerShot) {
            return;
        }
        GunCache gunCache = cache.get(player.getUniqueId());
        if(gunCache == null) {
            cache.put(player.getUniqueId(), new GunCache(game, bulletsPerShot, projectileConeMin));
        } else {
            gunCache.setRounds(bulletsPerShot);
            gunCache.setLastShot(System.currentTimeMillis());
        }
        delay.put(player.getUniqueId(), del);
    }

    public void reload(Player player, int slot) {
        ItemStack itemStack = player.getInventory().getItem(slot);
        if (!item.equals(itemStack) || itemStack.getAmount() >= magSize || reloading.containsKey(player.getUniqueId())) {
            return;
        }
        if(Utils.getReserveAmmo(itemStack) <= 0) {
            NmsUtils.sendActionBar(player, itemStack.getAmount() + " / " + Utils.getReserveAmmo(itemStack));
            //play sound;
            return;
        }
        //play sound

        reloading.put(player.getUniqueId(), new GunReload(player.getItemInHand().getAmount(), reloadDuration));
    }

    public void resetPlayer(Player player) {
        reloading.remove(player.getUniqueId());
        cache.remove(player.getUniqueId());
    }

    public void resetDelay(Player player) {
        delay.remove(player.getUniqueId());
    }

    public int getKillReward() {
        return killReward;
    }

    public void setKillReward(int killReward) {
        this.killReward = killReward;
    }

    public void tick() {
        if(!reloading.isEmpty()) {
            Iterator<Map.Entry<UUID, GunReload>> iterator = reloading.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<UUID, GunReload> entry = iterator.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                if(player != null && !player.isDead() && player.isOnline() && Main.getGameManager().getGame(player) != null) {
                    if(item.equals(player.getItemInHand())) {
                        player.getItemInHand().setDurability((short)(entry.getValue().getLeft() / entry.getValue().getDuration() * player.getItemInHand().getType().getMaxDurability()));
                        if(entry.getValue().getLeft() <= 0) {
                            iterator.remove();
                            int oldAmount = entry.getValue().getOldAmount();
                            int newAmount =
                                    Utils.getReserveAmmo(player.getItemInHand()) >= magSize || Utils.getReserveAmmo(player.getItemInHand()) >= magSize - oldAmount ?
                                            magSize :
                                            Utils.getReserveAmmo(player.getItemInHand()) + oldAmount;
                            Utils.setReserveAmmo(player.getItemInHand(),
                                    Utils.getReserveAmmo(player.getItemInHand()) - newAmount);
                            NmsUtils.sendActionBar(player, newAmount + " / " + Utils.getReserveAmmo(player.getItemInHand()));
                            player.getItemInHand().setAmount(newAmount);
                            player.getItemInHand().setDurability((short) 0);
                            //play sound?
                        }
                        entry.getValue().setLeft(entry.getValue().getLeft() - 1);
                    } else {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }
        }
        if(!delay.isEmpty()) {
            Iterator<Map.Entry<UUID, Long>> iterator = delay.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                long del = System.currentTimeMillis() / 49;
                if(player == null || player.isDead() || !player.isOnline() || Main.getGameManager().getGame(player) == null || del - entry.getValue() > delayPerShot) {
                    iterator.remove();
                }
            }
        }
        if (cache.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, GunCache>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GunCache> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            GunCache gunCache = entry.getValue();
            if(System.currentTimeMillis() - gunCache.getLastShot() > delayPerShot*50) {
                gunCache.setCone(Math.max(gunCache.getCone() - resetPerTick, projectileConeMin));
            }
            if (!reloading.containsKey(entry.getKey())) {
                if (player != null && !player.isDead() && player.isOnline() && gunCache.getGame() != null) {
                    if (!gunCache.getGame().getSpectators().contains(player)) {
                        gunCache.setTicksLeft(gunCache.getTicksLeft() - 1);
                        if (gunCache.getRounds() > 0 && item.equals(player.getInventory().getItemInHand())) {
                            gunCache.setTicks(gunCache.getTicks() + 1);
                            if (gunCache.isFirstShot()) {
                                gunCache.setFirstShot(false);
                            } else if (gunCache.getTicks() % delayBullets != 0) {
                                return;
                            }
                            if(gunCache.getCone() == projectileConeMin) {
                                gunCache.setFirstShot(true);
                            }
                            if (player.getItemInHand().getAmount() == 1) {
                                iterator.remove();
                                reload(player, getType().ordinal());

                                //play sound
                            } else {
                                gunCache.setRounds(gunCache.getRounds() - 1);
                                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                            }
                            Location eye = player.getLocation();
                            boolean isMoving = player.getVelocity().equals(new Vector());
                            if (player.isSneaking()) {
                                eye.subtract(0, 0.03, 0);
                            } else {
                                eye.subtract(0, 0.1, 0);
                            }
                            //play sound

                            if (accuracy == 0) {
                                gunCache.setAccuracyPitch(0);
                                gunCache.setAccuracyYaw(0);
                            }
                            float yaw = eye.getYaw();
                            float pitch = eye.getPitch();
                            double x = eye.getX();
                            double y = eye.getY();
                            double z = eye.getZ();
                            if (isMoving) {
                                gunCache.setAccuracyYaw(Randomizer.randomRange((int) (-accuracy * 10),
                                        (int) (accuracy * 10)) + 0.5f);
                                gunCache.setAccuracyPitch(Randomizer.randomRange((int) (-accuracy * 10),
                                        (int) (accuracy * 10)) + 0.5f);
                            } else if (scope && player.isSneaking()) {
                                gunCache.setAccuracyYaw(0);
                                gunCache.setAccuracyPitch(0);
                            } else if (scope && !player.isSneaking()) {
                                gunCache.setAccuracyYaw(Randomizer.randomRange((int) (-accuracy),
                                        (int) (accuracy)) + 0.5f);
                                gunCache.setAccuracyPitch(Randomizer.randomRange((int) (-accuracy),
                                        (int) (accuracy)) + 0.5f);
                            }
                            double yawRad = Math.toRadians(Utils.dumbMinecraftDegrees(yaw + 0.6) + gunCache.getAccuracyYaw() + 90);
                            double pitchRad = Math.toRadians(pitch + gunCache.getAccuracyPitch() + 90);
                            double cot = Math.sin(pitchRad) * Math.cos(yawRad);
                            double cos = Math.cos(pitchRad);
                            double sin2 = Math.sin(pitchRad) * Math.sin(yawRad);
                            if (projectile) {
                                if (isShotgun) {
                                    shotgun(player, isMoving, gunCache);
                                } else {
                                    projectile(player, isMoving, gunCache);
                                }
                            } else {
                                hitscan(player, eye, x, y, z, cot, cos, sin2, gunCache);
                            }
                            eye.setX(x);
                            eye.setY(y);
                            eye.setZ(sin2);
                        } else {
                            if (gunCache.getTicksLeft() > 0) {
                                continue;
                            }
                            iterator.remove();
                        }
                    } else {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

    private void hitscan(Player player, Location eye, double x, double y, double z, double cot, double cos,
                         double sin2, GunCache gunCache) {
        double distance = 0.5;
        while(distance < 100) {
            eye.setX(x + distance * cot);
            eye.setY(y + distance * cos);
            eye.setZ(z + distance * sin2);
            //particle effect?
            Material type = eye.getBlock().getType();
            if(type != Material.CROPS) {
                int xMod = (int)((eye.getX() - eye.getBlockX()) * 1000.0);
                int yMod = (int)((eye.getY() - eye.getBlockY()) * 10.0);
                int zMod = (int)((eye.getZ() - eye.getBlockZ()) * 1000.0);
                String data = eye.getBlock().getState().getData().toString();
                boolean inverted = data.contains("inverted");
                //check non full blocks to see if hit passes
                if(type.name().contains("FENCE")) {
                    if(xMod >= 370 && xMod <= 620 && zMod >= 370 && zMod <= 620) {
                        break;
                    }
                } else if(type == Material.COBBLE_WALL) {
                    if (xMod <= 750 && xMod >= 240 && zMod <= 750) {
                        break;
                    }
                } else if (type.name().contains("STEP") || type.name().contains("STONE_SLAB2")) {
                    if (inverted) {
                        if (yMod > 5) {
                            break;
                        }
                    } else if (yMod < 5) {
                        break;
                    }
                } else {
                    if (!type.name().contains("STAIRS")) {
                        break;
                    }
                    if (data.contains("NORTH")) {
                        if (inverted) {
                            if (zMod > 500 || (zMod < 500 && yMod >= 5)) {
                                break;
                            }
                        } else if (zMod > 500 || (zMod < 500 && yMod < 5)) {
                            break;
                        }
                    } else if (data.contains("SOUTH")) {
                        if (inverted) {
                            if (zMod < 500 || (zMod > 500 && yMod >= 5)) {
                                break;
                            }
                        } else if (zMod < 500 || (zMod > 500 && yMod < 5)) {
                            break;
                        }
                    } else if (data.contains("EAST")) {
                        if (inverted) {
                            if (xMod < 500 || (xMod > 500 && yMod >= 5)) {
                                break;
                            }
                        } else if (xMod < 500 || (xMod > 500 && yMod < 5)) {
                            break;
                        }
                    } else if (data.contains("WEST")) {
                        if (inverted) {
                            if (xMod > 500 || (xMod < 500 && yMod >= 5)) {
                                break;
                            }
                        } else if (xMod > 500 || (xMod < 500 && yMod < 5)) {
                            break;
                        }
                    }
                }
            }
            Player hit = null;
            for(Player p : gunCache.getGame().getTeamA().getPlayers()) {
                if (hitscanHit(player, gunCache, eye, p)) {
                    hit = p;
                    break;
                }
            }
            for(Player p : gunCache.getGame().getTeamB().getPlayers()) {
                if (hitscanHit(player, gunCache, eye, p)) {
                    hit = p;
                    break;
                }
            }
            if(hit == null) {
                distance += 0.25;
                continue;
            }
            if((hit.isSneaking() || eye.getY() - hit.getLocation().getY() <= 1.35 || eye.getY() - hit.getLocation().getY() >= 1.9) && (eye.getY() - hit.getLocation().getY() <= 1.27 || eye.getY() - hit.getLocation().getY() >= 1.82)) {
                double armorPen = hit.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE ? 1 :
                        this.armorPen;
                double rangeFalloff = (dropoffPerBlock * distance);
                double damage = getDamage();
                double finalDamage = Math.max(0,armorPen*(damage - rangeFalloff));

                Main.getInstance().getServer().getPluginManager().callEvent(new GunDamageEvent(finalDamage, false, player, hit));
                Main.getGameManager().damage(gunCache.getGame(), player, hit,
                        finalDamage, name);
                break;
            }
            double armorPen = hit.getInventory().getChestplate().getType() == Material.LEATHER_HELMET ? 1 :
                    this.armorPen;
            double rangeFalloff = (dropoffPerBlock * distance);
            double damage = getDamage()*2.5;
            double finalDamage = Math.max(0,armorPen*(damage - rangeFalloff));
            Main.getInstance().getServer().getPluginManager().callEvent(new GunDamageEvent(finalDamage, true, player, hit));
            gunCache.getGame().getStats().get(player.getUniqueId()).addHeadshots(1);
            Main.getGameManager().damage(gunCache.getGame(), player, hit,
                    finalDamage, name + " headshot");
            break;
        }

    }

    private void projectile(Player player, boolean moving, GunCache cache) {
        Snowball snowball = player.launchProjectile(Snowball.class);

        double cone = projectileSpray(player, moving, cache);
        Vector spray;
        if(cache.isFirstShot() && !player.isOnGround()) {
            spray = new Vector(-0.5, -0.5, -0.5);
        } else {
            spray = new Vector(Randomizer.random() - 0.5, (Randomizer.random() - 0.2) * (5d/8d),
                    Randomizer.random() - 0.5);
        }
        spray.normalize();
        spray.multiply(cone);
        spray.add(player.getLocation().getDirection());
        spray.normalize();

        snowball.setVelocity(spray.multiply(4));
        cache.setCone(Math.min(projectileConeMax, cone + coneIncPerBullet));
        Main.getWeaponManager().getProjectiles().put(snowball, new ProjectileStats(name, player.getLocation().clone(),
                dropoffPerBlock, damage, armorPen, player));
    }

    private void shotgun(Player player, boolean moving, GunCache cache) {
        for(int i = 0; i < bulletsPerShot; i++) {
            //todo sound here
            projectile(player, moving, cache);
        }
    }

    private double projectileSpray(Player player, boolean isMoving, GunCache cache) {
        double cone = cache.getCone();
        if(!player.isOnGround()) {
            cone += 0.12;
        } else if(player.isSprinting()) {
            cone += 0.06;
        } else if(player.isSneaking()) {
            cone *= 0.8;
        }
        return cone;
    }

    private boolean hitscanHit(Player player, GunCache gunCache, Location eye, Player p) {
        return player != p && !gunCache.getGame().getSpectators().contains(p) && !gunCache.getGame().sameTeam(p,
                player) && (p.getLocation().add(0, 0.2, 0).distance(eye) <= 0.4 || p.getLocation().add(0, 1, 0).distance(eye) <= 0.5 || p.getEyeLocation().distance(eye) <= 0.35 && !p.isDead());
    }

    public void setArmorPen(double armorPen) {
        this.armorPen = armorPen;
    }

    public double getProjectileConeMin() {
        return projectileConeMin;
    }

    public void setProjectileConeMin(double projectileConeMin) {
        this.projectileConeMin = projectileConeMin;
    }

    public double getProjectileConeMax() {
        return projectileConeMax;
    }

    public void setProjectileConeMax(double projectileConeMax) {
        this.projectileConeMax = projectileConeMax;
    }

    public double getConeIncPerBullet() {
        return coneIncPerBullet;
    }

    public void setConeIncPerBullet(double coneIncPerBullet) {
        this.coneIncPerBullet = coneIncPerBullet;
    }

    public double getResetPerTick() {
        return resetPerTick;
    }

    public void setResetPerTick(double resetPerTick) {
        this.resetPerTick = resetPerTick;
    }
}
