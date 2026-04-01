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

public record ShopOfferDefinition(String id, int price, ItemStack previewStack, Component title, Component description, OfferFactory factory) {

    public static final List<ShopOfferDefinition> CORE_OFFERS = List.of(
            new ShopOfferDefinition(
                    "hard_augment",
                    120,
                    new ItemStack(ModItems.HARD_AUGMENT.get()),
                    Component.translatable("shop.gatewayexpansion.offer.hard_augment"),
                    Component.translatable("shop.gatewayexpansion.offer.hard_augment.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.HARD_AUGMENT.get());
                        AugmentStackData.ensureDefinition(stack, AugmentDifficultyTier.HARD, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "loot_catalyst",
                    185,
                    new ItemStack(ModItems.LOOT_CATALYST.get()),
                    Component.translatable("shop.gatewayexpansion.offer.loot_catalyst"),
                    Component.translatable("shop.gatewayexpansion.offer.loot_catalyst.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.LOOT_CATALYST.get());
                        CatalystStackData.ensureDefinition(stack, CatalystArchetype.LOOT, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "tier_3_crystal",
                    260,
                    new ItemStack(ModItems.TIER_3_CRYSTAL.get()),
                    Component.translatable("shop.gatewayexpansion.offer.tier_3_crystal"),
                    Component.translatable("shop.gatewayexpansion.offer.tier_3_crystal.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.TIER_3_CRYSTAL.get());
                        CrystalForgeData.ensureProfile(stack, 50, 69, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "extreme_augment",
                    360,
                    new ItemStack(ModItems.EXTREME_AUGMENT.get()),
                    Component.translatable("shop.gatewayexpansion.offer.extreme_augment"),
                    Component.translatable("shop.gatewayexpansion.offer.extreme_augment.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.EXTREME_AUGMENT.get());
                        AugmentStackData.ensureDefinition(stack, AugmentDifficultyTier.EXTREME, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "volatile_catalyst",
                    490,
                    new ItemStack(ModItems.HIGHRISK_CATALYST.get()),
                    Component.translatable("shop.gatewayexpansion.offer.volatile_catalyst"),
                    Component.translatable("shop.gatewayexpansion.offer.volatile_catalyst.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.HIGHRISK_CATALYST.get());
                        CatalystStackData.ensureDefinition(stack, CatalystArchetype.VOLATILE, random);
                        return stack;
                    }));

    public static final List<ShopOfferDefinition> TEMP_OFFERS = List.of(
            new ShopOfferDefinition(
                    "time_catalyst_temp",
                    145,
                    new ItemStack(ModItems.TIME_CATALYST.get()),
                    Component.translatable("shop.gatewayexpansion.offer.time_catalyst"),
                    Component.translatable("shop.gatewayexpansion.offer.time_catalyst.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.TIME_CATALYST.get());
                        CatalystStackData.ensureDefinition(stack, CatalystArchetype.TIME, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "tier_4_crystal_temp",
                    410,
                    new ItemStack(ModItems.TIER_4_CRYSTAL.get()),
                    Component.translatable("shop.gatewayexpansion.offer.tier_4_crystal"),
                    Component.translatable("shop.gatewayexpansion.offer.tier_4_crystal.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.TIER_4_CRYSTAL.get());
                        CrystalForgeData.ensureProfile(stack, 70, 89, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "medium_augment_temp",
                    100,
                    new ItemStack(ModItems.MEDIUM_AUGMENT.get()),
                    Component.translatable("shop.gatewayexpansion.offer.medium_augment"),
                    Component.translatable("shop.gatewayexpansion.offer.medium_augment.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.MEDIUM_AUGMENT.get());
                        AugmentStackData.ensureDefinition(stack, AugmentDifficultyTier.MEDIUM, random);
                        return stack;
                    }),
            new ShopOfferDefinition(
                    "stat_catalyst_temp",
                    225,
                    new ItemStack(ModItems.STAT_CATALYST.get()),
                    Component.translatable("shop.gatewayexpansion.offer.stat_catalyst"),
                    Component.translatable("shop.gatewayexpansion.offer.stat_catalyst.desc"),
                    random -> {
                        ItemStack stack = new ItemStack(ModItems.STAT_CATALYST.get());
                        CatalystStackData.ensureDefinition(stack, CatalystArchetype.STAT, random);
                        return stack;
                    }));

    public ItemStack createStack(RandomSource random) {
        return this.factory.create(random);
    }

    @FunctionalInterface
    public interface OfferFactory {
        ItemStack create(RandomSource random);
    }
}
