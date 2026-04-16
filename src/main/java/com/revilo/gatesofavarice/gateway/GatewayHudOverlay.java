package com.revilo.gatesofavarice.gateway;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class GatewayHudOverlay {

    private static boolean showingGatewayStatus;

    private GatewayHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui) {
            clearGatewayStatus(minecraft);
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
            clearGatewayStatus(minecraft);
            return;
        }

        if (ShopkeeperManager.isGatewayAnimation(gateway)) {
            clearGatewayStatus(minecraft);
            return;
        }

        minecraft.gui.setOverlayMessage(buildGatewayStatus(gateway), false);
        showingGatewayStatus = true;
    }

    private static Component buildGatewayStatus(GatewayEntity gateway) {
        int timeRemaining = gateway.isWaveActive()
                ? Math.max(0, gateway.getMaxWaveTime() - gateway.getTicksActive())
                : Math.max(0, gateway.getSetupTime() - gateway.getTicksActive());
        int waveNumber = gateway.getWave() + 1;
        int totalWaves = gateway.getGateway() instanceof NormalGateway normalGateway ? normalGateway.getNumWaves() : waveNumber;
        int enemies = gateway.getActiveEnemies();
        MutableComponent timer = Component.literal(formatTime(timeRemaining)).withStyle(style -> style.withColor(lowTimeColor(gateway, timeRemaining)));
        return timer
                .append(Component.literal(" | ").withStyle(style -> style.withColor(0xAAAAAA)))
                .append(Component.literal("Wave " + waveNumber + "/" + totalWaves).withStyle(style -> style.withColor(0xFFFFFF)))
                .append(Component.literal(" | ").withStyle(style -> style.withColor(0xAAAAAA)))
                .append(Component.literal(enemies + " enemies").withStyle(style -> style.withColor(0xFFFFFF)));
    }

    private static void clearGatewayStatus(Minecraft minecraft) {
        if (showingGatewayStatus) {
            minecraft.gui.setOverlayMessage(Component.empty(), false);
            showingGatewayStatus = false;
        }
    }

    private static String formatTime(int ticks) {
        int totalSeconds = Mth.ceil(ticks / 20.0F);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format(java.util.Locale.ROOT, "%02d", seconds);
    }

    private static int lowTimeColor(GatewayEntity gateway, int timeRemaining) {
        if (timeRemaining >= 15 * 20) {
            return 0xFFFFFF;
        }
        return ((gateway.tickCount / 6) % 2 == 0) ? 0xFF3B30 : 0x8A0F0F;
    }
}
