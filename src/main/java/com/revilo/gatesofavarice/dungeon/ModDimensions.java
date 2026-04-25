package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {

    public static final ResourceKey<Level> DUNGEON_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "dungeon")
    );

    private ModDimensions() {
    }
}
