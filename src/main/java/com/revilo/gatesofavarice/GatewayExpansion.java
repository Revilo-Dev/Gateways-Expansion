package com.revilo.gatesofavarice;

import com.revilo.gatesofavarice.command.CoinCommands;
import com.revilo.gatesofavarice.client.GatewayExpansionClient;
import com.revilo.gatesofavarice.config.GatewayExpansionConfig;
import com.revilo.gatesofavarice.gateway.GatewayDisplayManager;
import com.revilo.gatesofavarice.gateway.GatewayPartyScaling;
import com.revilo.gatesofavarice.integration.GatewayFailureEvents;
import com.revilo.gatesofavarice.integration.GeneratedGatewayPearlTracker;
import com.revilo.gatesofavarice.integration.GatewayDrownedHandler;
import com.revilo.gatesofavarice.integration.StabilityPearlHandler;
import com.revilo.gatesofavarice.integration.GatewayThornsHandler;
import com.revilo.gatesofavarice.integration.CuriosCompat;
import com.revilo.gatesofavarice.integration.LevelUpGatewayIntegration;
import com.revilo.gatesofavarice.integration.LevelUpGatewayXpRewards;
import com.revilo.gatesofavarice.integration.LevelUpHudGateStateManager;
import com.revilo.gatesofavarice.integration.MagnetHandler;
import com.revilo.gatesofavarice.network.GatewayExpansionNetwork;
import com.revilo.gatesofavarice.registry.ModAttachments;
import com.revilo.gatesofavarice.registry.ModAttributes;
import com.revilo.gatesofavarice.registry.ModBlockEntities;
import com.revilo.gatesofavarice.registry.ModBlocks;
import com.revilo.gatesofavarice.registry.ModCreativeTabs;
import com.revilo.gatesofavarice.registry.ModEntities;
import com.revilo.gatesofavarice.registry.ModItems;
import com.revilo.gatesofavarice.registry.ModMenus;
import com.revilo.gatesofavarice.registry.ModMobEffects;
import com.revilo.gatesofavarice.registry.ModRecipes;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(GatewayExpansion.MOD_ID)
public final class GatewayExpansion {

    public static final String MOD_ID = "gatesofavarice";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GatewayExpansion(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, GatewayExpansionConfig.SPEC);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModAttributes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        GatewayExpansionNetwork.register(modEventBus);
        GatewayExpansionClient.register(modEventBus);
        if (com.revilo.gatesofavarice.integration.ModCompat.isAnyLoaded("curios")) {
            CuriosCompat.register(modEventBus);
        }
        NeoForge.EVENT_BUS.register(CoinCommands.class);
        NeoForge.EVENT_BUS.register(GatewayFailureEvents.class);
        NeoForge.EVENT_BUS.register(GatewayDrownedHandler.class);
        NeoForge.EVENT_BUS.register(GeneratedGatewayPearlTracker.class);
        NeoForge.EVENT_BUS.register(StabilityPearlHandler.class);
        NeoForge.EVENT_BUS.register(LevelUpGatewayIntegration.class);
        NeoForge.EVENT_BUS.register(LevelUpGatewayXpRewards.class);
        NeoForge.EVENT_BUS.register(LevelUpHudGateStateManager.class);
        NeoForge.EVENT_BUS.register(MagnetHandler.class);
        NeoForge.EVENT_BUS.register(GatewayThornsHandler.class);
        NeoForge.EVENT_BUS.register(GatewayDisplayManager.class);
        NeoForge.EVENT_BUS.register(GatewayPartyScaling.class);
        NeoForge.EVENT_BUS.register(ShopkeeperManager.class);
    }
}
