package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.client.ClientData;
import com.plusultra.quirk.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server -> client: everything the HUD needs to draw. Cooldowns are sent as ticks remaining. */
public record SyncPayload(String quirk, int level, int xp, int xpNeeded, float stamina, float maxStamina,
                          int cd0, int cd1, int cd2, int points, int skillPoints, long talents, String fusionQuirk, String mission, int missionProgress,
                          int missionGoal) implements CustomPacketPayload {

    public static final Type<SyncPayload> TYPE = new Type<>(PlusUltra.id("sync"));
    public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC =
            StreamCodec.of(SyncPayload::write, SyncPayload::read);

    public static SyncPayload from(ServerPlayer p, PlayerData d) {
        long now = p.level().getGameTime();
        return new SyncPayload(d.quirk, d.level, d.xp, d.xpNeeded(), d.stamina, d.maxStamina(),
                (int) Math.max(0, d.cd[0] - now), (int) Math.max(0, d.cd[1] - now),
                (int) Math.max(0, d.cd[2] - now), d.points, d.skillPoints, d.talents, d.fusionQuirk, d.mission, d.missionProgress, d.missionGoal);
    }

    private static void write(FriendlyByteBuf buf, SyncPayload p) {
        buf.writeUtf(p.quirk);
        buf.writeVarInt(p.level);
        buf.writeVarInt(p.xp);
        buf.writeVarInt(p.xpNeeded);
        buf.writeFloat(p.stamina);
        buf.writeFloat(p.maxStamina);
        buf.writeVarInt(p.cd0);
        buf.writeVarInt(p.cd1);
        buf.writeVarInt(p.cd2);
        buf.writeVarInt(p.points);
        buf.writeVarInt(p.skillPoints);
        buf.writeLong(p.talents);
        buf.writeUtf(p.fusionQuirk);
        buf.writeUtf(p.mission);
        buf.writeVarInt(p.missionProgress);
        buf.writeVarInt(p.missionGoal);
    }

    private static SyncPayload read(FriendlyByteBuf buf) {
        return new SyncPayload(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readLong(), buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientData.update(payload));
    }
}
