package com.podcrash.squadassault.nms;

import net.jafama.FastMath;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;

import java.util.List;

public class BulletSnowball extends EntitySnowball {
    private int blockX = -1;
    private int blockY = -1;
    private int blockZ = -1;
    private int ar;
    private int i;
    private Block inBlockId;

    public BulletSnowball(World var1) {
        super(var1);
    }

    public BulletSnowball(World var1, EntityLiving var2) {
        super(var1, var2);
    }

    public BulletSnowball(World var1, double var2, double var4, double var6) {
        super(var1, var2, var4, var6);
        this.i = 0;
    }

    @Override
    public void t_() {
        this.P = this.locX;
        this.Q = this.locY;
        this.R = this.locZ;
        super.t_();
        if (this.shake > 0) {
            --this.shake;
        }

        if (this.inGround) {
            if (this.world.getType(new BlockPosition(this.blockX, this.blockY, this.blockZ)).getBlock() == this.inBlockId) {
                ++this.i;
                if (this.i == 1200) {
                    this.die();
                }

                return;
            }

            this.inGround = false;
            this.motX *= this.random.nextFloat() * 0.2F;
            this.motY *= this.random.nextFloat() * 0.2F;
            this.motZ *= this.random.nextFloat() * 0.2F;
            this.i = 0;
            this.ar = 0;
        } else {
            ++this.ar;
        }

        Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
        Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);
        vec3d = new Vec3D(this.locX, this.locY, this.locZ);
        vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        if (movingobjectposition != null) {
            vec3d1 = new Vec3D(movingobjectposition.pos.a, movingobjectposition.pos.b, movingobjectposition.pos.c);
        }

        if (!this.world.isClientSide) {
            Entity entity = null;
            List list = this.world.getEntities(this, this.getBoundingBox().a(this.motX, this.motY, this.motZ).grow(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            EntityLiving entityliving = this.getShooter();

            for (Object o : list) {
                Entity entity1 = (Entity) o;
                if (entity1.ad() && (entity1 != entityliving || this.ar >= 5)) {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.a(vec3d, vec3d1);
                    if (movingobjectposition1 != null) {
                        double d1 = vec3d.distanceSquared(movingobjectposition1.pos);
                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
        }

        if (movingobjectposition != null && movingobjectposition.entity instanceof EntityPlayer && this.shooter != null && this.shooter instanceof EntityPlayer && !((EntityPlayer)this.shooter).getBukkitEntity().canSee(((EntityPlayer)movingobjectposition.entity).getBukkitEntity())) {
            movingobjectposition = null;
        }

        if (movingobjectposition != null) {
            if (movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK && (this.world.getType(movingobjectposition.a()).getBlock() == Blocks.PORTAL || this.world.getType(movingobjectposition.a()).getBlock() == Blocks.WHEAT)) {
                this.d(movingobjectposition.a());
            } else if(movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK && (this.world.getType(movingobjectposition.a()).getBlock() == Blocks.GLASS_PANE || this.world.getType(movingobjectposition.a()).getBlock() == Blocks.STAINED_GLASS_PANE)) {
                BlockPosition position = movingobjectposition.a();
                org.bukkit.block.Block block = world.getWorld().getBlockAt(position.getX(), position.getY(),
                        position.getZ());
                block.breakNaturally();
                this.a(movingobjectposition);
            } else {
                this.a(movingobjectposition);
                if (this.dead) {
                    CraftEventFactory.callProjectileHitEvent(this);
                }
            }
        }

        this.locX += this.motX;
        this.locY += this.motY;
        this.locZ += this.motZ;
        float f1 = (float) FastMath.sqrt(this.motX * this.motX + this.motZ * this.motZ);
        this.yaw = (float)(MathHelper.b(this.motX, this.motZ) * 180.0D / 3.1415927410125732D);

        for(this.pitch = (float)(MathHelper.b(this.motY, f1) * 180.0D / 3.1415927410125732D); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F) {
        }

        while(this.pitch - this.lastPitch >= 180.0F) {
            this.lastPitch += 360.0F;
        }

        while(this.yaw - this.lastYaw < -180.0F) {
            this.lastYaw -= 360.0F;
        }

        while(this.yaw - this.lastYaw >= 180.0F) {
            this.lastYaw += 360.0F;
        }

        this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
        this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
        float f2 = 0.99F;
        float f3 = this.m();
        if (this.V()) {
            for(int j = 0; j < 4; ++j) {
                float f4 = 0.25F;
                this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX - this.motX * (double)f4, this.locY - this.motY * (double)f4, this.locZ - this.motZ * (double)f4, this.motX, this.motY, this.motZ);
            }

            f2 = 0.8F;
        }

        this.motX *= f2;
        this.motY *= f2;
        this.motZ *= f2;
        this.motY -= f3;
        this.setPosition(this.locX, this.locY, this.locZ);
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.blockX = nbttagcompound.getShort("xTile");
        this.blockY = nbttagcompound.getShort("yTile");
        this.blockZ = nbttagcompound.getShort("zTile");
        if (nbttagcompound.hasKeyOfType("inTile", 8)) {
            this.inBlockId = Block.getByName(nbttagcompound.getString("inTile"));
        } else {
            this.inBlockId = Block.getById(nbttagcompound.getByte("inTile") & 255);
        }

        this.shake = nbttagcompound.getByte("shake") & 255;
        this.inGround = nbttagcompound.getByte("inGround") == 1;
        this.shooter = null;
        this.shooterName = nbttagcompound.getString("ownerName");
        if (this.shooterName != null && this.shooterName.length() == 0) {
            this.shooterName = null;
        }

        this.shooter = this.getShooter();
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setShort("xTile", (short)this.blockX);
        nbttagcompound.setShort("yTile", (short)this.blockY);
        nbttagcompound.setShort("zTile", (short)this.blockZ);
        MinecraftKey minecraftkey = (MinecraftKey)Block.REGISTRY.c(this.inBlockId);
        nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
        nbttagcompound.setByte("shake", (byte)this.shake);
        nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
        if ((this.shooterName == null || this.shooterName.length() == 0) && this.shooter instanceof EntityHuman) {
            this.shooterName = this.shooter.getName();
        }

        nbttagcompound.setString("ownerName", this.shooterName == null ? "" : this.shooterName);
    }

    @Override
    public void shoot(double d0, double d1, double d2, float f, float f1) {
        super.shoot(d0, d1, d2, f, f1);
        this.i = 0;
    }
}
