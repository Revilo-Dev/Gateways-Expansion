package com.revilo.gatesofavarice.network;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DungeonWaveHudPayload(boolean active, int waveNumber, int mobsRemaining, int totalMobs) implements CustomPacketPayload {

    public static final Type<DungeonWaveHudPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "dungeon_wave_hud"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonWaveHudPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBoolean(payload.active);
                        buffer.writeVarInt(payload.waveNumber);
                        buffer.writeVarInt(payload.mobsRemaining);
                        buffer.writeVarInt(payload.totalMobs);
                    },
                    buffer -> new DungeonWaveHudPayload(
                            buffer.readBoolean(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    )
            );

    @Override
    public Type<DungeonWaveHudPayload> type() {
        return TYPE;
    }
}
