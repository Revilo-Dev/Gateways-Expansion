package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class AugmentStackData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String ID_KEY = "augment_id";
    private static final String REWARD_TYPE_KEY = "augment_reward_type";
    private static final String REWARD_VALUE_KEY = "augment_reward_value";
    private static final String REWARD_SECONDARY_VALUE_KEY = "augment_reward_secondary_value";
    private static final String REWARD_REFERENCE_KEY = "augment_reward_reference";
    private static final String REWARD_DESCRIPTION_KEY = "augment_reward_description";

    private AugmentStackData() {
    }

    public static AugmentDefinition ensureDefinition(ItemStack stack, AugmentDifficultyTier tier, RandomSource random) {
        AugmentDefinition existing = getDefinition(stack, tier);
        if (existing != null) {
            return existing;
        }
        AugmentDefinition definition = AugmentDefinitionPool.random(tier, random);
        setDefinition(stack, definition);
        return definition;
    }

    public static void setDefinition(ItemStack stack, AugmentDefinition definition) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putString(ID_KEY, definition.id());
            writeRewardEffect(root, definition.rewardEffect());
            tag.put(ROOT_KEY, root);
        });
    }

    public static void setDefinitionId(ItemStack stack, String definitionId) {
        AugmentDefinition definition = AugmentDefinitionPool.getById(definitionId);
        if (definition == null) {
            return;
        }
        setDefinition(stack, definition);
    }

    public static AugmentDefinition getDefinition(ItemStack stack, AugmentDifficultyTier tier) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        if (root.contains(ID_KEY)) {
            AugmentDefinition definition = AugmentDefinitionPool.getById(root.getString(ID_KEY));
            if (definition != null) {
                ForgeEffect rewardEffect = readRewardEffect(root, definition.rewardEffect());
                return new AugmentDefinition(definition.id(), definition.title(), definition.difficultyTier(), definition.modifierEffects(), rewardEffect, definition.tags());
            }
        }
        return stack.has(DataComponents.CUSTOM_DATA) ? AugmentDefinitionPool.fallback(tier) : null;
    }

    private static void writeRewardEffect(CompoundTag root, ForgeEffect effect) {
        root.putString(REWARD_TYPE_KEY, effect.type().name());
        root.putDouble(REWARD_VALUE_KEY, effect.value());
        root.putDouble(REWARD_SECONDARY_VALUE_KEY, effect.secondaryValue());
        root.putString(REWARD_DESCRIPTION_KEY, effect.description());
        if (effect.referenceId() != null) {
            root.putString(REWARD_REFERENCE_KEY, effect.referenceId().toString());
        } else {
            root.remove(REWARD_REFERENCE_KEY);
        }
    }

    private static ForgeEffect readRewardEffect(CompoundTag root, ForgeEffect fallback) {
        if (!root.contains(REWARD_TYPE_KEY)) {
            return fallback;
        }

        ForgeEffectType type = ForgeEffectType.valueOf(root.getString(REWARD_TYPE_KEY));
        double value = root.contains(REWARD_VALUE_KEY) ? root.getDouble(REWARD_VALUE_KEY) : fallback.value();
        double secondaryValue = root.contains(REWARD_SECONDARY_VALUE_KEY) ? root.getDouble(REWARD_SECONDARY_VALUE_KEY) : fallback.secondaryValue();
        String description = root.contains(REWARD_DESCRIPTION_KEY) ? root.getString(REWARD_DESCRIPTION_KEY) : fallback.description();
        ResourceLocation referenceId = root.contains(REWARD_REFERENCE_KEY) ? ResourceLocation.parse(root.getString(REWARD_REFERENCE_KEY)) : fallback.referenceId();
        return new ForgeEffect(type, value, secondaryValue, referenceId, description);
    }
}
