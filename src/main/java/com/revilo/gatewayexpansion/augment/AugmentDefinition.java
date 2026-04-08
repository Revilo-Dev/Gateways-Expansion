package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import java.util.List;
import java.util.Set;

public record AugmentDefinition(
        String id,
        String title,
        AugmentDifficultyTier difficultyTier,
        List<ForgeEffect> modifierEffects,
        List<ForgeEffect> rewardEffects,
        Set<String> tags
) {
    public AugmentDefinition {
        modifierEffects = List.copyOf(modifierEffects);
        rewardEffects = List.copyOf(rewardEffects);
        tags = Set.copyOf(tags);
    }
}
