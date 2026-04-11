package com.revilo.gatewayexpansion.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

public interface RarityTintedItemName {

    ChatFormatting nameColor();

    default Component tintedName(ItemStack stack, Component baseName) {
        if (this.nameColor() == ChatFormatting.GOLD) {
            return shimmeringLegendary(baseName.getString());
        }
        return baseName.copy().withStyle(this.nameColor());
    }

    private static Component shimmeringLegendary(String text) {
        if (text.isEmpty()) {
            return Component.empty();
        }

        final int baseGold = 0xB57A00;
        final int edgeGold = 0xD8A000;
        final int warmYellow = 0xFFE066;
        final int hotWhite = 0xFFFFFF;

        int length = text.length();
        // One sweeping highlight moving left -> right across the full text.
        double sweep = ((System.currentTimeMillis() % 1800L) / 1800.0D) * (length + 8) - 4.0D;
        MutableComponent result = Component.empty();
        for (int i = 0; i < length; i++) {
            double distance = Math.abs(i - sweep);
            int color;
            if (distance < 0.6D) {
                color = hotWhite;
            } else if (distance < 1.6D) {
                color = lerpColor(warmYellow, hotWhite, (float) (1.6D - distance));
            } else if (distance < 2.8D) {
                color = lerpColor(edgeGold, warmYellow, (float) ((2.8D - distance) / 1.2D));
            } else {
                color = baseGold;
            }
            result = result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }
        return result;
    }

    private static int lerpColor(int from, int to, float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int rr = Math.round(fr + (tr - fr) * clamped);
        int rg = Math.round(fg + (tg - fg) * clamped);
        int rb = Math.round(fb + (tb - fb) * clamped);
        return (rr << 16) | (rg << 8) | rb;
    }
}
