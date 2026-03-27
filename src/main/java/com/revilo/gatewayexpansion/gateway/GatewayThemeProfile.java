package com.revilo.gatewayexpansion.gateway;

import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record GatewayThemeProfile(
        CrystalTheme theme,
        int color,
        String prefix,
        String rewardKeyRoot,
        List<ResourceLocation> effects,
        List<GateTypeProfile> gateTypes,
        ResourceLocation waveLoot,
        ResourceLocation commonLoot,
        ResourceLocation rareLoot
) {

    public static GatewayThemeProfile forTheme(CrystalTheme theme, int level) {
        String tierSuffix = "tier_" + rewardTier(level);
        return switch (theme) {
            case UNDEAD -> new GatewayThemeProfile(
                    theme,
                    0x6AA36A,
                    "Grave",
                    "grave",
                    List.of(ResourceLocation.withDefaultNamespace("strength"), ResourceLocation.withDefaultNamespace("resistance"), ResourceLocation.withDefaultNamespace("slowness")),
                    List.of(
                            gateType("ossuary", "Ossuary", "rewards/types/undead/ossuary"),
                            gateType("plague", "Plague", "rewards/types/undead/plague"),
                            gateType("mooncrypt", "Mooncrypt", "rewards/types/undead/mooncrypt")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/waves/undead"),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/undead/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/undead/" + tierSuffix));
            case BEAST -> new GatewayThemeProfile(
                    theme,
                    0xB87B32,
                    "Feral",
                    "feral",
                    List.of(ResourceLocation.withDefaultNamespace("speed"), ResourceLocation.withDefaultNamespace("jump_boost"), ResourceLocation.withDefaultNamespace("regeneration")),
                    List.of(
                            gateType("pack", "Pack", "rewards/types/beast/pack"),
                            gateType("hive", "Hive", "rewards/types/beast/hive"),
                            gateType("thornwild", "Thornwild", "rewards/types/beast/thornwild")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/waves/beast"),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/beast/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/beast/" + tierSuffix));
            case ARCANE -> new GatewayThemeProfile(
                    theme,
                    0x3EA5D9,
                    "Astral",
                    "astral",
                    List.of(ResourceLocation.withDefaultNamespace("glowing"), ResourceLocation.withDefaultNamespace("levitation"), ResourceLocation.withDefaultNamespace("regeneration")),
                    List.of(
                            gateType("rift", "Rift", "rewards/types/arcane/rift"),
                            gateType("observatory", "Observatory", "rewards/types/arcane/observatory"),
                            gateType("stormglass", "Stormglass", "rewards/types/arcane/stormglass")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/waves/arcane"),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/arcane/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/arcane/" + tierSuffix));
            case NETHER -> new GatewayThemeProfile(
                    theme,
                    0xD9492B,
                    "Infernal",
                    "infernal",
                    List.of(ResourceLocation.withDefaultNamespace("fire_resistance"), ResourceLocation.withDefaultNamespace("strength"), ResourceLocation.withDefaultNamespace("speed")),
                    List.of(
                            gateType("forge", "Forge", "rewards/types/nether/forge"),
                            gateType("bastion", "Bastion", "rewards/types/nether/bastion"),
                            gateType("ashfall", "Ashfall", "rewards/types/nether/ashfall")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/waves/nether"),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/nether/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "rewards/finals/nether/" + tierSuffix));
        };
    }

    public GateTypeProfile gateType(long seed) {
        return this.gateTypes.get((int) Math.floorMod(seed, this.gateTypes.size()));
    }

    public String waveDescKey() {
        return "rewards.gatewayexpansion.wave_cache";
    }

    public String themedWaveDescKey() {
        return "rewards.gatewayexpansion." + this.rewardKeyRoot + "_wave_bonus";
    }

    public String finalDescKey() {
        return "rewards.gatewayexpansion." + this.rewardKeyRoot + "_final_cache";
    }

    public String rareDescKey() {
        return "rewards.gatewayexpansion." + this.rewardKeyRoot + "_jackpot_cache";
    }

    private static GateTypeProfile gateType(String id, String title, String lootPath) {
        return new GateTypeProfile(
                id,
                title,
                ResourceLocation.fromNamespaceAndPath("gatewayexpansion", lootPath),
                "rewards.gatewayexpansion.gate_type." + id
        );
    }

    private static int rewardTier(int level) {
        if (level >= 90) return 5;
        if (level >= 70) return 4;
        if (level >= 50) return 3;
        if (level >= 20) return 2;
        return 1;
    }

    public record GateTypeProfile(
            String id,
            String title,
            ResourceLocation bonusLoot,
            String descKey
    ) {
    }
}
