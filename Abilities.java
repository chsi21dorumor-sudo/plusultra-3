package com.plusultra.quirk;

import com.plusultra.config.PUConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The actual effects of every Quirk ability. Everything here runs on the logical server. */
public final class Abilities {
    private Abilities() {}

    // ------------------------------------------------------------------ helpers

    private static ServerLevel level(ServerPlayer p) {
        return p.serverLevel();
    }

    private static boolean canHit(ServerPlayer p, Entity e) {
        if (!(e instanceof LivingEntity le) || e == p || !le.isAlive()) return false;
        if (le instanceof Player && !PUConfig.PVP.get()) return false;
        if (le instanceof TamableAnimal pet && pet.isOwnedBy(p)) return false;
        return true;
    }

    /** Living entities in front of the player within range; cosMin is the cosine of the half-angle. */
    private static List<LivingEntity> cone(ServerPlayer p, double range, double cosMin) {
        range *= PlayerData.get(p).rangeMultiplier();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class,
                p.getBoundingBox().inflate(range), x -> canHit(p, x))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range) continue;
            if (d < 1.2 || to.normalize().dot(look) >= cosMin) out.add(e);
        }
        return out;
    }

    private static List<LivingEntity> around(ServerPlayer p, Vec3 center, double r) {
        r *= PlayerData.get(p).rangeMultiplier();
        return p.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(r),
                x -> canHit(p, x) && x.position().distanceTo(center) <= r);
    }

    private static void hurt(ServerPlayer p, LivingEntity target, double amount) {
        target.hurt(p.damageSources().playerAttack(p), (float) (amount * PUConfig.DAMAGE_MULT.get()));
    }

    private static void shove(LivingEntity target, Vec3 dir, double horizontal, double vertical) {
        target.push(dir.x * horizontal, vertical, dir.z * horizontal);
        target.hurtMarked = true;
    }

    private static Vec3 flat(Vec3 v) {
        return new Vec3(v.x, 0, v.z).normalize();
    }

    private static void dash(ServerPlayer p, double speed, double lift) {
        Vec3 l = p.getLookAngle();
        p.setDeltaMovement(l.x * speed, l.y * speed * 0.6 + lift, l.z * speed);
        p.hurtMarked = true;
        p.fallDistance = 0;
    }

    private static void particles(ServerPlayer p, ParticleOptions type, Vec3 at, int count, double spread, double speed) {
        level(p).sendParticles(type, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    private static void sound(ServerPlayer p, SoundEvent s, float pitch) {
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, 1.0f, pitch);
    }

    private static void effect(LivingEntity e, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> eff,
                               int ticks, int amp) {
        e.addEffect(new MobEffectInstance(eff, ticks, amp));
    }

    /** Visual and optional real explosion at a point. Damage is applied manually. */
    private static void blastAt(ServerPlayer p, Vec3 at, double radius, double damage) {
        ServerLevel l = level(p);
        l.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        l.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 30, radius * 0.3, radius * 0.3, radius * 0.3, 0.05);
        l.playSound(null, at.x, at.y, at.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5f, 0.6f);
        l.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.6f);
        for (LivingEntity t : around(p, at, radius)) {
            hurt(p, t, damage);
            Vec3 away = t.position().subtract(at);
            shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.0, 0.5);
        }
        if (PUConfig.BLOCK_DAMAGE.get()) {
            l.explode(p, at.x, at.y, at.z, (float) (radius * 0.6), Level.ExplosionInteraction.MOB);
        }
    }

    // ------------------------------------------------------------------ One For All

    public static void smash(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : cone(p, 4.5, 0.6)) {
            hurt(p, t, 9 * power);
            shove(t, flat(look), 1.8, 0.45);
        }
        Vec3 pt = p.getEyePosition().add(look.scale(2.5));
        particles(p, ParticleTypes.EXPLOSION, pt, 1, 0, 0);
        particles(p, ParticleTypes.CLOUD, pt, 20, 0.5, 0.1);
        sound(p, SoundEvents.PLAYER_ATTACK_STRONG, 0.7f);
        sound(p, SoundEvents.ANVIL_LAND, 1.4f);
    }

    public static void delawareLeap(ServerPlayer p, double power) {
        p.setDeltaMovement(p.getLookAngle().x * 1.0, 1.4 + 0.05 * power, p.getLookAngle().z * 1.0);
        p.hurtMarked = true;
        p.fallDistance = 0;
        effect(p, MobEffects.SLOW_FALLING, 80, 0);
        particles(p, ParticleTypes.CLOUD, p.position(), 25, 0.5, 0.05);
        sound(p, SoundEvents.ENDER_DRAGON_FLAP, 1.2f);
    }

    public static void fullCowl(ServerPlayer p, double power) {
        int t = 300;
        effect(p, MobEffects.DAMAGE_BOOST, t, 2);
        effect(p, MobEffects.MOVEMENT_SPEED, t, 1);
        effect(p, MobEffects.DAMAGE_RESISTANCE, t, 1);
        effect(p, MobEffects.JUMP, t, 1);
        particles(p, ParticleTypes.ELECTRIC_SPARK, p.position().add(0, 1, 0), 40, 0.6, 0.2);
        sound(p, SoundEvents.LIGHTNING_BOLT_THUNDER, 1.8f);
    }

    // ------------------------------------------------------------------ Half-Cold Half-Hot

    public static void flameBurst(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : cone(p, 7, 0.75)) {
            hurt(p, t, 6 * power);
            t.setRemainingFireTicks(100);
        }
        for (int i = 1; i <= 6; i++) {
            particles(p, ParticleTypes.FLAME, p.getEyePosition().add(look.scale(i)), 6, 0.25, 0.02);
        }
        sound(p, SoundEvents.FIRECHARGE_USE, 1.0f);
    }

    public static void iceBurst(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : cone(p, 7, 0.75)) {
            hurt(p, t, 5 * power);
            t.setTicksFrozen(200);
            effect(t, MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
        }
        for (int i = 1; i <= 6; i++) {
            particles(p, ParticleTypes.SNOWFLAKE, p.getEyePosition().add(look.scale(i)), 8, 0.3, 0.02);
        }
        sound(p, SoundEvents.GLASS_BREAK, 0.6f);
    }

    public static void flashfreezeHeatwave(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : around(p, c, 9)) {
            hurt(p, t, 10 * power);
            t.setRemainingFireTicks(120);
            t.setTicksFrozen(300);
            effect(t, MobEffects.MOVEMENT_SLOWDOWN, 120, 3);
            shove(t, t.position().subtract(c).normalize(), 1.2, 0.4);
        }
        for (int i = 0; i < 24; i++) {
            double a = i / 24.0 * Math.PI * 2;
            Vec3 ring = c.add(Math.cos(a) * 6, 0.5, Math.sin(a) * 6);
            particles(p, i % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SNOWFLAKE, ring, 10, 0.5, 0.03);
        }
        sound(p, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8f);
    }

    // ------------------------------------------------------------------ Explosion

    public static void blast(ServerPlayer p, double power) {
        Vec3 at = p.getEyePosition().add(p.getLookAngle().scale(3.0));
        blastAt(p, at, 2.5, 7 * power);
    }

    public static void blastPropulsion(ServerPlayer p, double power) {
        dash(p, 1.8, 0.2);
        particles(p, ParticleTypes.EXPLOSION, p.position().add(0, 0.5, 0), 2, 0.2, 0);
        particles(p, ParticleTypes.SMOKE, p.position().add(0, 0.5, 0), 20, 0.3, 0.05);
        sound(p, SoundEvents.FIRECHARGE_USE, 1.2f);
    }

    public static void howitzerImpact(ServerPlayer p, double power) {
        Vec3 at = p.pick(20, 1.0f, false).getLocation();
        blastAt(p, at, 5.5, 14 * power);
    }

    // ------------------------------------------------------------------ Hardening

    public static void harden(ServerPlayer p, double power) {
        effect(p, MobEffects.DAMAGE_RESISTANCE, 400, 1);
        effect(p, MobEffects.ABSORPTION, 400, 1);
        effect(p, MobEffects.MOVEMENT_SLOWDOWN, 400, 0);
        particles(p, ParticleTypes.CRIT, p.position().add(0, 1, 0), 30, 0.5, 0.1);
        sound(p, SoundEvents.ANVIL_LAND, 0.8f);
    }

    public static void redRiotPunch(ServerPlayer p, double power) {
        List<LivingEntity> hit = cone(p, 3.5, 0.8);
        if (!hit.isEmpty()) {
            LivingEntity t = hit.get(0);
            hurt(p, t, 12 * power);
            shove(t, flat(p.getLookAngle()), 1.2, 0.3);
            particles(p, ParticleTypes.CRIT, t.getBoundingBox().getCenter(), 25, 0.4, 0.2);
        }
        sound(p, SoundEvents.PLAYER_ATTACK_CRIT, 0.8f);
    }

    public static void unbreakable(ServerPlayer p, double power) {
        effect(p, MobEffects.DAMAGE_RESISTANCE, 200, 3);
        effect(p, MobEffects.DAMAGE_BOOST, 200, 1);
        effect(p, MobEffects.ABSORPTION, 200, 3);
        particles(p, ParticleTypes.CRIT, p.position().add(0, 1, 0), 60, 0.6, 0.3);
        sound(p, SoundEvents.ANVIL_LAND, 0.5f);
    }

    // ------------------------------------------------------------------ Zero Gravity

    public static void floatTargets(ServerPlayer p, double power) {
        Vec3 c = p.getEyePosition().add(p.getLookAngle().scale(4));
        for (LivingEntity t : around(p, c, 5)) {
            effect(t, MobEffects.LEVITATION, 80, 0);
        }
        particles(p, ParticleTypes.END_ROD, c, 30, 1.5, 0.02);
        sound(p, SoundEvents.ENDER_EYE_LAUNCH, 1.2f);
    }

    public static void weightless(ServerPlayer p, double power) {
        effect(p, MobEffects.SLOW_FALLING, 200, 0);
        effect(p, MobEffects.JUMP, 200, 3);
        effect(p, MobEffects.MOVEMENT_SPEED, 200, 0);
        particles(p, ParticleTypes.END_ROD, p.position().add(0, 1, 0), 20, 0.5, 0.03);
        sound(p, SoundEvents.ENDER_EYE_LAUNCH, 1.6f);
    }

    public static void meteorShower(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : around(p, c, 9)) {
            hurt(p, t, 4 * power);
            effect(t, MobEffects.LEVITATION, 100, 1);
            effect(t, MobEffects.GLOWING, 200, 0);
        }
        for (int i = 0; i < 40; i++) {
            double a = p.getRandom().nextDouble() * Math.PI * 2;
            double r = p.getRandom().nextDouble() * 8;
            particles(p, ParticleTypes.END_ROD, c.add(Math.cos(a) * r, 2 + p.getRandom().nextDouble() * 4, Math.sin(a) * r), 2, 0.1, 0.1);
        }
        sound(p, SoundEvents.ENDER_DRAGON_GROWL, 1.8f);
    }

    // ------------------------------------------------------------------ Engine

    public static void reciproBurst(ServerPlayer p, double power) {
        effect(p, MobEffects.MOVEMENT_SPEED, 120, 3);
        particles(p, ParticleTypes.CLOUD, p.position(), 20, 0.4, 0.05);
        sound(p, SoundEvents.FIRECHARGE_USE, 1.6f);
    }

    public static void engineKick(ServerPlayer p, double power) {
        for (LivingEntity t : cone(p, 5, 0.7)) {
            hurt(p, t, 8 * power);
            shove(t, flat(p.getLookAngle()), 1.4, 0.35);
        }
        dash(p, 1.6, 0.1);
        particles(p, ParticleTypes.CLOUD, p.position(), 25, 0.4, 0.1);
        sound(p, SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0f);
    }

    public static void reciproExtend(ServerPlayer p, double power) {
        effect(p, MobEffects.MOVEMENT_SPEED, 240, 4);
        effect(p, MobEffects.JUMP, 240, 1);
        effect(p, MobEffects.DIG_SPEED, 240, 1);
        particles(p, ParticleTypes.ELECTRIC_SPARK, p.position().add(0, 1, 0), 40, 0.5, 0.2);
        sound(p, SoundEvents.LIGHTNING_BOLT_THUNDER, 2.0f);
    }
}
