package com.revilo.gatesofavarice.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;

public class RarityTintedSmithingTemplateItem extends SmithingTemplateItem implements RarityTintedItemName {

    private final ChatFormatting nameColor;

    public RarityTintedSmithingTemplateItem(
            ChatFormatting nameColor,
            Component appliesTo,
            Component ingredients,
            Component upgradeDescription,
            Component baseSlotDescription,
            Component additionsSlotDescription,
            List<ResourceLocation> baseSlotEmptyIcons,
            List<ResourceLocation> additionalSlotEmptyIcons) {
        super(appliesTo, ingredients, upgradeDescription, baseSlotDescription, additionsSlotDescription, baseSlotEmptyIcons, additionalSlotEmptyIcons);
        this.nameColor = nameColor;
    }

    @Override
    public ChatFormatting nameColor() {
        return this.nameColor;
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.tintedName(stack, super.getName(stack));
    }
}
