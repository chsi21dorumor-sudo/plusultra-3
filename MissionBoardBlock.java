package com.plusultra.mission;

import com.plusultra.quirk.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Right-click for a mission, right-click while sneaking to abandon the current one. */
public class MissionBoardBlock extends Block {
    public MissionBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            PlayerData d = PlayerData.get(sp);
            if (Missions.hasMission(d)) {
                if (sp.isShiftKeyDown()) {
                    Missions.abandon(sp);
                } else {
                    sp.sendSystemMessage(Component.literal(Missions.describe(d)
                            + "  (sneak + click to abandon)").withStyle(ChatFormatting.YELLOW));
                }
            } else {
                Missions.assign(sp, sl, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
