package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.integration.ModCompat;
import com.revilo.gatesofavarice.shop.GatewaySellValues;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

public class ShopGatewayPearlItem extends Item {

    private static final ResourceLocation SHOP_GATEWAY_ID = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "shopkeeper_bazaar");
    private static final String GATEWAY_REGISTRY_CLASS = "dev.shadowsoffire.gateways.gate.GatewayRegistry";

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
        if (!ModCompat.isAnyLoaded("gateways")) {
            player.sendSystemMessage(Component.translatable("message.gatesofavarice.shopkeeper_spawn_failed"));
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        Entity entity = createGatewayEntity(level, player);
        if (entity == null) {
            player.sendSystemMessage(Component.translatable("message.gatesofavarice.shopkeeper_spawn_failed"));
            return InteractionResult.FAIL;
        }
        double spacing = resolveGatewaySpacing();
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        entity.setPos(pos.getX() + 0.5D, pos.getY() + (shape.isEmpty() ? 0.0D : shape.max(net.minecraft.core.Direction.Axis.Y)), pos.getZ() + 0.5D);
        if (!level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(spacing), ShopGatewayPearlItem::isGatewayEntity).isEmpty()) {
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
            player.sendSystemMessage(Component.translatable("message.gatesofavarice.shopkeeper_spawn_failed"));
            return InteractionResult.FAIL;
        }

        ShopkeeperManager.markGatewayAnimation(entity);
        level.addFreshEntity(entity);
        invokeOnGateCreated(entity);

        ItemStack stack = context.getItemInHand();
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
    }

    private static Entity createGatewayEntity(Level level, Player player) {
        try {
            Class<?> registryClass = Class.forName(GATEWAY_REGISTRY_CLASS);
            Object instance = registryClass.getField("INSTANCE").get(null);
            Object holder = registryClass.getMethod("holder", ResourceLocation.class).invoke(instance, SHOP_GATEWAY_ID);
            Object bound = holder.getClass().getMethod("isBound").invoke(holder);
            if (!(bound instanceof Boolean b) || !b) {
                return null;
            }
            Object gateway = holder.getClass().getMethod("get").invoke(holder);
            Object entity = gateway.getClass().getMethod("createEntity", Level.class, Player.class).invoke(gateway, level, player);
            return entity instanceof Entity e ? e : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static double resolveGatewaySpacing() {
        try {
            Class<?> registryClass = Class.forName(GATEWAY_REGISTRY_CLASS);
            Object instance = registryClass.getField("INSTANCE").get(null);
            Object holder = registryClass.getMethod("holder", ResourceLocation.class).invoke(instance, SHOP_GATEWAY_ID);
            Object bound = holder.getClass().getMethod("isBound").invoke(holder);
            if (!(bound instanceof Boolean b) || !b) {
                return 0.0D;
            }
            Object gateway = holder.getClass().getMethod("get").invoke(holder);
            Object rules = gateway.getClass().getMethod("rules").invoke(gateway);
            Object spacing = rules.getClass().getMethod("spacing").invoke(rules);
            return spacing instanceof Number n ? Math.max(0.0D, n.doubleValue()) : 0.0D;
        } catch (ReflectiveOperationException ignored) {
            return 0.0D;
        }
    }

    private static void invokeOnGateCreated(Entity entity) {
        try {
            entity.getClass().getMethod("onGateCreated").invoke(entity);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean isGatewayEntity(Entity entity) {
        ResourceLocation typeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return typeId != null && "gateways".equals(typeId.getNamespace()) && typeId.getPath().contains("gateway");
    }
}
