package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.item.data.AugmentDefinition;
import com.revilo.gatewayexpansion.item.data.AugmentStatEntry;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AugmentItem extends Item {

    private final AugmentDefinition definition;

    public AugmentItem(AugmentDefinition definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public AugmentDefinition definition() {
        return this.definition;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.augment.type").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.augment.difficulty", this.definition.difficultyTier().displayName()).withStyle(ChatFormatting.GRAY));

        for (AugmentStatEntry statEntry : this.definition.statEntries()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.gatewayexpansion.augment.stat_line",
                    statEntry.category().displayName(),
                    statEntry.magnitude(),
                    statEntry.detail()
            ).withStyle(ChatFormatting.GRAY));
        }

        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.augment.reward_bonus", this.definition.rewardBonusPercent()).withStyle(ChatFormatting.GOLD));
        if (!this.definition.tags().isEmpty()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.gatewayexpansion.common.tags",
                    this.definition.tags().stream().collect(Collectors.joining(", "))
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
