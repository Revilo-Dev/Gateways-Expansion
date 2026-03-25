package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GatewayExpansion.MOD_ID);

    public static final DeferredHolder<Item, CrystalItem> TIER_1_CRYSTAL = ITEMS.register("tier_1_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(1, 1, 10), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_2_CRYSTAL = ITEMS.register("tier_2_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(2, 11, 20), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_3_CRYSTAL = ITEMS.register("tier_3_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(3, 21, 30), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_4_CRYSTAL = ITEMS.register("tier_4_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(4, 31, 40), new Item.Properties()));
    public static final DeferredHolder<Item, CrystalItem> TIER_5_CRYSTAL = ITEMS.register("tier_5_crystal",
            () -> new CrystalItem(new CrystalItem.CrystalTier(5, 41, 50), new Item.Properties()));

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

    public static final DeferredHolder<Item, BlockItem> GATEWAY_WORKBENCH = ITEMS.register("gateway_workbench",
            () -> new BlockItem(ModBlocks.GATEWAY_WORKBENCH.get(), new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
