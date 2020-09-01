package com.podcrash.squadassault.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

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

}
