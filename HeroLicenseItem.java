package com.plusultra.item;

import com.plusultra.mission.Ranks;
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

/** Shows your hero rank and points. */
public class HeroLicenseItem extends Item {
    public HeroLicenseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            Ranks.Rank rank = Ranks.of(d.points);
            Ranks.Rank next = Ranks.next(d.points);
            sp.sendSystemMessage(Component.literal("== Hero License ==").withStyle(ChatFormatting.GOLD));
            sp.sendSystemMessage(Component.literal("Name: " + sp.getName().getString()));
            sp.sendSystemMessage(Component.literal("Rank: " + rank.name() + " (" + d.points + " pts)"));
            if (next != null) {
                sp.sendSystemMessage(Component.literal("Next rank: " + next.name() + " at " + next.minPoints() + " pts")
                        .withStyle(ChatFormatting.GRAY));
            }
            if (d.hasQuirk()) {
                sp.sendSystemMessage(Component.literal("Quirk: ")
                        .append(Component.translatable("quirk.plusultra." + d.quirk))
                        .append(Component.literal("  Lv." + d.level)));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
