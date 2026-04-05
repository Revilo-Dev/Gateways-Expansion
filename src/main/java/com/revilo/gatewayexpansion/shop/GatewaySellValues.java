package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GatewaySellValues {

    private GatewaySellValues() {
    }

    public static boolean isSellable(ItemStack stack) {
        return getUnitValue(stack) > 0;
    }

    public static int getUnitValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        Item item = stack.getItem();
        if (item == ModItems.GRIMSTONE.get()) return 18;
        if (item == ModItems.MYSTIC_ESSENCE.get()) return 20;
        if (item == ModItems.SCRAP_METAL.get()) return 16;
        if (item == ModItems.MANA_GEMS.get()) return 48;
        if (item == ModItems.ARCANE_ESSENCE.get()) return 60;
        if (item == ModItems.MANASTONES.get()) return 75;
        if (item == ModItems.SOLAR_CRYSTAL.get()) return 140;
        if (item == ModItems.PRISMATIC_DIAMOND.get()) return 260;
        if (item == ModItems.DARK_ESSENCE.get()) return 210;
        if (item == ModItems.PRISMATIC_CORE.get()) return 540;
        if (item == ModItems.EASY_AUGMENT.get()) return 27;
        if (item == ModItems.MEDIUM_AUGMENT.get()) return 50;
        if (item == ModItems.HARD_AUGMENT.get()) return 60;
        if (item == ModItems.EXTREME_AUGMENT.get()) return 180;
        if (item == ModItems.TIME_CATALYST.get()) return 72;
        if (item == ModItems.STAT_CATALYST.get()) return 112;
        if (item == ModItems.LOOT_CATALYST.get()) return 92;
        if (item == ModItems.HIGHRISK_CATALYST.get()) return 245;
        return 0;
    }

    public static int getStackValue(ItemStack stack) {
        return getUnitValue(stack) * stack.getCount();
    }

    public static void appendSellValueTooltip(ItemStack stack, List<Component> tooltipComponents) {
        int value = getUnitValue(stack);
        if (value <= 0) {
            return;
        }

        tooltipComponents.add(Component.literal("◎ " + value + " Sell Value").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
