package com.revilo.gatewayexpansion.integration;

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
    private static final ResourceLocation GATEWAY_COMPLETE_SOURCE = ResourceLocation.fromNamespaceAndPath("levelup", "gateway_complete");
    private static final ResourceLocation OBJECTIVE_COMPLETE_SOURCE = ResourceLocation.fromNamespaceAndPath("levelup", "objective_complete");
    private static Method awardXpMethod;
    private static boolean initialized;

    private LevelUpGatewayXpRewards() {
    }

    @SubscribeEvent
    public static void onWaveCompleted(GateEvent.WaveEnd event) {
        if (!(event.getEntity().summonerOrClosest() instanceof ServerPlayer player) || !LevelUpIntegration.isLoaded()) {
            return;
        }

        int xp = computeWaveXp(event.getEntity());
        if (xp > 0) {
            awardXp(player, xp, OBJECTIVE_COMPLETE_SOURCE);
        }
    }

    @SubscribeEvent
    public static void onGatewayCompleted(GateEvent.Completed event) {
        if (!(event.getEntity().summonerOrClosest() instanceof ServerPlayer player) || !LevelUpIntegration.isLoaded()) {
            return;
        }

        int xp = computeCompletionXp(event.getEntity());
        if (xp > 0) {
            awardXp(player, xp, GATEWAY_COMPLETE_SOURCE);
        }
    }

    private static int computeWaveXp(GatewayEntity gatewayEntity) {
        Gateway gateway = gatewayEntity.getGateway();
        int level = GatewayForgeService.getGatewayLevel(gateway);
        int tier = Math.max(1, GatewayForgeService.getGatewayCrystalTier(gateway));
        if (level <= 0) {
            level = tier * 20;
        }
        return Math.max(6, (level / 2) + (tier * 8));
    }

    private static int computeCompletionXp(GatewayEntity gatewayEntity) {
        Gateway gateway = gatewayEntity.getGateway();
        int level = GatewayForgeService.getGatewayLevel(gateway);
        int tier = Math.max(1, GatewayForgeService.getGatewayCrystalTier(gateway));
        if (level <= 0) {
            level = tier * 20;
        }
        return Math.max(24, level * 3 + tier * 20);
    }

    private static void awardXp(ServerPlayer player, long amount, ResourceLocation source) {
        init();
        if (awardXpMethod == null) {
            return;
        }
        try {
            awardXpMethod.invoke(null, player, amount, source);
        } catch (ReflectiveOperationException ignored) {
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
        } catch (ReflectiveOperationException ignored) {
            awardXpMethod = null;
        }
    }
}
