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

    private static final float CRYSTAL_HEIGHT = 1.03F;
    private static final float ORBIT_HEIGHT = 1.035F;
    private static final float CRYSTAL_SCALE = 0.9F;
    private static final float ORBIT_SCALE = 0.42F;
    private static final float ORBIT_RADIUS = 0.42F;

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
        poseStack.translate(0.5D, CRYSTAL_HEIGHT + (Mth.sin(time * 0.09F) * 0.01F), 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((time * 1.8F) % 360.0F));
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        minecraft.getItemRenderer().renderStatic(
                crystal,
                ItemDisplayContext.GROUND,
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
        List<ItemStack> orbitStacks = buildInterleavedOrbitStacks(blockEntity);
        if (orbitStacks.isEmpty()) {
            return;
        }

        for (int i = 0; i < orbitStacks.size(); i++) {
            ItemStack stack = orbitStacks.get(i);
            double angle = ((Math.PI * 2D) / orbitStacks.size()) * i + (time * 0.045D);
            float x = 0.5F + (float) Math.cos(angle) * ORBIT_RADIUS;
            float z = 0.5F + (float) Math.sin(angle) * ORBIT_RADIUS;

            poseStack.pushPose();
            poseStack.translate(x, ORBIT_HEIGHT, z);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-Math.toDegrees(angle)) + 90.0F));
            poseStack.scale(ORBIT_SCALE, ORBIT_SCALE, ORBIT_SCALE);
            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
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

    private List<ItemStack> buildInterleavedOrbitStacks(GatewayWorkbenchBlockEntity blockEntity) {
        List<ItemStack> catalysts = GatewayWorkbenchSlots.collectCatalysts(blockEntity);
        List<ItemStack> augments = GatewayWorkbenchSlots.collectAugments(blockEntity);
        List<ItemStack> orbitStacks = new ArrayList<>(catalysts.size() + augments.size());
        int max = Math.max(catalysts.size(), augments.size());
        for (int index = 0; index < max; index++) {
            if (index < catalysts.size()) {
                orbitStacks.add(catalysts.get(index));
            }
            if (index < augments.size()) {
                orbitStacks.add(augments.get(index));
            }
        }
        return orbitStacks;
    }
}
