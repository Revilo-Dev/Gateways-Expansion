package com.revilo.gatewayexpansion.gateway;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class GatewayHudOverlay {

    private GatewayHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui) {
            return;
        }

        GatewayEntity gateway = minecraft.level.getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(96.0D))
                .stream()
                .filter(GatewayEntity::isValid)
                .min(java.util.Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (gateway == null) {
            return;
        }

        int x = event.getGuiGraphics().guiWidth() / 2 - 91;
        int y = 12;
        gateway.getGateway().renderBossBar(gateway, event.getGuiGraphics(), x, y, false);
    }
}
