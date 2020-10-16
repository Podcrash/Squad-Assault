package com.podcrash.squadassault.util;

import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
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

    public static Vector getTrajectory(Vector from, Vector to) {
        return to.subtract(from.normalize());
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

    public static SATeam.Team nullSafeValueOf(String s) {
        SATeam.Team[] values;
        for (int length = (values = SATeam.Team.values()).length, i = 0; i < length; ++i) {
            final SATeam.Team team = values[i];
            if (team.name().equals(s)) {
                return team;
            }
        }
        return null;
    }

    public static int getReserveAmmo(ItemStack stack) {
        return NmsUtils.getNBTInteger(stack, "reserve");
    }


    //    public static int getReserveAmmo(ItemStack stack) {
//        ItemMeta meta = stack.getItemMeta();
//        if(meta == null || meta.getLore() == null) {
//            return -1;
//        }
//        List<String> lore = meta.getLore();
//        String line = lore.get(0);
//        if(line.contains("Reserve Ammo: ")) {
//            try {
//                return Integer.parseInt(line.substring(line.indexOf(": ")+2));
//            } catch (NumberFormatException e) {
//                Main.getInstance().getLogger().log(Level.SEVERE, "invalid item meta " + line + " belonging to " + stack.getType());
//                return -1;
//            }
//        } else {
//            return -1;
//        }
//    }

    public static ItemStack setReserveAmmo(ItemStack stack, int ammo) {
        stack = NmsUtils.addNBTInteger(stack, "reserve", ammo);
        setReserveAmmoLore(stack, ammo);
        return stack;
    }

    public static void setReserveAmmoLore(ItemStack stack, int ammo) {
        ItemMeta meta = stack.getItemMeta();
        if(meta != null) {
            if(meta.getLore() == null) {
                meta.setLore(new ArrayList<>(Collections.singletonList("Reserve Ammo: " + ammo)));
            } else {
                meta.getLore().set(0, "Reserve Ammo: " + ammo);
            }
            stack.setItemMeta(meta);
        }
    }
}
