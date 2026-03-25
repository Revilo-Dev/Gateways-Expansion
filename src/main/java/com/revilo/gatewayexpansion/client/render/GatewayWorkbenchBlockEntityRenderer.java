package com.revilo.gatewayexpansion.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.revilo.gatewayexpansion.block.entity.GatewayWorkbenchBlockEntity;
import com.revilo.gatewayexpansion.workbench.GatewayWorkbenchSlots;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GatewayWorkbenchBlockEntityRenderer implements BlockEntityRenderer<GatewayWorkbenchBlockEntity> {

    public GatewayWorkbenchBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GatewayWorkbenchBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        ItemStack crystal = blockEntity.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (crystal.isEmpty()) {
            return;
        }

        float time = blockEntity.getLevel().getGameTime() + partialTick;
        renderCrystal(blockEntity, crystal, time, poseStack, buffer, packedLight);
        renderOrbitItems(blockEntity, time, poseStack, buffer, packedLight);
    }

    private void renderCrystal(GatewayWorkbenchBlockEntity blockEntity, ItemStack crystal, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.15D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((time * 2.8F) % 360.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(18.0F));
        poseStack.scale(0.9F, 0.9F, 0.9F);
        minecraft.getItemRenderer().renderStatic(
                crystal,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();
    }

    private void renderOrbitItems(GatewayWorkbenchBlockEntity blockEntity, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        List<ItemStack> orbitStacks = new ArrayList<>();
        orbitStacks.addAll(GatewayWorkbenchSlots.collectCatalysts(blockEntity));
        orbitStacks.addAll(GatewayWorkbenchSlots.collectAugments(blockEntity));
        if (orbitStacks.isEmpty()) {
            return;
        }

        float radius = 0.42F;
        for (int i = 0; i < orbitStacks.size(); i++) {
            ItemStack stack = orbitStacks.get(i);
            double angle = ((Math.PI * 2D) / orbitStacks.size()) * i + (time * 0.045D);
            float x = 0.5F + (float) Math.cos(angle) * radius;
            float z = 0.5F + (float) Math.sin(angle) * radius;
            float bob = 0.015F * Mth.sin((time * 0.12F) + i);

            poseStack.pushPose();
            poseStack.translate(x, 1.02F + bob, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((time * 4.0F) + (i * 35.0F)));
            poseStack.scale(0.36F, 0.36F, 0.36F);
            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    i
            );
            poseStack.popPose();
        }
    }
}
