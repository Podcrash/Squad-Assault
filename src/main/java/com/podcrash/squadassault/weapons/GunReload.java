package com.podcrash.squadassault.weapons;

public class GunReload {
    private final int duration;
    private double left;

    public GunReload(int duration) {
        this.duration = duration;
        this.left = duration;
    }

    public int getDuration() {
        return duration;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }
}
