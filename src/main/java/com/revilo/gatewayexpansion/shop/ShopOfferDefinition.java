package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.augment.AugmentStackData;
import com.revilo.gatewayexpansion.catalyst.CatalystStackData;
import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import com.revilo.gatewayexpansion.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
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
        offers.add(shopOnlyOffer("rusty_coin", 12, 0, ModItems.RUSTY_COIN.get(), "Cheap filler currency from weak gates."));
        offers.add(shopOnlyOffer("hardened_flesh", 14, 0, ModItems.HARDENED_FLESH.get(), "Cheap undead residue used in early crafting."));
        offers.add(simpleOffer("scrap_metal", "scrap_metal", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SCRAP_METAL.get())), 0, ModItems.SCRAP_METAL.get()));
        offers.add(simpleOffer("mana_gems", "mana_gems", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_GEMS.get())), 0, ModItems.MANA_GEMS.get()));
        offers.add(simpleOffer("mana_steel_scrap", "mana_steel_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_SCRAP.get())), 5, ModItems.MANA_STEEL_SCRAP.get()));
        offers.add(simpleOffer("mana_steel_ingot", "mana_steel_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_INGOT.get())), 5, ModItems.MANA_STEEL_INGOT.get()));
        offers.add(simpleOffer("upgrade_base", "upgrade_base", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.UPGRADE_BASE.get())), 5, ModItems.UPGRADE_BASE.get()));
        offers.add(swordTemplateOffer("mana_steel_upgrade_template", 5, ModItems.MANA_STEEL_UPGRADE_TEMPLATE.get(), ModItems.MANA_STEEL_SCRAP.get(), 8));
        offers.add(simpleOffer("mana_steel_magnet", "mana_steel_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_MAGNET.get())), 15, ModItems.MANA_STEEL_MAGNET.get()));
        offers.add(simpleOffer("mana_steel_paxel", "mana_steel_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANA_STEEL_PAXEL.get())), 5, ModItems.MANA_STEEL_PAXEL.get()));
        offers.add(simpleOffer("arcane_essence", "arcane_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANE_ESSENCE.get())), 0, ModItems.ARCANE_ESSENCE.get()));
        offers.add(simpleOffer("manastones", "manastones", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MANASTONES.get())), 0, ModItems.MANASTONES.get()));
        offers.add(simpleOffer("elixrite_scrap", "elixrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_SCRAP.get())), 5, ModItems.ELIXRITE_SCRAP.get()));
        offers.add(simpleOffer("elixrite_ingot", "elixrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_INGOT.get())), 5, ModItems.ELIXRITE_INGOT.get()));
        offers.add(swordTemplateOffer("elixrite_upgrade_template", 5, ModItems.ELIXRITE_UPGRADE_TEMPLATE.get(), ModItems.ELIXRITE_SCRAP.get(), 8));
        offers.add(simpleOffer("elixrite_magnet", "elixrite_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_MAGNET.get())), 25, ModItems.ELIXRITE_MAGNET.get()));
        offers.add(simpleOffer("elixrite_paxel", "elixrite_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ELIXRITE_PAXEL.get())), 5, ModItems.ELIXRITE_PAXEL.get()));
        offers.add(simpleOffer("astrite_scrap", "astrite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_SCRAP.get())), 21, ModItems.ASTRITE_SCRAP.get()));
        offers.add(simpleOffer("astrite_ingot", "astrite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_INGOT.get())), 21, ModItems.ASTRITE_INGOT.get()));
        offers.add(swordTemplateOffer("astrite_upgrade_template", 21, ModItems.ASTRITE_UPGRADE_TEMPLATE.get(), ModItems.ASTRITE_SCRAP.get(), 8));
        offers.add(simpleOffer("astrite_magnet", "astrite_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_MAGNET.get())), 32, ModItems.ASTRITE_MAGNET.get()));
        offers.add(simpleOffer("astrite_paxel", "astrite_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ASTRITE_PAXEL.get())), 21, ModItems.ASTRITE_PAXEL.get()));
        offers.add(simpleOffer("solar_shard", "solar_shard", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SOLAR_SHARD.get())), 30, ModItems.SOLAR_SHARD.get()));
        offers.add(simpleOffer("dark_essence", "dark_essence", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.DARK_ESSENCE.get())), 35, ModItems.DARK_ESSENCE.get()));
        offers.add(simpleOffer("prismatic_diamond", "prismatic_diamond", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_DIAMOND.get())), 40, ModItems.PRISMATIC_DIAMOND.get()));
        offers.add(simpleOffer("lunarium_scrap", "lunarium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_SCRAP.get())), 21, ModItems.LUNARIUM_SCRAP.get()));
        offers.add(simpleOffer("lunarium_ingot", "lunarium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_INGOT.get())), 21, ModItems.LUNARIUM_INGOT.get()));
        offers.add(swordTemplateOffer("lunarium_upgrade_template", 21, ModItems.LUNARIUM_UPGRADE_TEMPLATE.get(), ModItems.LUNARIUM_SCRAP.get(), 8));
        offers.add(simpleOffer("lunarium_magnet", "lunarium_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_MAGNET.get())), 40, ModItems.LUNARIUM_MAGNET.get()));
        offers.add(simpleOffer("lunarium_paxel", "lunarium_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.LUNARIUM_PAXEL.get())), 21, ModItems.LUNARIUM_PAXEL.get()));
        offers.add(simpleOffer("ignite_scrap", "ignite_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_SCRAP.get())), 41, ModItems.IGNITE_SCRAP.get()));
        offers.add(simpleOffer("ignite_ingot", "ignite_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_INGOT.get())), 41, ModItems.IGNITE_INGOT.get()));
        offers.add(swordTemplateOffer("ignite_upgrade_template", 41, ModItems.IGNITE_UPGRADE_TEMPLATE.get(), ModItems.IGNITE_SCRAP.get(), 8));
        offers.add(simpleOffer("ignite_magnet", "ignite_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_MAGNET.get())), 48, ModItems.IGNITE_MAGNET.get()));
        offers.add(simpleOffer("ignite_paxel", "ignite_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IGNITE_PAXEL.get())), 41, ModItems.IGNITE_PAXEL.get()));
        offers.add(simpleOffer("iridium_scrap", "iridium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_SCRAP.get())), 41, ModItems.IRIDIUM_SCRAP.get()));
        offers.add(simpleOffer("iridium_ingot", "iridium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_INGOT.get())), 41, ModItems.IRIDIUM_INGOT.get()));
        offers.add(swordTemplateOffer("iridium_upgrade_template", 41, ModItems.IRIDIUM_UPGRADE_TEMPLATE.get(), ModItems.IRIDIUM_SCRAP.get(), 8));
        offers.add(simpleOffer("iridium_magnet", "iridium_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_MAGNET.get())), 55, ModItems.IRIDIUM_MAGNET.get()));
        offers.add(simpleOffer("iridium_paxel", "iridium_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.IRIDIUM_PAXEL.get())), 41, ModItems.IRIDIUM_PAXEL.get()));
        offers.add(simpleOffer("mythril_scrap", "mythril_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_SCRAP.get())), 61, ModItems.MYTHRIL_SCRAP.get()));
        offers.add(simpleOffer("mythril_ingot", "mythril_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_INGOT.get())), 61, ModItems.MYTHRIL_INGOT.get()));
        offers.add(swordTemplateOffer("mythril_upgrade_template", 61, ModItems.MYTHRIL_UPGRADE_TEMPLATE.get(), ModItems.MYTHRIL_INGOT.get(), 8));
        offers.add(simpleOffer("mythril_magnet", "mythril_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_MAGNET.get())), 68, ModItems.MYTHRIL_MAGNET.get()));
        offers.add(simpleOffer("mythril_paxel", "mythril_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MYTHRIL_PAXEL.get())), 61, ModItems.MYTHRIL_PAXEL.get()));
        offers.add(simpleOffer("arcanium_scrap", "arcanium_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_SCRAP.get())), 61, ModItems.ARCANIUM_SCRAP.get()));
        offers.add(simpleOffer("arcanium_ingot", "arcanium_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_INGOT.get())), 61, ModItems.ARCANIUM_INGOT.get()));
        offers.add(swordTemplateOffer("arcanium_upgrade_template", 61, ModItems.ARCANIUM_UPGRADE_TEMPLATE.get(), ModItems.ARCANIUM_INGOT.get(), 8));
        offers.add(simpleOffer("arcanium_magnet", "arcanium_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_MAGNET.get())), 76, ModItems.ARCANIUM_MAGNET.get()));
        offers.add(simpleOffer("arcanium_paxel", "arcanium_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.ARCANIUM_PAXEL.get())), 61, ModItems.ARCANIUM_PAXEL.get()));
        offers.add(simpleOffer("prismatic_steel_scrap", "prismatic_steel_scrap", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_SCRAP.get())), 81, ModItems.PRISMATIC_STEEL_SCRAP.get()));
        offers.add(simpleOffer("prismatic_steel_ingot", "prismatic_steel_ingot", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_INGOT.get())), 81, ModItems.PRISMATIC_STEEL_INGOT.get()));
        offers.add(simpleOffer("prismatic_steel_magnet", "prismatic_steel_magnet", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_MAGNET.get())), 90, ModItems.PRISMATIC_STEEL_MAGNET.get()));
        offers.add(simpleOffer("prismatic_steel_paxel", "prismatic_steel_paxel", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_STEEL_PAXEL.get())), 81, ModItems.PRISMATIC_STEEL_PAXEL.get()));
        offers.add(simpleOffer("prismatic_core", "prismatic_core", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.PRISMATIC_CORE.get())), 60, ModItems.PRISMATIC_CORE.get()));
        offers.add(shopOnlyOffer("iron_ingot", 90, 0, Items.IRON_INGOT, ""));
        offers.add(shopOnlyOffer("gold_ingot", 120, 0, Items.GOLD_INGOT, ""));
        offers.add(shopOnlyOffer("diamond", 420, 10, Items.DIAMOND, ""));
        offers.add(shopOnlyOffer("golden_apple", 850, 0, Items.GOLDEN_APPLE, ""));
        offers.add(shopOnlyOffer("netherite_scrap", 1500, 35, Items.NETHERITE_SCRAP, ""));
        offers.add(shopOnlyOffer("enchanted_golden_apple", 15000, 40, Items.ENCHANTED_GOLDEN_APPLE, ""));
        appendOptionalRunicOffers(offers);
        appendOptionalModdedOffers(offers);
        offers.add(shopOnlyOffer("shop_gateway", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.SHOP_GATEWAY.get())), 28, ModItems.SHOP_GATEWAY.get(), "Summons a traveling mythic merchant."));
        addCrystalThemeOffers(offers);
        offers.add(augmentOffer("easy_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EASY_AUGMENT.get())), 0, ModItems.EASY_AUGMENT.get(), AugmentDifficultyTier.EASY));
        offers.add(augmentOffer("medium_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.MEDIUM_AUGMENT.get())), 20, ModItems.MEDIUM_AUGMENT.get(), AugmentDifficultyTier.MEDIUM));
        offers.add(augmentOffer("hard_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.HARD_AUGMENT.get())), 30, ModItems.HARD_AUGMENT.get(), AugmentDifficultyTier.HARD));
        offers.add(augmentOffer("extreme_augment", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.EXTREME_AUGMENT.get())), 50, ModItems.EXTREME_AUGMENT.get(), AugmentDifficultyTier.EXTREME));
        offers.add(catalystOffer("time_catalyst", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.TIME_CATALYST.get())), 15, ModItems.TIME_CATALYST.get()));
        offers.add(simpleOffer("stability_pearl", "stability_pearl", GatewaySellValues.getSuggestedBuyPrice(new ItemStack(ModItems.STABILITY_PEARL.get())), 15, ModItems.STABILITY_PEARL.get()));
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
                (random, playerLevel) -> new ItemStack(item)
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
                (random, playerLevel) -> new ItemStack(item)
        );
    }

    private static ShopOfferDefinition swordTemplateOffer(String id, int requiredLevel, ItemLike template, Item ingredient, int ingredientCount) {
        ItemStack previewStack = new ItemStack(template);
        int ingredientValue = GatewaySellValues.getUnitValue(new ItemStack(ingredient));
        int baseValue = GatewaySellValues.getUnitValue(new ItemStack(ModItems.UPGRADE_BASE.get()));
        int price = Math.max(1, (int) Math.ceil((baseValue + ingredientValue * ingredientCount) * 1.1D));
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                true,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                (random, playerLevel) -> new ItemStack(template)
        );
    }

    private static void appendOptionalRunicOffers(List<ShopOfferDefinition> offers) {
        if (!ModCompat.isRunicLoaded()) {
            return;
        }

        addOptionalRegistryOffer(offers, "runic:repair_rune", 10, "A utility rune focused on restoration.");
        addOptionalRegistryOffer(offers, "runic:reroll_inscription", 25, "Lets you reroll a runic result.");
        addOptionalRegistryOffer(offers, "runic:expansion_rune", 30, "Expands the potential of runic gear.");
        addOptionalRegistryOffer(offers, "runic:nullification_rune", 35, "Cancels an unwanted runic trait.");
        addOptionalRegistryOffer(offers, "runic:upgrade_rune", 40, "Upgrades a runic result upward.");
        addOptionalRegistryOffer(offers, "runic:wild_inscription", 45, "A volatile inscription with broader outcomes.");
        addOptionalRegistryOffer(offers, "runic:extraction_inscription", 50, "Extracts existing runic power.");
        addOptionalRegistryOffer(offers, "runic:cursed_inscription", 55, "A risky inscription with stronger variance.");
    }

    private static void appendOptionalModdedOffers(List<ShopOfferDefinition> offers) {
        addOptionalRegistryOffer(offers, "friendsandfoes:totem_of_illusion", 40, "A rare charm that distorts enemy perception.");
        addOptionalRegistryOffer(offers, "friendsandfoes:totem_of_freezing", 40, "A rare charm infused with freezing illager magic.");
        addOptionalRegistryOffer(offers, "endermanoverhaul:enderman_tooth", 35, "A rare trophy pulled from a warped end stalker.");
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
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    AugmentStackData.ensureDefinition(stack, difficultyTier, random, playerLevel);
                    return stack;
                }
        );
    }

    private static ShopOfferDefinition catalystOffer(String id, int price, int requiredLevel, ItemLike item) {
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                false,
                new ItemStack(item),
                Component.translatable("shop.gatewayexpansion.offer." + id),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    CatalystStackData.ensureDefinition(stack, com.revilo.gatewayexpansion.catalyst.CatalystArchetype.TIME, random, playerLevel);
                    return stack;
                }
        );
    }

    private static void addCrystalThemeOffers(List<ShopOfferDefinition> offers) {
        offers.add(crystalOffer("tier_1_crystal_undead", 8, 0, ModItems.TIER_1_CRYSTAL.get(), 0, 19, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_2_crystal_undead", 15, 10, ModItems.TIER_2_CRYSTAL.get(), 20, 49, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_2_crystal_nether", 18, 25, ModItems.TIER_2_CRYSTAL.get(), 20, 49, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_3_crystal_undead", 42, 50, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_3_crystal_raider", 46, 50, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_3_crystal_nether", 48, 50, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_3_crystal_arcane", 54, 50, ModItems.TIER_3_CRYSTAL.get(), 50, 69, CrystalTheme.ARCANE));
        offers.add(crystalOffer("tier_4_crystal_undead", 68, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_4_crystal_raider", 74, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_4_crystal_nether", 76, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_4_crystal_arcane", 82, 50, ModItems.TIER_4_CRYSTAL.get(), 70, 89, CrystalTheme.ARCANE));
        offers.add(crystalOffer("tier_5_crystal_undead", 102, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.UNDEAD));
        offers.add(crystalOffer("tier_5_crystal_raider", 110, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.RAIDER));
        offers.add(crystalOffer("tier_5_crystal_nether", 112, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.NETHER));
        offers.add(crystalOffer("tier_5_crystal_arcane", 120, 70, ModItems.TIER_5_CRYSTAL.get(), 90, 100, CrystalTheme.ARCANE));
    }

    private static ShopOfferDefinition crystalOffer(String id, int price, int requiredLevel, ItemLike item, int minLevel, int maxLevel, CrystalTheme theme) {
        ItemStack previewStack = new ItemStack(item);
        CrystalForgeData.ensureProfile(previewStack, minLevel, maxLevel, RandomSource.create(0L));
        CrystalForgeData.attuneTheme(previewStack, theme);
        return new ShopOfferDefinition(
                id,
                price,
                requiredLevel,
                false,
                previewStack,
                previewStack.getHoverName(),
                Component.translatable("shop.gatewayexpansion.offer." + id + ".desc"),
                (random, playerLevel) -> {
                    ItemStack stack = new ItemStack(item);
                    CrystalForgeData.ensureProfile(stack, minLevel, maxLevel, random);
                    CrystalForgeData.attuneTheme(stack, theme);
                    return stack;
                }
        );
    }

    public ItemStack createStack(RandomSource random, int playerLevel) {
        return this.factory.create(random, playerLevel);
    }

    @FunctionalInterface
    public interface OfferFactory {
        ItemStack create(RandomSource random, int playerLevel);
    }
}
