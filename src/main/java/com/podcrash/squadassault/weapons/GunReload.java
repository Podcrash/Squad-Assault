package com.podcrash.squadassault.weapons;

public class GunReload {
    private final int oldAmount;
    private final int duration;
    private double left;
    private boolean outOfAmmo;

    public GunReload(int oldAmount, int duration) {
        this.oldAmount = oldAmount;
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

    public int getOldAmount() {
        return oldAmount;
    }

    public boolean isOutOfAmmo() {
        return outOfAmmo;
    }

    public void setOutOfAmmo(boolean outOfAmmo) {
        this.outOfAmmo = outOfAmmo;
    }
}
