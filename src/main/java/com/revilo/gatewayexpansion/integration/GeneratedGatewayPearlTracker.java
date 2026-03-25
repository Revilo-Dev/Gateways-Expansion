package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class GeneratedGatewayPearlTracker {

    private GeneratedGatewayPearlTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) {
            return;
        }

        boolean restored = false;
        for (ItemStack stack : player.getInventory().items) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack);
        }
        if (restored) {
            GatewayForgeService.syncGatewayRegistry(player);
        }
    }
}
