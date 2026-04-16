package com.revilo.gatesofavarice.shop;

import com.revilo.gatesofavarice.augment.AugmentStackData;
import com.revilo.gatesofavarice.catalyst.CatalystStackData;
import com.revilo.gatesofavarice.integration.ModCompat;
import com.revilo.gatesofavarice.item.data.AugmentDifficultyTier;
import com.revilo.gatesofavarice.item.data.CrystalForgeData;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import com.revilo.gatesofavarice.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public record ShopOfferDefinition(
        String id,
        int price,
        int minLevel,
        int maxLevel,
        int minStock,
        int maxStock,
        int minPriceFluctuationPercent,
        int maxPriceFluctuationPercent,
        boolean material,
        ItemStack previewStack,
        Component title,
        Component description,
        OfferFactory factory
) {

    private static final int MAX_PLAYER_LEVEL = 100;
    private static List<ShopOfferDefinition> allOffers;

    public ShopOfferDefinition {
        if (minStock < 0 || maxStock < minStock) {
            throw new IllegalArgumentException("Invalid stock range for " + id + ": " + minStock + "-" + maxStock);
        }
        if (minLevel < 0 || maxLevel < minLevel) {
            throw new IllegalArgumentException("Invalid level range for " + id + ": " + minLevel + "-" + maxLevel);
        }
        if (minPriceFluctuationPercent > maxPriceFluctuationPercent) {
            throw new IllegalArgumentException("Invalid price fluctuation range for " + id + ": " + minPriceFluctuationPercent + "-" + maxPriceFluctuationPercent);
        }
    }

    public static List<ShopOfferDefinition> allOffers() {
        if (allOffers == null) {
            allOffers = List.copyOf(buildOffers());
        }
        return allOffers;
    }

    private static List<ShopOfferDefinition> buildOffers() {
        List<ShopOfferDefinition> offers = new ArrayList<>();

        // id, price, minLevel, maxLevel, minStock, maxStock, minFluct, maxFluct
        addMaterialOffer(offers, "grimstone", "grimstone", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.GRIMSTONE.get())), 0, MAX_PLAYER_LEVEL, 32, 48, 0, 0, ModItems.GRIMSTONE.get());
        addMaterialOffer(offers, "mystic_essence", "mystic_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYSTIC_ESSENCE.get())), 0, MAX_PLAYER_LEVEL, 32, 48, 0, 0, ModItems.MYSTIC_ESSENCE.get());
        addShopOnlyOffer(offers, "rusty_coin", 12, 0, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.RUSTY_COIN.get(), "Cheap filler currency from weak gates.");
        addShopOnlyOffer(offers, "hardened_flesh", 14, 0, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.HARDENED_FLESH.get(), "Cheap undead residue used in early crafting.");
        addMaterialOffer(offers, "scrap_metal", "scrap_metal", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SCRAP_METAL.get())), 0, MAX_PLAYER_LEVEL, 32, 48, 0, 0, ModItems.SCRAP_METAL.get());
        addMaterialOffer(offers, "mana_gems", "mana_gems", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_GEMS.get())), 0, MAX_PLAYER_LEVEL, 18, 28, 0, 0, ModItems.MANA_GEMS.get());
        addMaterialOffer(offers, "mana_steel_scrap", "mana_steel_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_SCRAP.get())), 5, MAX_PLAYER_LEVEL, 1, 7, 0, 0, ModItems.MANA_STEEL_SCRAP.get());
        addMaterialOffer(offers, "mana_steel_ingot", "mana_steel_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_INGOT.get())), 5, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.MANA_STEEL_INGOT.get());
        addMaterialOffer(offers, "upgrade_base", "upgrade_base", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.UPGRADE_BASE.get())), 5, MAX_PLAYER_LEVEL, 1, 3, 0, 0, ModItems.UPGRADE_BASE.get());
        addSwordTemplateOffer(offers, "mana_steel_upgrade_template", 5, MAX_PLAYER_LEVEL, ModItems.MANA_STEEL_UPGRADE_TEMPLATE.get(), ModItems.MANA_STEEL_SCRAP.get(), 8);
        addMaterialOffer(offers, "arcane_essence", "arcane_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANE_ESSENCE.get())), 0, MAX_PLAYER_LEVEL, 18, 28, 0, 0, ModItems.ARCANE_ESSENCE.get());
        addMaterialOffer(offers, "manastones", "manastones", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANASTONES.get())), 0, MAX_PLAYER_LEVEL, 18, 28, 0, 0, ModItems.MANASTONES.get());
        addMaterialOffer(offers, "elixrite_scrap", "elixrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_SCRAP.get())), 5, MAX_PLAYER_LEVEL, 1, 7, 0, 0, ModItems.ELIXRITE_SCRAP.get());
        addMaterialOffer(offers, "elixrite_ingot", "elixrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_INGOT.get())), 5, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.ELIXRITE_INGOT.get());
        addSwordTemplateOffer(offers, "elixrite_upgrade_template", 5, MAX_PLAYER_LEVEL, ModItems.ELIXRITE_UPGRADE_TEMPLATE.get(), ModItems.ELIXRITE_SCRAP.get(), 8);
        addMaterialOffer(offers, "astrite_scrap", "astrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_SCRAP.get())), 21, MAX_PLAYER_LEVEL, 1, 5, 0, 0, ModItems.ASTRITE_SCRAP.get());
        addMaterialOffer(offers, "astrite_ingot", "astrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_INGOT.get())), 21, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.ASTRITE_INGOT.get());
        addSwordTemplateOffer(offers, "astrite_upgrade_template", 21, MAX_PLAYER_LEVEL, ModItems.ASTRITE_UPGRADE_TEMPLATE.get(), ModItems.ASTRITE_SCRAP.get(), 8);
        addMaterialOffer(offers, "solar_shard", "solar_shard", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SOLAR_SHARD.get())), 30, MAX_PLAYER_LEVEL, 10, 16, 0, 0, ModItems.SOLAR_SHARD.get());
        addMaterialOffer(offers, "arcane_apple", "arcane_apple", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANE_APPLE.get())), 10, MAX_PLAYER_LEVEL, 2, 4, 0, 0, ModItems.ARCANE_APPLE.get());
        addMaterialOffer(offers, "enchanted_arcane_apple", "enchanted_arcane_apple", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ENCHANTED_ARCANE_APPLE.get())), 35, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.ENCHANTED_ARCANE_APPLE.get());
        addMaterialOffer(offers, "dark_essence", "dark_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.DARK_ESSENCE.get())), 35, MAX_PLAYER_LEVEL, 10, 16, 0, 0, ModItems.DARK_ESSENCE.get());
        addMaterialOffer(offers, "prismatic_shard", "prismatic_shard", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_SHARD.get())), 50, MAX_PLAYER_LEVEL, 2, 5, -5, 5, ModItems.PRISMATIC_SHARD.get());
        addMaterialOffer(offers, "prismatic_diamond", "prismatic_diamond", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_DIAMOND.get())), 50, MAX_PLAYER_LEVEL, 1, 2, -5, 5, ModItems.PRISMATIC_DIAMOND.get());
        addMaterialOffer(offers, "lunarium_scrap", "lunarium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_SCRAP.get())), 21, MAX_PLAYER_LEVEL, 1, 4, -5, 5, ModItems.LUNARIUM_SCRAP.get());
        addMaterialOffer(offers, "lunarium_ingot", "lunarium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_INGOT.get())), 21, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.LUNARIUM_INGOT.get());
        addSwordTemplateOffer(offers, "lunarium_upgrade_template", 21, MAX_PLAYER_LEVEL, ModItems.LUNARIUM_UPGRADE_TEMPLATE.get(), ModItems.LUNARIUM_SCRAP.get(), 8);
        addMaterialOffer(offers, "ignite_scrap", "ignite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_SCRAP.get())), 41, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.IGNITE_SCRAP.get());
        addMaterialOffer(offers, "ignite_ingot", "ignite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_INGOT.get())), 41, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.IGNITE_INGOT.get());
        addSwordTemplateOffer(offers, "ignite_upgrade_template", 41, MAX_PLAYER_LEVEL, ModItems.IGNITE_UPGRADE_TEMPLATE.get(), ModItems.IGNITE_SCRAP.get(), 8);
        addMaterialOffer(offers, "iridium_scrap", "iridium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_SCRAP.get())), 41, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.IRIDIUM_SCRAP.get());
        addMaterialOffer(offers, "iridium_ingot", "iridium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_INGOT.get())), 41, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.IRIDIUM_INGOT.get());
        addSwordTemplateOffer(offers, "iridium_upgrade_template", 41, MAX_PLAYER_LEVEL, ModItems.IRIDIUM_UPGRADE_TEMPLATE.get(), ModItems.IRIDIUM_SCRAP.get(), 8);
        addMaterialOffer(offers, "mythril_scrap", "mythril_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_SCRAP.get())), 61, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.MYTHRIL_SCRAP.get());
        addMaterialOffer(offers, "mythril_ingot", "mythril_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_INGOT.get())), 61, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.MYTHRIL_INGOT.get());
        addSwordTemplateOffer(offers, "mythril_upgrade_template", 61, MAX_PLAYER_LEVEL, ModItems.MYTHRIL_UPGRADE_TEMPLATE.get(), ModItems.MYTHRIL_INGOT.get(), 8);
        addMaterialOffer(offers, "arcanium_scrap", "arcanium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_SCRAP.get())), 61, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.ARCANIUM_SCRAP.get());
        addMaterialOffer(offers, "arcanium_ingot", "arcanium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_INGOT.get())), 61, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.ARCANIUM_INGOT.get());
        addSwordTemplateOffer(offers, "arcanium_upgrade_template", 61, MAX_PLAYER_LEVEL, ModItems.ARCANIUM_UPGRADE_TEMPLATE.get(), ModItems.ARCANIUM_INGOT.get(), 8);
        addMaterialOffer(offers, "prismatic_steel_scrap", "prismatic_steel_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_SCRAP.get())), 81, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.PRISMATIC_STEEL_SCRAP.get());
        addMaterialOffer(offers, "prismatic_steel_ingot", "prismatic_steel_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_INGOT.get())), 81, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.PRISMATIC_STEEL_INGOT.get());
        addMaterialOffer(offers, "prismatic_core", "prismatic_core", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_CORE.get())), 60, MAX_PLAYER_LEVEL, 3, 5, -5, 5, ModItems.PRISMATIC_CORE.get());

        addShopOnlyOffer(offers, "iron_ingot", 90, 0, MAX_PLAYER_LEVEL, 6, 12, 0, 0, Items.IRON_INGOT, "");
        addShopOnlyOffer(offers, "gold_ingot", 120, 0, MAX_PLAYER_LEVEL, 6, 12, 0, 0, Items.GOLD_INGOT, "");
        addShopOnlyOffer(offers, "diamond", 420, 10, MAX_PLAYER_LEVEL, 2, 5, 0, 0, Items.DIAMOND, "");
        addShopOnlyOffer(offers, "golden_apple", 850, 0, MAX_PLAYER_LEVEL, 1, 3, 0, 0, Items.GOLDEN_APPLE, "");
        addShopOnlyOffer(offers, "netherite_scrap", 1500, 35, MAX_PLAYER_LEVEL, 1, 2, 0, 0, Items.NETHERITE_SCRAP, "");
        addShopOnlyOffer(offers, "enchanted_golden_apple", 15000, 40, MAX_PLAYER_LEVEL, 1, 1, 0, 0, Items.ENCHANTED_GOLDEN_APPLE, "");
        addShopOnlyOffer(offers, "shop_gateway", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SHOP_GATEWAY.get())), 28, MAX_PLAYER_LEVEL, 8, 14, 0, 0, ModItems.SHOP_GATEWAY.get(), "Summons a traveling mythic merchant.");

        appendOptionalRunicOffers(offers);
        appendOptionalModdedOffers(offers);
        addCrystalThemeOffers(offers);
        addAugmentAndCatalystOffers(offers);
        addMaterialOffer(offers, "stability_pearl", "stability_pearl", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.STABILITY_PEARL.get())), 15, MAX_PLAYER_LEVEL, 1, 2, 0, 0, ModItems.STABILITY_PEARL.get());
        return offers;
    }

    private static void addMaterialOffer(List<ShopOfferDefinition> offers, String id, String descKey, int price, int minLevel, int maxLevel, int minStock, int maxStock, int minFluctuation, int maxFluctuation, ItemLike item) {
        ItemStack previewStack = new ItemStack(item);
        offers.add(new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                minStock,
                maxStock,
                minFluctuation,
                maxFluctuation,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatesofavarice.offer." + descKey + ".desc"),
                (random, playerLevel) -> new ItemStack(item)
        ));
    }

    private static void addShopOnlyOffer(List<ShopOfferDefinition> offers, String id, int price, int minLevel, int maxLevel, int minStock, int maxStock, int minFluctuation, int maxFluctuation, ItemLike item, String description) {
        ItemStack previewStack = new ItemStack(item);
        offers.add(new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                minStock,
                maxStock,
                minFluctuation,
                maxFluctuation,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.literal(description),
                (random, playerLevel) -> new ItemStack(item)
        ));
    }

    private static void addSwordTemplateOffer(List<ShopOfferDefinition> offers, String id, int minLevel, int maxLevel, ItemLike template, Item ingredient, int ingredientCount) {
        ItemStack previewStack = new ItemStack(template);
        int ingredientValue = GatewaySellValues.getUnitValue(new ItemStack(ingredient));
        int baseValue = GatewaySellValues.getUnitValue(new ItemStack(ModItems.UPGRADE_BASE.get()));
        int price = Math.max(1, (int) Math.ceil((baseValue + ingredientValue * ingredientCount) * 1.1D));
        offers.add(new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                1,
                1,
                0,
                0,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatesofavarice.offer." + id + ".desc"),
                (random, playerLevel) -> new ItemStack(template)
        ));
    }

    private static void appendOptionalRunicOffers(List<ShopOfferDefinition> offers) {
        if (!ModCompat.isRunicLoaded()) {
            return;
        }

        addOptionalRegistryOffer(offers, "runic:repair_rune", 10, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "A utility rune focused on restoration.");
        addOptionalRegistryOffer(offers, "runic:reroll_inscription", 25, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "Lets you reroll a runic result.");
        addOptionalRegistryOffer(offers, "runic:expansion_rune", 30, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "Expands the potential of runic gear.");
        addOptionalRegistryOffer(offers, "runic:nullification_rune", 35, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "Cancels an unwanted runic trait.");
        addOptionalRegistryOffer(offers, "runic:upgrade_rune", 40, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "Upgrades a runic result upward.");
        addOptionalRegistryOffer(offers, "runic:wild_inscription", 45, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "A volatile inscription with broader outcomes.");
        addOptionalRegistryOffer(offers, "runic:extraction_inscription", 50, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "Extracts existing runic power.");
        addOptionalRegistryOffer(offers, "runic:cursed_inscription", 55, 0, MAX_PLAYER_LEVEL, 1, 1, 0, 0, "A risky inscription with stronger variance.");
    }

    private static void appendOptionalModdedOffers(List<ShopOfferDefinition> offers) {
        addOptionalRegistryOffer(offers, "friendsandfoes:totem_of_illusion", 12000, 40, MAX_PLAYER_LEVEL, 1, 1, -5, 5, "A rare charm that distorts enemy perception.");
        addOptionalRegistryOffer(offers, "friendsandfoes:totem_of_freezing", 14000, 40, MAX_PLAYER_LEVEL, 1, 1, -5, 5, "A rare freezing charm that uses illager magic.");
        addOptionalRegistryOffer(offers, "minecraft:totem_of_undying", 10000, 30, MAX_PLAYER_LEVEL, 1, 1, -5, 5, "A totem that prevents death once.");
        addOptionalRegistryOffer(offers, "endermanoverhaul:enderman_tooth", 35, 0, MAX_PLAYER_LEVEL, 1, 1, -5, 5, "A rare trophy pulled from a warped end stalker.");
    }

    private static void addOptionalRegistryOffer(List<ShopOfferDefinition> offers, String itemId, int fixedPrice, int minLevel, int maxLevel, int minStock, int maxStock, int minFluctuation, int maxFluctuation, String description) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return;
        }
        ItemLike item = BuiltInRegistries.ITEM.get(id);
        addShopOnlyOffer(offers, id.getPath(), fixedPrice, minLevel, maxLevel, minStock, maxStock, minFluctuation, maxFluctuation, item, description);
    }

    private static void addAugmentAndCatalystOffers(List<ShopOfferDefinition> offers) {
        offers.add(augmentOffer("easy_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EASY_AUGMENT.get())), 0, MAX_PLAYER_LEVEL, ModItems.EASY_AUGMENT.get(), AugmentDifficultyTier.EASY));
        offers.add(augmentOffer("medium_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MEDIUM_AUGMENT.get())), 20, MAX_PLAYER_LEVEL, ModItems.MEDIUM_AUGMENT.get(), AugmentDifficultyTier.MEDIUM));
        offers.add(augmentOffer("hard_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.HARD_AUGMENT.get())), 30, MAX_PLAYER_LEVEL, ModItems.HARD_AUGMENT.get(), AugmentDifficultyTier.HARD));
        offers.add(augmentOffer("extreme_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EXTREME_AUGMENT.get())), 50, MAX_PLAYER_LEVEL, ModItems.EXTREME_AUGMENT.get(), AugmentDifficultyTier.EXTREME));
        offers.add(catalystOffer("time_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.TIME_CATALYST.get())), 15, MAX_PLAYER_LEVEL, ModItems.TIME_CATALYST.get()));
    }

    private static ShopOfferDefinition augmentOffer(String id, int price, int minLevel, int maxLevel, ItemLike item, AugmentDifficultyTier difficultyTier) {
        return new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                7,
                13,
                0,
                0,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatesofavarice.offer." + id),
                Component.translatable("shop.gatesofavarice.offer." + id + ".desc"),
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    AugmentStackData.ensureDefinition(stack, difficultyTier, random, playerLevel);
                    return stack;
                }
        );
    }

    private static ShopOfferDefinition catalystOffer(String id, int price, int minLevel, int maxLevel, ItemLike item) {
        return new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                7,
                13,
                0,
                0,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatesofavarice.offer." + id),
                Component.translatable("shop.gatesofavarice.offer." + id + ".desc"),
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    CatalystStackData.ensureDefinition(stack, com.revilo.gatesofavarice.catalyst.CatalystArchetype.TIME, random, playerLevel);
                    return stack;
                }
        );
    }

    private static void addCrystalThemeOffers(List<ShopOfferDefinition> offers) {
        offers.add(crystalOffer("tier_1_crystal_undead", 8, 0, MAX_PLAYER_LEVEL, ModItems.TIER_1_CRYSTAL.get(), 0, 19, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_2_crystal_undead", 15, 10, MAX_PLAYER_LEVEL, ModItems.TIER_2_CRYSTAL.get(), 20, 49, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_2_crystal_nether", 18, 25, MAX_PLAYER_LEVEL, ModItems.TIER_2_CRYSTAL.get(), 20, 49, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_3_crystal_undead", 42, 50, MAX_PLAYER_LEVEL, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_3_crystal_raider", 46, 50, MAX_PLAYER_LEVEL, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_3_crystal_nether", 48, 50, MAX_PLAYER_LEVEL, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_3_crystal_arcane", 54, 50, MAX_PLAYER_LEVEL, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.ARCANE));
        offers.add(crystalOffer("tier_4_crystal_undead", 68, 50, MAX_PLAYER_LEVEL, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_4_crystal_raider", 74, 50, MAX_PLAYER_LEVEL, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_4_crystal_nether", 76, 50, MAX_PLAYER_LEVEL, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_4_crystal_arcane", 82, 50, MAX_PLAYER_LEVEL, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.ARCANE));
        offers.add(crystalOffer("tier_5_crystal_undead", 102, 70, MAX_PLAYER_LEVEL, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_5_crystal_raider", 110, 70, MAX_PLAYER_LEVEL, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_5_crystal_nether", 112, 70, MAX_PLAYER_LEVEL, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_5_crystal_arcane", 120, 70, MAX_PLAYER_LEVEL, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.ARCANE));
    }

    private static ShopOfferDefinition crystalOffer(String id, int price, int minLevel, int maxLevel, ItemLike item, int crystalMinLevel, int crystalMaxLevel, CrystalTheme theme) {
        ItemStack previewStack = new ItemStack(item);
        CrystalForgeData.ensureProfile(previewStack, crystalMinLevel, crystalMaxLevel, RandomSource.create(0L));
        CrystalForgeData.attuneTheme(previewStack, theme);
        return new ShopOfferDefinition(
                id,
                price,
                minLevel,
                maxLevel,
                2,
                4,
                0,
                0,
                false,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatesofavarice.offer." + id + ".desc"),
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    CrystalForgeData.ensureProfile(stack, crystalMinLevel, crystalMaxLevel, random);
                    CrystalForgeData.attuneTheme(stack, theme);
                    return stack;
                }
        );
    }

    public int rollStock(RandomSource random) {
        if (this.maxStock <= this.minStock) {
            return this.minStock;
        }
        return this.minStock + random.nextInt(this.maxStock - this.minStock + 1);
    }

    public int rollPrice(RandomSource random) {
        if (this.minPriceFluctuationPercent == 0 && this.maxPriceFluctuationPercent == 0) {
            return this.price;
        }
        int deltaPercent = this.minPriceFluctuationPercent;
        if (this.maxPriceFluctuationPercent > this.minPriceFluctuationPercent) {
            deltaPercent += random.nextInt(this.maxPriceFluctuationPercent - this.minPriceFluctuationPercent + 1);
        }
        double multiplier = 1.0D + (deltaPercent / 100.0D);
        return Math.max(1, (int) Math.round(this.price * multiplier));
    }

    public ItemStack createStack(RandomSource random, int playerLevel) {
        return this.factory.create(random, playerLevel);
    }

    @FunctionalInterface
    public interface OfferFactory {
        ItemStack create(RandomSource random, int playerLevel);
    }
}
