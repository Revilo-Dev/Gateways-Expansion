package com.revilo.gatewayexpansion.catalyst;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import java.util.Set;

public record CatalystDefinition(
        String id,
        String title,
        ForgeEffect positiveEffect,
        ForgeEffect negativeEffect,
        Set<String> tags
) {
    public CatalystDefinition {
        tags = Set.copyOf(tags);
    }
}
