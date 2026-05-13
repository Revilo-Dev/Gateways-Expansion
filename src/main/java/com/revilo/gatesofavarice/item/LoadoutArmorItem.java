package com.revilo.gatesofavarice.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class LoadoutArmorItem extends ArmorItem {
    private final String setId;

    public LoadoutArmorItem(net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> material, Type type, Properties properties, String setId) {
        super(material, type, properties);
        this.setId = setId;
    }

    public String setId() {
        return this.setId;
    }
}
