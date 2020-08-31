package com.podcrash.squadassault.game;

public class Defuse {
    private int time;
    private int max;

    public Defuse(int time) {
        this.time = time;
        this.max = time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return this.time;
    }

    public int getMax() {
        return this.max;
    }


}
