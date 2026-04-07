package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class AugmentDefinitionPool {

    private static final Map<String, AugmentDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<AugmentDifficultyTier, List<AugmentDefinition>> BY_TIER = new LinkedHashMap<>();

    static {
        register(a("grave_tide", "Grave Tide", AugmentDifficultyTier.EASY,  tags("population", "undead"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 2, "+2 minions per wave")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "loot range")));
        register(a("swarm_wake", "Swarm Wake", AugmentDifficultyTier.EASY, tags("population", "swarm"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 3, "+3 minions per wave")),
                fx(ForgeEffectType.COIN_REWARD_MULTIPLIER, 1.5D, "coin range")));
        register(a("pack_call", "Pack Call", AugmentDifficultyTier.EASY, tags("support", "beast"),
                List.of(dual(ForgeEffectType.SUPPORT_PACK_EVERY, 2, 1, "+1 support mob every 2 waves")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.12D, "loot range")));
        register(a("pressure_spike", "Pressure Spike", AugmentDifficultyTier.EASY, tags("tempo"),
                List.of(fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Shorter gateway, denser waves")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll")));
        register(a("march_of_bones", "March of Bones", AugmentDifficultyTier.EASY, tags("wave", "undead"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 1, "+1 wave")),
                fx(ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER, 3.0D, "experience range")));
        register(a("acid_fangs", "Acid Fangs", AugmentDifficultyTier.EASY, tags("effect", "beast"),
                List.of(effect("minecraft:poison", 0, "+Poison pressure")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "loot range")));
        register(a("hex_surge", "Hex Surge", AugmentDifficultyTier.EASY, tags("effect", "arcane"),
                List.of(effect("minecraft:glowing", 0, "+Glowing enemies")),
                fx(ForgeEffectType.RARITY_REWARD_MULTIPLIER, 1.1D, "rarity range")));
        register(a("cinder_rush", "Cinder Rush", AugmentDifficultyTier.EASY, tags("speed", "nether"),
                List.of(fx(ForgeEffectType.SPEED_MULTIPLIER, 0.10D, "+10% movement speed")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "loot range")));
        register(a("grave_drift", "Grave Drift", AugmentDifficultyTier.EASY, tags("undead", "time"),
                List.of(fx(ForgeEffectType.WAVE_TIME_DELTA, -40, "-2s wave time"), fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 1, "+1 minion per wave")),
                fx(ForgeEffectType.LEVEL_XP_MULTIPLIER, 2.3D, "level range")));
        register(a("ember_watch", "Ember Watch", AugmentDifficultyTier.EASY, tags("nether", "effect"),
                List.of(effect("minecraft:fire_resistance", 0, "Enemies gain fire resistance")),
                fx(ForgeEffectType.COIN_REWARD_MULTIPLIER, 1.5D, "coin range")));

        register(a("late_harvest", "Late Harvest", AugmentDifficultyTier.MEDIUM, tags("wave", "reward"),
                List.of(fx(ForgeEffectType.EXTEND_LATE_WAVES, 50, "Extended later waves")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.20D, "loot range")));
        register(a("bolstered_dead", "Bolstered Dead", AugmentDifficultyTier.MEDIUM, tags("health", "undead"),
                List.of(fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.18D, "+18% health")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.15D, "loot range")));
        register(a("ambush_lines", "Ambush Lines", AugmentDifficultyTier.MEDIUM, tags("ranged", "arcane"),
                List.of(fx(ForgeEffectType.RANGED_PACKS, 1, "More ranged enemies"), fx(ForgeEffectType.PROJECTILE_DAMAGE, 0.15D, "+15% ranged damage")),
                fx(ForgeEffectType.RARITY_REWARD_MULTIPLIER, 1.12D, "rarity range")));
        register(a("predator_stride", "Predator Stride", AugmentDifficultyTier.MEDIUM, tags("speed", "beast"),
                List.of(fx(ForgeEffectType.SPEED_MULTIPLIER, 0.15D, "+15% speed"), effect("minecraft:speed", 0, "Enemies gain Speed")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.15D, "loot range")));
        register(a("iron_advance", "Iron Advance", AugmentDifficultyTier.MEDIUM, tags("elite", "armor"),
                List.of(fx(ForgeEffectType.ARMOR_BONUS, 4, "+4 armor"), fx(ForgeEffectType.ELITE_CHANCE, 0.08D, "+8% elite chance")),
                fx(ForgeEffectType.LEVEL_XP_MULTIPLIER, 2.3D, "level range")));
        register(a("grave_command", "Grave Command", AugmentDifficultyTier.MEDIUM, tags("elite", "undead"),
                List.of(fx(ForgeEffectType.ELITE_EVERY, 2, "+1 elite every 2 waves")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("chaos_breach", "Chaos Breach", AugmentDifficultyTier.MEDIUM, tags("chaos"),
                List.of(fx(ForgeEffectType.MIXED_PACKS, 1, "Mixed enemy pool additions"), fx(ForgeEffectType.THEMED_REINFORCEMENTS, 1, "Random reinforcements")),
                fx(ForgeEffectType.RARITY_REWARD_MULTIPLIER, 1.12D, "rarity range")));
        register(a("burning_raiders", "Burning Raiders", AugmentDifficultyTier.MEDIUM, tags("nether", "damage"),
                List.of(effect("minecraft:fire_resistance", 0, "Enemies resist fire"), fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.15D, "+15% damage")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.18D, "loot range")));
        register(a("ritual_surge", "Ritual Surge", AugmentDifficultyTier.MEDIUM, tags("undead", "elite"),
                List.of(fx(ForgeEffectType.ELITE_CHANCE, 0.08D, "+8% elite chance"), effect("minecraft:strength", 0, "Enemies gain Strength")),
                fx(ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER, 3.0D, "experience range")));
        register(a("starved_pack", "Starved Pack", AugmentDifficultyTier.MEDIUM, tags("beast", "support"),
                List.of(dual(ForgeEffectType.SUPPORT_PACK_EVERY, 3, 2, "+2 support mobs every 3 waves"), fx(ForgeEffectType.SPEED_MULTIPLIER, 0.10D, "+10% speed")),
                fx(ForgeEffectType.COIN_REWARD_MULTIPLIER, 1.5D, "coin range")));

        register(a("siege_lords", "Siege Lords", AugmentDifficultyTier.HARD, tags("elite", "finale"),
                List.of(fx(ForgeEffectType.FINAL_WAVE_ELITES, 2, "+2 stronger final wave units"), fx(ForgeEffectType.ELITE_CHANCE, 0.12D, "+12% elite chance")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("reaper_arms", "Reaper Arms", AugmentDifficultyTier.HARD, tags("damage", "undead"),
                List.of(fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.22D, "+22% damage"), fx(ForgeEffectType.ARMOR_PIERCE, 2, "+2 armor pierce")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.20D, "loot range")));
        register(a("unyielding_hide", "Unyielding Hide", AugmentDifficultyTier.HARD, tags("beast", "defense"),
                List.of(fx(ForgeEffectType.KNOCKBACK_RESISTANCE, 0.25D, "+25% knockback resistance"), fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.20D, "+20% health")),
                fx(ForgeEffectType.LEVEL_XP_MULTIPLIER, 2.3D, "level range")));
        register(a("void_wardens", "Void Wardens", AugmentDifficultyTier.HARD, tags("arcane", "elite"),
                List.of(effect("minecraft:regeneration", 0, "Enemies regenerate"), fx(ForgeEffectType.ELITE_UPGRADE_CHANCE, 0.18D, "Elite upgrades on some waves")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("hellbound_march", "Hellbound March", AugmentDifficultyTier.HARD, tags("nether", "wave"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 2, "+2 waves")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("venom_furnace", "Venom Furnace", AugmentDifficultyTier.HARD, tags("effect", "damage"),
                List.of(effect("minecraft:poison", 1, "Stronger poison"), fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.18D, "+18% damage")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.18D, "loot range")));
        register(a("blood_tithe", "Blood Tithe", AugmentDifficultyTier.HARD, tags("lifesteal"),
                List.of(fx(ForgeEffectType.LIFE_STEAL, 0.10D, "+10% life steal")),
                fx(ForgeEffectType.COIN_REWARD_MULTIPLIER, 1.75D, "coin range")));
        register(a("crossfire_choir", "Crossfire Choir", AugmentDifficultyTier.HARD, tags("ranged", "arcane"),
                List.of(fx(ForgeEffectType.RANGED_PACKS, 2, "Heavy ranged pressure"), fx(ForgeEffectType.PROJECTILE_DAMAGE, 0.25D, "+25% ranged damage")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.22D, "loot range")));
        register(a("crypt_bulwark", "Crypt Bulwark", AugmentDifficultyTier.HARD, tags("undead", "defense"),
                List.of(fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.16D, "+16% health"), fx(ForgeEffectType.ARMOR_BONUS, 5, "+5 armor"), effect("minecraft:resistance", 0, "Enemies gain Resistance")),
                fx(ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER, 2.2D, "experience range")));
        register(a("storm_lattice", "Storm Lattice", AugmentDifficultyTier.HARD, tags("arcane", "chaos"),
                List.of(fx(ForgeEffectType.RANGED_PACKS, 1, "More ranged enemies"), fx(ForgeEffectType.MIXED_PACKS, 2, "Mixed enemy additions"), effect("minecraft:glowing", 0, "Glowing enemies")),
                fx(ForgeEffectType.RARITY_REWARD_MULTIPLIER, 1.15D, "rarity range")));

        register(a("cataclysm_engine", "Cataclysm Engine", AugmentDifficultyTier.EXTREME, tags("extreme", "chaos"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 2, "+2 waves"), fx(ForgeEffectType.THEMED_REINFORCEMENTS, 2, "Heavy random reinforcements"), fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Dangerous final wave bonus")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("nightmare_legion", "Nightmare Legion", AugmentDifficultyTier.EXTREME, tags("extreme", "elite"),
                List.of(fx(ForgeEffectType.ELITE_EVERY, 1, "+1 elite every wave"), fx(ForgeEffectType.FINAL_WAVE_ELITES, 3, "+3 final wave elites"), fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.28D, "+28% health")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("ashen_stampede", "Ashen Stampede", AugmentDifficultyTier.EXTREME, tags("beast", "nether"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 4, "+4 minions per wave"), fx(ForgeEffectType.SPEED_MULTIPLIER, 0.22D, "+22% speed"), fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Dense encounter")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.25D, "loot range")));
        register(a("deathless_coven", "Deathless Coven", AugmentDifficultyTier.EXTREME, tags("undead", "effect"),
                List.of(effect("minecraft:strength", 0, "Enemies gain Strength"), effect("minecraft:regeneration", 0, "Enemies regenerate"), fx(ForgeEffectType.MINIBOSS_CHANCE, 0.20D, "Occasional miniboss chance")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("shattered_observatory", "Shattered Observatory", AugmentDifficultyTier.EXTREME, tags("arcane", "special"),
                List.of(fx(ForgeEffectType.NAMED_ELITE_CHANCE, 0.30D, "Named enemy injection"), fx(ForgeEffectType.RANGED_PACKS, 2, "Arcane ranged pressure"), effect("minecraft:slowness", 0, "Slowing attacks")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("infernal_overrun", "Infernal Overrun", AugmentDifficultyTier.EXTREME, tags("nether", "finale"),
                List.of(fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.25D, "+25% damage"), fx(ForgeEffectType.ARMOR_PIERCE, 3, "+3 armor pierce"), fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Infernal final wave")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.28D, "loot range")));
        register(a("voidstamp_host", "Voidstamp Host", AugmentDifficultyTier.EXTREME, tags("extreme", "arcane"),
                List.of(fx(ForgeEffectType.NAMED_ELITE_CHANCE, 0.24D, "Named enemies appear"), fx(ForgeEffectType.ELITE_UPGRADE_CHANCE, 0.22D, "Elite upgrades on some waves"), effect("minecraft:glowing", 0, "Glowing enemies")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("warpit_maw", "Warpit Maw", AugmentDifficultyTier.EXTREME, tags("extreme", "beast"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 3, "+3 minions per wave"), dual(ForgeEffectType.SUPPORT_PACK_EVERY, 2, 2, "+2 support mobs every 2 waves"), fx(ForgeEffectType.SPEED_MULTIPLIER, 0.18D, "+18% speed")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.24D, "loot range")));
    }

    private AugmentDefinitionPool() {
    }

    public static AugmentDefinition getById(String id) {
        return BY_ID.get(id);
    }

    public static AugmentDefinition random(AugmentDifficultyTier tier, RandomSource random) {
        return random(tier, random, -1);
    }

    public static AugmentDefinition random(AugmentDifficultyTier tier, RandomSource random, int level) {
        List<AugmentDefinition> entries = BY_TIER.getOrDefault(tier, List.of());
        return materialize(entries.get(random.nextInt(entries.size())), random, level);
    }

    public static AugmentDefinition fallback(AugmentDifficultyTier tier) {
        return BY_TIER.getOrDefault(tier, List.of()).getFirst();
    }

    public static List<AugmentDefinition> definitionsFor(AugmentDifficultyTier tier) {
        return List.copyOf(BY_TIER.getOrDefault(tier, List.of()));
    }

    private static AugmentDefinition materialize(AugmentDefinition template, RandomSource random, int level) {
        return new AugmentDefinition(
                template.id(),
                template.title(),
                template.difficultyTier(),
                template.modifierEffects(),
                rollReward(template.rewardEffect(), template.difficultyTier(), random, level),
                template.tags());
    }

    private static void register(AugmentDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_TIER.computeIfAbsent(definition.difficultyTier(), ignored -> new ArrayList<>()).add(definition);
    }

    private static AugmentDefinition a(String id, String title, AugmentDifficultyTier tier, Set<String> tags, List<ForgeEffect> modifiers, ForgeEffect reward) {
        return new AugmentDefinition(id, title, tier, modifiers, reward, tags);
    }

    private static ForgeEffect fx(ForgeEffectType type, double value, String description) {
        return ForgeEffect.of(type, value, description);
    }

    private static ForgeEffect dual(ForgeEffectType type, double value, double secondaryValue, String description) {
        return ForgeEffect.dual(type, value, secondaryValue, description);
    }

    private static ForgeEffect effect(String effectId, int amplifier, String description) {
        return ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.parse(effectId), amplifier, 0.0D, description);
    }

    private static ForgeEffect rollReward(ForgeEffect reward, AugmentDifficultyTier tier, RandomSource random, int level) {
        return switch (reward.type()) {
            case COIN_REWARD_MULTIPLIER -> rangedReward(reward.type(), rollLevelRange(random, level), "coin multiplier");
            case LEVEL_XP_MULTIPLIER -> rangedReward(reward.type(), rollLevelRange(random, level), "level gain");
            case EXPERIENCE_REWARD_MULTIPLIER -> rangedReward(reward.type(), rollLevelRange(random, level), "experience");
            case REWARD_MULTIPLIER -> {
                double lootMultiplier = rollLevelRange(random, level);
                yield rangedReward(reward.type(), lootMultiplier - 1.0D, "loot", lootMultiplier);
            }
            case RARITY_REWARD_MULTIPLIER -> rangedReward(reward.type(), rollLevelRange(random, level), "rarity");
            default -> reward;
        };
    }

    private static ForgeEffect rangedReward(ForgeEffectType type, double value, String noun) {
        return rangedReward(type, value, noun, value);
    }

    private static ForgeEffect rangedReward(ForgeEffectType type, double storedValue, String noun, double displayedMultiplier) {
        return ForgeEffect.of(type, storedValue, "x" + trim(displayedMultiplier) + " " + noun);
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

    private static double rollRange(RandomSource random, AugmentDifficultyTier tier, double easyMin, double easyMax, double mediumMin, double mediumMax, double hardMin, double hardMax, double extremeMin, double extremeMax) {
        double min;
        double max;
        switch (tier) {
            case EASY -> {
                min = easyMin;
                max = easyMax;
            }
            case MEDIUM -> {
                min = mediumMin;
                max = mediumMax;
            }
            case HARD -> {
                min = hardMin;
                max = hardMax;
            }
            case EXTREME -> {
                min = extremeMin;
                max = extremeMax;
            }
            default -> throw new IllegalStateException("Unexpected tier: " + tier);
        }
        return Math.round((min + random.nextDouble() * (max - min)) * 100.0D) / 100.0D;
    }

    private static String trim(double value) {
        String text = String.format(java.util.Locale.ROOT, "%.2f", value);
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static Set<String> tags(String... tags) {
        return Set.of(tags);
    }
}
