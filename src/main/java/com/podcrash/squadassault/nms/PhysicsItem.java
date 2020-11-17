package com.podcrash.squadassault.nms;


import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface PhysicsItem {

    Location getLocation();

    boolean isRemoved();

    void remove();

    Entity getEntity();

}
