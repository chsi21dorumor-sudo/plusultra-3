package com.plusultra.client;

import com.plusultra.net.SyncPayload;

/** Client-side mirror of the server's player data. Plain fields only, safe to load anywhere. */
public final class ClientData {
    public static String quirk = "";
    public static int level = 1;
    public static int xp = 0;
    public static int xpNeeded = 50;
    public static float stamina = 100f;
    public static float maxStamina = 100f;
    public static int points = 0;
    public static int skillPoints = 0;
    public static long talents = 0L;
    public static String fusionQuirk = "";
    public static String mission = "";
    public static int missionProgress = 0;
    public static int missionGoal = 0;

    /** Incremented every client tick; cooldowns are stored as the tick they end on. */
    public static long tick = 0;
    public static final long[] cdEnd = new long[3];

    private ClientData() {}

    public static void update(SyncPayload p) {
        quirk = p.quirk();
        level = p.level();
        xp = p.xp();
        xpNeeded = p.xpNeeded();
        stamina = p.stamina();
        maxStamina = p.maxStamina();
        points = p.points();
        skillPoints = p.skillPoints();
        talents = p.talents();
        fusionQuirk = p.fusionQuirk();
        mission = p.mission();
        missionProgress = p.missionProgress();
        missionGoal = p.missionGoal();
        cdEnd[0] = tick + p.cd0();
        cdEnd[1] = tick + p.cd1();
        cdEnd[2] = tick + p.cd2();
    }

    public static int cooldownTicks(int slot) {
        return (int) Math.max(0, cdEnd[slot] - tick);
    }
}
