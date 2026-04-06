package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.LootMaterialItem;
import com.revilo.gatewayexpansion.item.MythicCoinItem;
import com.revilo.gatewayexpansion.item.PaxelItem;
import com.revilo.gatewayexpansion.item.ShopGatewayPearlItem;
import com.revilo.gatewayexpansion.item.StabilityPearlItem;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.LootRarity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GatewayExpansion.MOD_ID);

    public static final DeferredHolder<Item, CrystalItem> TIER_1_CRYSTAL = ITEMS.register("tier_1_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(1, 0, 19), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_2_CRYSTAL = ITEMS.register("tier_2_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(2, 20, 49), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_3_CRYSTAL = ITEMS.register("tier_3_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(3, 50, 69), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_4_CRYSTAL = ITEMS.register("tier_4_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(4, 70, 89), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_5_CRYSTAL = ITEMS.register("tier_5_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(5, 90, 100), new Item.Properties()));

    public static final DeferredHolder<Item, AugmentItem> EASY_AUGMENT = ITEMS.register("easy_augment",
            () -> new AugmentItem(AugmentDifficultyTier.EASY, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> MEDIUM_AUGMENT = ITEMS.register("medium_augment",
            () -> new AugmentItem(AugmentDifficultyTier.MEDIUM, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> HARD_AUGMENT = ITEMS.register("hard_augment",
            () -> new AugmentItem(AugmentDifficultyTier.HARD, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> EXTREME_AUGMENT = ITEMS.register("extreme_augment",
            () -> new AugmentItem(AugmentDifficultyTier.EXTREME, new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, CatalystItem> TIME_CATALYST = ITEMS.register("time_catalyst",
            () -> new CatalystItem(CatalystArchetype.TIME, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> STAT_CATALYST = ITEMS.register("stat_catalyst",
            () -> new CatalystItem(CatalystArchetype.STAT, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> LOOT_CATALYST = ITEMS.register("loot_catalyst",
            () -> new CatalystItem(CatalystArchetype.LOOT, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> HIGHRISK_CATALYST = ITEMS.register("highrisk_catalyst",
            () -> new CatalystItem(CatalystArchetype.VOLATILE, new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, MythicCoinItem> MYTHIC_COIN = ITEMS.register("mythic_coin",
            () -> new MythicCoinItem(new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, StabilityPearlItem> STABILITY_PEARL = ITEMS.register("stability_pearl",
            () -> new StabilityPearlItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> GRIMSTONE = ITEMS.register("grimstone",
            () -> new LootMaterialItem(LootRarity.COMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> MYSTIC_ESSENCE = ITEMS.register("mystic_essence",
            () -> new LootMaterialItem(LootRarity.COMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> DARK_ESSENCE = ITEMS.register("dark_essence",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> ARCANE_ESSENCE = ITEMS.register("arcane_essence",
            () -> new LootMaterialItem(LootRarity.UNCOMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> MANASTONES = ITEMS.register("manastones",
            () -> new LootMaterialItem(LootRarity.UNCOMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> MANA_GEMS = ITEMS.register("mana_gems",
            () -> new LootMaterialItem(LootRarity.UNCOMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> MANA_STEEL_SCRAP = ITEMS.register("mana_steel_scrap",
            () -> new LootMaterialItem(LootRarity.UNCOMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> MANA_STEEL_INGOT = ITEMS.register("mana_steel_ingot",
            () -> new LootMaterialItem(LootRarity.UNCOMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> SCRAP_METAL = ITEMS.register("scrap_metal",
            () -> new LootMaterialItem(LootRarity.COMMON, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> ELIXRITE_SCRAP = ITEMS.register("elixrite_scrap",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> ELIXRITE_INGOT = ITEMS.register("elixrite_ingot",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> ASTRITE_SCRAP = ITEMS.register("astrite_scrap",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> ASTRITE_INGOT = ITEMS.register("astrite_ingot",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> PRISMATIC_CORE = ITEMS.register("prismatic_core",
            () -> new LootMaterialItem(LootRarity.LEGENDARY, new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, LootMaterialItem> SOLAR_CRYSTAL = ITEMS.register("solar_crystal",
            () -> new LootMaterialItem(LootRarity.RARE, new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, LootMaterialItem> PRISMATIC_DIAMOND = ITEMS.register("prismatic_diamond",
            () -> new LootMaterialItem(LootRarity.EPIC, new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, LootMaterialItem> LUNARIUM_SCRAP = ITEMS.register("lunarium_scrap",
            () -> new LootMaterialItem(LootRarity.EPIC, new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, LootMaterialItem> LUNARIUM_INGOT = ITEMS.register("lunarium_ingot",
            () -> new LootMaterialItem(LootRarity.EPIC, new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, PaxelItem> MANA_STEEL_PAXEL = ITEMS.register("mana_steel_paxel",
            () -> new PaxelItem(ModToolTiers.MANA_STEEL, new Item.Properties(), 4.0F, -2.9F));
    public static final DeferredHolder<Item, PaxelItem> ELIXRITE_PAXEL = ITEMS.register("elixrite_paxel",
            () -> new PaxelItem(ModToolTiers.ELIXRITE, new Item.Properties().rarity(Rarity.RARE), 5.0F, -2.8F));
    public static final DeferredHolder<Item, PaxelItem> ASTRITE_PAXEL = ITEMS.register("astrite_paxel",
            () -> new PaxelItem(ModToolTiers.ASTRITE, new Item.Properties().rarity(Rarity.RARE), 5.0F, -2.8F));
    public static final DeferredHolder<Item, PaxelItem> LUNARIUM_PAXEL = ITEMS.register("lunarium_paxel",
            () -> new PaxelItem(ModToolTiers.LUNARIUM, new Item.Properties().rarity(Rarity.EPIC), 6.0F, -2.8F));
    public static final DeferredHolder<Item, ShopGatewayPearlItem> SHOP_GATEWAY = ITEMS.register("shop_gateway",
            () -> new ShopGatewayPearlItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, BlockItem> GATEWAY_WORKBENCH = ITEMS.register("gateway_workbench",
            () -> new BlockItem(ModBlocks.GATEWAY_WORKBENCH.get(), new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
