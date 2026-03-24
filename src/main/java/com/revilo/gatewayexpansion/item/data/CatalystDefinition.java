package com.revilo.gatewayexpansion.item.data;

import java.util.List;

public record CatalystDefinition(
        String id,
        CatalystEffectEntry positiveEffect,
        CatalystEffectEntry negativeEffect,
        List<String> tags
) {
    public CatalystDefinition {
        tags = List.copyOf(tags);
    }
}
