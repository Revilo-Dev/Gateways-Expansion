package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GatewayExpansion.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gatewayexpansion.main"))
                    .icon(() -> new ItemStack(ModItems.GATEWAY_WORKBENCH.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GATEWAY_WORKBENCH.get());
                        output.accept(ModItems.TIER_1_CRYSTAL.get());
                        output.accept(ModItems.TIER_2_CRYSTAL.get());
                        output.accept(ModItems.TIER_3_CRYSTAL.get());
                        output.accept(ModItems.TIER_4_CRYSTAL.get());
                        output.accept(ModItems.TIER_5_CRYSTAL.get());
                        output.accept(ModItems.EASY_AUGMENT.get());
                        output.accept(ModItems.MEDIUM_AUGMENT.get());
                        output.accept(ModItems.HARD_AUGMENT.get());
                        output.accept(ModItems.EXTREME_AUGMENT.get());
                        output.accept(ModItems.TIME_CATALYST.get());
                        output.accept(ModItems.STAT_CATALYST.get());
                        output.accept(ModItems.LOOT_CATALYST.get());
                        output.accept(ModItems.HIGHRISK_CATALYST.get());
                        output.accept(ModItems.MYTHIC_COIN.get());
                        output.accept(ModItems.GRIMSTONE.get());
                        output.accept(ModItems.MYSTIC_ESSENCE.get());
                        output.accept(ModItems.DARK_ESSENCE.get());
                        output.accept(ModItems.ARCANE_ESSENCE.get());
                        output.accept(ModItems.MANASTONES.get());
                        output.accept(ModItems.MANA_GEMS.get());
                        output.accept(ModItems.SCRAP_METAL.get());
                        output.accept(ModItems.PRISMATIC_CORE.get());
                        output.accept(ModItems.SOLAR_CRYSTAL.get());
                        output.accept(ModItems.PRISMATIC_DIAMOND.get());
                        output.accept(ModItems.SHOP_GATEWAY.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
