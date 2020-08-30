package com.podcrash.squadassault.util;

public enum Message {
    BOSSBAR_WAITING("§7You are currently playing §4Squad Assault §7on §b%name%"),
    SCOREBOARD_TITLE("§4Squad Assault"),
    BOSSBAR_INGAME("§fMap: §b%name% §fLocation: §b%callout% §fTime Left: §b%timer%"),
    SHOP_30_S("§cThe time to buy items has expired!"),
    CANCEL_DEFUSE("§b§lBomb defusing has been cancelled!"),
    BOMB_DEFUSE("§b§lBomb Defusing"),
    BOMB_DEFUSED("§cBomb Defused"),
    ROUND_WINNER_ALPHA("§eRound winner: §bAlpha"),
    ROUND_WINNER_OMEGA("§eRound winner: §cOmega");

    private final String msg;

    Message(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}
