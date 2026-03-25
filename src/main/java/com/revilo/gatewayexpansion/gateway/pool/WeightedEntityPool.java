package com.revilo.gatewayexpansion.gateway.pool;

import com.revilo.gatewayexpansion.GatewayExpansion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

public final class WeightedEntityPool {

    private final String name;
    private final List<Entry> entries = new ArrayList<>();

    public WeightedEntityPool(String name) {
        this.name = name;
    }

    public WeightedEntityPool add(EntityType<?> type, int weight, String source) {
        if (type != null && weight > 0) {
            this.entries.add(new Entry(type, weight, source));
        }
        return this;
    }

    public WeightedEntityPool addAll(List<EntityType<?>> types, int weight, String source) {
        for (EntityType<?> type : types) {
            this.add(type, weight, source);
        }
        return this;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public EntityType<?> pick(RandomSource random, WeightedEntityPool fallback) {
        if (this.entries.isEmpty()) {
            if (fallback != null && fallback != this) {
                GatewayExpansion.LOGGER.debug("Enemy pool fallback used for {}", this.name);
                return fallback.pick(random, null);
            }
            return null;
        }

        int totalWeight = this.entries.stream().mapToInt(Entry::weight).sum();
        int roll = random.nextInt(totalWeight);
        for (Entry entry : this.entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.type();
            }
        }
        return this.entries.getLast().type();
    }

    public int size() {
        return this.entries.size();
    }

    public record Entry(EntityType<?> type, int weight, String source) {
    }
}
