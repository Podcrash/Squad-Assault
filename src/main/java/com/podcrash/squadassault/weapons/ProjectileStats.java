package com.podcrash.squadassault.weapons;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProjectileStats {

    private final String gunName;
    private final Location location;
    private final double dropoff;
    private final double damage;
    private final double armorPen;
    private final Player shooter;

    public ProjectileStats(String gunName, Location location, double dropoff, double damage, double armorPen, Player shooter) {
        this.gunName = gunName;
        this.location = location;
        this.dropoff = dropoff;
        this.damage = damage;
        this.armorPen = armorPen;
        this.shooter = shooter;
    }

    public Location getLocation() {
        return location;
    }

    public double getDropoff() {
        return dropoff;
    }

    public double getDamage() {
        return damage;
    }

    public double getArmorPen() {
        return armorPen;
    }

    public Player getShooter() {
        return shooter;
    }

    public String getGunName() {
        return gunName;
    }
}
