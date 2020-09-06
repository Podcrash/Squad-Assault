package com.podcrash.squadassault.game;

public class PlayerStats {

    private final String name;
    private int kills;
    private int assists;
    private int deaths;
    private int headshots;
    private int bombPlants;
    private int bombDefuses;
    private int damage;
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

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getHeadshots() {
        return headshots;
    }

    public void setHeadshots(int headshots) {
        this.headshots = headshots;
    }

    public int getBombPlants() {
        return bombPlants;
    }

    public void setBombPlants(int bombPlants) {
        this.bombPlants = bombPlants;
    }

    public int getBombDefuses() {
        return bombDefuses;
    }

    public void setBombDefuses(int bombDefuses) {
        this.bombDefuses = bombDefuses;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }

    public double getADR() {
        return (double) damage / (double) roundsPlayed;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }
    //todo hook this in with ky's leaderboard stuff
}
