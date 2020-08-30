package com.podcrash.squadassault.util;

public enum Message {
    BOSSBAR_WAITING("§7You are currently playing §4Squad Assault §7on §b%name%"),
    SCOREBOARD_TITLE("§4Squad Assault"),
    BOSSBAR_INGAME("§fMap: §b%name% §fLocation: §b%callout% §fTime Left: §b%timer%");

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
