package com.plusultra.item;

import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Quirk;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Awakens a random Quirk. Only works if you don't already have one. */
public class QuirkOrbItem extends Item {
    public QuirkOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            if (d.hasQuirk()) {
                sp.displayClientMessage(Component.literal("Your body already holds a Quirk. Use a Quirk Eraser first.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }
            Quirk q = Quirk.random(sp.getRandom());
            d.setQuirk(q.id());
            stack.consume(1, sp);

            sp.sendSystemMessage(Component.literal("Your Quirk awakened: ")
                    .append(Component.translatable("quirk.plusultra." + q.id()).withStyle(s -> s.withColor(q.color())))
                    .withStyle(ChatFormatting.BOLD));
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1, sp.getZ(), 40, 0.5, 0.8, 0.5, 0.1);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
            }
            AbilityManager.send(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
