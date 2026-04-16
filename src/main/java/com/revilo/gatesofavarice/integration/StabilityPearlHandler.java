package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.registry.ModMobEffects;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

public final class StabilityPearlHandler {

    private static final String GATEWAY_ID_KEY = "gatesofavarice.stability_gateway_id";
    private static final String GATEWAY_LEVEL_KEY = "gatesofavarice.stability_gateway_level";

    private StabilityPearlHandler() {
    }

    public static void linkToGateway(ServerPlayer player, GatewayEntity gateway) {
        player.getPersistentData().putUUID(GATEWAY_ID_KEY, gateway.getUUID());
        player.getPersistentData().putString(GATEWAY_LEVEL_KEY, gateway.level().dimension().location().toString());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }

        if (!player.getPersistentData().hasUUID(GATEWAY_ID_KEY)) {
            if (player.hasEffect(ModMobEffects.STABILITY_DRAIN)) {
                player.removeEffect(ModMobEffects.STABILITY_DRAIN);
            }
            return;
        }

        ServerLevel gatewayLevel = resolveGatewayLevel(player);
        UUID gatewayId = player.getPersistentData().getUUID(GATEWAY_ID_KEY);
        Entity entity = gatewayLevel == null ? null : gatewayLevel.getEntity(gatewayId);
        if (entity instanceof GatewayEntity gateway && gateway.isValid() && !gateway.isRemoved() && !ShopkeeperManager.isGatewayAnimation(gateway)) {
            return;
        }

        player.removeEffect(ModMobEffects.STABILITY_DRAIN);
        player.getPersistentData().remove(GATEWAY_ID_KEY);
        player.getPersistentData().remove(GATEWAY_LEVEL_KEY);
    }

    private static ServerLevel resolveGatewayLevel(ServerPlayer player) {
        String levelId = player.getPersistentData().getString(GATEWAY_LEVEL_KEY);
        ResourceLocation location = ResourceLocation.tryParse(levelId);
        if (location == null || player.getServer() == null) {
            return null;
        }
        return player.getServer().getLevel(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location));
    }
}
