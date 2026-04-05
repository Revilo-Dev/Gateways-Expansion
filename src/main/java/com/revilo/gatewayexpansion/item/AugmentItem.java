package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.augment.AugmentDefinition;
import com.revilo.gatewayexpansion.augment.AugmentStackData;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.shop.GatewaySellValues;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AugmentItem extends Item {

    private final AugmentDifficultyTier difficultyTier;

    public AugmentItem(AugmentDifficultyTier difficultyTier, Properties properties) {
        super(properties);
        this.difficultyTier = difficultyTier;
    }

    public AugmentDifficultyTier difficultyTier() {
        return this.difficultyTier;
    }

    public AugmentDefinition definition(ItemStack stack) {
        return AugmentStackData.getDefinition(stack, this.difficultyTier);
    }

    @Override
    public Component getName(ItemStack stack) {
        AugmentDefinition definition = this.definition(stack);
        return definition != null ? Component.literal(definition.title()) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        AugmentDefinition definition = this.definition(stack);
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.augment.type").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.augment.difficulty", this.difficultyTier.displayName()).withStyle(ChatFormatting.GRAY));
        if (definition == null) {
            return;
        }
        for (var effect : definition.modifierEffects()) {
            tooltipComponents.add(Component.literal(effect.description()).withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.literal(definition.rewardEffect().description()).withStyle(ChatFormatting.GOLD));
        if (!definition.tags().isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.common.tags", String.join(", ", definition.tags())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            AugmentStackData.ensureDefinition(stack, this.difficultyTier, serverLevel.random);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
