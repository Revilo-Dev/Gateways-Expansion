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
                plusSeconds(),
                minusLoot()));
        register(CatalystArchetype.TIME, c("patient_siege", "Patient Siege", tags("time", "safe"),
                plusQuantity(),
                plusWave()));
        register(CatalystArchetype.TIME, c("rapid_clock", "Rapid Clock", tags("time", "reward"),
                plusRarity(),
                minusWaveCooldown()));
        register(CatalystArchetype.TIME, c("sudden_death", "Sudden Death", tags("time", "volatile"),
                plusRareReward(),
                minusSeconds()));
        register(CatalystArchetype.TIME, c("fast_reset", "Fast Reset", tags("setup", "tempo"),
                plusXp(),
                minusWaveCooldown()));
        register(CatalystArchetype.TIME, c("staged_breath", "Staged Breath", tags("setup", "safe"),
                plusSeconds(),
                plusMobs()));
        register(CatalystArchetype.TIME, c("borrowed_sun", "Borrowed Sun", tags("time", "tempo"),
                plusXp(),
                minusSeconds()));
        register(CatalystArchetype.TIME, c("ember_lull", "Ember Lull", tags("setup", "safe"),
                plusRarity(),
                minusLoot()));

        register(CatalystArchetype.STAT, c("thickened_hide", "Thickened Hide", tags("stat", "loot"),
                plusQuantity(),
                plusMobs()));
        register(CatalystArchetype.STAT, c("savage_edge", "Savage Edge", tags("stat", "damage"),
                plusXp(),
                plusMobs()));
        register(CatalystArchetype.STAT, c("hunting_wind", "Hunting Wind", tags("stat", "speed"),
                plusRareReward(),
                plusMobs()));
        register(CatalystArchetype.STAT, c("platebreaker", "Platebreaker", tags("stat", "elite"),
                plusRarity(),
                plusWave()));
        register(CatalystArchetype.STAT, c("bloodsalt", "Bloodsalt", tags("stat", "volatile"),
                plusQuantity(),
                minusSeconds()));
        register(CatalystArchetype.STAT, c("sundered_quarry", "Sundered Quarry", tags("stat", "damage"),
                plusXp(),
                minusLoot()));
        register(CatalystArchetype.STAT, c("glass_jaw", "Glass Jaw", tags("stat", "risk"),
                plusSeconds(),
                plusWave()));
        register(CatalystArchetype.STAT, c("witchsteel", "Witchsteel", tags("stat", "ranged"),
                plusRarity(),
                plusMobs()));

        register(CatalystArchetype.LOOT, c("gilded_teeth", "Gilded Teeth", tags("loot", "risk"),
                plusQuantity(),
                plusMobs()));
        register(CatalystArchetype.LOOT, c("treasure_hunger", "Treasure Hunger", tags("loot", "risk"),
                plusRareReward(),
                minusSeconds()));
        register(CatalystArchetype.LOOT, c("rare_bait", "Rare Bait", tags("loot", "elite"),
                plusRarity(),
                plusWave()));
        register(CatalystArchetype.LOOT, c("prize_cache", "Prize Cache", tags("loot", "xp"),
                plusXp(),
                plusMobs()));
        register(CatalystArchetype.LOOT, c("safe_hands", "Safe Hands", tags("loot", "safe"),
                plusSeconds(),
                minusLoot()));
        register(CatalystArchetype.LOOT, c("mercy_clause", "Mercy Clause", tags("loot", "safe"),
                plusQuantity(),
                minusLoot()));
        register(CatalystArchetype.LOOT, c("deep_pockets", "Deep Pockets", tags("loot", "reward"),
                plusRarity(),
                minusWaveCooldown()));
        register(CatalystArchetype.LOOT, c("grave_tribute", "Grave Tribute", tags("loot", "xp"),
                plusRareReward(),
                plusMobs()));

        register(CatalystArchetype.VOLATILE, c("razor_dusk", "Razor Dusk", tags("volatile", "time"),
                plusRareReward(),
                minusSeconds()));
        register(CatalystArchetype.VOLATILE, c("kingmaker", "Kingmaker", tags("volatile", "elite"),
                plusRarity(),
                plusWave()));
        register(CatalystArchetype.VOLATILE, c("war_drum", "War Drum", tags("volatile", "pressure"),
                plusQuantity(),
                plusMobs()));
        register(CatalystArchetype.VOLATILE, c("red_feast", "Red Feast", tags("volatile", "lifesteal"),
                plusXp(),
                minusSeconds()));
        register(CatalystArchetype.VOLATILE, c("abyssal_tax", "Abyssal Tax", tags("volatile", "miniboss"),
                plusRareReward(),
                plusMobs()));
        register(CatalystArchetype.VOLATILE, c("last_bell", "Last Bell", tags("volatile", "finale"),
                plusRarity(),
                minusWaveCooldown()));
        register(CatalystArchetype.VOLATILE, c("black_banner", "Black Banner", tags("volatile", "elite"),
                plusQuantity(),
                plusWave()));
        register(CatalystArchetype.VOLATILE, c("furnace_heart", "Furnace Heart", tags("volatile", "damage"),
                plusXp(),
                minusLoot()));
    }

    private CatalystDefinitionPool() {
    }

    public static CatalystDefinition getById(String id) {
        return BY_ID.get(id);
    }

    public static CatalystDefinition random(CatalystArchetype archetype, RandomSource random) {
        return random(archetype, random, -1);
    }

    public static CatalystDefinition random(CatalystArchetype archetype, RandomSource random, int level) {
        List<CatalystDefinition> entries = BY_ARCHETYPE.getOrDefault(archetype, List.of());
        return materialize(entries.get(random.nextInt(entries.size())), random, level);
    }

    public static CatalystDefinition fallback(CatalystArchetype archetype) {
        return BY_ARCHETYPE.getOrDefault(archetype, List.of()).getFirst();
    }

    public static List<CatalystDefinition> definitionsFor(CatalystArchetype archetype) {
        return List.copyOf(BY_ARCHETYPE.getOrDefault(archetype, List.of()));
    }

    private static void register(CatalystArchetype archetype, CatalystDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_ARCHETYPE.computeIfAbsent(archetype, ignored -> new ArrayList<>()).add(definition);
    }

    private static CatalystDefinition materialize(CatalystDefinition template, RandomSource random, int level) {
        return new CatalystDefinition(
                template.id(),
                template.title(),
                rollPositive(template.positiveEffect(), random, level),
                template.negativeEffect(),
                template.tags());
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

    private static ForgeEffect plusRareReward() {
        return fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward");
    }

    private static ForgeEffect plusRarity() {
        return fx(ForgeEffectType.RARITY_REWARD_MULTIPLIER, 1.10D, "+10% item rarity");
    }

    private static ForgeEffect plusQuantity() {
        return fx(ForgeEffectType.REWARD_MULTIPLIER, 0.05D, "+5% item quantity");
    }

    private static ForgeEffect plusXp() {
        return fx(ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER, 1.07D, "+7% XP");
    }

    private static ForgeEffect plusSeconds() {
        return fx(ForgeEffectType.WAVE_TIME_DELTA, 100, "+5 seconds");
    }

    private static ForgeEffect minusSeconds() {
        return fx(ForgeEffectType.WAVE_TIME_DELTA, -200, "-10 seconds");
    }

    private static ForgeEffect minusLoot() {
        return fx(ForgeEffectType.REWARD_MULTIPLIER, -0.05D, "-5% loot");
    }

    private static ForgeEffect plusMobs() {
        return fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 5, "+5 mobs");
    }

    private static ForgeEffect plusWave() {
        return fx(ForgeEffectType.BONUS_WAVES, 1, "+1 wave");
    }

    private static ForgeEffect minusWaveCooldown() {
        return fx(ForgeEffectType.SETUP_TIME_DELTA, -60, "-3s wave cooldown");
    }

    private static Set<String> tags(String... tags) {
        return Set.of(tags);
    }

    private static ForgeEffect rollPositive(ForgeEffect effect, RandomSource random, int level) {
        return switch (effect.type()) {
            case REWARD_MULTIPLIER -> {
                double value = rollLevelRange(random, level);
                yield ForgeEffect.of(effect.type(), value - 1.0D, "+" + trimPercent((value - 1.0D) * 100.0D) + "% item quantity");
            }
            case RARITY_REWARD_MULTIPLIER -> {
                double value = rollLevelRange(random, level);
                yield ForgeEffect.of(effect.type(), value, "+" + trimPercent((value - 1.0D) * 100.0D) + "% item rarity");
            }
            case LEVEL_XP_MULTIPLIER -> {
                double value = rollLevelRange(random, level);
                yield ForgeEffect.of(effect.type(), value, "+" + trimPercent((value - 1.0D) * 100.0D) + "% levels");
            }
            case EXPERIENCE_REWARD_MULTIPLIER -> {
                double value = rollLevelRange(random, level);
                yield ForgeEffect.of(effect.type(), value, "+" + trimPercent((value - 1.0D) * 100.0D) + "% XP");
            }
            default -> effect;
        };
    }

    private static double rollLevelRange(RandomSource random, int level) {
        double min;
        double max;
        if (level >= 90) {
            min = 10.0D;
            max = 20.0D;
        } else if (level >= 80) {
            min = 6.0D;
            max = 10.0D;
        } else if (level >= 50) {
            min = 5.0D;
            max = 8.0D;
        } else if (level >= 41) {
            min = 3.0D;
            max = 6.0D;
        } else if (level >= 21) {
            min = 2.0D;
            max = 4.0D;
        } else {
            min = 1.5D;
            max = 2.0D;
        }
        return Math.round((min + random.nextDouble() * (max - min)) * 100.0D) / 100.0D;
    }

    private static String trimPercent(double value) {
        String text = String.format(java.util.Locale.ROOT, "%.0f", value);
        return text;
    }
}
