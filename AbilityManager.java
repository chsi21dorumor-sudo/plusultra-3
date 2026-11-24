package com.plusultra.quirk;

import com.plusultra.config.PUConfig;
import com.plusultra.core.ModDimensions;
import com.plusultra.net.SyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Validates and runs an ability request coming from a player's keybind. */
public final class AbilityManager {
    private AbilityManager() {}

    public static void use(ServerPlayer p, int slot) {
        if (slot < 0 || slot > 2) return;
        PlayerData d = PlayerData.get(p);
        Quirk q = Quirk.byId(d.quirk);
        if (q == null) {
            p.displayClientMessage(Component.literal("You have no Quirk. Use a Quirk Orb to awaken one.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        QuirkAbility ability = q.abilities()[slot];

        if (slot == 2 && d.level < PUConfig.ULT_LEVEL.get()) {
            p.displayClientMessage(Component.literal("Plus Ultra unlocks at level " + PUConfig.ULT_LEVEL.get() + ".")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        long now = p.level().getGameTime();
        if (d.cd[slot] > now) {
            double secs = (d.cd[slot] - now) / 20.0;
            p.displayClientMessage(Component.literal(String.format("On cooldown: %.1fs", secs))
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        boolean free = p.isCreative() || p.level().dimension().equals(ModDimensions.TRAINING);
        double cost = ability.cost() * d.costMultiplier();
        if (!free && d.stamina < cost) {
            p.displayClientMessage(Component.literal("Not enough stamina!").withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!free) d.stamina -= (float) cost;
        long cooldown = Math.max(1L, Math.round(ability.cooldownTicks() * d.cooldownMultiplier()));
        d.cd[slot] = now + (free ? cooldown / 2 : cooldown);

        double power = (1.0 + 0.12 * (d.level - 1)) * d.powerMultiplier();
        ability.action().run(p, power);

        addXp(p, d, slot == 2 ? 15 : 5);
        send(p);
    }

    public static void addXp(ServerPlayer p, PlayerData d, int amount) {
        if (!d.hasQuirk()) return;
        d.xp += amount;
        int max = PUConfig.MAX_LEVEL.get();
        while (d.level < max && d.xp >= d.xpNeeded()) {
            d.xp -= d.xpNeeded();
            d.level++;
            d.stamina = d.maxStamina();
            p.sendSystemMessage(Component.literal("Your Quirk grew stronger! Level " + d.level)
                    .withStyle(ChatFormatting.GOLD));
            if (d.level == PUConfig.ULT_LEVEL.get()) {
                p.sendSystemMessage(Component.literal("PLUS ULTRA! Your third ability is unlocked.")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            }
        }
        if (d.level >= max) d.xp = 0;
    }

    public static void send(ServerPlayer p) {
        PacketDistributor.sendToPlayer(p, SyncPayload.from(p, PlayerData.get(p)));
    }
}
