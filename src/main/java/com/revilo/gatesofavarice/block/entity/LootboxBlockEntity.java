package com.revilo.gatesofavarice.block.entity;

import com.revilo.gatesofavarice.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LootboxBlockEntity extends BlockEntity {

    private static final String ROOT_KEY = "gatesofavarice";
    private static final String LOOT_KEY = "lootbox_loot";
    private NonNullList<ItemStack> loot = NonNullList.create();

    public LootboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LOOTBOX.get(), pos, blockState);
    }

    public void readFromItemStack(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        this.loot = NonNullList.create();
        if (this.level != null && root.contains(LOOT_KEY, 9)) {
            net.minecraft.nbt.ListTag list = root.getList(LOOT_KEY, 10);
            for (int i = 0; i < list.size(); i++) {
                ItemStack parsed = ItemStack.parseOptional(this.level.registryAccess(), list.getCompound(i));
                if (!parsed.isEmpty()) this.loot.add(parsed);
            }
        }
        setChanged();
    }

    public void writeToItemStack(ItemStack stack) {
        CompoundTag all = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag root = all.getCompound(ROOT_KEY);
        if (this.level != null) {
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            for (ItemStack entry : this.loot) {
                if (!entry.isEmpty()) list.add(entry.saveOptional(this.level.registryAccess()));
            }
            root.put(LOOT_KEY, list);
            all.put(ROOT_KEY, root);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
        }
    }

    public void setLoot(NonNullList<ItemStack> loot) {
        this.loot = loot;
        setChanged();
    }

    public void burstLoot(Level level, BlockPos pos) {
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack.copy());
            }
        }
        this.loot.clear();
        setChanged();
    }
}
