package com.revilo.gatewayexpansion.client;

import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.registry.ModItems;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class InventoryWalletOverlay {

    private static final ItemStack COIN_STACK = new ItemStack(ModItems.MYTHIC_COIN.get());
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0##");
    private static final int PLAYER_BOX_CENTER_X = 51;
    private static final int PLAYER_BOX_CENTER_Y = 66;
    private static final int ICON_SIZE = 16;
    private static final float BASE_TEXT_SCALE = 0.75F;
    private static boolean hoveredLastFrame;

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
        WalletLayout layout = WalletLayout.create(minecraft, screen);
        boolean hovered = layout.isMouseOver(event.getMouseX(), event.getMouseY());
        if (hovered && !hoveredLastFrame) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.15F));
        }
        hoveredLastFrame = hovered;

        renderWallet(event.getGuiGraphics(), minecraft, layout, hovered);

        if (hovered) {
            event.getGuiGraphics().renderTooltip(
                    minecraft.font,
                    List.of(
                            Component.literal("Mythic Coins"),
                            Component.empty(),
                            Component.literal("Multiplier " + MULTIPLIER_FORMAT.format(MythicCoinWallet.getTotalMultiplier(minecraft.player)) + "x")
                    ),
                    COIN_STACK.getTooltipImage(),
                    COIN_STACK,
                    event.getMouseX(),
                    event.getMouseY());
        }
    }

    private static void renderWallet(GuiGraphics guiGraphics, Minecraft minecraft, WalletLayout layout, boolean hovered) {
        float hoverScale = hovered ? 1.1F : 1.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(layout.iconX(), layout.iconY(), 250.0F);
        guiGraphics.pose().scale(hoverScale, hoverScale, 1.0F);
        guiGraphics.renderItem(COIN_STACK, 0, 0);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(layout.textX(), layout.textY(), 250.0F);
        guiGraphics.pose().scale(layout.textScale() * hoverScale, layout.textScale() * hoverScale, 1.0F);
        guiGraphics.drawString(minecraft.font, layout.text(), 0, 0, 0xB06CFF, false);
        guiGraphics.pose().popPose();
    }

    private record WalletLayout(String text, float textScale, int iconX, int iconY, float textX, float textY, int hoverLeft, int hoverTop, int hoverRight, int hoverBottom) {

        private static WalletLayout create(Minecraft minecraft, AbstractContainerScreen<?> screen) {
            String text = Integer.toString(MythicCoinWallet.get(minecraft.player));
            int digits = text.length();
            int extraDigits = Math.max(0, digits - 4);
            float textScale = Math.max(0.42F, BASE_TEXT_SCALE - (extraDigits * 0.08F));
            float textWidth = minecraft.font.width(text) * textScale;
            float gap = 4.0F + (extraDigits * 1.5F);
            float groupWidth = ICON_SIZE + gap + textWidth;
            float centerX = screen.getGuiLeft() + PLAYER_BOX_CENTER_X;
            float centerY = screen.getGuiTop() + PLAYER_BOX_CENTER_Y;
            float groupLeft = centerX - (groupWidth / 2.0F);
            int iconX = Mth.floor(groupLeft);
            int iconY = Mth.floor(centerY - 8.0F);
            float textX = groupLeft + ICON_SIZE + gap;
            float textY = centerY - (4.0F * textScale);
            int hoverLeft = Mth.floor(groupLeft) - 2;
            int hoverTop = Mth.floor(centerY - 10.0F);
            int hoverRight = Mth.ceil(groupLeft + groupWidth) + 2;
            int hoverBottom = Mth.ceil(centerY + 8.0F);
            return new WalletLayout(text, textScale, iconX, iconY, textX, textY, hoverLeft, hoverTop, hoverRight, hoverBottom);
        }

        private boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.hoverLeft && mouseX <= this.hoverRight && mouseY >= this.hoverTop && mouseY <= this.hoverBottom;
        }
    }
}
