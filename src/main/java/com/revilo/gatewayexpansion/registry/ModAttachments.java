package com.revilo.gatewayexpansion.registry;

import com.mojang.serialization.Codec;
import com.revilo.gatewayexpansion.GatewayExpansion;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, GatewayExpansion.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> MYTHIC_COINS =
            ATTACHMENTS.register("mythic_coins", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
