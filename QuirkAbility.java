package com.plusultra.quirk;

import net.minecraft.server.level.ServerPlayer;

/** One usable ability: id (for the lang key), stamina cost, cooldown in ticks and the effect. */
public record QuirkAbility(String id, int cost, int cooldownTicks, Action action) {
    @FunctionalInterface
    public interface Action {
        /** @param power scales with quirk level, 1.0 at level 1 */
        void run(ServerPlayer player, double power);
    }
}
