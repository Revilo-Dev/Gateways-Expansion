package com.revilo.gatewayexpansion.catalyst;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class CatalystDefinitionPool {

    private static final Map<String, CatalystDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<CatalystArchetype, List<CatalystDefinition>> BY_ARCHETYPE = new LinkedHashMap<>();

    static {
        register(CatalystArchetype.TIME, c("long_burn", "Long Burn", tags("time", "safe"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, 100, "+5s wave time"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, -0.05D, "-5% loot")));
        register(CatalystArchetype.TIME, c("patient_siege", "Patient Siege", tags("time", "safe"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, 160, "+8s wave time"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, -0.10D, "-10% loot")));
        register(CatalystArchetype.TIME, c("rapid_clock", "Rapid Clock", tags("time", "reward"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "+10% loot"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, -100, "-5s wave time")));
        register(CatalystArchetype.TIME, c("sudden_death", "Sudden Death", tags("time", "volatile"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.15D, "+15% loot"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, -200, "-10s wave time")));
        register(CatalystArchetype.TIME, c("fast_reset", "Fast Reset", tags("setup", "tempo"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll"),
                fx(ForgeEffectType.SETUP_TIME_DELTA, -60, "-3s setup time")));
        register(CatalystArchetype.TIME, c("staged_breath", "Staged Breath", tags("setup", "safe"),
                fx(ForgeEffectType.SETUP_TIME_DELTA, 80, "+4s setup time"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, -0.06D, "-6% loot")));
        register(CatalystArchetype.TIME, c("borrowed_sun", "Borrowed Sun", tags("time", "tempo"),
                fx(ForgeEffectType.BONUS_EXPERIENCE, 18, "+18 bonus experience"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, -80, "-4s wave time")));
        register(CatalystArchetype.TIME, c("ember_lull", "Ember Lull", tags("setup", "safe"),
                fx(ForgeEffectType.SETUP_TIME_DELTA, 120, "+6s setup time"),
                fx(ForgeEffectType.EXTRA_ENTITY_LOOT_ROLLS, -1, "-1 entity loot roll")));

        register(CatalystArchetype.STAT, c("thickened_hide", "Thickened Hide", tags("stat", "loot"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.12D, "+12% loot"),
                fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.15D, "+15% enemy health")));
        register(CatalystArchetype.STAT, c("savage_edge", "Savage Edge", tags("stat", "damage"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.12D, "+12% loot"),
                fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.15D, "+15% enemy damage")));
        register(CatalystArchetype.STAT, c("hunting_wind", "Hunting Wind", tags("stat", "speed"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll"),
                fx(ForgeEffectType.SPEED_MULTIPLIER, 0.16D, "+16% enemy speed")));
        register(CatalystArchetype.STAT, c("platebreaker", "Platebreaker", tags("stat", "elite"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll"),
                fx(ForgeEffectType.ARMOR_BONUS, 4, "+4 armor")));
        register(CatalystArchetype.STAT, c("bloodsalt", "Bloodsalt", tags("stat", "volatile"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.16D, "+16% loot"),
                fx(ForgeEffectType.LIFE_STEAL, 0.10D, "+10% life steal")));
        register(CatalystArchetype.STAT, c("sundered_quarry", "Sundered Quarry", tags("stat", "damage"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll"),
                fx(ForgeEffectType.ARMOR_PIERCE, 2, "+2 armor pierce")));
        register(CatalystArchetype.STAT, c("glass_jaw", "Glass Jaw", tags("stat", "risk"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.14D, "+14% loot"),
                fx(ForgeEffectType.KNOCKBACK_RESISTANCE, 0.18D, "+18% knockback resistance")));
        register(CatalystArchetype.STAT, c("witchsteel", "Witchsteel", tags("stat", "ranged"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll"),
                fx(ForgeEffectType.PROJECTILE_DAMAGE, 0.18D, "+18% ranged damage")));

        register(CatalystArchetype.LOOT, c("gilded_teeth", "Gilded Teeth", tags("loot", "risk"),
                fx(ForgeEffectType.EXTRA_ENTITY_LOOT_ROLLS, 1, "+1 entity loot roll"),
                fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.12D, "+12% enemy health")));
        register(CatalystArchetype.LOOT, c("treasure_hunger", "Treasure Hunger", tags("loot", "risk"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll"),
                fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.12D, "+12% enemy damage")));
        register(CatalystArchetype.LOOT, c("rare_bait", "Rare Bait", tags("loot", "elite"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll"),
                fx(ForgeEffectType.ELITE_CHANCE, 0.08D, "+8% elite chance")));
        register(CatalystArchetype.LOOT, c("prize_cache", "Prize Cache", tags("loot", "xp"),
                fx(ForgeEffectType.BONUS_EXPERIENCE, 20, "+20 bonus experience"),
                fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 2, "+2 minions per wave")));
        register(CatalystArchetype.LOOT, c("safe_hands", "Safe Hands", tags("loot", "safe"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, 120, "+6s wave time"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, -1, "-1 final reward roll")));
        register(CatalystArchetype.LOOT, c("mercy_clause", "Mercy Clause", tags("loot", "safe"),
                fx(ForgeEffectType.HEALTH_MULTIPLIER, -0.08D, "-8% enemy health"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, -0.10D, "-10% loot")));
        register(CatalystArchetype.LOOT, c("deep_pockets", "Deep Pockets", tags("loot", "reward"),
                fx(ForgeEffectType.BONUS_LOOT_TABLE_CHANCE, 0.20D, "+20% extra cache chance"),
                fx(ForgeEffectType.ELITE_CHANCE, 0.07D, "+7% elite chance")));
        register(CatalystArchetype.LOOT, c("grave_tribute", "Grave Tribute", tags("loot", "xp"),
                fx(ForgeEffectType.BONUS_EXPERIENCE, 28, "+28 bonus experience"),
                fx(ForgeEffectType.FINAL_WAVE_ELITES, 1, "+1 final wave elite")));

        register(CatalystArchetype.VOLATILE, c("razor_dusk", "Razor Dusk", tags("volatile", "time"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls"),
                fx(ForgeEffectType.WAVE_TIME_DELTA, -240, "-12s wave time")));
        register(CatalystArchetype.VOLATILE, c("kingmaker", "Kingmaker", tags("volatile", "elite"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls"),
                fx(ForgeEffectType.FINAL_WAVE_ELITES, 2, "+2 final wave elites")));
        register(CatalystArchetype.VOLATILE, c("war_drum", "War Drum", tags("volatile", "pressure"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls"),
                fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Dense pressure waves")));
        register(CatalystArchetype.VOLATILE, c("red_feast", "Red Feast", tags("volatile", "lifesteal"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.20D, "+20% loot"),
                fx(ForgeEffectType.LIFE_STEAL, 0.15D, "+15% life steal")));
        register(CatalystArchetype.VOLATILE, c("abyssal_tax", "Abyssal Tax", tags("volatile", "miniboss"),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll"),
                fx(ForgeEffectType.MINIBOSS_CHANCE, 0.18D, "High miniboss chance")));
        register(CatalystArchetype.VOLATILE, c("last_bell", "Last Bell", tags("volatile", "finale"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.18D, "+18% loot"),
                fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Harder final wave")));
        register(CatalystArchetype.VOLATILE, c("black_banner", "Black Banner", tags("volatile", "elite"),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll"),
                fx(ForgeEffectType.ELITE_EVERY, 2, "+1 elite every 2 waves")));
        register(CatalystArchetype.VOLATILE, c("furnace_heart", "Furnace Heart", tags("volatile", "damage"),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.22D, "+22% loot"),
                fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.18D, "+18% enemy damage")));
    }

    private CatalystDefinitionPool() {
    }

    public static CatalystDefinition getById(String id) {
        return BY_ID.get(id);
    }

    public static CatalystDefinition random(CatalystArchetype archetype, RandomSource random) {
        List<CatalystDefinition> entries = BY_ARCHETYPE.getOrDefault(archetype, List.of());
        return entries.get(random.nextInt(entries.size()));
    }

    public static CatalystDefinition fallback(CatalystArchetype archetype) {
        return BY_ARCHETYPE.getOrDefault(archetype, List.of()).getFirst();
    }

    private static void register(CatalystArchetype archetype, CatalystDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_ARCHETYPE.computeIfAbsent(archetype, ignored -> new ArrayList<>()).add(definition);
    }

    private static CatalystDefinition c(String id, String title, Set<String> tags, ForgeEffect positive, ForgeEffect negative) {
        return new CatalystDefinition(id, title, positive, negative, tags);
    }

    private static ForgeEffect fx(ForgeEffectType type, double value, String description) {
        return ForgeEffect.of(type, value, description);
    }

    @SuppressWarnings("unused")
    private static ForgeEffect effect(String effectId, int amplifier, String description) {
        return ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.parse(effectId), amplifier, 0.0D, description);
    }

    private static Set<String> tags(String... tags) {
        return Set.of(tags);
    }
}
