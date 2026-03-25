package com.revilo.gatewayexpansion.gateway.builder;

import java.util.List;

public record GatewayPreview(
        int crystalTier,
        String crystalTheme,
        int crystalLevel,
        int playerLevel,
        boolean overleveled,
        int augmentCount,
        int catalystCount,
        String difficultyName,
        int rewardBonusPercent,
        int waves,
        String timePressure,
        List<String> previewLines
) {
    public GatewayPreview {
        previewLines = List.copyOf(previewLines);
    }
}
