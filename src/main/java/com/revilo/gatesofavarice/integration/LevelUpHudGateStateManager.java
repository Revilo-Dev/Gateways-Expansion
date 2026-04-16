package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;

public final class LevelUpHudGateStateManager {

    private static final Set<UUID> ACTIVE_GATES = new HashSet<>();

    private LevelUpHudGateStateManager() {
    }

    @SubscribeEvent
    public static void onGateOpened(GateEvent.Opened event) {
        GatewayEntity gate = event.getEntity();
        if (!shouldTrack(gate)) {
            return;
        }

        if (ACTIVE_GATES.add(gate.getUUID()) && ACTIVE_GATES.size() == 1) {
            runStayOnScreenCommand(gate, true);
        }
    }

    @SubscribeEvent
    public static void onGateCompleted(GateEvent.Completed event) {
        handleGateClosed(event.getEntity());
    }

    @SubscribeEvent
    public static void onGateFailed(GateEvent.Failed event) {
        handleGateClosed(event.getEntity());
    }

    private static void handleGateClosed(GatewayEntity gate) {
        if (!shouldTrack(gate)) {
            return;
        }

        if (ACTIVE_GATES.remove(gate.getUUID()) && ACTIVE_GATES.isEmpty()) {
            runStayOnScreenCommand(gate, false);
        }
    }

    private static boolean shouldTrack(GatewayEntity gate) {
        return gate != null
                && !gate.level().isClientSide
                && LevelUpIntegration.isLoaded()
                && !ShopkeeperManager.isGatewayAnimation(gate);
    }

    private static void runStayOnScreenCommand(GatewayEntity gate, boolean enabled) {
        MinecraftServer server = gate.level().getServer();
        if (server == null) {
            return;
        }

        try {
            CommandSourceStack source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            server.getCommands().performPrefixedCommand(source, "levels hud levelbar stayonscreen " + enabled);
        } catch (Exception ex) {
            GatewayExpansion.LOGGER.warn("Failed to set LevelUp HUD stay-on-screen to {}", enabled, ex);
        }
    }
}
