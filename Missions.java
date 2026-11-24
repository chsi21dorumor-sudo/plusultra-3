package com.plusultra.mission;

import com.plusultra.core.ModEntities;
import com.plusultra.entity.VillainEntity;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

/** Mission logic: two mission types, tracked in {@link PlayerData}. */
public final class Missions {
    public static final String PATROL = "patrol";
    public static final String VILLAIN = "villain";

    private static final String[] VILLAIN_NAMES = {
            "Shadow Broker", "Crimson Smuggler", "The Hollow Man", "Static Fang", "Night Reaver"
    };

    private Missions() {}

    public static boolean hasMission(PlayerData d) {
        return !d.mission.isEmpty();
    }

    public static String describe(PlayerData d) {
        return switch (d.mission) {
            case PATROL -> "Patrol: defeat hostile mobs " + d.missionProgress + "/" + d.missionGoal;
            case VILLAIN -> "Capture the villain " + d.missionProgress + "/" + d.missionGoal;
            default -> "No active mission";
        };
    }

    public static void assign(ServerPlayer p, ServerLevel level, BlockPos board) {
        PlayerData d = PlayerData.get(p);
        boolean villain = p.getRandom().nextFloat() < 0.4f;
        d.mission = villain ? VILLAIN : PATROL;
        d.missionProgress = 0;
        d.missionGoal = villain ? 1 : 10;

        if (villain) {
            spawnVillain(level, board, p);
            p.sendSystemMessage(Component.literal("MISSION: A villain was spotted near the board! Take them down.")
                    .withStyle(ChatFormatting.RED));
        } else {
            p.sendSystemMessage(Component.literal("MISSION: Patrol the area. Defeat 10 hostile mobs.")
                    .withStyle(ChatFormatting.YELLOW));
        }
        AbilityManager.send(p);
    }

    public static void abandon(ServerPlayer p) {
        PlayerData d = PlayerData.get(p);
        d.mission = "";
        d.missionProgress = 0;
        d.missionGoal = 0;
        p.sendSystemMessage(Component.literal("Mission abandoned.").withStyle(ChatFormatting.GRAY));
        AbilityManager.send(p);
    }

    private static void spawnVillain(ServerLevel level, BlockPos board, ServerPlayer p) {
        int dx = p.getRandom().nextInt(13) - 6;
        int dz = p.getRandom().nextInt(13) - 6;
        if (Math.abs(dx) < 4 && Math.abs(dz) < 4) dx += 6;
        int x = board.getX() + dx;
        int z = board.getZ() + dz;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        VillainEntity v = ModEntities.VILLAIN.get().create(level);
        if (v == null) return;
        v.moveTo(x + 0.5, y, z + 0.5, p.getRandom().nextFloat() * 360f, 0f);
        v.setCustomName(Component.literal(VILLAIN_NAMES[p.getRandom().nextInt(VILLAIN_NAMES.length)]));
        v.setPersistenceRequired();
        level.addFreshEntity(v);
    }

    /** Called when a player kills something. */
    public static void onKill(ServerPlayer p, LivingEntity dead) {
        PlayerData d = PlayerData.get(p);
        if (!hasMission(d)) return;

        boolean counts = switch (d.mission) {
            case PATROL -> dead instanceof Enemy && !(dead instanceof VillainEntity);
            case VILLAIN -> dead instanceof VillainEntity;
            default -> false;
        };
        if (!counts) return;

        d.missionProgress++;
        if (d.missionProgress >= d.missionGoal) {
            complete(p, d);
        } else {
            p.displayClientMessage(Component.literal(describe(d)).withStyle(ChatFormatting.YELLOW), true);
            AbilityManager.send(p);
        }
    }

    private static void complete(ServerPlayer p, PlayerData d) {
        int reward = d.mission.equals(VILLAIN) ? 50 : 20;
        Ranks.Rank before = Ranks.of(d.points);
        d.points += reward;
        Ranks.Rank after = Ranks.of(d.points);

        d.mission = "";
        d.missionProgress = 0;
        d.missionGoal = 0;

        ItemStack emeralds = new ItemStack(Items.EMERALD, reward / 5);
        if (!p.getInventory().add(emeralds)) p.drop(emeralds, false);
        AbilityManager.addXp(p, d, reward);

        p.sendSystemMessage(Component.literal("MISSION COMPLETE! +" + reward + " hero points ("
                + d.points + " total)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        if (after != before) {
            p.sendSystemMessage(Component.literal("RANK UP! You are now a " + after.name() + "!")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        AbilityManager.send(p);
    }
}
