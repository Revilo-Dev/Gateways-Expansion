package com.revilo.gatesofavarice.workbench;

import com.revilo.gatesofavarice.item.AugmentItem;
import com.revilo.gatesofavarice.item.CatalystItem;
import com.revilo.gatesofavarice.item.CrystalItem;
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
    public static final int OUTPUT_SLOT = AUGMENT_SLOT_START + AUGMENT_SLOT_COUNT;
    public static final int CUSTOM_SLOT_COUNT = 2 + CATALYST_SLOT_COUNT + AUGMENT_SLOT_COUNT;

    public static final int CATALYST_START_X = 8;
    public static final int AUGMENT_START_X = 116;
    public static final int GRID_START_Y = 7;
    public static final int GRID_COLUMNS = 3;
    public static final int GRID_ROWS = 4;
    public static final int SLOT_SPACING = 18;
    public static final int CRYSTAL_X = 80;
    public static final int CRYSTAL_Y = 62;
    public static final int DISPLAY_CENTER_X = 88;
    public static final int DISPLAY_CENTER_Y = 32;
    public static final int OUTPUT_X = DISPLAY_CENTER_X - 8;
    public static final int OUTPUT_Y = DISPLAY_CENTER_Y - 8;

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
        if (slot == OUTPUT_SLOT) {
            return false;
        }
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
