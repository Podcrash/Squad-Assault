package com.podcrash.squadassault.weapons;

import java.util.ArrayList;
import java.util.List;

public class WeaponManager {

    private List<Gun> guns;
    private List<Grenade> grenades;

    public WeaponManager() {
        guns = new ArrayList<>();
        grenades = new ArrayList<>();
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
}
