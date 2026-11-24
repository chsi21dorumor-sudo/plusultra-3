package com.plusultra;

import com.plusultra.config.PUConfig;
import com.plusultra.core.ModAttachments;
import com.plusultra.core.ModBlocks;
import com.plusultra.core.ModEntities;
import com.plusultra.core.ModItems;
import com.plusultra.core.ModTabs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(PlusUltra.MODID)
public class PlusUltra {
    public static final String MODID = "plusultra";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public PlusUltra(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModTabs.TABS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, PUConfig.SPEC);
    }
}
