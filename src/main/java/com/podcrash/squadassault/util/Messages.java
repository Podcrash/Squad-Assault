package com.podcrash.squadassault.util;

import org.bukkit.ChatColor;

public enum Messages {
    ROUND_OVER_TIME("&eAlpha &bdefended until round time elapsed"),
    ROUND_OVER_KILLS("&e%t% &bcompletely obliterated &e%ot%"),
    ROUND_OVER_DET("&eOmega &bwon the round by detonating the &eBomb"),
    ROUND_OVER_DEF("&eAlpha &bhas defused the &eBomb &band won the round"),
    GAME_OVER("&bThe game is over"),
    GAME_STARTING("&bGame begins in &e%v% &bseconds"),

    PLAYER_JOIN("&e%p% &bhas joined the game"),
    PLAYER_LEAVE("&e%p% &bhas left the game"),

    PLAYER_JOIN_FAIL("&bThe game you are trying to join doesn't exist, or is unavailable"),
    PLAYER_JOIN_FAIL_MULTIPLE("&bYou can't join multiple games at once"),
    PLAYER_JOIN_FAIL_DISABLED("&bThis game is disabled"),
    PLAYER_JOIN_FAIL_STARTED("&bThis game has already started"),
    PLAYER_JOIN_FAIL_FULL("&bThis game is full"),

    GAME_INSUFFICIENT_PLAYERS("&bThe game start has been cancelled due to insufficient players"),

    PLAYER_SELECT_TEAM("&bYou chose team &e%t%"),
    PLAYER_SELECT_TEAM_RANDOM("&bYou chose your team randomly and are now on team &e%t%"),

    PLAYER_JOIN_TEAM_DEFENCE("&bYou are &eAlpha, &bdefend the &eBombsites &bto win"),
    PLAYER_JOIN_TEAM_OFFENCE("&bYou are &eOmega, &bplant the &eBomb &bto win"),
    PLAYER_BOMB_SELF("&bYou have the &eBomb&b, plant it at a &eBombsite&b to win"),
    PLAYER_BOMB_OTHER("&e%p%&b has the &eBomb&b, protect them to win win"),

    PLAYER_SHOP_DENIED_OUTOFBOUNDS("&bYou can only access the &eShop &bin spawn"),
    PLAYER_SHOP_DENIED_ELAPSED("&bThe allotted purchase period has elapsed"),
    PLAYER_SHOP_DENIED_FUNDS("&bYou don't have enough funds to make this purchase"),
    PLAYER_SHOP_DENIED_ARMOR("&bYou need to be wearing &eKevlar &bto purchase a &eHelmet"),

    PLAYER_BALANCED("&e%p% &bis now on &e%t% &bfor balancing reasons"),
    TEAM_BALANCED("&bYou've been swapped to &e%t% &bfor half time"),

    BOSSBAR_WAITING("&7You are currently playing &4Squad Assault &7on &b%name%"),
    BOSSBAR_INGAME("&fMap: &b%name% &fTime Left: &b%timer%"),

    SCOREBOARD_TITLE("&b=== &eSquad Assault &b==="),
    SCOREBOARD_OBJECTIVE("&bObjective [&e%t%&b]"),
    SCOREBOARD_GOAL("&e%t%"),
    SCOREBOARD_MONEY("&bMoney &e%t%"),
    SCOREBOARD_STATLAYOUT("&eK&b/&eD&b/&eA&b/&eADR"),
    SCOREBOARD_STATS("&e%k%&b/&e%d%&b/&e%a%&b/&e%adr%"),
    SCOREBOARD_REMAINING("&bRemaining &e%t%&b/&e%to%"),
    SCOREBOARD_GAME_OVER("&eGame Over"),

    TEAM_CHAT_FORMAT("&b[&eTeam&b] &e%p%&b: %message%"),

    KILL_BASE("&e%p% &bwas killed by &e%op% &bwith &e%i%"),
    KILL_BASE_NO_KILLER("&e%p% &bwas killed by &e%op%"),

    BOMB_DEF_START("&eBomb &bis getting defused"),
    BOMB_DEF_COMPLETE("&eBomb &bdefused by &e%p%"),
    BOMB_DEF_IN_PROGRESS("&eBomb &bbeing defused"),
    BOMB_DEF_CANCELLED("&eBomb &bdefusal cancelled");

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