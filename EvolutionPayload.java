package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Quirk;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server actions for the Quirk Evolution screen. */
public record EvolutionPayload(int action, int value) implements CustomPacketPayload {
    public static final Type<EvolutionPayload> TYPE = new Type<>(PlusUltra.id("evolution"));
    public static final StreamCodec<ByteBuf, EvolutionPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, EvolutionPayload::action,
                    ByteBufCodecs.VAR_INT, EvolutionPayload::value, EvolutionPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(EvolutionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer p)) return;
            PlayerData d = PlayerData.get(p);
            switch (payload.action()) {
                case 0 -> unlockTalent(p, d, payload.value());
                case 1 -> fuse(p, d, payload.value());
                case 2 -> { d.fusionQuirk = ""; com.plusultra.quirk.AbilityManager.send(p); }
            }
        });
    }

    private static void unlockTalent(ServerPlayer p, PlayerData d, int index) {
        if (!d.hasQuirk() || index < 0 || index >= 6 || d.hasTalent(index)) return;
        if (d.level < 2 + index * 2) {
            p.displayClientMessage(Component.literal("Requires level " + (2 + index * 2) + ".").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (index > 0 && !d.hasTalent(index - 1)) {
            p.displayClientMessage(Component.literal("Unlock the previous talent first.").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (d.skillPoints < 1) {
            p.displayClientMessage(Component.literal("Not enough skill points.").withStyle(ChatFormatting.RED), true);
            return;
        }
        d.skillPoints--;
        d.unlockTalent(index);
        p.displayClientMessage(Component.literal("Talent unlocked!").withStyle(ChatFormatting.GREEN), true);
        com.plusultra.quirk.AbilityManager.send(p);
    }

    private static void fuse(ServerPlayer p, PlayerData d, int ordinal) {
        if (!d.hasQuirk() || d.level < 5 || d.skillPoints < 3) return;
        Quirk[] all = Quirk.values();
        if (ordinal < 0 || ordinal >= all.length || all[ordinal].id().equals(d.quirk)) return;
        d.skillPoints -= 3;
        d.fusionQuirk = all[ordinal].id();
        p.displayClientMessage(Component.literal("Fusion activated: " + all[ordinal].id()).withStyle(ChatFormatting.AQUA), true);
        com.plusultra.quirk.AbilityManager.send(p);
    }
}
