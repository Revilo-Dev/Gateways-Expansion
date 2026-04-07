package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import dev.shadowsoffire.gateways.gate.Gateway;
import java.lang.reflect.Method;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public final class LevelUpGatewayXpRewards {

    private static final String API_CLASS = "com.revilo.levelup.api.LevelUpApi";
    private static final String SOURCES_CLASS = "com.revilo.levelup.api.LevelUpSources";
    private static final ResourceLocation GATEWAY_COMPLETE_SOURCE_FALLBACK = ResourceLocation.fromNamespaceAndPath("levelup", "gateway_complete");
    private static final ResourceLocation OBJECTIVE_COMPLETE_SOURCE_FALLBACK = ResourceLocation.fromNamespaceAndPath("levelup", "objective_complete");
    private static Method awardXpMethod;
    private static ResourceLocation gatewayCompleteSource = GATEWAY_COMPLETE_SOURCE_FALLBACK;
    private static ResourceLocation objectiveCompleteSource = OBJECTIVE_COMPLETE_SOURCE_FALLBACK;
    private static boolean initialized;

    private LevelUpGatewayXpRewards() {
    }

    @SubscribeEvent
    public static void onWaveCompleted(GateEvent.WaveEnd event) {
        ServerPlayer player = findRewardPlayer(event.getEntity());
        if (player == null || !LevelUpIntegration.isLoaded()) {
            return;
        }

        int xp = computeWaveXp(event.getEntity());
        if (xp > 0) {
            awardXp(player, xp, objectiveCompleteSource);
        }
    }

    @SubscribeEvent
    public static void onGatewayCompleted(GateEvent.Completed event) {
        ServerPlayer player = findRewardPlayer(event.getEntity());
        if (player == null || !LevelUpIntegration.isLoaded()) {
            return;
        }

        int xp = computeCompletionXp(event.getEntity());
        if (xp > 0) {
            awardXp(player, xp, gatewayCompleteSource);
        }
    }

    private static int computeWaveXp(GatewayEntity gatewayEntity) {
        Gateway gateway = gatewayEntity.getGateway();
        int level = GatewayForgeService.getGatewayLevel(gateway);
        int tier = Math.max(1, GatewayForgeService.getGatewayCrystalTier(gateway));
        if (level <= 0) {
            level = tier * 20;
        }
        double levelMultiplier = GatewayForgeService.getGatewayLevelXpMultiplier(gateway);
        double experienceMultiplier = GatewayForgeService.getGatewayExperienceRewardMultiplier(gateway);
        int baseXp = Math.max(24, level * 10 + tier * 28);
        return Math.max(24, (int) Math.round(baseXp * levelMultiplier * experienceMultiplier));
    }

    private static int computeCompletionXp(GatewayEntity gatewayEntity) {
        Gateway gateway = gatewayEntity.getGateway();
        int level = GatewayForgeService.getGatewayLevel(gateway);
        int tier = Math.max(1, GatewayForgeService.getGatewayCrystalTier(gateway));
        if (level <= 0) {
            level = tier * 20;
        }
        double levelMultiplier = GatewayForgeService.getGatewayLevelXpMultiplier(gateway);
        double experienceMultiplier = GatewayForgeService.getGatewayExperienceRewardMultiplier(gateway);
        int baseXp = Math.max(96, level * 26 + tier * 84);
        return Math.max(96, (int) Math.round(baseXp * levelMultiplier * experienceMultiplier));
    }

    private static void awardXp(ServerPlayer player, long amount, ResourceLocation source) {
        init();
        if (awardXpMethod == null) {
            return;
        }
        try {
            awardXpMethod.invoke(null, player, amount, source);
        } catch (ReflectiveOperationException ex) {
            GatewayExpansion.LOGGER.warn("Failed to award LevelUp XP {} with source {}", amount, source, ex);
        }
    }

    private static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            awardXpMethod = apiClass.getMethod("awardXp", ServerPlayer.class, long.class, ResourceLocation.class);
            Class<?> sourcesClass = Class.forName(SOURCES_CLASS);
            gatewayCompleteSource = (ResourceLocation) sourcesClass.getField("GATEWAY_COMPLETE").get(null);
            objectiveCompleteSource = (ResourceLocation) sourcesClass.getField("OBJECTIVE_COMPLETE").get(null);
        } catch (ReflectiveOperationException ignored) {
            awardXpMethod = null;
            gatewayCompleteSource = GATEWAY_COMPLETE_SOURCE_FALLBACK;
            objectiveCompleteSource = OBJECTIVE_COMPLETE_SOURCE_FALLBACK;
        }
    }

    private static ServerPlayer findRewardPlayer(GatewayEntity gatewayEntity) {
        if (gatewayEntity.summonerOrClosest() instanceof ServerPlayer player) {
            return player;
        }
        return gatewayEntity.level().getNearestPlayer(gatewayEntity, 64.0D) instanceof ServerPlayer player ? player : null;
    }
}
