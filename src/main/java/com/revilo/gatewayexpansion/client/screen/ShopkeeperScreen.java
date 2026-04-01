package com.revilo.gatewayexpansion.client.screen;

import com.revilo.gatewayexpansion.menu.ShopkeeperMenu;
import com.revilo.gatewayexpansion.shop.ShopOfferDefinition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ShopkeeperScreen extends AbstractContainerScreen<ShopkeeperMenu> {

    private static final int OFFER_HEIGHT = 26;

    public ShopkeeperScreen(ShopkeeperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 216;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int baseX = this.leftPos + 152;
        int baseY = this.topPos + 24;
        for (int index = 0; index < this.menu.getOffers().size(); index++) {
            final int buttonId = index;
            this.addRenderableWidget(Button.builder(Component.translatable("screen.gatewayexpansion.shopkeeper.buy"), button -> {
                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
                }
            }).pos(baseX, baseY + index * OFFER_HEIGHT).size(52, 20).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xEE1A140D);
        guiGraphics.fill(this.leftPos + 6, this.topPos + 18, this.leftPos + this.imageWidth - 6, this.topPos + this.imageHeight - 8, 0xCC302316);
        guiGraphics.fill(this.leftPos + 8, this.topPos + 20, this.leftPos + 144, this.topPos + this.imageHeight - 10, 0x993E2D19);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 10, 6, 0xF4D38A, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.gatewayexpansion.shopkeeper.wallet", this.menu.getWalletBalance()), 10, 18, 0xFFF1C1, false);
        List<Component> tooltip = new ArrayList<>();

        List<ShopOfferDefinition> offers = this.menu.getOffers();
        for (int index = 0; index < offers.size(); index++) {
            ShopOfferDefinition offer = offers.get(index);
            int y = 34 + index * OFFER_HEIGHT;
            guiGraphics.renderItem(offer.previewStack(), 10, y - 2);
            guiGraphics.drawString(this.font, offer.title(), 30, y, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, Component.translatable("screen.gatewayexpansion.shopkeeper.price", offer.price()), 30, y + 10, 0xF4D38A, false);

            if (mouseX >= this.leftPos + 8 && mouseX <= this.leftPos + 144 && mouseY >= this.topPos + y - 4 && mouseY <= this.topPos + y + 16) {
                tooltip.add(offer.title());
                tooltip.add(offer.description());
            }
        }

        if (!tooltip.isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX - this.leftPos, mouseY - this.topPos);
        }
    }
}
