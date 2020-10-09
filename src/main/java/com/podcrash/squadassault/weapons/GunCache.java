package com.podcrash.squadassault.weapons;

import com.podcrash.squadassault.game.SAGame;

public class GunCache {
    private int ticks;
    private int rounds;
    private final SAGame game;
    private int ticksLeft;
    private int yawDirection;
    private float accuracyYaw;
    private float accuracyPitch;
    private boolean firstShot;
    private double cone;
    private boolean isActive;
    private long lastShot;

    public GunCache(SAGame game, int rounds, double cone) {
        this.ticks = 0;
        this.rounds = rounds;
        this.game = game;
        this.ticksLeft = 30;
        this.yawDirection = 0;
        this.accuracyYaw = 0;
        this.accuracyPitch = 0;
        this.firstShot = true;
        this.cone = cone;
        isActive = true;
        lastShot = System.currentTimeMillis();
    }


    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public SAGame getGame() {
        return game;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public void setTicksLeft(int ticksLeft) {
        this.ticksLeft = ticksLeft;
    }

    public int getYawDirection() {
        return yawDirection;
    }

    public void setYawDirection(int yawDirection) {
        this.yawDirection = yawDirection;
    }

    public float getAccuracyYaw() {
        return accuracyYaw;
    }

    public void setAccuracyYaw(float accuracyYaw) {
        this.accuracyYaw = accuracyYaw;
    }

    public float getAccuracyPitch() {
        return accuracyPitch;
    }

    public void setAccuracyPitch(float accuracyPitch) {
        this.accuracyPitch = accuracyPitch;
    }

    public boolean isFirstShot() {
        return firstShot;
    }

    public void setFirstShot(boolean firstShot) {
        this.firstShot = firstShot;
    }

    public double getCone() {
        return cone;
    }

    public void setCone(double cone) {
        this.cone = cone;
    }

    public long getLastShot() {
        return lastShot;
    }

    public void setLastShot(long lastShot) {
        this.lastShot = lastShot;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
