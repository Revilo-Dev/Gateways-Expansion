package com.revilo.gatesofavarice.menu;

import com.revilo.gatesofavarice.dungeon.DungeonRunManager;
import com.revilo.gatesofavarice.registry.ModMenus;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DungeonLoadoutMenu extends AbstractContainerMenu {

    public static final int LOADOUT_COUNT = 3;

    private final UUID ownerId;

    public DungeonLoadoutMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readUUID());
    }

    public DungeonLoadoutMenu(int containerId, Inventory inventory, UUID ownerId) {
        super(ModMenus.DUNGEON_LOADOUT.get(), containerId);
        this.ownerId = ownerId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return DungeonRunManager.handleLoadoutMenuClick(player, this.ownerId, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return DungeonRunManager.isLoadoutMenuValid(player, this.ownerId);
    }

    public static void writePayload(FriendlyByteBuf buffer, UUID ownerId) {
        buffer.writeUUID(ownerId);
    }
}
