package com.revilo.gatewayexpansion.integration;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class GatewayFailureEvents {

    private GatewayFailureEvents() {
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        List<GatewayEntity> gateways = player.level().getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(100.0D));
        for (GatewayEntity gateway : gateways) {
            if (gateway.isRemoved()) {
                continue;
            }
            gateway.setRemainingLives(1);
            gateway.playerDied(player);
        }
    }
}
