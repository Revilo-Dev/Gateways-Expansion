package com.revilo.gatesofavarice.registry;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.item.LoadoutArmorItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LoadoutArmorRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GatewayExpansion.MOD_ID);
    private static final Map<String, DeferredHolder<Item, LoadoutArmorItem>> BY_KEY = new HashMap<>();

    private LoadoutArmorRegistry() {
    }

    static {
        List<String> sets = List.of(
                "shadow_set", "steel_knight_set", "rage_set", "fortress_set", "windwalker_set", "soulbound_set",
                "hunter_set", "sharpshooter_set", "arena_set", "arcane_set", "tyrant_set", "traveler_set"
        );
        for (String setId : sets) {
            registerSet(setId);
        }
    }

    private static void registerSet(String setId) {
        registerPiece(setId, ArmorItem.Type.HELMET, "helmet");
        registerPiece(setId, ArmorItem.Type.CHESTPLATE, "chestplate");
        registerPiece(setId, ArmorItem.Type.LEGGINGS, "leggings");
        registerPiece(setId, ArmorItem.Type.BOOTS, "boots");
    }

    private static void registerPiece(String setId, ArmorItem.Type type, String suffix) {
        String key = setId + ":" + suffix;
        DeferredHolder<Item, LoadoutArmorItem> holder = ITEMS.register(
                setId + "_" + suffix,
                () -> new LoadoutArmorItem(ArmorMaterials.IRON, type, new Item.Properties().stacksTo(1), setId)
        );
        BY_KEY.put(key, holder);
    }

    public static Item get(String setId, ArmorItem.Type type) {
        String suffix = switch (type) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
            default -> "body";
        };
        DeferredHolder<Item, LoadoutArmorItem> holder = BY_KEY.get(setId + ":" + suffix);
        return holder == null ? net.minecraft.world.item.Items.AIR : holder.get();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

