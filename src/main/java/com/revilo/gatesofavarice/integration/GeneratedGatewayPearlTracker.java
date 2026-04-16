package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.gateway.builder.GatewayForgeService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class GeneratedGatewayPearlTracker {

    private GeneratedGatewayPearlTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) {
            return;
        }

        restoreFromInventory(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GatewayForgeService.restorePersistedGateways(player.serverLevel());
            restoreFromPlayerStorage(player);
            GatewayForgeService.syncGatewayRegistry(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreFromPlayerStorage(player);
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level == level.getServer().overworld()) {
            GatewayForgeService.restorePersistedGateways(level);
        }
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level != level.getServer().overworld()) {
            return;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            restoreFromPlayerStorage(player);
        }
    }

    private static boolean restoreFromPlayerStorage(ServerPlayer player) {
        boolean restored = restoreFromInventory(player);
        restored |= restoreFromContainer(player, player.getEnderChestInventory());
        return restored;
    }

    private static boolean restoreFromInventory(ServerPlayer player) {
        boolean restored = false;
        for (ItemStack stack : player.getInventory().items) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        for (ItemStack stack : player.getInventory().armor) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        for (ItemStack stack : player.getInventory().offhand) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(stack, player.serverLevel());
        }
        return restored;
    }

    private static boolean restoreFromContainer(ServerPlayer player, Container container) {
        boolean restored = false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            restored |= GatewayForgeService.restoreGatewayFromPearl(container.getItem(slot), player.serverLevel());
        }
        return restored;
    }
}
