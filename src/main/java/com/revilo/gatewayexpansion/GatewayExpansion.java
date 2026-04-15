package com.revilo.gatewayexpansion;

import com.revilo.gatewayexpansion.command.CoinCommands;
import com.revilo.gatewayexpansion.client.GatewayExpansionClient;
import com.revilo.gatewayexpansion.config.GatewayExpansionConfig;
import com.revilo.gatewayexpansion.gateway.GatewayDisplayManager;
import com.revilo.gatewayexpansion.gateway.GatewayPartyScaling;
import com.revilo.gatewayexpansion.integration.GatewayFailureEvents;
import com.revilo.gatewayexpansion.integration.GeneratedGatewayPearlTracker;
import com.revilo.gatewayexpansion.integration.GatewayDrownedHandler;
import com.revilo.gatewayexpansion.integration.StabilityPearlHandler;
import com.revilo.gatewayexpansion.integration.GatewayThornsHandler;
import com.revilo.gatewayexpansion.integration.CuriosCompat;
import com.revilo.gatewayexpansion.integration.LevelUpGatewayIntegration;
import com.revilo.gatewayexpansion.integration.LevelUpGatewayXpRewards;
import com.revilo.gatewayexpansion.integration.LevelUpHudGateStateManager;
import com.revilo.gatewayexpansion.integration.MagnetHandler;
import com.revilo.gatewayexpansion.integration.SwordUpgradeAttributeFixHandler;
import com.revilo.gatewayexpansion.network.GatewayExpansionNetwork;
import com.revilo.gatewayexpansion.registry.ModAttachments;
import com.revilo.gatewayexpansion.registry.ModAttributes;
import com.revilo.gatewayexpansion.registry.ModBlockEntities;
import com.revilo.gatewayexpansion.registry.ModBlocks;
import com.revilo.gatewayexpansion.registry.ModCreativeTabs;
import com.revilo.gatewayexpansion.registry.ModEntities;
import com.revilo.gatewayexpansion.registry.ModItems;
import com.revilo.gatewayexpansion.registry.ModMenus;
import com.revilo.gatewayexpansion.registry.ModMobEffects;
import com.revilo.gatewayexpansion.registry.ModRecipes;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(GatewayExpansion.MOD_ID)
public final class GatewayExpansion {

    public static final String MOD_ID = "gatewayexpansion";
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
        if (com.revilo.gatewayexpansion.integration.ModCompat.isAnyLoaded("curios")) {
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
        NeoForge.EVENT_BUS.register(SwordUpgradeAttributeFixHandler.class);
        NeoForge.EVENT_BUS.register(GatewayThornsHandler.class);
        NeoForge.EVENT_BUS.register(GatewayDisplayManager.class);
        NeoForge.EVENT_BUS.register(GatewayPartyScaling.class);
        NeoForge.EVENT_BUS.register(ShopkeeperManager.class);
    }
}
