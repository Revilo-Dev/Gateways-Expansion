package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.client.screen.GatewayWorkbenchScreen;
import com.revilo.gatewayexpansion.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class GatewayExpansionClient {

    private GatewayExpansionClient() {
    }

    public static void register(IEventBus modEventBus) {
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(ClientEvents.class);
        }
    }

    public static final class ClientEvents {

        private ClientEvents() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.GATEWAY_WORKBENCH.get(), GatewayWorkbenchScreen::new);
        }
    }
}
