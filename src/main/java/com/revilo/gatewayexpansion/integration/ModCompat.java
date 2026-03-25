package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.GatewayExpansion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.ModList;

public final class ModCompat {

    private static final Set<String> LOGGED_MODS = new LinkedHashSet<>();

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
            if (Arrays.stream(namespaces).anyMatch(namespace -> namespace.equals(id.getNamespace())) && isHostile(type)) {
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
            if (!isHostile(type) || Arrays.stream(namespaces).noneMatch(namespace -> namespace.equals(id.getNamespace()))) {
                continue;
            }
            if (pathKeywords.length == 0 || Arrays.stream(pathKeywords).anyMatch(keyword -> id.getPath().contains(keyword))) {
                matches.add(type);
            }
        }
        return matches;
    }

    public static void debugDetected(String label, String... modIds) {
        String key = label + Arrays.toString(modIds);
        if (LOGGED_MODS.add(key)) {
            GatewayExpansion.LOGGER.debug("{} support {}", label, isAnyLoaded(modIds) ? "enabled" : "not detected");
        }
    }

    private static boolean isHostile(EntityType<?> type) {
        return type.getCategory() == MobCategory.MONSTER;
    }
}
