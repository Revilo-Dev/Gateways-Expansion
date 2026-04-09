package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.registry.ModMobEffects;
import com.revilo.gatewayexpansion.integration.StabilityPearlHandler;
import com.revilo.gatewayexpansion.shop.GatewaySellValues;
import com.revilo.gatewayexpansion.shop.ShopkeeperManager;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private static final int TIME_EXTENSION_TICKS = 300;
    private static final int HEALTH_PENALTY_TICKS = Integer.MAX_VALUE;
    public StabilityPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        GatewayEntity gateway = findGateway(player);
        if (gateway == null) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.gatewayexpansion.no_active_gateway"));
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            applyStabilityPearl((ServerLevel) level, player, stack, gateway);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
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

    private static void applyStabilityPearl(ServerLevel level, Player player, ItemStack stack, GatewayEntity gateway) {
        gateway.getEntityData().set(GatewayEntity.TICKS_ACTIVE, Math.max(0, gateway.getTicksActive() - TIME_EXTENSION_TICKS));
        player.addEffect(new MobEffectInstance(ModMobEffects.STABILITY_DRAIN, HEALTH_PENALTY_TICKS, 0, false, true, true));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            StabilityPearlHandler.linkToGateway(serverPlayer, gateway);
        }
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        spawnShatterEffect(level, player, stack);
        player.sendSystemMessage(Component.translatable("message.gatewayexpansion.gateway_time_extended", TIME_EXTENSION_TICKS / 20));
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
    }

    private static void spawnShatterEffect(ServerLevel level, Player player, ItemStack stack) {
        ItemStack particleStack = stack.copy();
        particleStack.setCount(1);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                player.getX(), player.getEyeY() - 0.2D, player.getZ(),
                16,
                0.25D, 0.2D, 0.25D,
                0.08D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.15F);
    }
}
