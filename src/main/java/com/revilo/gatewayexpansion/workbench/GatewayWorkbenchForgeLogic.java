package com.revilo.gatewayexpansion.workbench;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import com.revilo.gatewayexpansion.gateway.builder.GatewayPreview;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public final class GatewayWorkbenchForgeLogic {

    private GatewayWorkbenchForgeLogic() {
    }

    public static boolean canForge(Container container) {
        return GatewayForgeService.canForge(container);
    }

    public static boolean forge(Player player, Container container) {
        if (!(player instanceof ServerPlayer serverPlayer) || !canForge(container)) {
            return false;
        }

        try {
            GatewayForgeService.forge(serverPlayer, container);
            return true;
        } catch (Exception ex) {
            GatewayExpansion.LOGGER.error("Failed to forge gateway pearl for {}", player.getScoreboardName(), ex);
            player.sendSystemMessage(Component.literal("Gateway forge failed. Check the logs for details.").withStyle(ChatFormatting.RED));
            return false;
        }
    }

    public static GatewayPreview buildPreview(Player player, Container container) {
        return GatewayForgeService.buildPreview(player, container);
    }
}
