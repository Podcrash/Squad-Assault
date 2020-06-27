package com.podcrash.squadassault.gun;

import org.bukkit.event.player.PlayerInteractEvent;

public interface Interactable {

    void onInteract(PlayerInteractEvent playerInteractEvent);
}
