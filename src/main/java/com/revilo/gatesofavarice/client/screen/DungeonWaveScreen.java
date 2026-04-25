package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DungeonWaveScreen extends AbstractContainerScreen<DungeonWaveMenu> {

    private final List<Button> optionButtons = new ArrayList<>();
    private Button bailButton;

    public DungeonWaveScreen(DungeonWaveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 248;
        this.imageHeight = 166;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.optionButtons.clear();

        int buttonWidth = 224;
        int buttonHeight = 20;
        int x = this.leftPos + 12;
        int startY = this.topPos + 36;

        for (int index = 0; index < this.menu.options().size(); index++) {
            final int optionIndex = index;
            DungeonWaveMenu.WaveOptionView option = this.menu.options().get(index);
            Button button = this.addRenderableWidget(Button.builder(
                            Component.literal("Wave " + (index + 1) + ": ").withStyle(ChatFormatting.GOLD).append(option.title()),
                            click -> this.selectOption(optionIndex))
                    .pos(x, startY + (index * 26))
                    .size(buttonWidth, buttonHeight)
                    .build());
            button.active = this.menu.ownerCanSelect();
            this.optionButtons.add(button);
        }

        this.bailButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.gatesofavarice.dungeon_wave.bail").withStyle(ChatFormatting.RED),
                        click -> this.selectBail())
                .pos(this.leftPos + 74, this.topPos + 124)
                .size(100, 20)
                .build());
        this.bailButton.active = this.menu.ownerCanSelect();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fillGradient(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE01A1A1A, 0xE0050505);
        guiGraphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, 0xA0121212);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.title", this.menu.waveNumber()), 12, 10, 0xF4E9FF, false);
        if (this.menu.ownerCanSelect()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.select_prompt"), 12, 22, 0xBFBFBF, false);
        }
        else {
            guiGraphics.drawString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.waiting_owner"), 12, 22, 0xBFBFBF, false);
        }

        for (int index = 0; index < this.menu.options().size(); index++) {
            DungeonWaveMenu.WaveOptionView option = this.menu.options().get(index);
            int y = 42 + (index * 26);
            guiGraphics.drawString(
                    this.font,
                    option.details().copy()
                            .append(Component.literal("  [In: " + option.inDungeonRewardPercent() + "%]").withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" [Out: " + option.externalRewardPercent() + "%]").withStyle(ChatFormatting.GREEN)),
                    16,
                    y + 13,
                    0xBFBFBF,
                    false
            );
        }
    }

    private void selectOption(int optionIndex) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, optionIndex);
    }

    private void selectBail() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DungeonWaveMenu.BAIL_BUTTON_ID);
    }
}
