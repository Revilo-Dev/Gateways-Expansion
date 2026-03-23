package com.revilo.gatewayexpansion.item.data;

import java.util.List;

public record CatalystDefinition(
        String id,
        String positiveEffect,
        String negativeEffect,
        List<String> tags
) {
    public CatalystDefinition {
        tags = List.copyOf(tags);
    }
}
