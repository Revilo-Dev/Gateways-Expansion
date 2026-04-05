package com.revilo.gatewayexpansion;

import com.revilo.gatewayexpansion.command.CoinCommands;
import com.revilo.gatewayexpansion.client.GatewayExpansionClient;
import com.revilo.gatewayexpansion.gateway.GatewayDisplayManager;
import com.revilo.gatewayexpansion.integration.GatewayFailureEvents;
import com.revilo.gatewayexpansion.integration.GeneratedGatewayPearlTracker;
import com.revilo.gatewayexpansion.registry.ModAttachments;
import com.revilo.gatewayexpansion.registry.ModAttributes;
import com.revilo.gatewayexpansion.registry.ModBlockEntities;
import com.revilo.gatewayexpansion.registry.ModBlocks;
import com.revilo.gatewayexpansion.registry.ModCreativeTabs;
import com.revilo.gatewayexpansion.registry.ModEntities;
import com.revilo.gatewayexpansion.registry.ModItems;
import com.revilo.gatewayexpansion.registry.ModMenus;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(GatewayExpansion.MOD_ID)
public final class GatewayExpansion {

    public static final String MOD_ID = "gatewayexpansion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GatewayExpansion(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModAttributes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        GatewayExpansionClient.register(modEventBus);
        NeoForge.EVENT_BUS.register(CoinCommands.class);
        NeoForge.EVENT_BUS.register(GatewayFailureEvents.class);
        NeoForge.EVENT_BUS.register(GeneratedGatewayPearlTracker.class);
        NeoForge.EVENT_BUS.register(GatewayDisplayManager.class);
        NeoForge.EVENT_BUS.register(ShopkeeperManager.class);
    }
}
