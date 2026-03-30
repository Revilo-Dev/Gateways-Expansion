package com.revilo.gatewayexpansion.catalyst;

import com.revilo.gatewayexpansion.GatewayExpansion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CatalystStackData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String ID_KEY = "catalyst_id";

    private CatalystStackData() {
    }

    public static CatalystDefinition ensureDefinition(ItemStack stack, CatalystArchetype archetype, RandomSource random) {
        CatalystDefinition existing = getDefinition(stack, archetype);
        if (existing != null) {
            return existing;
        }
        CatalystDefinition definition = CatalystDefinitionPool.random(archetype, random);
        setDefinitionId(stack, definition.id());
        return definition;
    }

    public static void setDefinitionId(ItemStack stack, String definitionId) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putString(ID_KEY, definitionId);
            tag.put(ROOT_KEY, root);
        });
    }

    public static CatalystDefinition getDefinition(ItemStack stack, CatalystArchetype archetype) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        if (root.contains(ID_KEY)) {
            CatalystDefinition definition = CatalystDefinitionPool.getById(root.getString(ID_KEY));
            if (definition != null) {
                return definition;
            }
        }
        return stack.has(DataComponents.CUSTOM_DATA) ? CatalystDefinitionPool.fallback(archetype) : null;
    }
}
