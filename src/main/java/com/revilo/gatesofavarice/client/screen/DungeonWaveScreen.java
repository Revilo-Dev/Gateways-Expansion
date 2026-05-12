package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
            guiGraphics.drawCenteredString(this.font, Component.literal("Run Changes"), x + boxW / 2, y + 8, 0xFFFFFF);
            int lineY = y + 22;
            for (Component line : this.menu.runChanges()) {
                drawScaledCentered(guiGraphics, line.getString(), x + boxW / 2 - this.leftPos, lineY - this.topPos, 0.70F, runChangeColor(line.getString()));
                lineY += 9;
                if (lineY > y + boxH - 10) break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String stageTitle = this.menu.stage() == 0 ? "Tarot Selection" : (this.menu.stage() == 2 ? "Loadout Selection" : "Upgrade Selection");
        guiGraphics.drawString(this.font, Component.literal("Wave " + this.menu.waveNumber() + " - " + stageTitle), 12, 2, 0x7C5A14, false);
        if (this.menu.ownerCanSelect()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.select_prompt"), this.imageWidth / 2, 218, 0x6C6C6C);
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable("screen.gatesofavarice.dungeon_wave.waiting_owner"), this.imageWidth / 2, 218, 0x6C6C6C);
        }

        for (int index = 0; index < this.menu.options().size() && index < this.optionButtons.size(); index++) {
            DungeonWaveMenu.WaveOptionView option = this.menu.options().get(index);
            Button button = this.optionButtons.get(index);
            int centerX = button.getX() + CARD_W / 2 - this.leftPos;
            int textMaxChars = 13;
            if (this.menu.stage() == 0) {
                int rowY = button.getY() + 9 - this.topPos;
                for (String line : option.details().getString().split("\\n")) {
                    if (line.startsWith("---")) {
                        drawScaledCentered(guiGraphics, "----------", centerX, rowY, 0.58F, 0x7A6A52);
                        rowY += 7;
                        continue;
                    }
                    for (String wrapped : wrap(line, 15)) {
                        drawScaledCentered(guiGraphics, wrapped, centerX, rowY, 0.62F, tarotLineColor(line));
                        rowY += 7;
                    }
                    if (rowY > button.getY() + CARD_H - 7 - this.topPos) break;
                }
                continue;
            }
            int rowY = button.getY() + 8 - this.topPos;
            for (String line : wrap(option.title().getString(), textMaxChars)) {
                drawScaledCentered(guiGraphics, line, centerX, rowY, 0.75F, 0x7C5A14);
                rowY += 8;
                if (rowY > button.getY() + 28 - this.topPos) break;
            }
            if (!option.displayStack().isEmpty()) {
                int iconY = button.getY() + 29 - this.topPos;
                int iconX = option.secondaryDisplayStack().isEmpty() ? centerX - 8 : centerX - 18;
                guiGraphics.renderItem(option.displayStack(), iconX, iconY);
                if (!option.secondaryDisplayStack().isEmpty()) {
                    guiGraphics.renderItem(option.secondaryDisplayStack(), centerX + 2, iconY);
                }
            } else if (this.menu.stage() != 2) {
                drawScaledCentered(guiGraphics, "*", centerX, button.getY() + 35 - this.topPos, 0.75F, 0x6E6E6E);
            }
            String details = option.details().getString();
            rowY = button.getY() + (this.menu.stage() == 2 ? 49 : 50) - this.topPos;
            for (String raw : details.split("\\n")) {
                for (String line : wrap(raw, this.menu.stage() == 2 ? 16 : textMaxChars)) {
                    drawScaledCentered(guiGraphics, line, centerX, rowY, this.menu.stage() == 2 ? 0.56F : 0.75F, detailColor(line));
                    rowY += this.menu.stage() == 2 ? 7 : 8;
                }
                if (rowY > button.getY() + CARD_H - 10 - this.topPos) break;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        for (int i = 0; i < this.optionButtons.size() && i < this.menu.options().size(); i++) {
            DungeonWaveMenu.WaveOptionView option = this.menu.options().get(i);
            if (option.displayStack().isEmpty()) continue;
            Button button = this.optionButtons.get(i);
            int centerX = button.getX() + CARD_W / 2;
            int itemX = option.secondaryDisplayStack().isEmpty() ? centerX - 8 : centerX - 18;
            int itemY = button.getY() + 29;
            if (x >= itemX && x < itemX + 16 && y >= itemY && y < itemY + 16) {
                guiGraphics.renderTooltip(this.font, option.displayStack(), x, y);
                return;
            }
            if (!option.secondaryDisplayStack().isEmpty()) {
                int secondaryX = centerX + 2;
                if (x >= secondaryX && x < secondaryX + 16 && y >= itemY && y < itemY + 16) {
                    guiGraphics.renderTooltip(this.font, option.secondaryDisplayStack(), x, y);
                    return;
                }
            }
        }
    }

    private void selectOption(int optionIndex) {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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

    private void drawScaledCentered(GuiGraphics gg, String text, int x, int y, float scale, int color) {
        int w = this.font.width(text);
        gg.pose().pushPose();
        gg.pose().translate(x - (w * scale) / 2.0F, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(this.font, text, 0, 0, color, false);
        gg.pose().popPose();
    }

    private void drawScaled(GuiGraphics gg, String text, int x, int y, float scale, int color) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(this.font, text, 0, 0, color, false);
        gg.pose().popPose();
    }

    private int detailColor(String line) {
        if (isNegativeModifier(line)) return 0xAF3E3E;
        if (isPositiveModifier(line)) return 0x2F8E42;
        return 0x5A5A5A;
    }

    private int tarotLineColor(String line) {
        if (isNegativeModifier(line) || line.toLowerCase(java.util.Locale.ROOT).contains("hoard")
                || line.toLowerCase(java.util.Locale.ROOT).contains("tank")
                || line.toLowerCase(java.util.Locale.ROOT).contains("archer")
                || line.toLowerCase(java.util.Locale.ROOT).contains("assassin")) {
            return 0xAF3E3E;
        }
        if (isPositiveModifier(line)) return 0x2F8E42;
        return 0x5A5A5A;
    }

    private boolean isNegativeModifier(String line) {
        String normalized = line.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("elite")
                || normalized.contains("mob speed")
                || normalized.contains("mob health")
                || normalized.contains("mob damage")
                || normalized.contains("mob resistance")
                || normalized.contains("mob regen")
                || normalized.contains("spawn chance");
    }

    private boolean isPositiveModifier(String line) {
        String normalized = line.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("quantity")
                || normalized.contains("rarity")
                || normalized.contains("coins")
                || normalized.contains("xp")
                || normalized.contains("levels");
    }

    private int runChangeColor(String line) {
        String normalized = line.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("+elite spawns")
                || normalized.contains("+mob speed")
                || normalized.contains("+mob health")
                || normalized.contains("+mob damage")
                || normalized.contains("+mob resistance")
                || normalized.contains("+mob regen")
                || normalized.contains("+spawn chance")) {
            return 0xB13A3A;
        }
        if (normalized.contains("+quantity")
                || normalized.contains("+rarity")
                || normalized.contains("+coins")
                || normalized.contains("+xp")
                || normalized.contains("+levels")) {
            return 0x2F8E42;
        }
        return 0xCFCFCF;
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
