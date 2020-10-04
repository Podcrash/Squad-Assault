package com.podcrash.squadassault.weapons;

import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponManager {

    private final List<Gun> guns;
    private final List<Grenade> grenades;
    private final Map<Projectile, ProjectileStats> projectiles;

    public WeaponManager() {
        guns = new ArrayList<>();
        grenades = new ArrayList<>();
        projectiles = new HashMap<>();
    }

    public List<Gun> getGuns() {
        return guns;
    }

    public Gun getGun(String name) {
        for(Gun gun : guns) {
            if(gun.getName().equals(name)) {
                return gun;
            }
        }
        return null;
    }

    public Gun getGun(ItemStack stack) {
        for(Gun gun : guns) {
            if(gun.getItem().equals(stack)) {
                return gun;
            }
        }
        return null;
    }

    public Grenade getGrenade(ItemStack stack) {
        for(Grenade grenade : grenades) {
            if(grenade.getItem().equals(stack)) {
                return grenade;
            }
        }
        return null;
    }

    public Grenade getGrenade(String name) {
        for(Grenade nade : grenades) {
            if(nade.getName().equals(name)) {
                return nade;
            }
        }
        return null;
    }

    public void addGun(Gun gun) {
        guns.add(gun);
    }

    public void addGrenade(Grenade grenade) {
        grenades.add(grenade);
    }

    public List<Grenade> getGrenades() {
        return grenades;
    }

    public Map<Projectile, ProjectileStats> getProjectiles() {
        return projectiles;
    }
}
