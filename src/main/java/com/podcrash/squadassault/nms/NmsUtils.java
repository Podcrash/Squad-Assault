package com.podcrash.squadassault.nms;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NmsUtils {

    public void sendActionBar(Player player, String string) {
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + string + "\"}"), (byte)2));
    }

    public void sendFakeItem(Player player, int n, ItemStack itemStack) {
        EntityPlayer handle = ((CraftPlayer)player).getHandle();
        handle.playerConnection.sendPacket(new PacketPlayOutSetSlot(handle.defaultContainer.windowId, n,
                CraftItemStack.asNMSCopy(itemStack)));
    }



}
