package com.revilo.gatewayexpansion.item.data;

import net.minecraft.network.chat.Component;

public enum CrystalTheme {
    UNDEAD,
    BEAST,
    ARCANE,
    NETHER;

    public Component displayName() {
        return Component.translatable("enum.gatewayexpansion.crystal_theme." + this.name().toLowerCase());
    }
}
