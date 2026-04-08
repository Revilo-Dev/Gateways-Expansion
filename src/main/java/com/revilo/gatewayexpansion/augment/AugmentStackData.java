package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

public final class AugmentStackData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String ID_KEY = "augment_id";
    private static final String MODIFIER_EFFECTS_KEY = "augment_modifier_effects";
    private static final String REWARD_EFFECTS_KEY = "augment_reward_effects";
    private static final String REWARD_TYPE_KEY = "augment_reward_type";
    private static final String REWARD_VALUE_KEY = "augment_reward_value";
    private static final String REWARD_SECONDARY_VALUE_KEY = "augment_reward_secondary_value";
    private static final String REWARD_REFERENCE_KEY = "augment_reward_reference";
    private static final String REWARD_DESCRIPTION_KEY = "augment_reward_description";

    private AugmentStackData() {
    }

    public static AugmentDefinition ensureDefinition(ItemStack stack, AugmentDifficultyTier tier, RandomSource random) {
        return ensureDefinition(stack, tier, random, -1);
    }

    public static AugmentDefinition ensureDefinition(ItemStack stack, AugmentDifficultyTier tier, RandomSource random, int level) {
        AugmentDefinition existing = getDefinition(stack, tier);
        if (existing != null) {
            return existing;
        }
        AugmentDefinition definition = AugmentDefinitionPool.random(tier, random, level);
        setDefinition(stack, definition);
        return definition;
    }

    public static void setDefinition(ItemStack stack, AugmentDefinition definition) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putString(ID_KEY, definition.id());
            root.put(MODIFIER_EFFECTS_KEY, writeEffectList(definition.modifierEffects()));
            root.put(REWARD_EFFECTS_KEY, writeEffectList(definition.rewardEffects()));
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
                List<ForgeEffect> modifierEffects = readEffectList(root, MODIFIER_EFFECTS_KEY, definition.modifierEffects());
                List<ForgeEffect> rewardEffects = readRewardEffects(root, definition.rewardEffects());
                return new AugmentDefinition(definition.id(), definition.title(), definition.difficultyTier(), modifierEffects, rewardEffects, definition.tags());
            }
        }
        return stack.has(DataComponents.CUSTOM_DATA) ? AugmentDefinitionPool.fallback(tier) : null;
    }

    private static ListTag writeEffectList(List<ForgeEffect> effects) {
        ListTag list = new ListTag();
        for (ForgeEffect effect : effects) {
            list.add(writeEffect(effect));
        }
        return list;
    }

    private static CompoundTag writeEffect(ForgeEffect effect) {
        CompoundTag tag = new CompoundTag();
        tag.putString(REWARD_TYPE_KEY, effect.type().name());
        tag.putDouble(REWARD_VALUE_KEY, effect.value());
        tag.putDouble(REWARD_SECONDARY_VALUE_KEY, effect.secondaryValue());
        tag.putString(REWARD_DESCRIPTION_KEY, effect.description());
        if (effect.referenceId() != null) {
            tag.putString(REWARD_REFERENCE_KEY, effect.referenceId().toString());
        }
        return tag;
    }

    private static List<ForgeEffect> readRewardEffects(CompoundTag root, List<ForgeEffect> fallback) {
        if (root.contains(REWARD_EFFECTS_KEY, Tag.TAG_LIST)) {
            return readEffectList(root, REWARD_EFFECTS_KEY, fallback);
        }
        if (root.contains(REWARD_TYPE_KEY)) {
            return List.of(readEffect(root, fallback.isEmpty() ? ForgeEffect.of(ForgeEffectType.REWARD_MULTIPLIER, 0.0D, "") : fallback.getFirst()));
        }
        return fallback;
    }

    private static List<ForgeEffect> readEffectList(CompoundTag root, String key, List<ForgeEffect> fallback) {
        if (!root.contains(key, Tag.TAG_LIST)) {
            return fallback;
        }

        ListTag list = root.getList(key, Tag.TAG_COMPOUND);
        List<ForgeEffect> effects = new ArrayList<>(list.size());
        for (Tag entry : list) {
            if (entry instanceof CompoundTag effectTag) {
                ForgeEffect fallbackEffect = fallback.isEmpty() ? ForgeEffect.of(ForgeEffectType.REWARD_MULTIPLIER, 0.0D, "") : fallback.get(Math.min(effects.size(), fallback.size() - 1));
                effects.add(readEffect(effectTag, fallbackEffect));
            }
        }
        return effects.isEmpty() ? fallback : List.copyOf(effects);
    }

    private static ForgeEffect readEffect(CompoundTag root, ForgeEffect fallback) {
        ForgeEffectType type = root.contains(REWARD_TYPE_KEY) ? ForgeEffectType.valueOf(root.getString(REWARD_TYPE_KEY)) : fallback.type();
        double value = root.contains(REWARD_VALUE_KEY) ? root.getDouble(REWARD_VALUE_KEY) : fallback.value();
        double secondaryValue = root.contains(REWARD_SECONDARY_VALUE_KEY) ? root.getDouble(REWARD_SECONDARY_VALUE_KEY) : fallback.secondaryValue();
        String description = root.contains(REWARD_DESCRIPTION_KEY) ? root.getString(REWARD_DESCRIPTION_KEY) : fallback.description();
        ResourceLocation referenceId = root.contains(REWARD_REFERENCE_KEY) ? ResourceLocation.parse(root.getString(REWARD_REFERENCE_KEY)) : fallback.referenceId();
        return new ForgeEffect(type, value, secondaryValue, referenceId, description);
    }
}
