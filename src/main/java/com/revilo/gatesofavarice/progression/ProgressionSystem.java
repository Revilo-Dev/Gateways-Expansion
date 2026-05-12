package com.revilo.gatesofavarice.progression;

public final class ProgressionSystem {

    public static final int BASE_WORKBENCH_UNLOCKED_SLOTS = 4;
    public static final int LEVELS_PER_WORKBENCH_UNLOCK = 5;
    public static final int SHOP_SLOT_LEVEL_STEP = 10;
    public static final double DUNGEON_WAVE_DIFFICULTY_STEP = 0.06D;
    public static final double DUNGEON_LEVEL_DIFFICULTY_STEP = 0.004D;

    private ProgressionSystem() {
    }

    public static int normalizedLevel(int level) {
        return Math.max(1, level);
    }

    public static int workbenchRequiredLevelForSequence(int sequence) {
        if (sequence < BASE_WORKBENCH_UNLOCKED_SLOTS) {
            return 0;
        }
        return (sequence - BASE_WORKBENCH_UNLOCKED_SLOTS + 1) * LEVELS_PER_WORKBENCH_UNLOCK;
    }

    public static int shopRequiredLevel(int slotIndex, int offerMinLevel) {
        int slotRequirement = slotIndex <= 1 ? 0 : (slotIndex - 1) * SHOP_SLOT_LEVEL_STEP;
        return Math.max(slotRequirement, Math.max(0, offerMinLevel));
    }

    public static double dungeonDifficultyScalar(int playerLevel, int waveNumber) {
        int safeLevel = normalizedLevel(playerLevel);
        int safeWave = Math.max(1, waveNumber);
        return 1.0D + safeWave * DUNGEON_WAVE_DIFFICULTY_STEP + safeLevel * DUNGEON_LEVEL_DIFFICULTY_STEP;
    }

    public static int dungeonLootRolls(int playerLevel, int waveNumber, int extraRewardRolls, double quantityModifier) {
        int safeLevel = normalizedLevel(playerLevel);
        int safeWave = Math.max(1, waveNumber);
        int base = 2 + safeWave / 2 + safeLevel / 10;
        int bonus = extraRewardRolls + (int) Math.floor(Math.max(0.0D, quantityModifier) * 2.0D);
        return Math.max(2, base + bonus);
    }

    public static int dungeonCoinReward(int playerLevel, int waveNumber, double quantityModifier) {
        int safeLevel = normalizedLevel(playerLevel);
        int safeWave = Math.max(1, waveNumber);
        int base = 15 + safeWave * 8 + safeLevel * 2;
        return Math.max(5, (int) Math.round(base * (1.0D + Math.max(0.0D, quantityModifier))));
    }

    public static int levelXpFromDungeonKillScore(double killScore) {
        return Math.max(0, (int) Math.round(killScore * 1.35D));
    }
}

