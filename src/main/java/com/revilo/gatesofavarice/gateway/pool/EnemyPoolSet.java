package com.revilo.gatesofavarice.gateway.pool;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;

public final class EnemyPoolSet {

    private final Map<EnemyPoolRole, WeightedEntityPool> pools = new EnumMap<>(EnemyPoolRole.class);

    public EnemyPoolSet() {
        WeightedEntityPool assassin = new WeightedEntityPool("assassin");
        WeightedEntityPool hoard = new WeightedEntityPool("hoard");
        WeightedEntityPool archer = new WeightedEntityPool("archer");
        WeightedEntityPool tank = new WeightedEntityPool("tank");

        this.pools.put(EnemyPoolRole.ASSASSIN, assassin);
        this.pools.put(EnemyPoolRole.HOARD, hoard);
        this.pools.put(EnemyPoolRole.ARCHER, archer);
        this.pools.put(EnemyPoolRole.TANK, tank);

        // Legacy role aliases mapped into canonical 4-category pools.
        this.pools.put(EnemyPoolRole.MELEE, hoard);
        this.pools.put(EnemyPoolRole.THEME, hoard);
        this.pools.put(EnemyPoolRole.SUPPORT, hoard);

        this.pools.put(EnemyPoolRole.RANGED, archer);

        this.pools.put(EnemyPoolRole.FAST, assassin);
        this.pools.put(EnemyPoolRole.ELITE, assassin);

        this.pools.put(EnemyPoolRole.BOSS, tank);
    }

    public WeightedEntityPool pool(EnemyPoolRole role) {
        return this.pools.get(role);
    }

    public EntityType<?> pick(RandomSource random, EnemyPoolRole primary, EnemyPoolRole fallback) {
        return this.pool(primary).pick(random, this.pool(fallback));
    }

    public EntityType<?> pickBoss(RandomSource random) {
        return this.pool(EnemyPoolRole.TANK).pick(random, this.pool(EnemyPoolRole.ASSASSIN));
    }

    public boolean hasBosses() {
        return !this.pool(EnemyPoolRole.TANK).isEmpty();
    }
}
