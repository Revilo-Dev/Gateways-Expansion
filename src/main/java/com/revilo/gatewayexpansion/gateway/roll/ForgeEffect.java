package com.revilo.gatewayexpansion.gateway.roll;

import net.minecraft.resources.ResourceLocation;

public record ForgeEffect(
        ForgeEffectType type,
        double value,
        double secondaryValue,
        ResourceLocation referenceId,
        String description
) {

    public static ForgeEffect of(ForgeEffectType type, double value, String description) {
        return new ForgeEffect(type, value, 0.0D, null, description);
    }

    public static ForgeEffect dual(ForgeEffectType type, double value, double secondaryValue, String description) {
        return new ForgeEffect(type, value, secondaryValue, null, description);
    }

    public static ForgeEffect ref(ForgeEffectType type, ResourceLocation referenceId, double value, double secondaryValue, String description) {
        return new ForgeEffect(type, value, secondaryValue, referenceId, description);
    }
}
