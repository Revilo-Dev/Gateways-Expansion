package com.revilo.gatesofavarice.integration;

import java.lang.reflect.Method;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class LevelUpIntegration {

    private static final String MOD_ID = "levelup";
    private static final String API_CLASS = "com.revilo.levelup.api.LevelUpApi";
    private static Method getLevelMethod;
    private static Method getProgressToNextLevelMethod;
    private static Method awardXpMethod;
    private static boolean initialized;

    private LevelUpIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static int getPlayerLevel(Player player) {
        if (!isLoaded()) {
            return -1;
        }
        init();
        if (getLevelMethod == null) {
            return -1;
        }
        try {
            return (int) getLevelMethod.invoke(null, player);
        } catch (ReflectiveOperationException ex) {
            return -1;
        }
    }

    public static boolean isCrystalOverleveled(Player player, int crystalLevel) {
        int playerLevel = getPlayerLevel(player);
        return playerLevel >= 0 && crystalLevel > playerLevel;
    }

    public static int getEffectiveLevel(Player player) {
        int integrated = getPlayerLevel(player);
        if (integrated >= 0) {
            return Math.max(1, integrated);
        }
        return Math.max(1, player.experienceLevel);
    }

    public static float getProgressToNextLevel(Player player) {
        if (!isLoaded()) {
            return 0.0F;
        }
        init();
        if (getProgressToNextLevelMethod == null) {
            return 0.0F;
        }
        try {
            return (float) getProgressToNextLevelMethod.invoke(null, player);
        } catch (ReflectiveOperationException ex) {
            return 0.0F;
        }
    }

    public static boolean awardXp(ServerPlayer player, long amount, ResourceLocation source) {
        if (!isLoaded() || amount <= 0L) {
            return false;
        }
        init();
        if (awardXpMethod == null) {
            return false;
        }
        try {
            awardXpMethod.invoke(null, player, amount, source);
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    private static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            getLevelMethod = apiClass.getMethod("getLevel", Player.class);
            getProgressToNextLevelMethod = apiClass.getMethod("getProgressToNextLevel", Player.class);
            awardXpMethod = apiClass.getMethod("awardXp", ServerPlayer.class, long.class, ResourceLocation.class);
        } catch (ReflectiveOperationException ignored) {
            getLevelMethod = null;
            getProgressToNextLevelMethod = null;
            awardXpMethod = null;
        }
    }
}
