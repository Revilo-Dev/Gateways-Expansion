package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.shop.GatewaySellValues;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MagnetItem extends Item implements RarityTintedItemName {

    private final ChatFormatting nameColor;
    private final int bonusRange;
    private final int pullSpeed;
    private final int runeSlots;

    public MagnetItem(ChatFormatting nameColor, int bonusRange, int pullSpeed, int runeSlots, Properties properties) {
        super(properties);
        this.nameColor = nameColor;
        this.bonusRange = bonusRange;
        this.pullSpeed = pullSpeed;
        this.runeSlots = runeSlots;
    }

    @Override
    public ChatFormatting nameColor() {
        return this.nameColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.tintedName(stack, super.getName(stack));
    }

    public int bonusRange() {
        return this.bonusRange;
    }

    public int pullSpeed() {
        return this.pullSpeed;
    }

    public double attractionRange() {
        return 3.0D + this.bonusRange;
    }

    public double attractionForce() {
        return 0.025D + (this.pullSpeed * 0.03D);
    }

    public int runeSlots() {
        return this.runeSlots;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        RunicItemSupport.ensureRunicData(stack, this.runeSlots());
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("+" + (this.bonusRange + 2) + " block magnet range").withStyle(ChatFormatting.AQUA));
        if (this.pullSpeed > 0) {
            tooltipComponents.add(Component.literal("+" + this.pullSpeed + " pull speed").withStyle(ChatFormatting.GREEN));
        }
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
    }
}
