package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.item.data.CatalystDefinition;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CatalystItem extends Item {

    private final CatalystDefinition definition;

    public CatalystItem(CatalystDefinition definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public CatalystDefinition definition() {
        return this.definition;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.catalyst.type").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.catalyst.positive", this.definition.positiveEffect().description()).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.catalyst.negative", this.definition.negativeEffect().description()).withStyle(ChatFormatting.RED));
        if (!this.definition.tags().isEmpty()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.gatewayexpansion.common.tags",
                    this.definition.tags().stream().collect(Collectors.joining(", "))
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
