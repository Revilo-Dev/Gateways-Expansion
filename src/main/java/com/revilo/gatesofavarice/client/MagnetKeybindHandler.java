package com.revilo.gatesofavarice.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.network.ToggleMagnetPayload;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class MagnetKeybindHandler {

    private static final String KEY_CATEGORY = "key.categories." + GatewayExpansion.MOD_ID;
    private static final String KEY_TOGGLE_MAGNET = "key." + GatewayExpansion.MOD_ID + ".toggle_magnet";

    private static final KeyMapping TOGGLE_MAGNET = new KeyMapping(
            KEY_TOGGLE_MAGNET,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            KEY_CATEGORY);

    private MagnetKeybindHandler() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MAGNET);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_MAGNET.consumeClick()) {
            PacketDistributor.sendToServer(ToggleMagnetPayload.INSTANCE);
        }
    }
}
