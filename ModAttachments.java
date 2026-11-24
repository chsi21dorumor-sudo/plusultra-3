package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.quirk.PlayerData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PlusUltra.MODID);

    /** All per-player progress: quirk, level, xp, stamina, cooldowns, mission, hero points. */
    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA = ATTACHMENTS.register(
            "player_data",
            () -> AttachmentType.builder(() -> new PlayerData())
                    .serialize(PlayerData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}
}
