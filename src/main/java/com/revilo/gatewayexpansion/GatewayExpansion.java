package com.revilo.gatewayexpansion;

import com.revilo.gatewayexpansion.client.GatewayExpansionClient;
import com.revilo.gatewayexpansion.registry.ModBlockEntities;
import com.revilo.gatewayexpansion.registry.ModBlocks;
import com.revilo.gatewayexpansion.registry.ModCreativeTabs;
import com.revilo.gatewayexpansion.registry.ModItems;
import com.revilo.gatewayexpansion.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GatewayExpansion.MOD_ID)
public final class GatewayExpansion {

    public static final String MOD_ID = "gatewayexpansion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GatewayExpansion(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        GatewayExpansionClient.register(modEventBus);
    }
}
