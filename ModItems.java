package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.item.HeroLicenseItem;
import com.plusultra.item.QuirkEraserItem;
import com.plusultra.item.QuirkOrbItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PlusUltra.MODID);

    public static final DeferredItem<QuirkOrbItem> QUIRK_ORB = ITEMS.register("quirk_orb",
            () -> new QuirkOrbItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<QuirkEraserItem> QUIRK_ERASER = ITEMS.register("quirk_eraser",
            () -> new QuirkEraserItem(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<HeroLicenseItem> HERO_LICENSE = ITEMS.register("hero_license",
            () -> new HeroLicenseItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> MISSION_BOARD =
            ITEMS.registerSimpleBlockItem("mission_board", ModBlocks.MISSION_BOARD);

    public static final DeferredItem<DeferredSpawnEggItem> VILLAIN_SPAWN_EGG = ITEMS.register("villain_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VILLAIN, 0x2B1B3D, 0xB22222, new Item.Properties()));

    private ModItems() {}
}
