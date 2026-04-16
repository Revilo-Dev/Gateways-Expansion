package com.revilo.gatesofavarice.workbench;

import java.util.ArrayList;
import java.util.List;

public final class GatewayWorkbenchProgression {

    public static final int BASE_UNLOCKED_SLOTS = 4;
    public static final int LEVELS_PER_UNLOCK = 5;
    private static final List<SlotUnlock> UNLOCK_ORDER = buildUnlockOrder();

    private GatewayWorkbenchProgression() {
    }

    public static int getUnlockedSlotCount(int playerLevel) {
        if (playerLevel < 0) {
            return BASE_UNLOCKED_SLOTS;
        }
        int extra = Math.max(0, playerLevel / LEVELS_PER_UNLOCK);
        return Math.min(UNLOCK_ORDER.size(), BASE_UNLOCKED_SLOTS + extra);
    }

    public static boolean isUnlocked(int slotIndex, int playerLevel) {
        SlotUnlock unlock = getUnlock(slotIndex);
        return unlock == null || playerLevel >= unlock.requiredLevel();
    }

    public static int getRequiredLevel(int slotIndex) {
        SlotUnlock unlock = getUnlock(slotIndex);
        return unlock == null ? 0 : unlock.requiredLevel();
    }

    public static SlotType getSlotType(int slotIndex) {
        SlotUnlock unlock = getUnlock(slotIndex);
        return unlock == null ? SlotType.CRYSTAL : unlock.slotType();
    }

    private static SlotUnlock getUnlock(int slotIndex) {
        return UNLOCK_ORDER.stream().filter(entry -> entry.slotIndex() == slotIndex).findFirst().orElse(null);
    }

    private static List<SlotUnlock> buildUnlockOrder() {
        List<SlotUnlock> order = new ArrayList<>();
        int sequence = 0;
        for (int row = 0; row < GatewayWorkbenchSlots.GRID_ROWS; row++) {
            for (int column = 0; column < GatewayWorkbenchSlots.GRID_COLUMNS; column++) {
                order.add(unlock(GatewayWorkbenchSlots.CATALYST_SLOT_START + (row * GatewayWorkbenchSlots.GRID_COLUMNS) + column, SlotType.CATALYST, sequence++));
                order.add(unlock(GatewayWorkbenchSlots.AUGMENT_SLOT_START + (row * GatewayWorkbenchSlots.GRID_COLUMNS) + column, SlotType.AUGMENT, sequence++));
            }
        }
        return List.copyOf(order);
    }

    private static SlotUnlock unlock(int slotIndex, SlotType slotType, int sequence) {
        int requiredLevel = sequence < BASE_UNLOCKED_SLOTS ? 0 : (sequence - BASE_UNLOCKED_SLOTS + 1) * LEVELS_PER_UNLOCK;
        return new SlotUnlock(slotIndex, slotType, requiredLevel);
    }

    public enum SlotType {
        CRYSTAL,
        CATALYST,
        AUGMENT
    }

    public record SlotUnlock(int slotIndex, SlotType slotType, int requiredLevel) {
    }
}
