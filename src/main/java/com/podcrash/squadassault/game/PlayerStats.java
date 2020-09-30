package com.podcrash.squadassault.game;

public class PlayerStats {

    private final String name;
    private int kills;
    private int assists;
    private int deaths;
    private int headshots;
    private int bombPlants;
    private int bombDefuses;
    private double damage;
    private int roundsPlayed;

    public PlayerStats(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public void addKills(int kills) {
        this.kills += kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeaths(int deaths) {
        this.deaths += deaths;
    }

    public int getHeadshots() {
        return headshots;
    }

    public void addHeadshots(int headshots) {
        this.headshots += headshots;
    }

    public int getBombPlants() {
        return bombPlants;
    }

    public void addBombPlants(int bombPlants) {
        this.bombPlants += bombPlants;
    }

    public int getBombDefuses() {
        return bombDefuses;
    }

    public void addBombDefuses(int bombDefuses) {
        this.bombDefuses += bombDefuses;
    }

    public double getDamage() {
        return damage;
    }

    public void addDamage(double damage) {
        this.damage += damage;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void addRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed += roundsPlayed;
    }

    public double getADR() {
        if(roundsPlayed == 0) {
            return damage;
        }
        double dmg100 = damage / 20 * 100; //normalize
        return dmg100 / (double) roundsPlayed;
    }

    public int getAssists() {
        return assists;
    }

    public void addAssists(int assists) {
        this.assists += assists;
    }

    public void export() {
        //todo hook this in with ky's leaderboard stuff
    }
}
