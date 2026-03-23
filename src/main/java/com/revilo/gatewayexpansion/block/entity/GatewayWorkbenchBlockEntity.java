package com.revilo.gatewayexpansion.block.entity;

import com.revilo.gatewayexpansion.menu.GatewayWorkbenchMenu;
import com.revilo.gatewayexpansion.registry.ModBlockEntities;
import com.revilo.gatewayexpansion.workbench.GatewayWorkbenchSlots;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GatewayWorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_COUNT = GatewayWorkbenchSlots.CUSTOM_SLOT_COUNT;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public GatewayWorkbenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GATEWAY_WORKBENCH.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GatewayWorkbenchBlockEntity blockEntity) {
        if (level.getGameTime() % 200L == 0L) {
            setChanged(level, pos, state);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.gatewayexpansion.gateway_workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GatewayWorkbenchMenu(containerId, playerInventory, this, ContainerLevelAccess.create(Objects.requireNonNull(this.level), this.worldPosition));
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : this.items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(this.items, slot);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getCenter()) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return GatewayWorkbenchSlots.mayPlace(slot, stack);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}
