package com.revilo.gatewayexpansion.catalyst;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.RandomSource;

public final class CatalystDefinitionPool {

    private static final Map<String, CatalystDefinition> BY_ID = new LinkedHashMap<>();
    private static final CatalystDefinition UNIVERSAL_TEMPLATE = new CatalystDefinition(
            "gateway_catalyst",
            "Gateway Catalyst",
            ForgeEffect.of(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "+10% item quantity"),
            ForgeEffect.of(ForgeEffectType.MOB_SPAWN_MULTIPLIER, 0.15D, "+15% mob spawns"),
            Set.of("universal"));
    private static final List<CatalystDefinition> UNIVERSAL_DEFINITIONS = List.of(UNIVERSAL_TEMPLATE);

    static {
        BY_ID.put(UNIVERSAL_TEMPLATE.id(), UNIVERSAL_TEMPLATE);
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
        ForgeEffect positive = rollPositive(random, level);
        return new CatalystDefinition(
                UNIVERSAL_TEMPLATE.id(),
                UNIVERSAL_TEMPLATE.title(),
                positive,
                rollNegative(random, positive, level),
                UNIVERSAL_TEMPLATE.tags());
    }

    public static CatalystDefinition fallback(CatalystArchetype archetype) {
        return UNIVERSAL_TEMPLATE;
    }

    public static List<CatalystDefinition> definitionsFor(CatalystArchetype archetype) {
        return UNIVERSAL_DEFINITIONS;
    }

    private static ForgeEffect rollPositive(RandomSource random, int level) {
        int roll = random.nextInt(9);
        return switch (roll) {
            case 0 -> rollRareRewards(random, level);
            case 1 -> rollPositiveSeconds(random, level);
            case 2 -> rollFlatCoinBoost(random, level);
            case 3 -> rollPercentMultiplier(ForgeEffectType.REWARD_MULTIPLIER, random, level, "item quantity");
            case 4 -> rollPercentMultiplier(ForgeEffectType.RARITY_REWARD_MULTIPLIER, random, level, "item rarity");
            case 5 -> rollPercentMultiplier(ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER, random, level, "experience");
            case 6 -> rollPercentMultiplier(ForgeEffectType.LEVEL_XP_MULTIPLIER, random, level, "levels");
            case 7 -> rollPercentMultiplier(ForgeEffectType.COIN_REWARD_MULTIPLIER, random, level, "coins");
            default -> ForgeEffect.of(ForgeEffectType.SETUP_TIME_DELTA, 100, "+5s cooldown");
        };
    }

    private static ForgeEffect rollNegative(RandomSource random, ForgeEffect positive, int level) {
        if (positive.type() == ForgeEffectType.EXTRA_RARE_REWARD_ROLLS
                || positive.type() == ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS
                || positive.type() == ForgeEffectType.EXTRA_LEGENDARY_REWARD_ROLLS) {
            return rewardTradeoffNegative(random, positive, level);
        }

        boolean positiveIsPercentType = isPercentPositiveType(positive.type());
        if (positiveIsPercentType && random.nextFloat() < 0.45F) {
            return crossTaxNegative(random, positive, level);
        }

        int roll = random.nextInt(10);
        return switch (roll) {
            case 0 -> negativeSeconds(random, positive, level);
            case 1 -> percentPenalty(ForgeEffectType.MOB_SPAWN_MULTIPLIER, random, level, "mob spawns");
            case 2 -> percentPenalty(ForgeEffectType.DAMAGE_MULTIPLIER, random, level, "mob damage");
            case 3 -> packPenalty(ForgeEffectType.RANGED_PACKS, random, level, "ranged mobs");
            case 4 -> packPenalty(ForgeEffectType.TANK_PACKS, random, level, "tank mobs");
            case 5 -> packPenalty(ForgeEffectType.HOARD_PACKS, random, level, "hoard mobs");
            case 6 -> packPenalty(ForgeEffectType.ARCHER_PACKS, random, level, "archers");
            case 7 -> percentPenalty(ForgeEffectType.HEALTH_MULTIPLIER, random, level, "mob health");
            case 8 -> thornsPenalty(random, level);
            default -> random.nextBoolean() ? ForgeEffect.of(ForgeEffectType.SETUP_TIME_DELTA, -40, "-2s cooldown") : wavePenalty(random, level);
        };
    }

    private static ForgeEffect rollRareRewards(RandomSource random, int level) {
        RareEpicRoll roll = rollRareEpicRewards(random, level);
        return ForgeEffect.dual(
                ForgeEffectType.EXTRA_RARE_REWARD_ROLLS,
                roll.rareRolls(),
                roll.epicRolls(),
                rareEpicDescription(roll.rareRolls(), roll.epicRolls()));
    }

    private static ForgeEffect rollPositiveSeconds(RandomSource random, int level) {
        int minSeconds = level >= 50 ? 6 : 5;
        int maxSeconds = level >= 70 ? 15 : (level >= 40 ? 12 : 9);
        int seconds = random.nextInt(minSeconds, maxSeconds + 1);
        return ForgeEffect.of(ForgeEffectType.WAVE_TIME_DELTA, seconds * 20.0D, "+" + seconds + " seconds");
    }

    private static ForgeEffect rollFlatCoinBoost(RandomSource random, int level) {
        int minCoins = 100;
        int maxCoins;
        if (level >= 90) {
            maxCoins = 1000;
        } else if (level >= 70) {
            maxCoins = 850;
        } else if (level >= 50) {
            maxCoins = 700;
        } else if (level >= 30) {
            maxCoins = 550;
        } else if (level >= 10) {
            maxCoins = 350;
        } else {
            maxCoins = 200;
        }
        int coins = random.nextInt(minCoins, Math.max(minCoins + 1, maxCoins + 1));
        return ForgeEffect.of(ForgeEffectType.BONUS_COINS_FLAT, coins, "+" + coins + " coins");
    }

    private static ForgeEffect thornsPenalty(RandomSource random, int level) {
        double min = level >= 50 ? 3.0D : 1.0D;
        double max = level >= 90 ? 8.0D : (level >= 50 ? 6.0D : 4.0D);
        double damage = Math.round((min + random.nextDouble() * (max - min)) * 10.0D) / 10.0D;
        return ForgeEffect.of(ForgeEffectType.THORNS_DAMAGE, damage, "Enemies have thorns (" + trimPercent(damage) + " dmg)");
    }

    private static ForgeEffect rollPercentMultiplier(ForgeEffectType type, RandomSource random, int level, String label) {
        double multiplier = rollRewardMultiplier(random, level);
        if (type == ForgeEffectType.REWARD_MULTIPLIER) {
            double additive = multiplier - 1.0D;
            return ForgeEffect.of(type, additive, "+" + trimPercent(additive * 100.0D) + "% " + label);
        }
        return ForgeEffect.of(type, multiplier, "+" + trimPercent((multiplier - 1.0D) * 100.0D) + "% " + label);
    }

    private static ForgeEffect negativeSeconds(RandomSource random, ForgeEffect positive, int level) {
        int minSeconds = 5;
        int maxSeconds = 15;
        if (isHighPowerPositive(positive, level)) {
            minSeconds = 10;
        }
        int seconds = random.nextInt(minSeconds, maxSeconds + 1);
        return ForgeEffect.of(ForgeEffectType.WAVE_TIME_DELTA, -seconds * 20.0D, "-" + seconds + "s seconds");
    }

    private static ForgeEffect percentPenalty(ForgeEffectType type, RandomSource random, int level, String label) {
        double percent = negativePercent(random, level);
        return ForgeEffect.of(type, percent, "+" + trimPercent(percent * 100.0D) + "% " + label);
    }

    private static ForgeEffect packPenalty(ForgeEffectType type, RandomSource random, int level, String label) {
        double percent = negativePercent(random, level);
        int packIncrease = 1 + Math.min(2, (int) Math.floor(percent * 5.0D));
        return ForgeEffect.of(type, packIncrease, "+" + trimPercent(percent * 100.0D) + "% " + label);
    }

    private static ForgeEffect wavePenalty(RandomSource random, int level) {
        int maxWavePenalty = Math.max(1, 1 + Math.max(0, level) / 20);
        int waves = random.nextInt(1, maxWavePenalty + 1);
        return ForgeEffect.of(ForgeEffectType.BONUS_WAVES, waves, "+" + waves + " wave");
    }

    private static ForgeEffect rewardTradeoffNegative(RandomSource random, ForgeEffect positive, int level) {
        boolean lowLevel = level < 40;
        if (lowLevel) {
            if (random.nextBoolean()) {
                int seconds = random.nextInt(2, 6);
                return ForgeEffect.of(ForgeEffectType.WAVE_TIME_DELTA, -seconds * 20.0D, "-" + seconds + "s seconds");
            }
            double spawnPenalty = Math.round((0.08D + random.nextDouble() * 0.12D) * 100.0D) / 100.0D;
            return ForgeEffect.of(ForgeEffectType.MOB_SPAWN_MULTIPLIER, spawnPenalty, "+" + trimPercent(spawnPenalty * 100.0D) + "% mob spawns");
        }

        if (positive.type() == ForgeEffectType.EXTRA_LEGENDARY_REWARD_ROLLS) {
            int waves = random.nextInt(1, 3);
            return ForgeEffect.of(ForgeEffectType.BONUS_WAVES, waves, "+" + waves + " wave");
        }

        if (random.nextFloat() < 0.45F) {
            double damagePenalty = Math.round((0.16D + random.nextDouble() * 0.20D) * 100.0D) / 100.0D;
            return ForgeEffect.of(ForgeEffectType.DAMAGE_MULTIPLIER, damagePenalty, "+" + trimPercent(damagePenalty * 100.0D) + "% mob damage");
        }

        int seconds = random.nextInt(5, 11);
        return ForgeEffect.of(ForgeEffectType.WAVE_TIME_DELTA, -seconds * 20.0D, "-" + seconds + "s seconds");
    }

    private static ForgeEffect crossTaxNegative(RandomSource random, ForgeEffect positive, int level) {
        List<ForgeEffectType> taxPool = List.of(
                ForgeEffectType.REWARD_MULTIPLIER,
                ForgeEffectType.RARITY_REWARD_MULTIPLIER,
                ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER,
                ForgeEffectType.LEVEL_XP_MULTIPLIER,
                ForgeEffectType.COIN_REWARD_MULTIPLIER);
        List<ForgeEffectType> candidates = taxPool.stream().filter(type -> type != positive.type()).toList();
        ForgeEffectType taxType = candidates.get(random.nextInt(candidates.size()));
        double taxMultiplier = negativeMultiplier(random, level);
        String label = switch (taxType) {
            case REWARD_MULTIPLIER -> "loot";
            case RARITY_REWARD_MULTIPLIER -> "rarity";
            case EXPERIENCE_REWARD_MULTIPLIER -> "experience";
            case LEVEL_XP_MULTIPLIER -> "levels";
            case COIN_REWARD_MULTIPLIER -> "coins";
            default -> "reward";
        };
        if (taxType == ForgeEffectType.REWARD_MULTIPLIER) {
            double additive = taxMultiplier - 1.0D;
            return ForgeEffect.of(taxType, additive, trimPercent(additive * 100.0D) + "% " + label);
        }
        return ForgeEffect.of(taxType, taxMultiplier, trimPercent((taxMultiplier - 1.0D) * 100.0D) + "% " + label);
    }

    private static boolean isPercentPositiveType(ForgeEffectType type) {
        return type == ForgeEffectType.REWARD_MULTIPLIER
                || type == ForgeEffectType.RARITY_REWARD_MULTIPLIER
                || type == ForgeEffectType.EXPERIENCE_REWARD_MULTIPLIER
                || type == ForgeEffectType.LEVEL_XP_MULTIPLIER
                || type == ForgeEffectType.COIN_REWARD_MULTIPLIER;
    }

    private static boolean isHighPowerPositive(ForgeEffect positive, int level) {
        if (positive.type() == ForgeEffectType.EXTRA_RARE_REWARD_ROLLS || positive.type() == ForgeEffectType.BONUS_WAVES) {
            return true;
        }
        if (positive.type() == ForgeEffectType.SETUP_TIME_DELTA) {
            return positive.value() >= 100.0D;
        }
        if (positive.type() == ForgeEffectType.WAVE_TIME_DELTA) {
            return positive.value() >= 160.0D;
        }
        if (positive.type() == ForgeEffectType.REWARD_MULTIPLIER) {
            return positive.value() >= 1.0D || level >= 50;
        }
        return positive.value() >= 2.0D || level >= 75;
    }

    private static int rareRewardRollBonus(int level) {
        if (level >= 90) return 6;
        if (level >= 70) return 5;
        if (level >= 50) return 4;
        if (level >= 20) return 3;
        return 2;
    }

    private static int epicRewardRollBonus(int level) {
        if (level >= 90) return 4;
        if (level >= 70) return 3;
        if (level >= 50) return 2;
        return 1;
    }

    private static RareEpicRoll rollRareEpicRewards(RandomSource random, int level) {
        int rareMax = rareRewardRollBonus(level);
        int epicMax = epicRewardRollBonus(level);
        int rareMin = level >= 50 ? 2 : 1;
        int rare = random.nextInt(rareMin, rareMax + 1);
        int epic = random.nextInt(0, epicMax + 1);
        if (rare <= 1 && epic <= 0) {
            epic = 1;
        }
        return new RareEpicRoll(rare, epic);
    }

    private static String rareEpicDescription(int rareRolls, int epicRolls) {
        if (epicRolls <= 0) {
            return "+" + rareRolls + " rare rewards";
        }
        return "+" + rareRolls + " rare rewards, +" + epicRolls + " epic rewards";
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

    private static double negativePercent(RandomSource random, int level) {
        double min = level >= 50 ? 0.20D : 0.12D;
        double max = level >= 90 ? 0.95D : (level >= 50 ? 0.70D : 0.45D);
        return Math.round((min + random.nextDouble() * (max - min)) * 100.0D) / 100.0D;
    }

    private static double negativeMultiplier(RandomSource random, int level) {
        double min = level >= 50 ? 0.05D : 0.20D;
        double max = level >= 90 ? 0.50D : (level >= 50 ? 0.70D : 0.85D);
        return Math.round((min + random.nextDouble() * (max - min)) * 100.0D) / 100.0D;
    }

    private static String trimPercent(double value) {
        return String.format(java.util.Locale.ROOT, "%.0f", value);
    }

    private record RareEpicRoll(int rareRolls, int epicRolls) {
    }
}
