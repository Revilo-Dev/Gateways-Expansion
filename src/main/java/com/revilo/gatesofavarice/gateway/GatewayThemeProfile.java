package com.revilo.gatesofavarice.gateway;

import com.revilo.gatesofavarice.integration.ModCompat;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
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
        String finalLootBase = ModCompat.isRunicLoaded() ? "rewards/finals/" : "rewards/finals_fallback/";
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
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/undead"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "undead/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "undead/" + tierSuffix));
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
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/beast"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "beast/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "beast/" + tierSuffix));
            case ARCANE -> new GatewayThemeProfile(
                    theme,
                    0x3EA5D9,
                    "Astral",
                    "astral",
                    List.of(ResourceLocation.withDefaultNamespace("glowing"), ResourceLocation.withDefaultNamespace("regeneration")),
                    List.of(
                            gateType("rift", "Rift", "rewards/types/arcane/rift"),
                            gateType("observatory", "Observatory", "rewards/types/arcane/observatory"),
                            gateType("stormglass", "Stormglass", "rewards/types/arcane/stormglass")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/arcane"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "arcane/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "arcane/" + tierSuffix));
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
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/nether"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "nether/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "nether/" + tierSuffix));
            case RAIDER -> new GatewayThemeProfile(
                    theme,
                    0x8B6F47,
                    "Raider",
                    "raider",
                    List.of(ResourceLocation.withDefaultNamespace("hero_of_the_village"), ResourceLocation.withDefaultNamespace("strength"), ResourceLocation.withDefaultNamespace("speed")),
                    List.of(
                            gateType("outpost", "Outpost", "rewards/types/raider/outpost"),
                            gateType("caravan", "Caravan", "rewards/types/raider/caravan"),
                            gateType("belfry", "Belfry", "rewards/types/raider/belfry")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/raider"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "raider/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "raider/" + tierSuffix));
            case WILD -> new GatewayThemeProfile(
                    theme,
                    0x6C4CD8,
                    "Wild",
                    "wild",
                    List.of(
                            ResourceLocation.withDefaultNamespace("strength"),
                            ResourceLocation.withDefaultNamespace("speed"),
                            ResourceLocation.withDefaultNamespace("fire_resistance"),
                            ResourceLocation.withDefaultNamespace("glowing")
                    ),
                    List.of(
                            gateType("pack", "Pack", "rewards/types/beast/pack"),
                            gateType("rift", "Rift", "rewards/types/arcane/rift"),
                            gateType("bastion", "Bastion", "rewards/types/nether/bastion"),
                            gateType("outpost", "Outpost", "rewards/types/raider/outpost")
                    ),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", "rewards/waves/beast"),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "beast/" + tierSuffix),
                    ResourceLocation.fromNamespaceAndPath("gatesofavarice", finalLootBase + "beast/" + tierSuffix));
        };
    }

    public GateTypeProfile gateType(long seed) {
        return this.gateTypes.get((int) Math.floorMod(seed, this.gateTypes.size()));
    }

    public String waveDescKey() {
        return "rewards.gatesofavarice.wave_cache";
    }

    public String themedWaveDescKey() {
        return "rewards.gatesofavarice." + this.rewardKeyRoot + "_wave_bonus";
    }

    public String finalDescKey() {
        return "rewards.gatesofavarice." + this.rewardKeyRoot + "_final_cache";
    }

    public String rareDescKey() {
        return "rewards.gatesofavarice." + this.rewardKeyRoot + "_jackpot_cache";
    }

    private static GateTypeProfile gateType(String id, String title, String lootPath) {
        return new GateTypeProfile(
                id,
                title,
                ResourceLocation.fromNamespaceAndPath("gatesofavarice", lootPath),
                "rewards.gatesofavarice.gate_type." + id
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
