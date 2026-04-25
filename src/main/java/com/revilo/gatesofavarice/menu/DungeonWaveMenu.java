package com.revilo.gatesofavarice.menu;

import com.revilo.gatesofavarice.dungeon.DungeonRunManager;
import com.revilo.gatesofavarice.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DungeonWaveMenu extends AbstractContainerMenu {

    public static final int OPTION_COUNT = 3;
    public static final int BAIL_BUTTON_ID = 100;

    private final UUID ownerId;
    private final int waveNumber;
    private final boolean ownerCanSelect;
    private final List<WaveOptionView> options;

    public DungeonWaveMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(
                containerId,
                inventory,
                data.readUUID(),
                data.readInt(),
                data.readBoolean(),
                readOptions(data)
        );
    }

    public DungeonWaveMenu(int containerId, Inventory inventory, UUID ownerId, int waveNumber, boolean ownerCanSelect, List<WaveOptionView> options) {
        super(ModMenus.DUNGEON_WAVE.get(), containerId);
        this.ownerId = ownerId;
        this.waveNumber = waveNumber;
        this.ownerCanSelect = ownerCanSelect;
        this.options = List.copyOf(options);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return DungeonRunManager.handleWaveMenuClick(player, this.ownerId, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return DungeonRunManager.isWaveMenuValid(player, this.ownerId);
    }

    public int waveNumber() {
        return this.waveNumber;
    }

    public boolean ownerCanSelect() {
        return this.ownerCanSelect;
    }

    public List<WaveOptionView> options() {
        return this.options;
    }

    private static List<WaveOptionView> readOptions(FriendlyByteBuf data) {
        int size = data.readInt();
        ArrayList<WaveOptionView> options = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            options.add(new WaveOptionView(
                    Component.literal(data.readUtf()),
                    Component.literal(data.readUtf()),
                    data.readInt(),
                    data.readInt()
            ));
        }
        return options;
    }

    public static void writePayload(FriendlyByteBuf buffer, UUID ownerId, int waveNumber, boolean ownerCanSelect, List<WaveOptionView> options) {
        buffer.writeUUID(ownerId);
        buffer.writeInt(waveNumber);
        buffer.writeBoolean(ownerCanSelect);
        buffer.writeInt(options.size());
        for (WaveOptionView option : options) {
            buffer.writeUtf(option.title().getString());
            buffer.writeUtf(option.details().getString());
            buffer.writeInt(option.inDungeonRewardPercent());
            buffer.writeInt(option.externalRewardPercent());
        }
    }

    public record WaveOptionView(
            Component title,
            Component details,
            int inDungeonRewardPercent,
            int externalRewardPercent
    ) {
    }
}
