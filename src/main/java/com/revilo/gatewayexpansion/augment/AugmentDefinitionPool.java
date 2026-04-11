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
    private static final double SECONDARY_REWARD_CHANCE = 0.22D;

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
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
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
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("ashen_stampede", "Ashen Stampede", AugmentDifficultyTier.EXTREME, tags("beast", "nether"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 4, "+4 minions per wave"), fx(ForgeEffectType.SPEED_MULTIPLIER, 0.22D, "+22% speed"), fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Dense encounter")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.25D, "loot range")));
        register(a("deathless_coven", "Deathless Coven", AugmentDifficultyTier.EXTREME, tags("undead", "effect"),
                List.of(effect("minecraft:strength", 0, "Enemies gain Strength"), effect("minecraft:regeneration", 0, "Enemies regenerate"), fx(ForgeEffectType.MINIBOSS_CHANCE, 0.20D, "Occasional miniboss chance")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("shattered_observatory", "Shattered Observatory", AugmentDifficultyTier.EXTREME, tags("arcane", "special"),
                List.of(fx(ForgeEffectType.ARCHER_PACKS, 2, "Archer pressure"), fx(ForgeEffectType.ASSASSIN_PACKS, 1, "Assassin pressure"), effect("minecraft:slowness", 0, "Slowing attacks")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("infernal_overrun", "Infernal Overrun", AugmentDifficultyTier.EXTREME, tags("nether", "finale"),
                List.of(fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.25D, "+25% damage"), fx(ForgeEffectType.ARMOR_PIERCE, 3, "+3 armor pierce"), fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Infernal final wave")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.28D, "loot range")));
        register(a("voidstamp_host", "Voidstamp Host", AugmentDifficultyTier.EXTREME, tags("extreme", "arcane"),
                List.of(fx(ForgeEffectType.HOARD_PACKS, 2, "Hoard pressure"), fx(ForgeEffectType.ELITE_UPGRADE_CHANCE, 0.22D, "Elite upgrades on some waves"), effect("minecraft:glowing", 0, "Glowing enemies")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
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
                rollModifierEffects(template.modifierEffects(), template.difficultyTier(), random, level),
                rollRewardEffects(template.rewardEffects(), template.difficultyTier(), random, level),
                template.tags());
    }

    private static void register(AugmentDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_TIER.computeIfAbsent(definition.difficultyTier(), ignored -> new ArrayList<>()).add(definition);
    }

    private static AugmentDefinition a(String id, String title, AugmentDifficultyTier tier, Set<String> tags, List<ForgeEffect> modifiers, ForgeEffect... rewards) {
        return new AugmentDefinition(id, title, tier, modifiers, List.of(rewards), tags);
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

    private static List<ForgeEffect> rollModifierEffects(List<ForgeEffect> effects, AugmentDifficultyTier tier, RandomSource random, int level) {
        List<ForgeEffect> rolled = new ArrayList<>(effects.size());
        for (ForgeEffect effect : effects) {
            rolled.add(rollModifier(effect, tier, random, level));
        }
        return List.copyOf(rolled);
    }

    private static List<ForgeEffect> rollRewardEffects(List<ForgeEffect> rewards, AugmentDifficultyTier tier, RandomSource random, int level) {
        List<ForgeEffect> rolled = new ArrayList<>(rewards.size() + 1);
        for (ForgeEffect reward : rewards) {
            rolled.add(rollReward(reward, tier, random, level));
        }
        ForgeEffect secondary = maybeRollSecondaryReward(rolled, tier, random, level);
        if (secondary != null) {
            rolled.add(secondary);
        }
        return List.copyOf(rolled);
    }

    private static ForgeEffect rollModifier(ForgeEffect effect, AugmentDifficultyTier tier, RandomSource random, int level) {
        return switch (effect.type()) {
            case DAMAGE_MULTIPLIER -> rollScaledModifier(effect.type(), "damage", tier, random, level);
            case SPEED_MULTIPLIER -> rollScaledModifier(effect.type(), "speed", tier, random, level);
            case HEALTH_MULTIPLIER -> rollScaledModifier(effect.type(), "health", tier, random, level);
            case KNOCKBACK_RESISTANCE -> rollScaledModifier(effect.type(), "knockback resistance", tier, random, level);
            default -> effect;
        };
    }

    private static ForgeEffect rollReward(ForgeEffect reward, AugmentDifficultyTier tier, RandomSource random, int level) {
        double rewardScale = extremeRewardScale(tier, level);
        return switch (reward.type()) {
            case COIN_REWARD_MULTIPLIER -> rangedReward(reward.type(), scaleMultiplier(rollRewardMultiplier(random, level), rewardScale), "coin multiplier");
            case LEVEL_XP_MULTIPLIER -> rangedReward(reward.type(), scaleMultiplier(rollRewardMultiplier(random, level), rewardScale), "level gain");
            case EXPERIENCE_REWARD_MULTIPLIER -> rangedReward(reward.type(), scaleMultiplier(rollRewardMultiplier(random, level), rewardScale), "experience");
            case REWARD_MULTIPLIER -> {
                double lootMultiplier = scaleMultiplier(rollRewardMultiplier(random, level), rewardScale);
                yield rangedReward(reward.type(), lootMultiplier - 1.0D, "loot", lootMultiplier);
            }
            case RARITY_REWARD_MULTIPLIER -> rangedReward(reward.type(), scaleMultiplier(rollRewardMultiplier(random, level), rewardScale), "rarity");
            case EXTRA_RARE_REWARD_ROLLS -> {
                int rareRolls = rareRewardRollBonus(level);
                int epicRolls = epicRewardRollBonus(level);
                String description = "+" + rareRolls + " rare rewards, +" + epicRolls + " epic rewards"
                        + (level >= 40 ? ", +1 legendary reward" : "");
                yield ForgeEffect.dual(reward.type(), rareRolls, epicRolls, description);
            }
            case EXTRA_FINAL_REWARD_ROLLS -> {
                int epicRolls = epicRewardRollBonus(level);
                yield ForgeEffect.of(reward.type(), epicRolls, "+" + epicRolls + " epic rewards");
            }
            case EXTRA_LEGENDARY_REWARD_ROLLS ->
                    ForgeEffect.of(reward.type(), level >= 40 ? 1.0D : 0.0D, level >= 40 ? "+1 legendary reward" : "Legendary reward unlocks at level 40");
            case EXTRA_ENTITY_LOOT_ROLLS ->
                    ForgeEffect.of(reward.type(), Math.max(1.0D, Math.round(reward.value() * rewardScale)), "+" + (int) Math.max(1.0D, Math.round(reward.value() * rewardScale)) + rewardSuffix(reward.type()));
            default -> reward;
        };
    }

    private static ForgeEffect maybeRollSecondaryReward(List<ForgeEffect> existingRewards, AugmentDifficultyTier tier, RandomSource random, int level) {
        if (level < 10 || random.nextDouble() >= secondaryRewardChance(tier, level)) {
            return null;
        }

        List<ForgeEffectType> pool = List.of(
                ForgeEffectType.REWARD_MULTIPLIER,
                ForgeEffectType.RARITY_REWARD_MULTIPLIER,
                ForgeEffectType.COIN_REWARD_MULTIPLIER,
                ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER);
        ForgeEffectType type = pool.get(random.nextInt(pool.size()));
        for (ForgeEffect reward : existingRewards) {
            if (reward.type() == type) {
                type = ForgeEffectType.REWARD_MULTIPLIER;
                break;
            }
        }

        return switch (type) {
            case REWARD_MULTIPLIER -> {
                double quantityMultiplier = scaleMultiplier(rollRewardMultiplier(random, level), secondaryRewardScale(tier, level));
                yield rangedReward(type, quantityMultiplier - 1.0D, "item quantity", quantityMultiplier);
            }
            case RARITY_REWARD_MULTIPLIER -> {
                double rarityMultiplier = scaleMultiplier(rollRewardMultiplier(random, level), secondaryRewardScale(tier, level));
                yield rangedReward(type, rarityMultiplier, "item rarity");
            }
            case COIN_REWARD_MULTIPLIER -> {
                double coinMultiplier = scaleMultiplier(rollRewardMultiplier(random, level), secondaryRewardScale(tier, level));
                yield rangedReward(type, coinMultiplier, "coin multiplier");
            }
            case EXPERIENCE_REWARD_MULTIPLIER -> {
                double experienceMultiplier = scaleMultiplier(rollRewardMultiplier(random, level), secondaryRewardScale(tier, level));
                yield rangedReward(type, experienceMultiplier, "experience");
            }
            default -> null;
        };
    }

    private static ForgeEffect rollScaledModifier(ForgeEffectType type, String noun, AugmentDifficultyTier tier, RandomSource random, int level) {
        Range range = modifierRange(type, tier, level);
        double value = roundPercent(range.min() + random.nextDouble() * (range.max() - range.min()));
        return ForgeEffect.of(type, value, "+" + percent(value) + " " + noun);
    }

    private static Range modifierRange(ForgeEffectType type, AugmentDifficultyTier tier, int level) {
        if (tier == AugmentDifficultyTier.EXTREME && level >= 50) {
            return switch (type) {
                case DAMAGE_MULTIPLIER -> new Range(0.60D, 0.90D);
                case SPEED_MULTIPLIER -> new Range(0.08D, 0.14D);
                case HEALTH_MULTIPLIER -> new Range(0.55D, 0.85D);
                case KNOCKBACK_RESISTANCE -> new Range(0.50D, 0.75D);
                default -> new Range(0.0D, 0.0D);
            };
        }

        return switch (type) {
            case DAMAGE_MULTIPLIER -> levelBandRange(level,
                    new Range(0.05D, 0.10D),
                    new Range(0.08D, 0.16D),
                    new Range(0.15D, 0.30D),
                    new Range(0.22D, 0.42D),
                    new Range(0.30D, 0.60D),
                    new Range(0.40D, 0.75D));
            case SPEED_MULTIPLIER -> levelBandRange(level,
                    new Range(0.02D, 0.06D),
                    new Range(0.03D, 0.08D),
                    new Range(0.04D, 0.10D),
                    new Range(0.05D, 0.12D),
                    new Range(0.06D, 0.14D),
                    new Range(0.08D, 0.16D));
            case HEALTH_MULTIPLIER -> levelBandRange(level,
                    new Range(0.08D, 0.15D),
                    new Range(0.12D, 0.22D),
                    new Range(0.18D, 0.32D),
                    new Range(0.22D, 0.40D),
                    new Range(0.28D, 0.55D),
                    new Range(0.36D, 0.68D));
            case KNOCKBACK_RESISTANCE -> levelBandRange(level,
                    new Range(0.05D, 0.12D),
                    new Range(0.08D, 0.18D),
                    new Range(0.12D, 0.28D),
                    new Range(0.18D, 0.36D),
                    new Range(0.25D, 0.50D),
                    new Range(0.32D, 0.60D));
            default -> new Range(0.0D, 0.0D);
        };
    }

    private static Range levelBandRange(int level, Range low, Range early, Range mid, Range late, Range high, Range endgame) {
        if (level <= 10) return low;
        if (level <= 20) return early;
        if (level <= 35) return mid;
        if (level <= 49) return late;
        if (level <= 75) return high;
        return endgame;
    }

    private static double scaleMultiplier(double multiplier, double rewardScale) {
        return Math.round((1.0D + ((multiplier - 1.0D) * rewardScale)) * 100.0D) / 100.0D;
    }

    private static double extremeRewardScale(AugmentDifficultyTier tier, int level) {
        return tier == AugmentDifficultyTier.EXTREME && level >= 50 ? 2.0D : 1.0D;
    }

    private static double secondaryRewardScale(AugmentDifficultyTier tier, int level) {
        return tier == AugmentDifficultyTier.EXTREME && level >= 50 ? 1.5D : 1.0D;
    }

    private static double secondaryRewardChance(AugmentDifficultyTier tier, int level) {
        double base = switch (tier) {
            case EASY -> 0.08D;
            case MEDIUM -> 0.12D;
            case HARD -> 0.18D;
            case EXTREME -> SECONDARY_REWARD_CHANCE;
        };
        if (tier == AugmentDifficultyTier.EXTREME && level >= 50) {
            base += 0.10D;
        }
        return Math.min(0.45D, base + Math.min(0.10D, level / 500.0D));
    }

    private static String rewardSuffix(ForgeEffectType type) {
        return switch (type) {
            case EXTRA_RARE_REWARD_ROLLS -> " rare reward rolls";
            case EXTRA_FINAL_REWARD_ROLLS -> " final reward rolls";
            case EXTRA_LEGENDARY_REWARD_ROLLS -> " legendary reward rolls";
            case EXTRA_ENTITY_LOOT_ROLLS -> " entity loot rolls";
            default -> " reward rolls";
        };
    }

    private static int rareRewardRollBonus(int level) {
        if (level >= 90) return 5;
        if (level >= 70) return 4;
        if (level >= 50) return 3;
        if (level >= 20) return 2;
        return 1;
    }

    private static int epicRewardRollBonus(int level) {
        if (level >= 90) return 3;
        if (level >= 50) return 2;
        return 1;
    }

    private static ForgeEffect rangedReward(ForgeEffectType type, double value, String noun) {
        return rangedReward(type, value, noun, value);
    }

    private static ForgeEffect rangedReward(ForgeEffectType type, double storedValue, String noun, double displayedMultiplier) {
        return ForgeEffect.of(type, storedValue, "x" + trim(displayedMultiplier) + " " + noun);
    }

    private static double rollRewardMultiplier(RandomSource random, int level) {
        double min;
        double max;
        if (level >= 90) {
            min = 2.0D;
            max = 15.0D;
        } else if (level >= 75) {
            min = 2.0D;
            max = 10.0D;
        } else if (level >= 51) {
            min = 1.5D;
            max = 5.0D;
        } else if (level >= 31) {
            min = 1.5D;
            max = 4.0D;
        } else if (level >= 15) {
            min = 1.5D;
            max = 3.0D;
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

    private static double roundPercent(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private static int percent(double value) {
        return (int) Math.round(value * 100.0D);
    }

    private record Range(double min, double max) {
    }
}
