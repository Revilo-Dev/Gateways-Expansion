package com.revilo.gatewayexpansion.item.data;

import net.minecraft.network.chat.Component;

public enum AugmentStatCategory {
    POPULATION,
    ELITE,
    SPEED,
    HEALTH,
    DAMAGE,
    EFFECT,
    CHAOS,
    LOOT;

    public Component displayName() {
        return Component.translatable("enum.gatewayexpansion.augment_stat." + this.name().toLowerCase());
    }
}
