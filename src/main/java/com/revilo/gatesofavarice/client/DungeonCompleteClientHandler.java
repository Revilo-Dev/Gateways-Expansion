package com.revilo.gatesofavarice.client;

import com.revilo.gatesofavarice.client.screen.DungeonCompleteScreen;
import com.revilo.gatesofavarice.network.DungeonCompletePayload;
import net.minecraft.client.Minecraft;

public final class DungeonCompleteClientHandler {

    private DungeonCompleteClientHandler() {
    }

    public static void open(DungeonCompletePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DungeonCompleteScreen(payload));
    }
}
