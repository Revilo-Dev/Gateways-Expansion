package com.revilo.gatesofavarice.integration;

import dev.shadowsoffire.gateways.event.GateEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class LevelUpGatewayIntegration {

    private static final String LEVELUP_DROP_TAG = "drops_levels";

    private LevelUpGatewayIntegration() {
    }

    @SubscribeEvent
    public static void onWaveEntitySpawned(GateEvent.WaveEntitySpawned event) {
        event.getWaveEntity().addTag(LEVELUP_DROP_TAG);
    }
}
