package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.shop.GatewaySellValues;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class MagnetItem extends Item {

    private static final String RUNE_SLOTS_KEY = "rune_slots";
    private static final String RUNIC_SLOTS_KEY = "runic_slots";
    private static final String MAX_RUNE_SLOTS_KEY = "max_rune_slots";
    private final int bonusRange;
    private final int pullSpeed;

    public MagnetItem(int bonusRange, int pullSpeed, Properties properties) {
        super(properties);
        this.bonusRange = bonusRange;
        this.pullSpeed = pullSpeed;
    }

    public int bonusRange() {
        return this.bonusRange;
    }

    public int pullSpeed() {
        return this.pullSpeed;
    }

    public double attractionRange() {
        return 1.0D + this.bonusRange;
    }

    public double attractionForce() {
        return 0.025D + (this.pullSpeed * 0.01D);
    }

    public int runeSlots() {
        return this.bonusRange >= 8 ? 3 : 2;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        ensureRunicData(stack);
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("+" + this.bonusRange + " block magnet range").withStyle(ChatFormatting.AQUA));
        if (this.pullSpeed > 0) {
            tooltipComponents.add(Component.literal("+" + this.pullSpeed + " pull speed").withStyle(ChatFormatting.GREEN));
        }
        tooltipComponents.add(Component.literal(this.runeSlots() + " rune slots").withStyle(ChatFormatting.LIGHT_PURPLE));
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
    }

    public static void ensureRunicData(ItemStack stack) {
        if (!(stack.getItem() instanceof MagnetItem magnet) || !ModCompat.isAnyLoaded("runic")) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(RUNE_SLOTS_KEY, magnet.runeSlots());
            tag.putInt(RUNIC_SLOTS_KEY, magnet.runeSlots());
            tag.putInt(MAX_RUNE_SLOTS_KEY, magnet.runeSlots());
        });
    }
}
