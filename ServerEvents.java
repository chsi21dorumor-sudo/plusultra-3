package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.agency.AgencyData;
import com.plusultra.command.ModCommands;
import com.plusultra.config.PUConfig;
import com.plusultra.mission.Missions;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PlusUltra.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        PlayerData d = PlayerData.get(sp);

        // Agency synergy: +20% regen per nearby agency member (max 3), recomputed once a second.
        if (sp.tickCount % 20 == 0) {
            double synergy = 1.0;
            AgencyData.Agency agency = AgencyData.get(sp.server).agencyOf(sp.getUUID());
            if (agency != null) {
                int near = 0;
                for (ServerPlayer other : sp.serverLevel().players()) {
                    if (other != sp && agency.members.contains(other.getUUID())
                            && other.distanceToSqr(sp) <= 16 * 16) near++;
                }
                synergy += 0.2 * Math.min(near, 3);
            }
            d.synergy = synergy;
        }

        if (d.hasQuirk()) {
            if (!d.fusionQuirk.isEmpty() && sp.tickCount % 20 == 0) applyFusionPassive(sp, d);
            if (sp.tickCount % 4 == 0) spawnQuirkAura(sp, d);
            float max = d.maxStamina();
            if (d.stamina < max) {
                d.stamina = Math.min(max, d.stamina + (float) (PUConfig.STAMINA_REGEN.get() / 20.0 * d.synergy));
            }
        }

        if (sp.tickCount % 5 == 0 && (d.hasQuirk() || d.points > 0 || Missions.hasMission(d))) {
            AbilityManager.send(sp);
        }
    }

    private static void spawnQuirkAura(ServerPlayer sp, PlayerData d) {
        net.minecraft.core.particles.ParticleOptions particle = switch (d.quirk) {
            case "one_for_all" -> net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case "half_cold_half_hot" -> net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
            case "explosion" -> net.minecraft.core.particles.ParticleTypes.SMOKE;
            case "hardening" -> net.minecraft.core.particles.ParticleTypes.CRIT;
            case "zero_gravity" -> net.minecraft.core.particles.ParticleTypes.END_ROD;
            case "engine" -> net.minecraft.core.particles.ParticleTypes.FLAME;
            default -> net.minecraft.core.particles.ParticleTypes.CLOUD;
        };
        sp.serverLevel().sendParticles(particle, sp.getX(), sp.getY() + 1.0, sp.getZ(), 3, 0.35, 0.55, 0.35, 0.015);
    }

    private static void applyFusionPassive(ServerPlayer sp, PlayerData d) {
        switch (d.fusionQuirk) {
            case "engine" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, false, false));
            case "hardening" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false));
            case "zero_gravity" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 40, 0, false, false));
            case "half_cold_half_hot" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
            case "one_for_all" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 40, 0, false, false));
            case "explosion" -> sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) AbilityManager.send(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            d.stamina = d.maxStamina();
            AbilityManager.send(sp);
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity src = event.getSource().getEntity();
        if (src instanceof ServerPlayer sp) {
            Missions.onKill(sp, event.getEntity());
        }
    }
}
