package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.client.DungeonUpgradeClientState;
import com.revilo.gatesofavarice.dungeon.loadout.LoadoutModels.UpgradeCard;
import com.revilo.gatesofavarice.network.SelectUpgradeCardPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class DungeonUpgradeCardsScreen extends Screen {
    private int centerX;
    private int top;

    public DungeonUpgradeCardsScreen() {
        super(Component.literal("Upgrade Cards"));
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.top = this.height / 2 - 98;
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> this.minecraft.setScreen(new DungeonUpgradeCategoryScreen()))
                .pos(this.centerX - 165, this.top + 192).size(70, 20).build());
        int y = this.top + 96;
        for (int i = 0; i < DungeonUpgradeClientState.cards.size(); i++) {
            UpgradeCard card = DungeonUpgradeClientState.cards.get(i);
            int idx = i;
            this.addRenderableWidget(Button.builder(Component.literal((i + 1) + ". " + card.title()), b -> choose(idx))
                    .pos(this.centerX - 150, y + i * 19).size(300, 18).build());
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick);
        gg.drawCenteredString(this.font, Component.literal("UPGRADE - " + DungeonUpgradeClientState.categoryName).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), this.centerX, this.top + 4, 0xFFFFFF);
        int itemX = this.centerX;
        int itemY = this.top + 42;
        gg.pose().pushPose();
        gg.pose().translate(itemX, itemY, 0);
        gg.pose().scale(3.0F, 3.0F, 1.0F);
        gg.renderFakeItem(DungeonUpgradeClientState.previewStack, -8, -8);
        gg.pose().popPose();
        gg.drawCenteredString(this.font, DungeonUpgradeClientState.previewStack.getHoverName(), this.centerX, this.top + 64, 0xD9D9D9);
        if (mouseX >= itemX - 24 && mouseX <= itemX + 24 && mouseY >= itemY - 24 && mouseY <= itemY + 24) {
            gg.renderTooltip(this.font, DungeonUpgradeClientState.previewStack, mouseX, mouseY);
        }
        int y = this.top + 96;
        for (int i = 0; i < DungeonUpgradeClientState.cards.size(); i++) {
            UpgradeCard card = DungeonUpgradeClientState.cards.get(i);
            String details = card.changeLabel() + ": " + card.currentValue() + " -> " + card.newValue();
            gg.drawString(this.font, Component.literal(details), this.centerX - 144, y + i * 19 + 10, 0x9F9F9F, false);
        }
    }

    private void choose(int idx) {
        if (idx < 0 || idx >= DungeonUpgradeClientState.cards.size()) return;
        PacketDistributor.sendToServer(new SelectUpgradeCardPayload(DungeonUpgradeClientState.sessionId, DungeonUpgradeClientState.cards.get(idx).id()));
    }
}
