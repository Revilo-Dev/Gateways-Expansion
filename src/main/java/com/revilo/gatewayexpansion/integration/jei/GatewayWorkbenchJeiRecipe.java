package com.revilo.gatewayexpansion.integration.jei;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record GatewayWorkbenchJeiRecipe(
        Component title,
        List<ItemStack> crystals,
        List<ItemStack> components,
        ItemStack output) {
}
