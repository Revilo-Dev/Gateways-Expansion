package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.dungeon.DungeonBoundItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class DungeonBoundTooltipHandler {

    private DungeonBoundTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!DungeonBoundItems.isDungeonBound(event.getItemStack())) {
            return;
        }
        event.getToolTip().addAll(List.of(Component.literal("Dungeon Bound").withStyle(ChatFormatting.RED)));
    }
}
