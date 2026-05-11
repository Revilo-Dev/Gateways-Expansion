package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.menu.DungeonLoadoutMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DungeonLoadoutScreen extends AbstractContainerScreen<DungeonLoadoutMenu> {

    private final List<Button> optionButtons = new ArrayList<>();

    public DungeonLoadoutScreen(DungeonLoadoutMenu menu, Inventory inventory, Component title) {
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
        int startY = this.topPos + 34;
        for (int index = 0; index < DungeonLoadoutMenu.LOADOUT_COUNT; index++) {
            int loadoutId = index;
            Button button = this.addRenderableWidget(Button.builder(
                            Component.literal(loadoutTitle(index)).withStyle(ChatFormatting.GOLD),
                            click -> this.selectLoadout(loadoutId))
                    .pos(x, startY + index * 32)
                    .size(buttonWidth, buttonHeight)
                    .build());
            this.optionButtons.add(button);
        }
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
        guiGraphics.drawString(this.font, Component.literal("Choose Loadout"), 12, 10, 0xF4E9FF, false);
        for (int i = 0; i < DungeonLoadoutMenu.LOADOUT_COUNT; i++) {
            guiGraphics.drawString(this.font, Component.literal(loadoutDescription(i)).withStyle(ChatFormatting.GRAY), 16, 58 + i * 32, 0xBFBFBF, false);
        }
    }

    private void selectLoadout(int index) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, index);
    }

    private static String loadoutTitle(int index) {
        return switch (index) {
            case 0 -> "Vanguard";
            case 1 -> "Ranger";
            default -> "Spellblade";
        };
    }

    private static String loadoutDescription(int index) {
        return switch (index) {
            case 0 -> "Main: Iron Sword | Secondary: Shield | Armor: Iron Set";
            case 1 -> "Main: Bow | Secondary: Stone Sword | Armor: Chainmail Set";
            default -> "Main: Trident | Secondary: Iron Axe | Armor: Gold Set";
        };
    }
}
