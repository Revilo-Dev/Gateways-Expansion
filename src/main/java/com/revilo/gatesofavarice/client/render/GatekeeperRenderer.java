package com.revilo.gatesofavarice.client.render;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.client.model.GatekeeperModel;
import com.revilo.gatesofavarice.entity.GatekeeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GatekeeperRenderer extends MobRenderer<GatekeeperEntity, GatekeeperModel<GatekeeperEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "textures/entity/gate_keeper.png");

    public GatekeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new GatekeeperModel<>(context.bakeLayer(GatekeeperModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GatekeeperEntity entity) {
        return TEXTURE;
    }
}
