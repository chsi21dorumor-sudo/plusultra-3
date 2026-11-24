package com.plusultra.quirk;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/** The Quirks a player can awaken. Slot 0/1 are basic abilities, slot 2 is the Plus Ultra move. */
public enum Quirk {
    ONE_FOR_ALL("one_for_all", 0x7CFC00,
            new QuirkAbility("smash", 20, 30, Abilities::smash),
            new QuirkAbility("delaware_leap", 15, 60, Abilities::delawareLeap),
            new QuirkAbility("full_cowl", 60, 1200, Abilities::fullCowl)),

    HALF_COLD_HALF_HOT("half_cold_half_hot", 0x7FDBFF,
            new QuirkAbility("flame_burst", 18, 40, Abilities::flameBurst),
            new QuirkAbility("ice_burst", 18, 40, Abilities::iceBurst),
            new QuirkAbility("flashfreeze_heatwave", 70, 1400, Abilities::flashfreezeHeatwave)),

    EXPLOSION("explosion", 0xFF8C1A,
            new QuirkAbility("blast", 15, 25, Abilities::blast),
            new QuirkAbility("blast_propulsion", 15, 50, Abilities::blastPropulsion),
            new QuirkAbility("howitzer_impact", 65, 1300, Abilities::howitzerImpact)),

    HARDENING("hardening", 0xC0392B,
            new QuirkAbility("harden", 20, 300, Abilities::harden),
            new QuirkAbility("red_riot_punch", 15, 30, Abilities::redRiotPunch),
            new QuirkAbility("unbreakable", 60, 1500, Abilities::unbreakable)),

    ZERO_GRAVITY("zero_gravity", 0xFFB6C1,
            new QuirkAbility("float", 20, 80, Abilities::floatTargets),
            new QuirkAbility("weightless", 15, 100, Abilities::weightless),
            new QuirkAbility("meteor_shower", 65, 1400, Abilities::meteorShower)),

    ENGINE("engine", 0x3498DB,
            new QuirkAbility("recipro_burst", 20, 200, Abilities::reciproBurst),
            new QuirkAbility("engine_kick", 15, 40, Abilities::engineKick),
            new QuirkAbility("recipro_extend", 60, 1300, Abilities::reciproExtend));

    private final String id;
    private final int color;
    private final QuirkAbility[] abilities;

    Quirk(String id, int color, QuirkAbility... abilities) {
        this.id = id;
        this.color = color;
        this.abilities = abilities;
    }

    public String id() { return id; }
    public int color() { return color; }
    public QuirkAbility[] abilities() { return abilities; }

    public static Quirk byId(String id) {
        for (Quirk q : values()) if (q.id.equals(id)) return q;
        return null;
    }

    public static Quirk random(RandomSource rng) {
        Quirk[] all = values();
        return all[rng.nextInt(all.length)];
    }

    public static List<String> ids() {
        List<String> out = new ArrayList<>();
        for (Quirk q : values()) out.add(q.id);
        return out;
    }
}
