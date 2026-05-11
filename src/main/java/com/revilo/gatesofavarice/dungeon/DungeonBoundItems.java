package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.GatewayExpansion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.CustomData;

public final class DungeonBoundItems {

    public static final String DUNGEON_BOUND_KEY = "dungeon_bound";

    private DungeonBoundItems() {
    }

    public static boolean isDungeonBound(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(GatewayExpansion.MOD_ID);
        return root.getBoolean(DUNGEON_BOUND_KEY);
    }

    public static void markIfDungeonBound(ItemStack stack) {
        if (!isBoundCandidate(stack)) {
            return;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag root = tag.getCompound(GatewayExpansion.MOD_ID);
        root.putBoolean(DUNGEON_BOUND_KEY, true);
        tag.put(GatewayExpansion.MOD_ID, root);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        applyArmorBindingCurse(stack);
    }

    public static void removeBoundItems(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            if (isDungeonBound(inv.items.get(i))) {
                inv.items.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            if (isDungeonBound(inv.armor.get(i))) {
                inv.armor.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            if (isDungeonBound(inv.offhand.get(i))) {
                inv.offhand.set(i, ItemStack.EMPTY);
            }
        }
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static boolean isBoundCandidate(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof DiggerItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof ShieldItem;
    }

    public static void applyArmorBindingCurse(ItemStack stack) {
        // TODO: Apply binding curse through NeoForge 1.21 enchantment holder API.
    }
}
