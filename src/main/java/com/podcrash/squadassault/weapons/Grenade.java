package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.util.Item;

public class Grenade {

    private Item item;
    private double effectPower;
    private String name;
    private int delay;
    private int duration;
    private GrenadeType type;
    private double throwSpeed;

    //todo actual throwing stuff

    public Grenade(String name, GrenadeType type, Item item, int delay, int duration, double throwSpeed, double effectPower) {
        this.name = name;
        this.type = type;
        this.item = item;
        this.delay = delay;
        this.duration = duration;
        this.throwSpeed = throwSpeed;
        this.effectPower = effectPower;
    }

    public Item getItem() {
        return item;
    }

    public double getEffectPower() {
        return effectPower;
    }

    public String getName() {
        return name;
    }

    public int getDelay() {
        return delay;
    }

    public int getDuration() {
        return duration;
    }

    public GrenadeType getType() {
        return type;
    }

    public double getThrowSpeed() {
        return throwSpeed;
    }
}
