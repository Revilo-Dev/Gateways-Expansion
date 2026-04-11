package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.integration.LevelUpIntegration;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class LevelUpGateOverlay {

    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-background.png");
    private static final ResourceLocation BAR_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-progress.png");
    private static final int BAR_WIDTH = 184;
    private static final int BAR_HEIGHT = 11;

    private LevelUpGateOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null || minecraft.options.hideGui || !LevelUpIntegration.isLoaded()) {
            return;
        }

        GatewayEntity gateway = findNearbyGateway(player);
        if (gateway == null) {
            return;
        }

        renderLevelBar(event.getGuiGraphics(), minecraft, player);
    }

    private static GatewayEntity findNearbyGateway(LocalPlayer player) {
        GatewayEntity nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (GatewayEntity candidate : player.level().getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(96.0D))) {
            if (!candidate.isValid() || ShopkeeperManager.isGatewayAnimation(candidate)) {
                continue;
            }

            double distance = player.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static void renderLevelBar(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player) {
        int level = Math.max(1, LevelUpIntegration.getPlayerLevel(player));
        float progress = Mth.clamp(LevelUpIntegration.getProgressToNextLevel(player), 0.0F, 1.0F);
        int progressWidth = Mth.clamp(Math.round(progress * BAR_WIDTH), 0, BAR_WIDTH);
        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_WIDTH) / 2;
        int y = 4;

        guiGraphics.blit(BAR_BACKGROUND, x, y, 0.0F, 0.0F, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        if (progressWidth > 0) {
            guiGraphics.blit(BAR_PROGRESS, x, y, 0.0F, 0.0F, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        Component label = Component.translatable("levelup.ui.level", level);
        Font font = minecraft.font;
        int textX = x + ((BAR_WIDTH - font.width(label)) / 2);
        guiGraphics.drawString(font, label, textX, y - 10, 0x53A4BC, false);
    }
}
