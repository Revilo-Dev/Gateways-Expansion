package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.registry.ModMobEffects;
import com.revilo.gatewayexpansion.shop.GatewaySellValues;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class StabilityPearlItem extends Item {

    private static final int RANGE = 96;
    private static final int TIME_EXTENSION_TICKS = 200;
    private static final int HEALTH_PENALTY_TICKS = 200;
    private static final int USE_DURATION = 32;

    public StabilityPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (findGateway(player) == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.gatewayexpansion.no_active_gateway"));
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof Player player)) {
            return stack;
        }

        GatewayEntity gateway = findGateway(player);
        if (gateway == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.gatewayexpansion.no_active_gateway"));
            }
            return stack;
        }

        gateway.getEntityData().set(GatewayEntity.TICKS_ACTIVE, Math.max(0, gateway.getTicksActive() - TIME_EXTENSION_TICKS));
        player.addEffect(new MobEffectInstance(ModMobEffects.STABILITY_DRAIN, HEALTH_PENALTY_TICKS, 0, false, true, true));
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));

        if (!level.isClientSide) {
            player.sendSystemMessage(Component.translatable("message.gatewayexpansion.gateway_time_extended", TIME_EXTENSION_TICKS / 20));
        }

        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.stability_pearl.effect").withStyle(ChatFormatting.AQUA));
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
    }

    private static GatewayEntity findGateway(Player player) {
        return player.level().getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(RANGE)).stream()
                .filter(GatewayEntity::isValid)
                .filter(gateway -> !gateway.isRemoved())
                .filter(gateway -> !ShopkeeperManager.isGatewayAnimation(gateway))
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
}
