package com.revilo.gatewayexpansion.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.ModList;

public final class ModCompat {

    private static final String[] RUNIC_MOD_IDS = {"runic", "runiclib", "runic_reloaded"};

    private ModCompat() {
    }

    public static boolean isAnyLoaded(String... modIds) {
        return Arrays.stream(modIds).anyMatch(id -> ModList.get().isLoaded(id));
    }

    public static List<EntityType<?>> hostileEntitiesForNamespaces(String... namespaces) {
        List<EntityType<?>> entities = new ArrayList<>();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            EntityType<?> type = entry.getValue();
            if (Arrays.stream(namespaces).anyMatch(namespace -> namespace.equals(id.getNamespace())) && isPoolEligible(id, type)) {
                entities.add(type);
            }
        }
        return entities;
    }

    public static EntityType<?> findFirstEntity(String... ids) {
        for (String idString : ids) {
            ResourceLocation id = ResourceLocation.tryParse(idString);
            if (id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                return BuiltInRegistries.ENTITY_TYPE.get(id);
            }
        }
        return null;
    }

    public static List<EntityType<?>> findEntitiesMatching(String[] namespaces, String... pathKeywords) {
        List<EntityType<?>> matches = new ArrayList<>();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            EntityType<?> type = entry.getValue();
            if (!isPoolEligible(id, type) || Arrays.stream(namespaces).noneMatch(namespace -> namespace.equals(id.getNamespace()))) {
                continue;
            }
            if (pathKeywords.length == 0 || Arrays.stream(pathKeywords).anyMatch(keyword -> id.getPath().contains(keyword))) {
                matches.add(type);
            }
        }
        return matches;
    }

    public static List<EntityType<?>> findEntitiesByIds(String... ids) {
        List<EntityType<?>> matches = new ArrayList<>();
        for (String idString : ids) {
            EntityType<?> type = findFirstEntity(idString);
            if (type != null && !matches.contains(type)) {
                matches.add(type);
            }
        }
        return matches;
    }

    public static void debugDetected(String label, String... modIds) {
        // Intentionally silent to avoid reload/init log spam from repeated pool evaluation.
    }

    public static boolean isRunicLoaded() {
        if (isAnyLoaded(RUNIC_MOD_IDS)) {
            return true;
        }
        if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("runic", "enhanced_rune"))) {
            return true;
        }
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            if ("runic".equals(id.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHostile(EntityType<?> type) {
        return type.getCategory() == MobCategory.MONSTER;
    }

    private static boolean isPoolEligible(ResourceLocation id, EntityType<?> type) {
        String path = id.getPath();
        return isHostile(type)
                && !path.contains("creeper")
                && !path.contains("ghast")
                && !path.contains("piglin");
    }
}
