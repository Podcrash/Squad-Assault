package com.podcrash.squadassault.game;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent blockIgniteEvent) {
        if (blockIgniteEvent.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            blockIgniteEvent.setCancelled(true);
        }
    }



}
