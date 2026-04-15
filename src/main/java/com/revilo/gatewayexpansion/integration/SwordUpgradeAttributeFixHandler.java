package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.item.GatewaySwordItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class SwordUpgradeAttributeFixHandler {

    private SwordUpgradeAttributeFixHandler() {
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (!(crafted.getItem() instanceof GatewaySwordItem)) {
            return;
        }

        // Smithing can carry attribute components from the base stack; clear them so the new tier's defaults apply.
        crafted.remove(DataComponents.ATTRIBUTE_MODIFIERS);
    }
}
