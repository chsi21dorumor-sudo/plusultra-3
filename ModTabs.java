package com.plusultra.core;

import com.plusultra.PlusUltra;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PlusUltra.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.plusultra"))
                    .icon(() -> ModItems.QUIRK_ORB.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(ModItems.QUIRK_ORB.get());
                        out.accept(ModItems.QUIRK_ERASER.get());
                        out.accept(ModItems.HERO_LICENSE.get());
                        out.accept(ModItems.MISSION_BOARD.get());
                        out.accept(ModItems.VILLAIN_SPAWN_EGG.get());
                    })
                    .build());

    private ModTabs() {}
}
