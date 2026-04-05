package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class GeneratedGatewayPearlTracker {

    private GeneratedGatewayPearlTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) {
            return;
        }

        boolean restored = restoreFromInventory(player);
        if (restored) {
            GatewayForgeService.syncGatewayRegistry(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && restoreFromInventory(player)) {
            GatewayForgeService.syncGatewayRegistry(player);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level == level.getServer().overworld()) {
            GatewayForgeService.restorePersistedGateways(level);
        }
    }

    private static boolean restoreFromInventory(ServerPlayer player) {
        boolean restored = false;
        for (ItemStack stack : player.getInventory().items) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        for (ItemStack stack : player.getInventory().armor) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        for (ItemStack stack : player.getInventory().offhand) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        return restored;
    }
}
