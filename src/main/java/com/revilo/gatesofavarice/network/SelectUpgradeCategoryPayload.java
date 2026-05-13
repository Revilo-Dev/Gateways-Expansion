package com.revilo.gatesofavarice.network;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectUpgradeCategoryPayload(String sessionId, int categoryOrdinal) implements CustomPacketPayload {
    public static final Type<SelectUpgradeCategoryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "select_upgrade_category"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectUpgradeCategoryPayload> STREAM_CODEC =
            StreamCodec.of(SelectUpgradeCategoryPayload::write, SelectUpgradeCategoryPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, SelectUpgradeCategoryPayload payload) {
        buffer.writeUtf(payload.sessionId);
        buffer.writeVarInt(payload.categoryOrdinal);
    }

    private static SelectUpgradeCategoryPayload read(RegistryFriendlyByteBuf buffer) {
        return new SelectUpgradeCategoryPayload(buffer.readUtf(), buffer.readVarInt());
    }

    @Override
    public Type<SelectUpgradeCategoryPayload> type() {
        return TYPE;
    }
}

