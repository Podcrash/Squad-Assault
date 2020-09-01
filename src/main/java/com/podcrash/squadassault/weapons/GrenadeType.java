package com.podcrash.squadassault.weapons;

public enum GrenadeType {
    FRAG(1), FIRE(1), DECOY(1), SMOKE(1), FLASH(2);

    int max;

    GrenadeType(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }
}
