package com.plusultra.quirk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.plusultra.core.ModAttachments;
import java.util.List;
import net.minecraft.world.entity.player.Player;

/** Mutable per-player state, stored as a NeoForge data attachment. */
public class PlayerData {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("quirk", "").forGetter(d -> d.quirk),
            Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
            Codec.FLOAT.optionalFieldOf("stamina", 100f).forGetter(d -> d.stamina),
            Codec.LONG.listOf().optionalFieldOf("cooldowns", List.of(0L, 0L, 0L))
                    .forGetter(d -> List.of(d.cd[0], d.cd[1], d.cd[2])),
            Codec.STRING.optionalFieldOf("mission", "").forGetter(d -> d.mission),
            Codec.INT.optionalFieldOf("mission_progress", 0).forGetter(d -> d.missionProgress),
            Codec.INT.optionalFieldOf("mission_goal", 0).forGetter(d -> d.missionGoal),
            Codec.INT.optionalFieldOf("points", 0).forGetter(d -> d.points),
            Codec.INT.optionalFieldOf("skill_points", 0).forGetter(d -> d.skillPoints),
            Codec.LONG.optionalFieldOf("talents", 0L).forGetter(d -> d.talents),
            Codec.STRING.optionalFieldOf("fusion_quirk", "").forGetter(d -> d.fusionQuirk)
    ).apply(i, PlayerData::new));

    public String quirk = "";
    public int level = 1;
    public int xp = 0;
    public float stamina = 100f;
    /** Game-time tick at which each ability slot is ready again. */
    public final long[] cd = new long[3];
    public String mission = "";
    public int missionProgress = 0;
    public int missionGoal = 0;
    public int points = 0;
    /** Skill points spent in the Quirk Evolution tree. */
    public int skillPoints = 0;
    /** Six talent nodes stored as a bit mask. */
    public long talents = 0L;
    /** Secondary Quirk used by fusion; empty when fusion is inactive. */
    public String fusionQuirk = "";

    /** Not saved: set every second from nearby agency members. */
    public double synergy = 1.0;

    public PlayerData() {}

    public PlayerData(String quirk, int level, int xp, float stamina, List<Long> cooldowns,
                      String mission, int missionProgress, int missionGoal, int points, int skillPoints, long talents, String fusionQuirk) {
        this.quirk = quirk;
        this.level = level;
        this.xp = xp;
        this.stamina = stamina;
        for (int k = 0; k < 3 && k < cooldowns.size(); k++) this.cd[k] = cooldowns.get(k);
        this.mission = mission;
        this.missionProgress = missionProgress;
        this.missionGoal = missionGoal;
        this.points = points;
        this.skillPoints = skillPoints;
        this.talents = talents;
        this.fusionQuirk = fusionQuirk;
    }

    public static PlayerData get(Player p) {
        return p.getData(ModAttachments.PLAYER_DATA);
    }

    public boolean hasQuirk() {
        return !quirk.isEmpty() && Quirk.byId(quirk) != null;
    }

    public float maxStamina() {
        return 100f + 10f * (level - 1);
    }

    public int xpNeeded() {
        return 50 * level;
    }

    public void setQuirk(String id) {
        this.quirk = id;
        this.level = 1;
        this.xp = 0;
        this.stamina = maxStamina();
        this.cd[0] = this.cd[1] = this.cd[2] = 0;
        this.skillPoints = 0;
        this.talents = 0L;
        this.fusionQuirk = "";
    }

    public void clearQuirk() {
        setQuirk("");
    }

    public boolean hasTalent(int index) {
        return index >= 0 && index < 6 && (talents & (1L << index)) != 0;
    }

    public void unlockTalent(int index) {
        if (index >= 0 && index < 6) talents |= 1L << index;
    }

    public int talentCount() {
        return Long.bitCount(talents);
    }

    public double powerMultiplier() {
        return 1.0 + (hasTalent(0) ? 0.10 : 0) + (hasTalent(5) ? 0.05 : 0);
    }

    public double costMultiplier() {
        return hasTalent(1) ? 0.90 : 1.0;
    }

    public double cooldownMultiplier() {
        return hasTalent(2) ? 0.90 : 1.0;
    }

    public double rangeMultiplier() {
        return hasTalent(3) ? 1.10 : 1.0;
    }
}
