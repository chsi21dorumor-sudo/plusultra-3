package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.mission.MissionBoardBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PlusUltra.MODID);

    public static final DeferredBlock<MissionBoardBlock> MISSION_BOARD = BLOCKS.register(
            "mission_board",
            () -> new MissionBoardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5f)
                    .sound(SoundType.WOOD)));

    private ModBlocks() {}
}
