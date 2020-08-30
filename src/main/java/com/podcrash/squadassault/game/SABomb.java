package com.podcrash.squadassault.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class SABomb {

    private Item item;
    private int timer;
    private Location location;
    private Player carrier;
    private boolean planted;

    public SABomb() {
        planted = false;
    }

    public boolean isPlanted() {
        return planted;
    }

    public void setDrop(Item item) {
        this.item = item;
        carrier = null;
    }

    public void setCarrier(Player carrier) {
        location = null;
        this.carrier = carrier;
        this.item = null;
    }

    public Player getCarrier() {
        return carrier;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void setLocation(Location location) {
        this.location = location;
        carrier = null;
        item = null;
    }

    public void reset() {
        timer = 0;
        if(location != null) {
            location.getBlock().setType(Material.AIR);
            location = null;
        }
        item = null;
        carrier = null;
        planted = false;
    }

    public Location getLocation() {
        if(location != null) {
            return location;
        }
        if(carrier != null) {
            return carrier.getLocation();
        }
        return item.getLocation();
    }

    public void setPlanted(boolean planted) {
        this.planted = planted;
    }
}
