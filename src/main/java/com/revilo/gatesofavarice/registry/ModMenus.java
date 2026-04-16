package com.revilo.gatesofavarice.registry;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.menu.GatewayWorkbenchMenu;
import com.revilo.gatesofavarice.menu.ShopkeeperMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GatewayExpansion.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<GatewayWorkbenchMenu>> GATEWAY_WORKBENCH =
            MENUS.register("gateway_workbench", () -> new MenuType<>(GatewayWorkbenchMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<ShopkeeperMenu>> SHOPKEEPER =
            MENUS.register("shopkeeper", () -> IMenuTypeExtension.create(ShopkeeperMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
