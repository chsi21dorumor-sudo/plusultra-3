package com.plusultra.item;

import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Removes your Quirk (and its level) so you can awaken a new one. */
public class QuirkEraserItem extends Item {
    public QuirkEraserItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            if (!d.hasQuirk()) {
                sp.displayClientMessage(Component.literal("You have no Quirk to erase.")
                        .withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            d.clearQuirk();
            stack.consume(1, sp);
            sp.sendSystemMessage(Component.literal("Your Quirk fades away...").withStyle(ChatFormatting.DARK_GRAY));
            AbilityManager.send(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
