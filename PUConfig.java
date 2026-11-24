package com.plusultra.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PUConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue DAMAGE_MULT = B
            .comment("Multiplier applied to all Quirk damage.")
            .defineInRange("damageMultiplier", 1.0, 0.1, 10.0);

    public static final ModConfigSpec.DoubleValue STAMINA_REGEN = B
            .comment("Stamina regained per second.")
            .defineInRange("staminaRegenPerSecond", 4.0, 0.0, 100.0);

    public static final ModConfigSpec.IntValue MAX_LEVEL = B
            .comment("Maximum Quirk level.")
            .defineInRange("maxLevel", 10, 1, 50);

    public static final ModConfigSpec.IntValue ULT_LEVEL = B
            .comment("Quirk level required to use the third ability (Plus Ultra).")
            .defineInRange("plusUltraUnlockLevel", 5, 1, 50);

    public static final ModConfigSpec.BooleanValue PVP = B
            .comment("If true, Quirk abilities can hurt other players.")
            .define("quirksHurtPlayers", false);

    public static final ModConfigSpec.BooleanValue BLOCK_DAMAGE = B
            .comment("If true, Explosion-type abilities break blocks (respects mobGriefing).")
            .define("explosionsBreakBlocks", false);

    public static final ModConfigSpec SPEC = B.build();

    private PUConfig() {}
}
