package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.network.DungeonWaveHudPayload;

public final class DungeonHudState {

    private static volatile boolean active;
    private static volatile int waveNumber;
    private static volatile int mobsRemaining;
    private static volatile int totalMobs;

    private DungeonHudState() {
    }

    public static void apply(DungeonWaveHudPayload payload) {
        active = payload.active();
        waveNumber = payload.waveNumber();
        mobsRemaining = payload.mobsRemaining();
        totalMobs = payload.totalMobs();
    }

    public static void clear() {
        active = false;
        waveNumber = 0;
        mobsRemaining = 0;
        totalMobs = 0;
    }

    public static boolean active() {
        return active;
    }

    public static int waveNumber() {
        return waveNumber;
    }

    public static int mobsRemaining() {
        return mobsRemaining;
    }

    public static int totalMobs() {
        return totalMobs;
    }
}
