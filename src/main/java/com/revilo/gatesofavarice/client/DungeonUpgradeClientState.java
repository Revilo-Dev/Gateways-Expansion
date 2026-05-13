package com.revilo.gatesofavarice.client;

import com.revilo.gatesofavarice.dungeon.loadout.LoadoutModels.UpgradeCard;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class DungeonUpgradeClientState {
    public static String sessionId = "";
    public static String loadoutName = "";
    public static String theme = "";
    public static String categoryName = "";
    public static ItemStack previewStack = ItemStack.EMPTY;
    public static List<UpgradeCard> cards = List.of();

    private DungeonUpgradeClientState() {}
}

