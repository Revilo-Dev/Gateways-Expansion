package com.revilo.gatesofavarice.catalyst;

import com.revilo.gatesofavarice.gateway.roll.ForgeEffect;
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
