package com.podcrash.squadassault.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;


public final class Utils {

    public static Location getDeserializedLocation(String s) {
        if (s == null) {
            return null;
        }
        final String[] split = s.split(",");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0, Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static List<Location> getDeserializedLocations(List<String> list) {
        List<Location> list2 = new ArrayList<>();
        for(String s : list) {
            list2.add(getDeserializedLocation(s));
        }
        return list2;
    }

    public static double dumbMinecraftDegrees(double n) {
        return (n > 179.9) ? (-180.0 + (n - 179.9)) : n;
    }

    public static double offset2d(Vector a, Vector b) {
        a.setY(0);
        b.setY(0);
        return a.subtract(b).length();
    }

    public static double offset(Vector a, Vector b) {
        return a.subtract(b).length();
    }

    public static String getSerializedLocation(Location location) {
        return location.getWorld().getName() + "," + (location.getBlockX() + 0.5) + "," + location.getBlockY() + "," + (location.getBlockZ() + 0.5) + "," + location.getYaw() + "," + location.getPitch();
    }

    public static List<String> getSerializedLocations(final List<Location> list) {
        List<String> list2 = new ArrayList<>();
        for (Location location : list) {
            list2.add(getSerializedLocation(location));
        }
        return list2;
    }


}
