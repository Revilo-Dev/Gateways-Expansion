package com.revilo.gatewayexpansion.gateway;

import com.revilo.gatewayexpansion.config.GatewayExpansionConfig;
import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public final class GatewayDisplayManager {

    private GatewayDisplayManager() {
    }

    @SubscribeEvent
    public static void onGatewayOpened(GateEvent.Opened event) {
        GatewayEntity gate = event.getEntity();
        String displayName = GatewayForgeService.getGatewayDisplayName(gate.getGateway());
        if (displayName != null && !displayName.isBlank()) {
            gate.setCustomName(Component.literal(displayName).withStyle(style -> style.withColor(gate.getGateway().color())));
        }

        if (!GatewayExpansionConfig.ANNOUNCE_GATE_OPEN_IN_CHAT.get() || gate.level().isClientSide || ShopkeeperManager.isGatewayAnimation(gate)) {
            return;
        }

        ServerPlayer source = findSourcePlayer(gate);
        if (source == null || gate.level().getServer() == null) {
            return;
        }

        int level = Math.max(1, GatewayForgeService.getGatewayLevel(gate.getGateway()));
        Component message = Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(source.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal("] opened a ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Lv" + level).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" gate").withStyle(ChatFormatting.GRAY));
        gate.level().getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    private static ServerPlayer findSourcePlayer(GatewayEntity gate) {
        if (gate.summonerOrClosest() instanceof ServerPlayer player) {
            return player;
        }
        return gate.level().getNearestPlayer(gate, 64.0D) instanceof ServerPlayer player ? player : null;
    }
}
