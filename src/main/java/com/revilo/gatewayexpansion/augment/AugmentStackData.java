package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class AugmentStackData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String ID_KEY = "augment_id";

    private AugmentStackData() {
    }

    public static AugmentDefinition ensureDefinition(ItemStack stack, AugmentDifficultyTier tier, RandomSource random) {
        AugmentDefinition existing = getDefinition(stack, tier);
        if (existing != null) {
            return existing;
        }
        AugmentDefinition definition = AugmentDefinitionPool.random(tier, random);
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

    public static AugmentDefinition getDefinition(ItemStack stack, AugmentDifficultyTier tier) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        if (root.contains(ID_KEY)) {
            AugmentDefinition definition = AugmentDefinitionPool.getById(root.getString(ID_KEY));
            if (definition != null) {
                return definition;
            }
        }
        return stack.has(DataComponents.CUSTOM_DATA) ? AugmentDefinitionPool.fallback(tier) : null;
    }
}
