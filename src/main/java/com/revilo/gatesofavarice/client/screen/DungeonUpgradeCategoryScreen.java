package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.client.DungeonUpgradeClientState;
import com.revilo.gatesofavarice.network.SelectUpgradeCategoryPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class DungeonUpgradeCategoryScreen extends Screen {
    public DungeonUpgradeCategoryScreen() {
        super(Component.literal("Upgrade Categories"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 44;
        this.addRenderableWidget(Button.builder(Component.literal("Primary Weapon Upgrade"), b -> select(0)).pos(centerX - 110, y).size(220, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Secondary Weapon Upgrade"), b -> select(1)).pos(centerX - 110, y + 24).size(220, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Armor Upgrade"), b -> select(2)).pos(centerX - 110, y + 48).size(220, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Item Upgrade"), b -> select(3)).pos(centerX - 110, y + 72).size(220, 20).build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick);
        gg.drawCenteredString(this.font, Component.literal("CHOOSE UPGRADE CATEGORY").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), this.width / 2, this.height / 2 - 78, 0xFFFFFF);
        gg.drawCenteredString(this.font, Component.literal("Loadout: " + DungeonUpgradeClientState.loadoutName), this.width / 2, this.height / 2 - 62, 0xE8D6A2);
        gg.drawCenteredString(this.font, Component.literal("Theme: " + DungeonUpgradeClientState.theme), this.width / 2, this.height / 2 - 50, 0xB5B5B5);
    }

    private void select(int ordinal) {
        PacketDistributor.sendToServer(new SelectUpgradeCategoryPayload(DungeonUpgradeClientState.sessionId, ordinal));
    }
}
