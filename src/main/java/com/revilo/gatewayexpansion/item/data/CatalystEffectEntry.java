package com.revilo.gatewayexpansion.item.data;

public record CatalystEffectEntry(
        CatalystEffectType type,
        double magnitude,
        String description
) {
}
