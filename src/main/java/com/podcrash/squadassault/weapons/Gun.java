package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.util.Item;

public class Gun {

    private final Item item;
    private final String name;
    private final GunHotbarType type;
    private final boolean projectile;
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
    private int bulletsPerYaw;
    private int bulletsPerPitch;
    private int delayBullets;
    private int bulletsPerBurst;
    //todo shooting cache stuff

    public Gun(String name, Item item, GunHotbarType type, boolean projectile, String shootSound, String reloadSound) {
        this.name = name;
        this.item = item;
        this.type = type;
        this.projectile = projectile;
        this.shootSound = shootSound;
        this.reloadSound = reloadSound;
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

    public boolean isScope() {
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
}
