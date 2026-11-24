package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.quirk.AbilityManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the player pressed an ability key (slot 0..2). */
public record UseAbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<UseAbilityPayload> TYPE = new Type<>(PlusUltra.id("use_ability"));
    public static final StreamCodec<ByteBuf, UseAbilityPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(UseAbilityPayload::new, UseAbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseAbilityPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                AbilityManager.use(sp, payload.slot());
            }
        });
    }
}
