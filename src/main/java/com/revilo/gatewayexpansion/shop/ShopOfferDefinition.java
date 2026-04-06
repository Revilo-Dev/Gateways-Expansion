package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.augment.AugmentStackData;
import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.catalyst.CatalystStackData;
import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import com.revilo.gatewayexpansion.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public record ShopOfferDefinition(String id, int price, int requiredLevel, boolean material, ItemStack previewStack, Component title, Component description, OfferFactory factory) {

    private static List<ShopOfferDefinition> allOffers;

    public static List<ShopOfferDefinition> allOffers() {
        if (allOffers == null) {
            allOffers = List.copyOf(buildOffers());
        }
        return allOffers;
    }

    private static List<ShopOfferDefinition> buildOffers() {
        List<ShopOfferDefinition> offers = new ArrayList<>();
        offers.add(simpleOffer("grimstone", "grimstone", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.GRIMSTONE.get())), 0, ModItems.GRIMSTONE.get()));
        offers.add(simpleOffer("mystic_essence", "mystic_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYSTIC_ESSENCE.get())), 0, ModItems.MYSTIC_ESSENCE.get()));
        offers.add(simpleOffer("scrap_metal", "scrap_metal", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SCRAP_METAL.get())), 0, ModItems.SCRAP_METAL.get()));
        offers.add(simpleOffer("mana_gems", "mana_gems", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_GEMS.get())), 0, ModItems.MANA_GEMS.get()));
        offers.add(simpleOffer("mana_steel_scrap", "mana_steel_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_SCRAP.get())), 5, ModItems.MANA_STEEL_SCRAP.get()));
        offers.add(simpleOffer("mana_steel_ingot", "mana_steel_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_INGOT.get())), 5, ModItems.MANA_STEEL_INGOT.get()));
        offers.add(simpleOffer("arcane_essence", "arcane_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANE_ESSENCE.get())), 0, ModItems.ARCANE_ESSENCE.get()));
        offers.add(simpleOffer("manastones", "manastones", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANASTONES.get())), 0, ModItems.MANASTONES.get()));
        offers.add(simpleOffer("elixrite_scrap", "elixrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_SCRAP.get())), 5, ModItems.ELIXRITE_SCRAP.get()));
        offers.add(simpleOffer("elixrite_ingot", "elixrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_INGOT.get())), 5, ModItems.ELIXRITE_INGOT.get()));
        offers.add(simpleOffer("astrite_scrap", "astrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_SCRAP.get())), 21, ModItems.ASTRITE_SCRAP.get()));
        offers.add(simpleOffer("astrite_ingot", "astrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_INGOT.get())), 21, ModItems.ASTRITE_INGOT.get()));
        offers.add(simpleOffer("solar_crystal", "solar_crystal", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SOLAR_CRYSTAL.get())), 30, ModItems.SOLAR_CRYSTAL.get()));
        offers.add(simpleOffer("dark_essence", "dark_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.DARK_ESSENCE.get())), 35, ModItems.DARK_ESSENCE.get()));
        offers.add(simpleOffer("prismatic_diamond", "prismatic_diamond", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_DIAMOND.get())), 40, ModItems.PRISMATIC_DIAMOND.get()));
        offers.add(simpleOffer("lunarium_scrap", "lunarium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_SCRAP.get())), 21, ModItems.LUNARIUM_SCRAP.get()));
        offers.add(simpleOffer("lunarium_ingot", "lunarium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_INGOT.get())), 21, ModItems.LUNARIUM_INGOT.get()));
        offers.add(simpleOffer("prismatic_core", "prismatic_core", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_CORE.get())), 60, ModItems.PRISMATIC_CORE.get()));
        offers.add(shopOnlyOffer("iron_ingot", 90, 0, Items.IRON_INGOT, "A common forging staple."));
        offers.add(shopOnlyOffer("gold_ingot", 120, 0, Items.GOLD_INGOT, "A soft metal used in catalyst and rune work."));
        offers.add(shopOnlyOffer("diamond", 420, 10, Items.DIAMOND, "A premium crystal lattice for higher-end crafting."));
        offers.add(shopOnlyOffer("golden_apple", 850, 0, Items.GOLDEN_APPLE, "A strong sustain item for tougher gates."));
        offers.add(shopOnlyOffer("netherite_scrap", 1500, 35, Items.NETHERITE_SCRAP, "Rare debris recovered from ancient nether forges."));
        appendOptionalRunicOffers(offers);
        offers.add(shopOnlyOffer("shop_gateway", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SHOP_GATEWAY.get())), 28, ModItems.SHOP_GATEWAY.get(), "Summons a traveling mythic merchant."));
        offers.add(crystalOffer("tier_1_crystal", 8, 0, ModItems.TIER_1_CRYSTAL.get(), 0, 19));
        offers.add(crystalOffer("tier_2_crystal", 15, 10, ModItems.TIER_2_CRYSTAL.get(), 20, 49));
        offers.add(crystalOffer("tier_3_crystal", 42, 30, ModItems.TIER_3_CRYSTAL.get(), 50, 69));
        offers.add(crystalOffer("tier_4_crystal", 68, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89));
        offers.add(crystalOffer("tier_5_crystal", 102, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100));
        offers.add(augmentOffer("easy_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EASY_AUGMENT.get())), 0, ModItems.EASY_AUGMENT.get(), AugmentDifficultyTier.EASY));
        offers.add(augmentOffer("medium_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MEDIUM_AUGMENT.get())), 20, ModItems.MEDIUM_AUGMENT.get(), AugmentDifficultyTier.MEDIUM));
        offers.add(augmentOffer("hard_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.HARD_AUGMENT.get())), 30, ModItems.HARD_AUGMENT.get(), AugmentDifficultyTier.HARD));
        offers.add(augmentOffer("extreme_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EXTREME_AUGMENT.get())), 55, ModItems.EXTREME_AUGMENT.get(), AugmentDifficultyTier.EXTREME));
        offers.add(catalystOffer("time_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.TIME_CATALYST.get())), 15, ModItems.TIME_CATALYST.get(), CatalystArchetype.TIME));
        offers.add(simpleOffer("stability_pearl", "stability_pearl", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.STABILITY_PEARL.get())), 35, ModItems.STABILITY_PEARL.get()));
        offers.add(catalystOffer("loot_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LOOT_CATALYST.get())), 25, ModItems.LOOT_CATALYST.get(), CatalystArchetype.LOOT));
        offers.add(catalystOffer("stat_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.STAT_CATALYST.get())), 35, ModItems.STAT_CATALYST.get(), CatalystArchetype.STAT));
        offers.add(catalystOffer("volatile_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.HIGHRISK_CATALYST.get())), 60, ModItems.HIGHRISK_CATALYST.get(), CatalystArchetype.VOLATILE));
        return offers;
    }

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

    private static ShopOfferDefinition shopOnlyOffer(String id, int price, int requiredLevel, ItemLike item, String description) {
        ItemStack previewStack = new ItemStack(item);
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.literal(description),
                random -> new ItemStack(item)
        );
    }

    private static void appendOptionalRunicOffers(List<ShopOfferDefinition> offers) {
        if (!ModCompat.isAnyLoaded("runic")) {
            return;
        }

        addOptionalRegistryOffer(offers, "runic:repair_rune", 10, "A utility rune focused on restoration.");
        addOptionalRegistryOffer(offers, "runic:enhanced_rune", 20, "An enhanced rune with rolled runic stats.");
        addOptionalRegistryOffer(offers, "runic:reroll_inscription", 25, "Lets you reroll a runic result.");
        addOptionalRegistryOffer(offers, "runic:expansion_rune", 30, "Expands the potential of runic gear.");
        addOptionalRegistryOffer(offers, "runic:nullification_rune", 35, "Cancels an unwanted runic trait.");
        addOptionalRegistryOffer(offers, "runic:upgrade_rune", 40, "Upgrades a runic result upward.");
        addOptionalRegistryOffer(offers, "runic:wild_inscription", 45, "A volatile inscription with broader outcomes.");
        addOptionalRegistryOffer(offers, "runic:extraction_inscription", 50, "Extracts existing runic power.");
        addOptionalRegistryOffer(offers, "runic:cursed_inscription", 55, "A risky inscription with stronger variance.");
        addOptionalRegistryOffer(offers, "runic:etching_table", 28, "A station for extracting and applying etchings.");
    }

    private static void addOptionalRegistryOffer(List<ShopOfferDefinition> offers, String itemId, int requiredLevel, String description) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return;
        }
        ItemLike item = BuiltInRegistries.ITEM.get(id);
        offers.add(shopOnlyOffer(id.getPath(), GatewaySellValues.getSuggestedBuyPrice(new ItemStack(item)), requiredLevel, item, description));
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
