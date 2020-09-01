package com.podcrash.squadassault.nms;

import com.podcrash.squadassault.game.SAGame;
import com.podcrash.squadassault.game.SATeam;
import com.podcrash.squadassault.scoreboard.SAScoreboard;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;

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
                    IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}")));
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
            if(game.getSpectators().contains(player))
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
            if(game.getSpectators().contains(player))
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
    }

    private static void sendListPacket(SATeam team, Packet<?> packet) {
        for(Player player : team.getPlayers()) {
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static PhysicsItem spawnPhysicsItem(Player player, ItemStack stack, double power) {
        return new PhysicsItem(((CraftPlayer)player).getHandle(), CraftItemStack.asNMSCopy(stack), power);
    }

}
