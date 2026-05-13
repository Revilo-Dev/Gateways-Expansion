package com.revilo.gatesofavarice.network;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenUpgradeCategoryPayload(String sessionId, String loadoutName, String theme) implements CustomPacketPayload {
    public static final Type<OpenUpgradeCategoryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "open_upgrade_category"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUpgradeCategoryPayload> STREAM_CODEC =
            StreamCodec.of(OpenUpgradeCategoryPayload::write, OpenUpgradeCategoryPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, OpenUpgradeCategoryPayload payload) {
        buffer.writeUtf(payload.sessionId);
        buffer.writeUtf(payload.loadoutName);
        buffer.writeUtf(payload.theme);
    }

    private static OpenUpgradeCategoryPayload read(RegistryFriendlyByteBuf buffer) {
        return new OpenUpgradeCategoryPayload(buffer.readUtf(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public Type<OpenUpgradeCategoryPayload> type() {
        return TYPE;
    }
}

