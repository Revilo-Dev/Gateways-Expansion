package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShopGatewayPearlItem extends Item {

    private static final ResourceLocation SHOP_GATEWAY_ID = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "shopkeeper_bazaar");

    public ShopGatewayPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        DynamicHolder<Gateway> gate = GatewayRegistry.INSTANCE.holder(SHOP_GATEWAY_ID);
        if (!gate.isBound()) {
            player.sendSystemMessage(Component.translatable("message.gatewayexpansion.shopkeeper_spawn_failed"));
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        GatewayEntity entity = gate.get().createEntity(level, player);
        entity.setPos(pos.getX() + 0.5D, pos.getY() + (shape.isEmpty() ? 0.0D : shape.max(net.minecraft.core.Direction.Axis.Y)), pos.getZ() + 0.5D);

        double spacing = Math.max(0.0D, gate.get().rules().spacing());
        if (!level.getEntitiesOfClass(GatewayEntity.class, entity.getBoundingBox().inflate(spacing)).isEmpty()) {
            return InteractionResult.FAIL;
        }

        int y = 0;
        while (y++ < 4) {
            if (!level.noCollision(entity)) {
                entity.setPos(entity.getX(), entity.getY() + 1.0D, entity.getZ());
            }
            else {
                break;
            }
        }

        if (!level.noCollision(entity)) {
            player.sendSystemMessage(Component.translatable("message.gatewayexpansion.shopkeeper_spawn_failed"));
            return InteractionResult.FAIL;
        }

        ShopkeeperManager.markGatewayAnimation(entity);
        level.addFreshEntity(entity);
        entity.onGateCreated();

        ItemStack stack = context.getItemInHand();
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
