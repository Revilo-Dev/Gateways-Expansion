package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.item.MagnetItem;
import com.revilo.gatesofavarice.item.RunicItemSupport;
import com.revilo.gatesofavarice.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class CuriosCompat {

    private CuriosCompat() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CuriosCompat::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> magnetItems().forEach(item -> CuriosApi.registerCurio(item, new MagnetCurio())));
    }

    private static List<Item> magnetItems() {
        return List.of(
                ModItems.MANA_STEEL_MAGNET.get(),
                ModItems.ELIXRITE_MAGNET.get(),
                ModItems.ASTRITE_MAGNET.get(),
                ModItems.LUNARIUM_MAGNET.get(),
                ModItems.IGNITE_MAGNET.get(),
                ModItems.IRIDIUM_MAGNET.get(),
                ModItems.MYTHRIL_MAGNET.get(),
                ModItems.ARCANIUM_MAGNET.get(),
                ModItems.PRISMATIC_STEEL_MAGNET.get());
    }

    private static final class MagnetCurio implements ICurioItem {

        @Override
        public void curioTick(SlotContext slotContext, ItemStack stack) {
            if (!(stack.getItem() instanceof MagnetItem magnet) || !isBeltSlot(slotContext)) {
                return;
            }
            RunicItemSupport.ensureRunicData(stack, magnet.runeSlots());
            MagnetHandler.pullNearbyItems(slotContext.entity(), magnet);
        }

        private boolean isBeltSlot(SlotContext slotContext) {
            return "belt".equals(slotContext.identifier());
        }
    }
}
