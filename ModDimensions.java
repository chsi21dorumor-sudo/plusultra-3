package com.plusultra.core;

import com.plusultra.PlusUltra;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ModDimensions {
    public static final ResourceKey<Level> TRAINING =
            ResourceKey.create(Registries.DIMENSION, PlusUltra.id("training_ground"));

    private ModDimensions() {}
}
