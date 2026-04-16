package com.revilo.gatesofavarice.item.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum AugmentDifficultyTier {
    EASY(ChatFormatting.GREEN),
    MEDIUM(ChatFormatting.YELLOW),
    HARD(ChatFormatting.GOLD),
    EXTREME(ChatFormatting.RED);

    private final ChatFormatting color;

    AugmentDifficultyTier(ChatFormatting color) {
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable("enum.gatesofavarice.augment_difficulty." + this.name().toLowerCase()).withStyle(this.color);
    }
}
