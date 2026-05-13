package com.revilo.gatesofavarice.client.screen;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.integration.LevelUpClientIntegration;
import com.revilo.gatesofavarice.network.DungeonCompletePayload;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class DungeonCompleteScreen extends Screen {

    private static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "textures/gui/dungeon-complete/dungeon-complete-gui.png");
    private static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "textures/gui/dungeon-complete/slot.png");
    private static final int GUI_W = 200;
    private static final int GUI_H = 166;
    private static final int REWARD_COLS = 4;
    private static final int REWARD_VISIBLE_ROWS = 6;
    private static final int REWARD_VISIBLE = REWARD_COLS * REWARD_VISIBLE_ROWS;

    private final DungeonCompletePayload payload;
    private int leftPos;
    private int topPos;
    private int rewardScroll;

    public DungeonCompleteScreen(DungeonCompletePayload payload) {
        super(Component.literal("Dungeon Complete"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - GUI_W) / 2;
        this.topPos = (this.height - GUI_H) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 1200.0F);
        guiGraphics.blit(GUI, this.leftPos, this.topPos, 0, 0, GUI_W, GUI_H, GUI_W, GUI_H);
        renderHeader(guiGraphics);
        renderStats(guiGraphics);
        renderRewards(guiGraphics);
        renderLevelBar(guiGraphics);
        renderRewardTooltip(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();
    }

    private void renderHeader(GuiGraphics guiGraphics) {
        guiGraphics.drawCenteredString(this.font, Component.literal("Survived").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), this.leftPos + 60, this.topPos + 11, 0xFFFFFF);
        drawCentered(guiGraphics, "Waves complete: " + this.payload.wavesComplete(), 60, 25, 0xFFE36B);
    }

    private void renderStats(GuiGraphics guiGraphics) {
        int x = this.leftPos + 10;
        int y = this.topPos + 42;
        String[] lines = {
                "Time Spent: " + formatTime(this.payload.timeSpentTicks()),
                "Level Points earnt: " + this.payload.levelPointsEarned(),
                "Coins earnt: " + this.payload.coinsEarned(),
                "Mobs Killed: " + this.payload.mobsKilled(),
                "Damage Delt: " + this.payload.damageDealt(),
                "Damage Receieved: " + this.payload.damageReceived(),
                "Experience earnt: " + this.payload.experienceEarned(),
                "Rarity level: " + this.payload.rarityLevel() + "%",
                "Qunatity level: " + this.payload.quantityLevel() + "%",
                "Mob Health: " + this.payload.mobHealth() + "%",
                "Mob Damage: " + this.payload.mobDamage() + "%"
        };
        int[] colors = {
                0xB7C5DD,
                0x8FD1FF,
                0xF6D37A,
                0xE39A6B,
                0xD28C8C,
                0xC592C9,
                0x98D8A8,
                0xCAA7E8,
                0xEFE09A,
                0xE29B9B,
                0xD98989
        };
        for (int i = 0; i < lines.length; i++) {
            drawScaled(guiGraphics, lines[i], x, y, 0.62F, colors[Math.min(i, colors.length - 1)]);
            y += 8;
        }
    }

    private void renderRewards(GuiGraphics guiGraphics) {
        List<ItemStack> rewards = this.payload.rewards();
        int startX = this.leftPos + 120;
        int startY = this.topPos + 24;
        for (int slot = 0; slot < REWARD_VISIBLE; slot++) {
            int col = slot % REWARD_COLS;
            int row = slot / REWARD_COLS;
            int x = startX + col * 18;
            int y = startY + row * 18;
            guiGraphics.blit(SLOT, x, y, 0, 0, 18, 18, 18, 18);

            int rewardIndex = this.rewardScroll * REWARD_COLS + slot;
            if (rewardIndex >= rewards.size()) {
                continue;
            }
            ItemStack stack = rewards.get(rewardIndex);
            guiGraphics.renderItem(stack, x + 1, y + 1);
            guiGraphics.renderItemDecorations(this.font, stack, x + 1, y + 1);
        }
    }

    private void renderLevelBar(GuiGraphics guiGraphics) {
        int targetWidth = 184;
        int targetHeight = 11;
        int barWidth = LevelUpClientIntegration.getLevelBarWidth();
        float scale = barWidth <= 0 ? 1.0F : targetWidth / (float) barWidth;
        int x = (this.width - targetWidth) / 2 - 1;
        int y = this.topPos + 148;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        boolean rendered = LevelUpClientIntegration.renderPlayerLevelBar(guiGraphics, 0, 0);
        guiGraphics.pose().popPose();
        if (!rendered) {
            guiGraphics.fill(x, y, x + targetWidth, y + targetHeight, 0xAA151515);
            guiGraphics.fill(x + 1, y + 1, x + targetWidth - 1, y + targetHeight - 1, 0xAA3A5EAA);
            guiGraphics.drawCenteredString(this.font, Component.literal("Level Progress"), this.leftPos + GUI_W / 2, y - 4, 0x6E5631);
        }
    }

    private void renderRewardTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int index = hoveredRewardIndex(mouseX, mouseY);
        if (index >= 0 && index < this.payload.rewards().size()) {
            guiGraphics.renderTooltip(this.font, this.payload.rewards().get(index), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, Mth.ceil((this.payload.rewards().size() - REWARD_VISIBLE) / (float) REWARD_COLS));
        if (maxScroll <= 0 || !isInRewardArea(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        this.rewardScroll = Mth.clamp(this.rewardScroll - (int) Math.signum(scrollY), 0, maxScroll);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int hoveredRewardIndex(double mouseX, double mouseY) {
        if (!isInRewardArea(mouseX, mouseY)) {
            return -1;
        }
        int localX = (int) mouseX - (this.leftPos + 120);
        int localY = (int) mouseY - (this.topPos + 24);
        int col = localX / 18;
        int row = localY / 18;
        if (col < 0 || col >= REWARD_COLS || row < 0 || row >= REWARD_VISIBLE_ROWS) {
            return -1;
        }
        return this.rewardScroll * REWARD_COLS + row * REWARD_COLS + col;
    }

    private boolean isInRewardArea(double mouseX, double mouseY) {
        return mouseX >= this.leftPos + 120 && mouseX < this.leftPos + 120 + REWARD_COLS * 18
                && mouseY >= this.topPos + 24 && mouseY < this.topPos + 24 + REWARD_VISIBLE_ROWS * 18;
    }

    private void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int y, int color) {
        guiGraphics.drawCenteredString(this.font, Component.literal(text), this.leftPos + centerX, this.topPos + y, color);
    }

    private void drawScaled(GuiGraphics guiGraphics, String text, int x, int y, float scale, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private static String formatTime(long ticks) {
        long seconds = Math.max(0L, ticks / 20L);
        long minutes = seconds / 60L;
        long remainder = seconds % 60L;
        return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, remainder);
    }
}
