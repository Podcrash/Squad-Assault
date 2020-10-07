package com.podcrash.squadassault.util;

import org.bukkit.ChatColor;

public enum Messages {
    ROUND_OVER_TIME("&eAlpha &5defended until round time elapsed"),
    ROUND_OVER_KILLS("&e%t% &5completely obliterated &e%ot%"),
    ROUND_OVER_DET("&eOmega &5won the round by detonating the &eBomb"),
    ROUND_OVER_DEF("&eAlpha &5has defused the &eBomb &5and won the round"),
    GAME_OVER("&5The game is over"),
    GAME_STARTING("&5Game begins in &e%v% &5seconds"),

    PLAYER_JOIN("&e%p% &5has joined the game"),
    PLAYER_LEAVE("&e%p% &5has left the game"),

    PLAYER_JOIN_FAIL("&5The game you are trying to join doesn't exist, or is unavailable"),
    PLAYER_JOIN_FAIL_MULTIPLE("&5You can't join multiple games at once"),
    PLAYER_JOIN_FAIL_DISABLED("&5This game is disabled"),
    PLAYER_JOIN_FAIL_STARTED("&5This game has already started"),
    PLAYER_JOIN_FAIL_FULL("&5This game is full"),

    GAME_INSUFFICIENT_PLAYERS("&5The game start has been cancelled due to insufficient players"),

    PLAYER_SELECT_TEAM("&5You chose team &e%t%"),
    PLAYER_SELECT_TEAM_RANDOM("&5You chose your team randomly and are now on team &e%t%"),

    PLAYER_JOIN_TEAM_DEFENCE("&5You are &eAlpha, &5defend the &eBombsites &5to win"),
    PLAYER_JOIN_TEAM_OFFENCE("&5You are &eOmega, &5plant the &eBomb &5to win"),
    PLAYER_BOMB_SELF("&5You have the &eBomb&5, plant it at a &eBombsite&5 to win"),
    PLAYER_BOMB_OTHER("&e%p%&5 has the &eBomb&5, protect them to win win"),

    PLAYER_SHOP_DENIED_OUTOFBOUNDS("&5You can only access the &eShop &5in spawn"),
    PLAYER_SHOP_DENIED_ELAPSED("&5The allotted purchase period has elapsed"),
    PLAYER_SHOP_DENIED_FUNDS("&5You don't have enough funds to make this purchase"),
    PLAYER_SHOP_DENIED_ARMOR("&5You need to be wearing &eKevlar &5to purchase a &eHelmet"),

    PLAYER_BALANCED("&e%p% &5is now on &e%t% &5for balancing reasons"),
    TEAM_BALANCED("&5You've been swapped to &e%t% &5for half time"),

    BOSSBAR_WAITING("&7You are currently playing &4Squad Assault &7on &b%name%"),
    BOSSBAR_INGAME("&fMap: &b%name% &fTime Left: &b%timer%"),

    SCOREBOARD_TITLE("&4Squad Assault"),
    SCOREBOARD_GAME_OVER("&4Game Over"),

    TEAM_CHAT_FORMAT("&5[&eTeam&5] &e%p%&5: %message%"),

    KILL_BASE("&e%p% &5was killed by &e%op% &5with &e%i%"),
    KILL_BASE_NO_KILLER("&e%p% &5was killed by &e%op%"),

    BOMB_DEF_START("&eBomb &5is getting defused"),
    BOMB_DEF_COMPLETE("&eBomb &5defused by &e%p%"),
    BOMB_DEF_IN_PROGRESS("&eBomb &5being defused"),
    BOMB_DEF_CANCELLED("&eBomb &5defusal cancelled");

    String text;

    Messages(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String replace(String to, String replace) {
        return ChatColor.translateAlternateColorCodes('&', text.replace(to, replace));
    }

}