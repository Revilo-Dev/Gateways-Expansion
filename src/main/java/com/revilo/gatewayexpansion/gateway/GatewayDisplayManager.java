package com.revilo.gatewayexpansion.gateway;

import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import net.minecraft.network.chat.Component;
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
    }
}
