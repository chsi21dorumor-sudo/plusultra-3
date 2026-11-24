package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = PlusUltra.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PlusUltra.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> VILLAIN = ENTITIES.register(
            "villain",
            () -> EntityType.Builder.of(VillainEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build("villain"));

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(VILLAIN.get(), VillainEntity.createAttributes().build());
    }

    private ModEntities() {}
}
