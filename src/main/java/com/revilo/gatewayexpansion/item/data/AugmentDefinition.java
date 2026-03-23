package com.revilo.gatewayexpansion.item.data;

import java.util.List;

public record AugmentDefinition(
        String id,
        AugmentDifficultyTier difficultyTier,
        List<AugmentStatEntry> statEntries,
        int rewardBonusPercent,
        List<String> tags
) {
    public AugmentDefinition {
        statEntries = List.copyOf(statEntries);
        tags = List.copyOf(tags);
    }
}
