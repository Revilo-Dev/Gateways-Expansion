package com.revilo.gatewayexpansion.workbench;

import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class GatewayWorkbenchSlots {

    public static final int CRYSTAL_SLOT = 0;
    public static final int CATALYST_SLOT_START = 1;
    public static final int CATALYST_SLOT_COUNT = 12;
    public static final int AUGMENT_SLOT_START = CATALYST_SLOT_START + CATALYST_SLOT_COUNT;
    public static final int AUGMENT_SLOT_COUNT = 12;
    public static final int CUSTOM_SLOT_COUNT = 1 + CATALYST_SLOT_COUNT + AUGMENT_SLOT_COUNT;

    public static final int CATALYST_START_X = 8;
    public static final int AUGMENT_START_X = 116;
    public static final int GRID_START_Y = 7;
    public static final int GRID_COLUMNS = 3;
    public static final int GRID_ROWS = 4;
    public static final int SLOT_SPACING = 18;
    public static final int CRYSTAL_X = 80;
    public static final int CRYSTAL_Y = 62;
    public static final int DISPLAY_CENTER_X = 87;
    public static final int DISPLAY_CENTER_Y = 31;

    private GatewayWorkbenchSlots() {
    }

    public static boolean isCrystalSlot(int slot) {
        return slot == CRYSTAL_SLOT;
    }

    public static boolean isCatalystSlot(int slot) {
        return slot >= CATALYST_SLOT_START && slot < CATALYST_SLOT_START + CATALYST_SLOT_COUNT;
    }

    public static boolean isAugmentSlot(int slot) {
        return slot >= AUGMENT_SLOT_START && slot < AUGMENT_SLOT_START + AUGMENT_SLOT_COUNT;
    }

    public static boolean mayPlace(int slot, ItemStack stack) {
        if (isCrystalSlot(slot)) {
            return stack.getItem() instanceof CrystalItem;
        }
        if (isCatalystSlot(slot)) {
            return stack.getItem() instanceof CatalystItem;
        }
        if (isAugmentSlot(slot)) {
            return stack.getItem() instanceof AugmentItem;
        }
        return false;
    }

    public static List<ItemStack> collectCatalysts(Container container) {
        return collect(container, CATALYST_SLOT_START, CATALYST_SLOT_COUNT);
    }

    public static List<ItemStack> collectAugments(Container container) {
        return collect(container, AUGMENT_SLOT_START, AUGMENT_SLOT_COUNT);
    }

    private static List<ItemStack> collect(Container container, int start, int count) {
        List<ItemStack> stacks = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) {
            ItemStack stack = container.getItem(start + slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}
