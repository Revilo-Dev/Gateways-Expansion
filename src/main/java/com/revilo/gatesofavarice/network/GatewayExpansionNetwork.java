package com.revilo.gatesofavarice.network;

import com.revilo.gatesofavarice.registry.ModAttachments;
import com.revilo.gatesofavarice.dungeon.DungeonHudState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class GatewayExpansionNetwork {

    private GatewayExpansionNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(GatewayExpansionNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ToggleMagnetPayload.TYPE, ToggleMagnetPayload.STREAM_CODEC, (payload, context) -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            boolean enabled = !player.getData(ModAttachments.MAGNET_ENABLED);
            player.setData(ModAttachments.MAGNET_ENABLED, enabled);
            player.displayClientMessage(
                    Component.translatable(enabled
                                    ? "message.gatesofavarice.magnet_enabled"
                                    : "message.gatesofavarice.magnet_disabled")
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED),
                    true);
        });
        registrar.playToClient(DungeonWaveHudPayload.TYPE, DungeonWaveHudPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> DungeonHudState.apply(payload)));
    }
}
