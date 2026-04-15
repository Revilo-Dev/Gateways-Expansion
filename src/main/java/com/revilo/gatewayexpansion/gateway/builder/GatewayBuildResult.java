package com.revilo.gatewayexpansion.gateway.builder;

import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record GatewayBuildResult(
        ResourceLocation gatewayId,
        String name,
        CrystalTheme theme,
        int color,
        int crystalLevel,
        int crystalTier,
        int playerLevel,
        boolean overleveled,
        int waveCount,
        int difficultyEstimate,
        double rewardMultiplier,
        int flatCoinBonus,
        double coinRewardMultiplier,
        double levelXpMultiplier,
        double experienceRewardMultiplier,
        double thornsDamage,
        double rarityRewardMultiplier,
        int rareRewardDrops,
        int epicRewardDrops,
        int legendaryRewardDrops,
        int finalExperienceReward,
        List<String> augmentSummary,
        List<String> catalystSummary,
        List<String> finalRollSummary,
        List<String> debugLines,
        NormalGateway gateway,
        String gatewayJson
) {
    public GatewayBuildResult {
        augmentSummary = List.copyOf(augmentSummary);
        catalystSummary = List.copyOf(catalystSummary);
        finalRollSummary = List.copyOf(finalRollSummary);
        debugLines = List.copyOf(debugLines);
    }
}
