package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class DungeonWaveScreen extends AbstractContainerScreen<DungeonWaveMenu> {

    private static final ResourceLocation CARD = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "textures/gui/dungeon/card.png");
    private static final ResourceLocation CARD_HOVERED = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "textures/gui/dungeon/card-hovered.png");
    private static final int CARD_W = 76;
    private static final int CARD_H = 103;
    private static final int CARD_GAP = 3;

    private final List<Button> optionButtons = new ArrayList<>();
    private Button bailButton;
    private Button rerollButton;
    private Button skipButton;
    private boolean showRunChanges = false;

    public DungeonWaveScreen(DungeonWaveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 340;
        this.imageHeight = 230;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void init() {
        super.init();
        this.optionButtons.clear();

        int totalWidth = DungeonWaveMenu.OPTION_COUNT * CARD_W + (DungeonWaveMenu.OPTION_COUNT - 1) * CARD_GAP;
        int x = this.leftPos + (this.imageWidth - totalWidth) / 2;
        int startY = this.topPos + 34;

        for (int index = 0; index < this.menu.options().size(); index++) {
            final int optionIndex = index;
            Button button = this.addRenderableWidget(new CardButton(
                    x + index * (CARD_W + CARD_GAP),
                    startY,
                    CARD_W,
                    CARD_H,
                    click -> this.selectOption(optionIndex)));
            button.active = this.menu.ownerCanSelect();
            this.optionButtons.add(button);
        }

        this.bailButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.gatesofavarice.dungeon_wave.bail").withStyle(ChatFormatting.RED),
                        click -> this.selectBail())
                .pos(this.leftPos + 58, this.topPos + 150)
                .size(224, 20)
                .build());
        this.bailButton.active = this.menu.ownerCanSelect() && this.menu.stage() == 0;

        this.rerollButton = this.addRenderableWidget(Button.builder(
                        Component.literal("Reroll (" + this.menu.rerollsLeft() + ") - " + this.menu.rerollCost() + " Mythic Coins").withStyle(ChatFormatting.GOLD),
                        click -> this.selectReroll())
                .pos(this.leftPos + 58, this.topPos + 175)
                .size(224, 20)
                .build());
        this.rerollButton.active = this.menu.ownerCanSelect() && this.menu.stage() == 1 && this.menu.rerollsLeft() > 0;

        this.skipButton = this.addRenderableWidget(Button.builder(
                        Component.literal("Skip").withStyle(ChatFormatting.GRAY),
                        click -> this.selectSkip())
                .pos(this.leftPos + 58, this.topPos + 199)
                .size(224, 20)
                .build());
        this.skipButton.active = this.menu.ownerCanSelect() && this.menu.stage() == 1;
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
        for (int i = 0; i < this.optionButtons.size(); i++) {
            Button button = this.optionButtons.get(i);
            ResourceLocation tex = button.isHoveredOrFocused() ? CARD_HOVERED : CARD;
            guiGraphics.blit(tex, button.getX(), button.getY(), 0, 0, CARD_W, CARD_H, CARD_W, CARD_H);
        }
        if (this.showRunChanges) {
            int boxW = 130;
            int boxH = 140;
            int x = this.leftPos + this.imageWidth - boxW - 8;
            int y = this.topPos + (this.imageHeight - boxH) / 2;
            guiGraphics.fill(x, y, x + boxW, y + boxH, 0xD0101010);
            guiGraphics.drawString(this.font, Component.literal("Run Changes"), x + 8, y + 8, 0xFFFFFF, false);
            int lineY = y + 24;
            for (Component line : this.menu.runChanges()) {
                guiGraphics.drawString(this.font, line, x + 8, lineY, 0xCFCFCF, false);
                lineY += 11;
                if (lineY > y + boxH - 10) break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String stageTitle = this.menu.stage() == 0 ? "Tarot Selection" : "Loot Selection";
        guiGraphics.drawString(this.font, Component.literal("Wave " + this.menu.waveNumber() + " - " + stageTitle), 12, 12, 0xF4E9FF, false);
        if (this.menu.ownerCanSelect()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.select_prompt"), 12, 22, 0xBFBFBF, false);
        }
        else {
            guiGraphics.drawString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.waiting_owner"), 12, 22, 0xBFBFBF, false);
        }

        for (int index = 0; index < this.menu.options().size() && index < this.optionButtons.size(); index++) {
            DungeonWaveMenu.WaveOptionView option = this.menu.options().get(index);
            Button button = this.optionButtons.get(index);
            int centerX = button.getX() + CARD_W / 2;
            int textX = button.getX() + 6;
            int textMaxChars = 12;
            int rowY = button.getY() + 8;
            for (String line : wrap(option.title().getString(), textMaxChars)) {
                drawCentered(guiGraphics, line, centerX, rowY, 0xF2E5C4);
                rowY += 10;
                if (rowY > button.getY() + 28) break;
            }
            guiGraphics.drawString(this.font, "◈", centerX - 3, button.getY() + 34, 0xEAD29E, false);
            String details = option.details().getString();
            rowY = button.getY() + 50;
            for (String line : wrap(details, textMaxChars)) {
                guiGraphics.drawString(this.font, line, textX, rowY, 0xDADADA, false);
                rowY += 10;
                if (rowY > button.getY() + CARD_H - 10) break;
            }
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

    private void selectReroll() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DungeonWaveMenu.REROLL_BUTTON_ID);
    }

    private void selectSkip() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DungeonWaveMenu.SKIP_BUTTON_ID);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.showRunChanges = !this.showRunChanges;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawCentered(GuiGraphics gg, String text, int x, int y, int color) {
        int w = this.font.width(text);
        gg.drawString(this.font, text, x - w / 2, y, color, false);
    }

    private List<String> wrap(String input, int max) {
        List<String> out = new ArrayList<>();
        String[] parts = input.split(" ");
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (current.isEmpty()) current.append(part);
            else if (current.length() + 1 + part.length() <= max) current.append(" ").append(part);
            else {
                out.add(current.toString());
                current = new StringBuilder(part);
            }
        }
        if (!current.isEmpty()) out.add(current.toString());
        return out;
    }

    private final class CardButton extends Button {
        private CardButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Intentionally blank: card art is rendered in screen background.
        }
    }
}
