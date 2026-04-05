package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.catalyst.CatalystDefinition;
import com.revilo.gatewayexpansion.catalyst.CatalystStackData;
import com.revilo.gatewayexpansion.shop.GatewaySellValues;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CatalystItem extends Item {

    private final CatalystArchetype archetype;

    public CatalystItem(CatalystArchetype archetype, Properties properties) {
        super(properties);
        this.archetype = archetype;
    }

    public CatalystArchetype archetype() {
        return this.archetype;
    }

    public CatalystDefinition definition(ItemStack stack) {
        return CatalystStackData.getDefinition(stack, this.archetype);
    }

    @Override
    public Component getName(ItemStack stack) {
        CatalystDefinition definition = this.definition(stack);
        return definition != null ? Component.literal(definition.title()) : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CatalystDefinition definition = this.definition(stack);
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
        if (definition == null) {
            return;
        }
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.catalyst.positive", definition.positiveEffect().description()).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.catalyst.negative", definition.negativeEffect().description()).withStyle(ChatFormatting.RED));
        if (!definition.tags().isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.common.tags", String.join(", ", definition.tags())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            CatalystStackData.ensureDefinition(stack, this.archetype, serverLevel.random);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
