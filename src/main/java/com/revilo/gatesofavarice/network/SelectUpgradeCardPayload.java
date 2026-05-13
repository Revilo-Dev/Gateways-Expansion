package com.revilo.gatesofavarice.network;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectUpgradeCardPayload(String sessionId, String cardId) implements CustomPacketPayload {
    public static final Type<SelectUpgradeCardPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "select_upgrade_card"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectUpgradeCardPayload> STREAM_CODEC =
            StreamCodec.of(SelectUpgradeCardPayload::write, SelectUpgradeCardPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, SelectUpgradeCardPayload payload) {
        buffer.writeUtf(payload.sessionId);
        buffer.writeUtf(payload.cardId);
    }

    private static SelectUpgradeCardPayload read(RegistryFriendlyByteBuf buffer) {
        return new SelectUpgradeCardPayload(buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public Type<SelectUpgradeCardPayload> type() {
        return TYPE;
    }
}

