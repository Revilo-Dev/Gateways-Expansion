package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.integration.ModCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class RunicItemSupport {

    private static final String RUNE_SLOTS_KEY = "rune_slots";
    private static final String RUNIC_SLOTS_KEY = "runic_slots";
    private static final String MAX_RUNE_SLOTS_KEY = "max_rune_slots";

    private RunicItemSupport() {
    }

    public static void ensureRunicData(ItemStack stack, int runeSlots) {
        if (!ModCompat.isRunicLoaded() || runeSlots <= 0) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(RUNE_SLOTS_KEY, runeSlots);
            tag.putInt(RUNIC_SLOTS_KEY, runeSlots);
            tag.putInt(MAX_RUNE_SLOTS_KEY, runeSlots);
        });
    }
}
