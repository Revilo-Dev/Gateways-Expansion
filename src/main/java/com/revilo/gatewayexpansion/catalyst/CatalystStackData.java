package com.revilo.gatewayexpansion.catalyst;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CatalystStackData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String ID_KEY = "catalyst_id";
    private static final String POSITIVE_TYPE_KEY = "catalyst_positive_type";
    private static final String POSITIVE_VALUE_KEY = "catalyst_positive_value";
    private static final String POSITIVE_SECONDARY_VALUE_KEY = "catalyst_positive_secondary_value";
    private static final String POSITIVE_REFERENCE_KEY = "catalyst_positive_reference";
    private static final String POSITIVE_DESCRIPTION_KEY = "catalyst_positive_description";
    private static final String NEGATIVE_TYPE_KEY = "catalyst_negative_type";
    private static final String NEGATIVE_VALUE_KEY = "catalyst_negative_value";
    private static final String NEGATIVE_SECONDARY_VALUE_KEY = "catalyst_negative_secondary_value";
    private static final String NEGATIVE_REFERENCE_KEY = "catalyst_negative_reference";
    private static final String NEGATIVE_DESCRIPTION_KEY = "catalyst_negative_description";

    private CatalystStackData() {
    }

    public static CatalystDefinition ensureDefinition(ItemStack stack, CatalystArchetype archetype, RandomSource random) {
        return ensureDefinition(stack, archetype, random, -1);
    }

    public static CatalystDefinition ensureDefinition(ItemStack stack, CatalystArchetype archetype, RandomSource random, int level) {
        CatalystDefinition existing = getDefinition(stack, archetype);
        if (existing != null) {
            return existing;
        }
        CatalystDefinition definition = CatalystDefinitionPool.random(archetype, random, level);
        setDefinition(stack, definition);
        return definition;
    }

    public static void setDefinitionId(ItemStack stack, String definitionId) {
        CatalystDefinition definition = CatalystDefinitionPool.getById(definitionId);
        if (definition == null) {
            return;
        }
        setDefinition(stack, definition);
    }

    public static void setDefinition(ItemStack stack, CatalystDefinition definition) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putString(ID_KEY, definition.id());
            writeEffect(root, POSITIVE_TYPE_KEY, POSITIVE_VALUE_KEY, POSITIVE_SECONDARY_VALUE_KEY, POSITIVE_REFERENCE_KEY, POSITIVE_DESCRIPTION_KEY, definition.positiveEffect());
            writeEffect(root, NEGATIVE_TYPE_KEY, NEGATIVE_VALUE_KEY, NEGATIVE_SECONDARY_VALUE_KEY, NEGATIVE_REFERENCE_KEY, NEGATIVE_DESCRIPTION_KEY, definition.negativeEffect());
            tag.put(ROOT_KEY, root);
        });
    }

    public static CatalystDefinition getDefinition(ItemStack stack, CatalystArchetype archetype) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        if (root.contains(ID_KEY)) {
            CatalystDefinition definition = CatalystDefinitionPool.getById(root.getString(ID_KEY));
            CatalystDefinition base = definition != null ? definition : CatalystDefinitionPool.fallback(archetype);
            if (base != null) {
                ForgeEffect positive = readEffect(root, POSITIVE_TYPE_KEY, POSITIVE_VALUE_KEY, POSITIVE_SECONDARY_VALUE_KEY, POSITIVE_REFERENCE_KEY, POSITIVE_DESCRIPTION_KEY, base.positiveEffect());
                ForgeEffect negative = readEffect(root, NEGATIVE_TYPE_KEY, NEGATIVE_VALUE_KEY, NEGATIVE_SECONDARY_VALUE_KEY, NEGATIVE_REFERENCE_KEY, NEGATIVE_DESCRIPTION_KEY, base.negativeEffect());
                String id = definition != null ? definition.id() : root.getString(ID_KEY);
                String title = definition != null ? definition.title() : base.title();
                return new CatalystDefinition(id, title, positive, negative, base.tags());
            }
        }
        return stack.has(DataComponents.CUSTOM_DATA) ? CatalystDefinitionPool.fallback(archetype) : null;
    }

    private static void writeEffect(CompoundTag root, String typeKey, String valueKey, String secondaryValueKey, String referenceKey, String descriptionKey, ForgeEffect effect) {
        root.putString(typeKey, effect.type().name());
        root.putDouble(valueKey, effect.value());
        root.putDouble(secondaryValueKey, effect.secondaryValue());
        root.putString(descriptionKey, effect.description());
        if (effect.referenceId() != null) {
            root.putString(referenceKey, effect.referenceId().toString());
        } else {
            root.remove(referenceKey);
        }
    }

    private static ForgeEffect readEffect(CompoundTag root, String typeKey, String valueKey, String secondaryValueKey, String referenceKey, String descriptionKey, ForgeEffect fallback) {
        if (!root.contains(typeKey)) {
            return fallback;
        }

        ForgeEffectType type = ForgeEffectType.valueOf(root.getString(typeKey));
        double value = root.contains(valueKey) ? root.getDouble(valueKey) : fallback.value();
        double secondaryValue = root.contains(secondaryValueKey) ? root.getDouble(secondaryValueKey) : fallback.secondaryValue();
        String description = root.contains(descriptionKey) ? root.getString(descriptionKey) : fallback.description();
        ResourceLocation referenceId = root.contains(referenceKey) ? ResourceLocation.parse(root.getString(referenceKey)) : fallback.referenceId();
        return new ForgeEffect(type, value, secondaryValue, referenceId, description);
    }
}
