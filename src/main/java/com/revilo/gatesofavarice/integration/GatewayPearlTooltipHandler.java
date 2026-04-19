package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import dev.shadowsoffire.gateways.GatewayObjects;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class GatewayPearlTooltipHandler {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String THEME_KEY = "theme";

    private GatewayPearlTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(GatewayObjects.GATE_PEARL.value())) {
            return;
        }

        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
        if (!root.contains(THEME_KEY)) {
            return;
        }

        CrystalTheme theme = parseTheme(root.getString(THEME_KEY));
        if (theme == null) {
            return;
        }

        if (isAltHeld()) {
            event.getToolTip().addAll(themeSummary(theme));
        }
    }

    private static CrystalTheme parseTheme(String name) {
        try {
            return CrystalTheme.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static List<Component> themeSummary(CrystalTheme theme) {
        List<Component> lines = new ArrayList<>();
        switch (theme) {
            case UNDEAD -> {
                lines.add(themeStat("More mobs"));
                lines.add(themeStat("High item quantity"));
                lines.add(themeStat("Low rarity"));
            }
            case RAIDER -> {
                lines.add(themeStat("More assassins"));
                lines.add(themeStat("High rarity"));
                lines.add(themeStat("High coins"));
                lines.add(themeStat("Low levels"));
            }
            case NETHER -> {
                lines.add(themeStat("More tanks"));
                lines.add(themeStat("High xp"));
                lines.add(themeStat("High quantity"));
                lines.add(themeStat("Low coins"));
            }
            case ARCANE -> {
                lines.add(themeStat("More Chaos"));
                lines.add(themeStat("High levels"));
                lines.add(themeStat("High coins"));
                lines.add(themeStat("High rarity"));
                lines.add(themeStat("Low xp"));
            }
            case BEAST -> {
            }
            case WILD -> {
                lines.add(themeStat("Unpredictable waves"));
                lines.add(themeStat("Balanced archetypes"));
                lines.add(themeStat("All enemy factions"));
            }
        }
        return lines;
    }

    private static Component themeStat(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static boolean isAltHeld() {
        try {
            Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
            Object value = screenClass.getMethod("hasAltDown").invoke(null);
            return value instanceof Boolean bool && bool;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
