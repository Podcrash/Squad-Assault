package com.podcrash.squadassault.util;

import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.nms.NmsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
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

    public static String randomString(final int n) {
        final StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            sb.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt(Randomizer.randomInt(
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".length())));
        }
        return sb.toString();
    }


    public static Location getArmTip(ArmorStand as) {
        // Gets shoulder location
        Location asl = as.getLocation().clone();
        asl.setYaw(asl.getYaw() + 90f);
        Vector dir = asl.getDirection();
        asl.setX(asl.getX() + 5f / 16f * dir.getX());
        asl.setY(asl.getY() + 22f / 16f);
        asl.setZ(asl.getZ() + 5f / 16f * dir.getZ());
        // Get Hand Location

        EulerAngle ea = as.getRightArmPose();
        Vector armDir = getDirection(ea.getY(), ea.getX(), -ea.getZ());
        armDir = rotateAroundAxisY(armDir, Math.toRadians(asl.getYaw()-90f));
        asl.setX(asl.getX() + 10f / 16f * armDir.getX());
        asl.setY(asl.getY() + 10f / 16f * armDir.getY());
        asl.setZ(asl.getZ() + 10f / 16f * armDir.getZ());

        return asl;
    }

    public static Vector getDirection(Double yaw, Double pitch, Double roll) {
        Vector v = new Vector(0, -1, 0);
        v = rotateAroundAxisX(v, pitch);
        v = rotateAroundAxisY(v, yaw);
        v = rotateAroundAxisZ(v, roll);
        return v;
    }

    private static Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    private static Vector rotateAroundAxisY(Vector v, double angle) {
        angle = -angle;
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    private static Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

}
