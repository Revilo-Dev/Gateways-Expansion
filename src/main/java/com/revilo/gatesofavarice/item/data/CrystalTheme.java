package com.revilo.gatesofavarice.item.data;

import net.minecraft.network.chat.Component;

public enum CrystalTheme {
    UNDEAD,
    BEAST,
    ARCANE,
    NETHER,
    RAIDER,
    WILD;

    public Component displayName() {
        return Component.translatable("enum.gatesofavarice.crystal_theme." + this.name().toLowerCase());
    }
}
