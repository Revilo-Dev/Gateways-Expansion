package com.revilo.gatewayexpansion.client.screen;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.menu.GatewayWorkbenchMenu;
import com.revilo.gatewayexpansion.workbench.GatewayWorkbenchForgeLogic;
import com.revilo.gatewayexpansion.workbench.GatewayWorkbenchSlots;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GatewayWorkbenchScreen extends AbstractContainerScreen<GatewayWorkbenchMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "textures/gui/workbench.png");

    private float crystalHoverScale = 1.0F;

    public GatewayWorkbenchScreen(GatewayWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        boolean crystalHovered = this.isHoveringCrystal(mouseX, mouseY);
        this.crystalHoverScale = Mth.lerp(0.25F, this.crystalHoverScale, crystalHovered ? 1.12F : 1.0F);

        this.renderOrbitingItems(guiGraphics, partialTick);
        this.renderCenterCrystal(guiGraphics, crystalHovered);

        if (crystalHovered && !this.menu.getCrystalStack().isEmpty()) {
            this.renderCrystalTooltip(guiGraphics, mouseX, mouseY);
        } else {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }

        this.renderLevelWarning(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The workbench texture carries the layout; keep labels hidden for a cleaner centerpiece.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
                && this.isHoveringCrystal(mouseX, mouseY)
                && this.menu.canForge()
                && this.menu.getCarried().isEmpty()
                && !hasShiftDown()
                && this.minecraft != null
                && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, GatewayWorkbenchMenu.FORGE_BUTTON_ID);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderCenterCrystal(GuiGraphics guiGraphics, boolean hovered) {
        ItemStack crystal = this.menu.getCrystalStack();
        if (crystal.isEmpty()) {
            return;
        }

        int centerX = this.leftPos + GatewayWorkbenchSlots.DISPLAY_CENTER_X;
        int centerY = this.topPos + GatewayWorkbenchSlots.DISPLAY_CENTER_Y;
        WorkbenchCrystalRenderer.render(guiGraphics, crystal, centerX, centerY, this.minecraft == null ? 0.0F : this.minecraft.getTimer().getGameTimeDeltaPartialTick(false), this.crystalHoverScale);

    }

    private void renderOrbitingItems(GuiGraphics guiGraphics, float partialTick) {
        float time = (this.minecraft != null && this.minecraft.player != null) ? this.minecraft.player.tickCount + partialTick : partialTick;
        int centerX = this.leftPos + GatewayWorkbenchSlots.DISPLAY_CENTER_X;
        int centerY = this.topPos + GatewayWorkbenchSlots.DISPLAY_CENTER_Y;

        List<ItemStack> orbitStacks = new ArrayList<>();
        orbitStacks.addAll(this.menu.getCatalystStacks());
        orbitStacks.addAll(this.menu.getAugmentStacks());
        this.renderOrbitGroup(guiGraphics, orbitStacks, centerX, centerY, 24.0D, 0.014D, time);
    }

    private void renderOrbitGroup(GuiGraphics guiGraphics, List<ItemStack> stacks, int centerX, int centerY, double radius, double speed, float time) {
        int stackCount = stacks.size();
        if (stackCount == 0) {
            return;
        }

        for (int index = 0; index < stackCount; index++) {
            double angle = (Math.PI * 2D / stackCount) * index - (time * speed);
            int renderX = Mth.floor(centerX + Math.cos(angle) * radius - 8.0D);
            int renderY = Mth.floor(centerY + Math.sin(angle) * radius - 8.0D);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 150.0F);
            guiGraphics.renderItem(stacks.get(index), renderX, renderY);
            guiGraphics.pose().popPose();
        }
    }

    private void renderCrystalTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack crystal = this.menu.getCrystalStack();
        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, crystal));
        GatewayWorkbenchForgeLogic.PreviewData previewData = this.menu.getPreviewData();
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.preview_label"));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.preview_tier", previewData.crystalTier()));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.preview_augments", previewData.augmentCount()));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.preview_catalysts", previewData.catalystCount()));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.difficulty", previewData.difficultyName()));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.reward", "+" + previewData.rewardBonusPercent() + "%"));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.waves", previewData.waves()));
        tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.time_pressure", previewData.timePressure()));
        if (this.menu.canForge()) {
            tooltip.add(Component.translatable("screen.gatewayexpansion.gateway_workbench.click_to_forge"));
        }
        guiGraphics.renderTooltip(this.font, tooltip, crystal.getTooltipImage(), crystal, mouseX, mouseY);
    }

    private void renderLevelWarning(GuiGraphics guiGraphics) {
        // Reserved for future crystal-level versus player-level warning logic.
    }

    private boolean isHoveringCrystal(double mouseX, double mouseY) {
        int left = this.leftPos + GatewayWorkbenchSlots.DISPLAY_CENTER_X - 26;
        int top = this.topPos + GatewayWorkbenchSlots.DISPLAY_CENTER_Y - 26;
        return mouseX >= left && mouseX <= left + 52 && mouseY >= top && mouseY <= top + 52;
    }
}
