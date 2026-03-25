package com.revilo.gatewayexpansion.gateway;

import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record GatewayThemeProfile(
        CrystalTheme theme,
        int color,
        String prefix,
        List<ResourceLocation> effects,
        ResourceLocation commonLoot,
        ResourceLocation rareLoot
) {

    public static GatewayThemeProfile forTheme(CrystalTheme theme, int level) {
        return switch (theme) {
            case UNDEAD -> new GatewayThemeProfile(
                    theme,
                    0x6AA36A,
                    "Grave",
                    List.of(ResourceLocation.withDefaultNamespace("strength"), ResourceLocation.withDefaultNamespace("resistance"), ResourceLocation.withDefaultNamespace("slowness")),
                    ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
                    ResourceLocation.withDefaultNamespace("chests/stronghold_library"));
            case BEAST -> new GatewayThemeProfile(
                    theme,
                    0xB87B32,
                    "Feral",
                    List.of(ResourceLocation.withDefaultNamespace("speed"), ResourceLocation.withDefaultNamespace("jump_boost"), ResourceLocation.withDefaultNamespace("regeneration")),
                    ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
                    ResourceLocation.withDefaultNamespace("chests/jungle_temple"));
            case ARCANE -> new GatewayThemeProfile(
                    theme,
                    0x3EA5D9,
                    "Astral",
                    List.of(ResourceLocation.withDefaultNamespace("glowing"), ResourceLocation.withDefaultNamespace("levitation"), ResourceLocation.withDefaultNamespace("regeneration")),
                    ResourceLocation.withDefaultNamespace("chests/desert_pyramid"),
                    ResourceLocation.withDefaultNamespace("chests/ancient_city"));
            case NETHER -> new GatewayThemeProfile(
                    theme,
                    0xD9492B,
                    "Infernal",
                    List.of(ResourceLocation.withDefaultNamespace("fire_resistance"), ResourceLocation.withDefaultNamespace("strength"), ResourceLocation.withDefaultNamespace("speed")),
                    ResourceLocation.withDefaultNamespace("chests/nether_bridge"),
                    ResourceLocation.withDefaultNamespace("chests/bastion_treasure"));
        };
    }
}
