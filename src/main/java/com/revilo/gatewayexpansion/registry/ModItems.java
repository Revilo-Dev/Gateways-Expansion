package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.data.AugmentDefinition;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.AugmentStatCategory;
import com.revilo.gatewayexpansion.item.data.AugmentStatEntry;
import com.revilo.gatewayexpansion.item.data.CatalystDefinition;
import java.util.List;
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
            () -> new AugmentItem(new AugmentDefinition(
                    "easy_augment",
                    AugmentDifficultyTier.EASY,
                    List.of(
                            new AugmentStatEntry(AugmentStatCategory.POPULATION, 2, "Wider spawn pool"),
                            new AugmentStatEntry(AugmentStatCategory.LOOT, 1, "Extra common loot")
                    ),
                    10,
                    List.of("starter", "stable")
            ), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> MEDIUM_AUGMENT = ITEMS.register("medium_augment",
            () -> new AugmentItem(new AugmentDefinition(
                    "medium_augment",
                    AugmentDifficultyTier.MEDIUM,
                    List.of(
                            new AugmentStatEntry(AugmentStatCategory.SPEED, 2, "Faster wave cadence"),
                            new AugmentStatEntry(AugmentStatCategory.HEALTH, 1, "Sturdier foes")
                    ),
                    18,
                    List.of("tempo", "pressure")
            ), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> HARD_AUGMENT = ITEMS.register("hard_augment",
            () -> new AugmentItem(new AugmentDefinition(
                    "hard_augment",
                    AugmentDifficultyTier.HARD,
                    List.of(
                            new AugmentStatEntry(AugmentStatCategory.ELITE, 2, "More elite enemies"),
                            new AugmentStatEntry(AugmentStatCategory.DAMAGE, 2, "Sharper enemy hits")
                    ),
                    30,
                    List.of("elite", "danger")
            ), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, AugmentItem> EXTREME_AUGMENT = ITEMS.register("extreme_augment",
            () -> new AugmentItem(new AugmentDefinition(
                    "extreme_augment",
                    AugmentDifficultyTier.EXTREME,
                    List.of(
                            new AugmentStatEntry(AugmentStatCategory.CHAOS, 3, "Chaotic encounter mix"),
                            new AugmentStatEntry(AugmentStatCategory.EFFECT, 2, "More status pressure"),
                            new AugmentStatEntry(AugmentStatCategory.LOOT, 3, "High-end reward spike")
                    ),
                    45,
                    List.of("volatile", "late-game")
            ), new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, CatalystItem> TIME_CATALYST = ITEMS.register("time_catalyst",
            () -> new CatalystItem(new CatalystDefinition("time_catalyst", "Shorter completion clock", "Tighter wave timing", List.of("time", "tempo")), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> STAT_CATALYST = ITEMS.register("stat_catalyst",
            () -> new CatalystItem(new CatalystDefinition("stat_catalyst", "Sharper stat modifier", "Enemies scale harder", List.of("stats", "pressure")), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> LOOT_CATALYST = ITEMS.register("loot_catalyst",
            () -> new CatalystItem(new CatalystDefinition("loot_catalyst", "Better reward rolls", "Riskier encounter tags", List.of("loot", "greed")), new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, CatalystItem> HIGHRISK_CATALYST = ITEMS.register("highrisk_catalyst",
            () -> new CatalystItem(new CatalystDefinition("highrisk_catalyst", "Large reward swing", "Severe danger spike", List.of("high-risk", "volatile")), new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, BlockItem> GATEWAY_WORKBENCH = ITEMS.register("gateway_workbench",
            () -> new BlockItem(ModBlocks.GATEWAY_WORKBENCH.get(), new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
