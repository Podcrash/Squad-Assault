package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Hitbox {

    private final Vector max;
    private final Vector min;

    public Hitbox(Block block) {
        this(block.getLocation().toVector(), block.getLocation().toVector().add(new Vector(1.0,
                (block.getType() == Material.STEP) ? 0.5 : 1.0, 1.0)));
    }

    public Hitbox(Vector vector, Vector vector2) {
        this(vector.getX(), vector.getY(), vector.getZ(), vector2.getX(), vector2.getY(), vector2.getZ());
    }

    public Hitbox(double x, double y, double z, double x2, double y2, double z2) {
        this.max = new Vector(Math.max(x, x2), Math.max(y, y2), Math.max(z, z2));
        this.min = new Vector(Math.min(x, x2), Math.min(y, y2), Math.min(z, z2));
    }

    public boolean intersects(Hitbox hitbox) {
        return max.getX() >= hitbox.min.getX() && min.getX() <= hitbox.max.getX() && max.getY() >= hitbox.min.getY() && min.getY() <= hitbox.max.getY() && max.getZ() >= hitbox.min.getZ() && min.getZ() <= hitbox.max.getZ();
    }

    public Vector getMax() {
        return max;
    }

    public Vector getMin() {
        return min;
    }

    public Hitbox grow(double x, double y, double z) {
        this.min.subtract(new Vector(x, y, z));
        this.max.add(new Vector(x, y, z));
        return this;
    }

    public AxisAlignedBB toNmsHitbox() {
        return AxisAlignedBB.a(this.min.getX(), this.min.getY(), this.min.getZ(), this.max.getX(), this.max.getY(),
                this.max.getZ());
    }

}
