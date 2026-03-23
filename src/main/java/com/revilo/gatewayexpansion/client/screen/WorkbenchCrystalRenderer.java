package com.revilo.gatewayexpansion.client.screen;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WorkbenchCrystalRenderer {

    public static final float BASE_SPIN_SPEED = 0.5F;

    private WorkbenchCrystalRenderer() {
    }

    public static void render(GuiGraphics guiGraphics, ItemStack stack, int absCenterX, int absCenterY, float partialTick, float scaleBoost, float spinSpeed) {
        if (stack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        Level level = minecraft.level;

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(absCenterX, absCenterY + 4.0F, 220.0F);

        float scale = 36.0F * scaleBoost;
        guiGraphics.pose().scale(scale, -scale, scale);

        float time = level != null ? (level.getGameTime() + partialTick) : (Util.getMillis() / 16.6667F);
        float yaw = (time * spinSpeed) % 360.0F;
        guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(18.0F));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(yaw));

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                guiGraphics.pose(),
                buffers,
                level,
                0
        );

        guiGraphics.pose().popPose();
        buffers.endBatch();
    }
}
