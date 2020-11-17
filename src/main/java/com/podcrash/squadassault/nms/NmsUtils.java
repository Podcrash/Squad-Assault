package com.podcrash.squadassault.nms;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import me.dpohvar.powernbt.api.NBTCompound;
import me.dpohvar.powernbt.api.NBTManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NmsUtils {

    public static void sendActionBar(Player player, String string) {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + string + "\"}"), (byte)2));
    }

    public static void sendFakeItem(Player player, int n, ItemStack itemStack) {
        EntityPlayer handle = ((CraftPlayer)player).getHandle();
        handle.playerConnection.sendPacket(new PacketPlayOutSetSlot(handle.defaultContainer.windowId, n,
                CraftItemStack.asNMSCopy(itemStack)));
    }

    public static void sendTitle(Player player, int fadeIn, int duration, int fadeOut, String title, String subtitle) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, (IChatBaseComponent) null, fadeIn,
                duration, fadeOut));
        if(subtitle != null) {
            playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                    IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}")));
        }
        if(title != null) {
            playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                    IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}")));
        }
    }

    public static BossBar createBossbar(String s) {
        return new BossBar(s);
    }

    public static void hideNametag(Team team) {
        ((CraftScoreboard)team.getScoreboard()).getHandle().getTeam(team.getName()).setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
    }

    public static void sendInvisibility(SAScoreboard scoreboard, SAGame game) {
        for(Player player : game.getTeamA().getPlayers()) {
            if(!player.isOnline() || game.getSpectators().contains(player))
                continue;
            ScoreboardTeam team = ((CraftScoreboard)scoreboard.getScoreboard()).getHandle().getTeam(player.getName());
            try {
                Field declaredField = team.getClass().getDeclaredField("i");
                declaredField.setAccessible(true);
                declaredField.set(team, ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sendListPacket(game.getTeamA(), new PacketPlayOutScoreboardTeam(team, 2));
        }
        for(Player player : game.getTeamB().getPlayers()) {
            if(!player.isOnline() || game.getSpectators().contains(player))
                continue;
            ScoreboardTeam team = ((CraftScoreboard)scoreboard.getScoreboard()).getHandle().getTeam(player.getName());
            try {
                Field declaredField = team.getClass().getDeclaredField("i");
                declaredField.setAccessible(true);
                declaredField.set(team, ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sendListPacket(game.getTeamB(), new PacketPlayOutScoreboardTeam(team, 2));
        }
    }

    private static void sendListPacket(SATeam team, Packet<?> packet) {
        for(Player player : team.getPlayers()) {
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static PhysicsItem spawnPhysicsItem(Player player, ItemStack stack, double power, boolean fire) {
        return fire ? new ComplexPhysicsItem(((CraftPlayer)player).getHandle(), CraftItemStack.asNMSCopy(stack), power) : new SimplePhysicsItem(player, stack, power);
    }

    public static ItemStack addNBTInteger(ItemStack stack, String key, int value) {
        NBTManager manager = NBTManager.getInstance();
        NBTCompound nbtCompound = manager.read(stack);
        nbtCompound.put(key, value);
        manager.write(stack, nbtCompound);
        return stack;
    }

    public static int getNBTInteger(ItemStack stack, String key) {
        NBTManager manager = NBTManager.getInstance();
        NBTCompound nbtCompound = manager.read(stack);
        return (int) nbtCompound.get(key);
    }

    public static void injectEntity(String name, int id, Class<? extends Entity> customClass) {
        try {
            List<Map<?, ?>> dataMap = new ArrayList<>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMap.get(2).containsKey(id)){
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}