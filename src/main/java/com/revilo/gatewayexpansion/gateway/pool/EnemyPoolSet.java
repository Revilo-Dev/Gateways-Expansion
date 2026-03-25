package com.revilo.gatewayexpansion.gateway.pool;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

public final class EnemyPoolSet {

    private final Map<EnemyPoolRole, WeightedEntityPool> pools = new EnumMap<>(EnemyPoolRole.class);

    public EnemyPoolSet() {
        for (EnemyPoolRole role : EnemyPoolRole.values()) {
            this.pools.put(role, new WeightedEntityPool(role.name().toLowerCase()));
        }
    }

    public WeightedEntityPool pool(EnemyPoolRole role) {
        return this.pools.get(role);
    }

    public EntityType<?> pick(RandomSource random, EnemyPoolRole primary, EnemyPoolRole fallback) {
        return this.pool(primary).pick(random, this.pool(fallback));
    }

    public EntityType<?> pickBoss(RandomSource random) {
        return this.pool(EnemyPoolRole.BOSS).pick(random, this.pool(EnemyPoolRole.ELITE));
    }

    public boolean hasBosses() {
        return !this.pool(EnemyPoolRole.BOSS).isEmpty();
    }
}
