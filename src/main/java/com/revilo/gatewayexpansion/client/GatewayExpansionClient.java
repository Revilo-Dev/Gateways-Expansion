package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.client.model.GatekeeperModel;
import com.revilo.gatewayexpansion.client.render.GatekeeperRenderer;
import com.revilo.gatewayexpansion.client.screen.ShopkeeperScreen;
import com.revilo.gatewayexpansion.client.render.GatewayWorkbenchBlockEntityRenderer;
import com.revilo.gatewayexpansion.client.screen.GatewayWorkbenchScreen;
import com.revilo.gatewayexpansion.gateway.GatewayHudOverlay;
import com.revilo.gatewayexpansion.registry.ModBlockEntities;
import com.revilo.gatewayexpansion.registry.ModEntities;
import com.revilo.gatewayexpansion.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class GatewayExpansionClient {

    private GatewayExpansionClient() {
    }

    public static void register(IEventBus modEventBus) {
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.register(ClientEvents.class);
            NeoForge.EVENT_BUS.register(InventoryWalletOverlay.class);
            NeoForge.EVENT_BUS.register(GatewayHudOverlay.class);
        }
    }

    public static final class ClientEvents {

        private ClientEvents() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.GATEWAY_WORKBENCH.get(), GatewayWorkbenchScreen::new);
            event.register(ModMenus.SHOPKEEPER.get(), ShopkeeperScreen::new);
        }

        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.GATEWAY_WORKBENCH.get(), GatewayWorkbenchBlockEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.GATEKEEPER.get(), GatekeeperRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(GatekeeperModel.LAYER_LOCATION, GatekeeperModel::createBodyLayer);
        }
    }
}
