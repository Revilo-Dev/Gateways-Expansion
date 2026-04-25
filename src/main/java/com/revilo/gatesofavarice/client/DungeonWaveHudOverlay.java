package com.revilo.gatesofavarice.client;

import com.revilo.gatesofavarice.dungeon.DungeonHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class DungeonWaveHudOverlay {

    private DungeonWaveHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui || !DungeonHudState.active()) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int barWidth = 170;
        int barHeight = 10;
        int x = (screenWidth - barWidth) / 2;
        int y = 8;

        int total = Math.max(1, DungeonHudState.totalMobs());
        int remaining = Mth.clamp(DungeonHudState.mobsRemaining(), 0, total);
        int filled = Math.round((barWidth - 2) * ((total - remaining) / (float) total));

        event.getGuiGraphics().fill(x, y, x + barWidth, y + barHeight, 0xA0000000);
        event.getGuiGraphics().fill(x + 1, y + 1, x + 1 + filled, y + barHeight - 1, 0xCC5BD35B);

        Component label = Component.literal("Wave " + DungeonHudState.waveNumber() + " - " + remaining + " mobs remaining");
        int labelWidth = minecraft.font.width(label);
        event.getGuiGraphics().drawString(minecraft.font, label, (screenWidth - labelWidth) / 2, y + barHeight + 2, 0xFFFFFF, false);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DungeonHudState.clear();
    }
}
