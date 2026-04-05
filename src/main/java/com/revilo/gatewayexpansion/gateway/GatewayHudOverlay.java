package com.revilo.gatewayexpansion.gateway;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
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

        GatewayEntity gateway = null;
        double bestDistance = Double.MAX_VALUE;
        for (GatewayEntity candidate : minecraft.level.getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(96.0D))) {
            if (!candidate.isValid()) {
                continue;
            }

            double distance = player.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                gateway = candidate;
            }
        }
        if (gateway == null) {
            return;
        }

        if (ShopkeeperManager.isGatewayAnimation(gateway)) {
            return;
        }

        int x = event.getGuiGraphics().guiWidth() / 2 - 91;
        int y = 12;
        gateway.getGateway().renderBossBar(gateway, event.getGuiGraphics(), x, y, false);
    }
}
