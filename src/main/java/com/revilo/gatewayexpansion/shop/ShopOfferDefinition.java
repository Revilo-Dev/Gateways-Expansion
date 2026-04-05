package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.augment.AugmentStackData;
import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.catalyst.CatalystStackData;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import com.revilo.gatewayexpansion.registry.ModItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ShopOfferDefinition(String id, int price, int requiredLevel, boolean material, ItemStack previewStack, Component title, Component description, OfferFactory factory) {

    public static final List<ShopOfferDefinition> ALL_OFFERS = List.of(
            simpleOffer("grimstone", "grimstone", 180, 0, ModItems.GRIMSTONE.get()),
            simpleOffer("mystic_essence", "mystic_essence", 200, 0, ModItems.MYSTIC_ESSENCE.get()),
            simpleOffer("scrap_metal", "scrap_metal", 160, 0, ModItems.SCRAP_METAL.get()),
            simpleOffer("mana_gems", "mana_gems", 480, 10, ModItems.MANA_GEMS.get()),
            simpleOffer("arcane_essence", "arcane_essence", 600, 20, ModItems.ARCANE_ESSENCE.get()),
            simpleOffer("manastones", "manastones", 750, 20, ModItems.MANASTONES.get()),
            simpleOffer("solar_crystal", "solar_crystal", 1400, 30, ModItems.SOLAR_CRYSTAL.get()),
            simpleOffer("dark_essence", "dark_essence", 2100, 35, ModItems.DARK_ESSENCE.get()),
            simpleOffer("prismatic_diamond", "prismatic_diamond", 2600, 40, ModItems.PRISMATIC_DIAMOND.get()),
            simpleOffer("prismatic_core", "prismatic_core", 5400, 60, ModItems.PRISMATIC_CORE.get()),
            crystalOffer("tier_1_crystal", 45, 0, ModItems.TIER_1_CRYSTAL.get(), 0, 19),
            crystalOffer("tier_2_crystal", 90, 10, ModItems.TIER_2_CRYSTAL.get(), 20, 49),
            crystalOffer("tier_3_crystal", 260, 30, ModItems.TIER_3_CRYSTAL.get(), 50, 69),
            crystalOffer("tier_4_crystal", 410, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89),
            crystalOffer("tier_5_crystal", 620, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100),
            augmentOffer("easy_augment", 55, 0, ModItems.EASY_AUGMENT.get(), AugmentDifficultyTier.EASY),
            augmentOffer("medium_augment", 100, 20, ModItems.MEDIUM_AUGMENT.get(), AugmentDifficultyTier.MEDIUM),
            augmentOffer("hard_augment", 120, 30, ModItems.HARD_AUGMENT.get(), AugmentDifficultyTier.HARD),
            augmentOffer("extreme_augment", 360, 55, ModItems.EXTREME_AUGMENT.get(), AugmentDifficultyTier.EXTREME),
            catalystOffer("time_catalyst", 145, 15, ModItems.TIME_CATALYST.get(), CatalystArchetype.TIME),
            catalystOffer("loot_catalyst", 185, 25, ModItems.LOOT_CATALYST.get(), CatalystArchetype.LOOT),
            catalystOffer("stat_catalyst", 225, 35, ModItems.STAT_CATALYST.get(), CatalystArchetype.STAT),
            catalystOffer("volatile_catalyst", 490, 60, ModItems.HIGHRISK_CATALYST.get(), CatalystArchetype.VOLATILE));

    private static ShopOfferDefinition simpleOffer(String id, String descKey, int price, int requiredLevel, ItemLike item) {
        ItemStack previewStack = new ItemStack(item);
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatewayexpansion.offer." + descKey + ".desc"),
                random -> new ItemStack(item)
        );
    }

    private static ShopOfferDefinition augmentOffer(String id, int price, int requiredLevel, ItemLike item, AugmentDifficultyTier difficultyTier) {
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatewayexpansion.offer." + id),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                random -> {
                    ItemStack stack = new ItemStack(item);
                    AugmentStackData.ensureDefinition(stack, difficultyTier, random);
                    return stack;
                }
        );
    }

    private static ShopOfferDefinition catalystOffer(String id, int price, int requiredLevel, ItemLike item, CatalystArchetype archetype) {
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatewayexpansion.offer." + id),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                random -> {
                    ItemStack stack = new ItemStack(item);
                    CatalystStackData.ensureDefinition(stack, archetype, random);
                    return stack;
                }
        );
    }

    private static ShopOfferDefinition crystalOffer(String id, int price, int requiredLevel, ItemLike item, int minLevel, int maxLevel) {
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatewayexpansion.offer." + id),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                random -> {
                    ItemStack stack = new ItemStack(item);
                    CrystalForgeData.ensureProfile(stack, minLevel, maxLevel, random);
                    return stack;
                }
        );
    }

    public ItemStack createStack(RandomSource random) {
        return this.factory.create(random);
    }

    @FunctionalInterface
    public interface OfferFactory {
        ItemStack create(RandomSource random);
    }
}
