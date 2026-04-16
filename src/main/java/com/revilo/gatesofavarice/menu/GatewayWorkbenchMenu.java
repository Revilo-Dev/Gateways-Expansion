package com.revilo.gatesofavarice.menu;

import com.revilo.gatesofavarice.integration.LevelUpIntegration;
import com.revilo.gatesofavarice.item.AugmentItem;
import com.revilo.gatesofavarice.item.CatalystItem;
import com.revilo.gatesofavarice.item.CrystalItem;
import com.revilo.gatesofavarice.gateway.builder.GatewayPreview;
import com.revilo.gatesofavarice.registry.ModBlocks;
import com.revilo.gatesofavarice.registry.ModMenus;
import com.revilo.gatesofavarice.workbench.GatewayWorkbenchForgeLogic;
import com.revilo.gatesofavarice.workbench.GatewayWorkbenchProgression;
import com.revilo.gatesofavarice.workbench.GatewayWorkbenchSlots;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GatewayWorkbenchMenu extends AbstractContainerMenu {

    public static final int FORGE_BUTTON_ID = 0;
    public static final int CRYSTAL_SLOT = GatewayWorkbenchSlots.CRYSTAL_SLOT;
    public static final int AUGMENT_SLOT_START = GatewayWorkbenchSlots.AUGMENT_SLOT_START;
    public static final int AUGMENT_SLOT_COUNT = GatewayWorkbenchSlots.AUGMENT_SLOT_COUNT;
    public static final int CATALYST_SLOT_START = GatewayWorkbenchSlots.CATALYST_SLOT_START;
    public static final int CATALYST_SLOT_COUNT = GatewayWorkbenchSlots.CATALYST_SLOT_COUNT;
    public static final int OUTPUT_SLOT = GatewayWorkbenchSlots.OUTPUT_SLOT;
    public static final int CUSTOM_SLOT_COUNT = GatewayWorkbenchSlots.CUSTOM_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = CUSTOM_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerLevelAccess access;
    private final Player player;

    public GatewayWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CUSTOM_SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    public GatewayWorkbenchMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.GATEWAY_WORKBENCH.get(), containerId);
        checkContainerSize(container, CUSTOM_SLOT_COUNT);
        this.container = container;
        this.access = access;
        this.player = playerInventory.player;
        this.container.startOpen(playerInventory.player);

        this.addSlot(new FilteredSlot(container, CRYSTAL_SLOT, GatewayWorkbenchSlots.CRYSTAL_X, GatewayWorkbenchSlots.CRYSTAL_Y, stack -> stack.getItem() instanceof CrystalItem));

        for (int index = 0; index < CATALYST_SLOT_COUNT; index++) {
            int x = GatewayWorkbenchSlots.CATALYST_START_X + (index % GatewayWorkbenchSlots.GRID_COLUMNS) * GatewayWorkbenchSlots.SLOT_SPACING;
            int y = GatewayWorkbenchSlots.GRID_START_Y + (index / GatewayWorkbenchSlots.GRID_COLUMNS) * GatewayWorkbenchSlots.SLOT_SPACING;
            int slotIndex = CATALYST_SLOT_START + index;
            this.addSlot(new ProgressionSlot(container, slotIndex, x, y, stack -> stack.getItem() instanceof CatalystItem, GatewayWorkbenchProgression.getRequiredLevel(slotIndex), GatewayWorkbenchProgression.SlotType.CATALYST));
        }

        for (int index = 0; index < AUGMENT_SLOT_COUNT; index++) {
            int x = GatewayWorkbenchSlots.AUGMENT_START_X + (index % GatewayWorkbenchSlots.GRID_COLUMNS) * GatewayWorkbenchSlots.SLOT_SPACING;
            int y = GatewayWorkbenchSlots.GRID_START_Y + (index / GatewayWorkbenchSlots.GRID_COLUMNS) * GatewayWorkbenchSlots.SLOT_SPACING;
            int slotIndex = AUGMENT_SLOT_START + index;
            this.addSlot(new ProgressionSlot(container, slotIndex, x, y, stack -> stack.getItem() instanceof AugmentItem, GatewayWorkbenchProgression.getRequiredLevel(slotIndex), GatewayWorkbenchProgression.SlotType.AUGMENT));
        }
        this.addSlot(new ResultSlot(container, OUTPUT_SLOT, GatewayWorkbenchSlots.OUTPUT_X, GatewayWorkbenchSlots.OUTPUT_Y));

        this.addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalStack = stackInSlot.copy();

        if (index == OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stackInSlot, originalStack);
        } else if (index < CUSTOM_SLOT_COUNT) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.getItem() instanceof CrystalItem) {
            if (!this.moveItemStackTo(stackInSlot, CRYSTAL_SLOT, CRYSTAL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.getItem() instanceof CatalystItem) {
            if (!this.moveItemStackToProgressionSlots(stackInSlot, CATALYST_SLOT_START, CATALYST_SLOT_START + CATALYST_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.getItem() instanceof AugmentItem) {
            if (!this.moveItemStackToProgressionSlots(stackInSlot, AUGMENT_SLOT_START, AUGMENT_SLOT_START + AUGMENT_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INVENTORY_END) {
            if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stackInSlot);
        return originalStack;
    }

    private boolean moveItemStackToProgressionSlots(ItemStack stack, int startInclusive, int endExclusive, boolean reverse) {
        if (reverse) {
            for (int slotIndex = endExclusive - 1; slotIndex >= startInclusive; slotIndex--) {
                if (this.slots.get(slotIndex) instanceof ProgressionSlot progressionSlot && !progressionSlot.isLocked()
                        && this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                    return true;
                }
            }
            return false;
        }

        for (int slotIndex = startInclusive; slotIndex < endExclusive; slotIndex++) {
            if (this.slots.get(slotIndex) instanceof ProgressionSlot progressionSlot && !progressionSlot.isLocked()
                    && this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != FORGE_BUTTON_ID) {
            return false;
        }

        if (GatewayWorkbenchForgeLogic.forge(player, this.container)) {
            this.broadcastChanges();
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.GATEWAY_WORKBENCH.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public ItemStack getCrystalStack() {
        return this.container.getItem(CRYSTAL_SLOT);
    }

    public List<ItemStack> getCatalystStacks() {
        return GatewayWorkbenchSlots.collectCatalysts(this.container);
    }

    public List<ItemStack> getAugmentStacks() {
        return GatewayWorkbenchSlots.collectAugments(this.container);
    }

    public boolean canForge() {
        return GatewayWorkbenchForgeLogic.canForge(this.player, this.container);
    }

    public GatewayPreview getPreviewData() {
        return GatewayWorkbenchForgeLogic.buildPreview(this.player, this.container);
    }

    private boolean canAcceptWorkbenchInput() {
        return this.container.getItem(OUTPUT_SLOT).isEmpty();
    }

    public int getPlayerLevel() {
        return LevelUpIntegration.getPlayerLevel(this.player);
    }

    public boolean isSlotLocked(int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        return slot instanceof ProgressionSlot progressionSlot && progressionSlot.isLocked();
    }

    public int getRequiredLevelForSlot(int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        return slot instanceof ProgressionSlot progressionSlot ? progressionSlot.requiredLevel() : 0;
    }

    private class FilteredSlot extends Slot {

        private final Predicate<ItemStack> predicate;

        private FilteredSlot(Container container, int slot, int x, int y, Predicate<ItemStack> predicate) {
            super(container, slot, x, y);
            this.predicate = predicate;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return GatewayWorkbenchMenu.this.canAcceptWorkbenchInput() && this.predicate.test(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    public final class ProgressionSlot extends FilteredSlot {

        private final int requiredLevel;
        private final GatewayWorkbenchProgression.SlotType slotType;

        private ProgressionSlot(Container container, int slot, int x, int y, Predicate<ItemStack> predicate, int requiredLevel, GatewayWorkbenchProgression.SlotType slotType) {
            super(container, slot, x, y, predicate);
            this.requiredLevel = requiredLevel;
            this.slotType = slotType;
        }

        public boolean isLocked() {
            return GatewayWorkbenchMenu.this.getPlayerLevel() < this.requiredLevel;
        }

        public int requiredLevel() {
            return this.requiredLevel;
        }

        public GatewayWorkbenchProgression.SlotType slotType() {
            return this.slotType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.isLocked() && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !this.isLocked() && super.mayPickup(player);
        }
    }

    private static final class ResultSlot extends Slot {

        private ResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
