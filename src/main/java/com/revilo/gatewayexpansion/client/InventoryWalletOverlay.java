package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.registry.ModItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class InventoryWalletOverlay {

    private InventoryWalletOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
        int x = screen.getGuiLeft() + 33;
        int y = screen.getGuiTop() + 64;
        ItemStack stack = new ItemStack(ModItems.MYTHIC_COIN.get());
        event.getGuiGraphics().renderItem(stack, x, y);
        event.getGuiGraphics().drawString(
                minecraft.font,
                Integer.toString(MythicCoinWallet.get(minecraft.player)),
                x + 18,
                y + 4,
                0xB06CFF,
                false);

        int hoverLeft = x;
        int hoverTop = y;
        int hoverRight = x + 16 + minecraft.font.width(Integer.toString(MythicCoinWallet.get(minecraft.player))) + 20;
        int hoverBottom = y + 16;
        if (event.getMouseX() >= hoverLeft && event.getMouseX() <= hoverRight && event.getMouseY() >= hoverTop && event.getMouseY() <= hoverBottom) {
            event.getGuiGraphics().renderTooltip(
                    minecraft.font,
                    List.of(Component.literal("Mythic Coins")),
                    stack.getTooltipImage(),
                    stack,
                    event.getMouseX(),
                    event.getMouseY());
        }
    }
}
