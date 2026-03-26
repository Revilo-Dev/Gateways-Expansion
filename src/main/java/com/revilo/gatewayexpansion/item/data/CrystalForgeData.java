package com.revilo.gatewayexpansion.item.data;

import com.revilo.gatewayexpansion.GatewayExpansion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CrystalForgeData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final CrystalTheme[] THEMES = CrystalTheme.values();
    private static final String THEME_KEY = "theme";
    private static final String LEVEL_KEY = "level";
    private static final String SEED_KEY = "seed";

    private CrystalForgeData() {
    }

    public static CrystalProfile ensureProfile(ItemStack stack, int minLevel, int maxLevel, RandomSource random) {
        CompoundTag rootTag = getRootTag(stack);
        if (!rootTag.contains(THEME_KEY) || !rootTag.contains(LEVEL_KEY) || !rootTag.contains(SEED_KEY)) {
            CrystalTheme theme = THEMES[random.nextInt(THEMES.length)];
            int level = Mth.clamp(minLevel + random.nextInt(Math.max(1, maxLevel - minLevel + 1)), minLevel, maxLevel);
            long seed = random.nextLong();
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                CompoundTag updatedRoot = tag.getCompound(ROOT_KEY);
                updatedRoot.putString(THEME_KEY, theme.name());
                updatedRoot.putInt(LEVEL_KEY, level);
                updatedRoot.putLong(SEED_KEY, seed);
                tag.put(ROOT_KEY, updatedRoot);
            });
            return new CrystalProfile(theme, level, seed);
        }
        return readProfile(rootTag, minLevel, maxLevel);
    }

    public static CrystalProfile getProfile(ItemStack stack, int minLevel, int maxLevel) {
        return readProfile(getRootTag(stack), minLevel, maxLevel);
    }

    public static List<Component> buildCrystalTooltip(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        CompoundTag rootTag = getRootTag(stack);
        if (rootTag.contains(THEME_KEY)) {
            CrystalTheme theme = CrystalTheme.valueOf(rootTag.getString(THEME_KEY));
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.theme", theme.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (rootTag.contains(LEVEL_KEY)) {
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.level", rootTag.getInt(LEVEL_KEY)).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
    }

    private static CrystalProfile readProfile(CompoundTag rootTag, int minLevel, int maxLevel) {
        CrystalTheme theme = rootTag.contains(THEME_KEY) ? CrystalTheme.valueOf(rootTag.getString(THEME_KEY)) : CrystalTheme.UNDEAD;
        int level = rootTag.contains(LEVEL_KEY) ? Mth.clamp(rootTag.getInt(LEVEL_KEY), minLevel, maxLevel) : minLevel;
        long seed = rootTag.contains(SEED_KEY) ? rootTag.getLong(SEED_KEY) : level * 31L + theme.ordinal();
        return new CrystalProfile(theme, level, seed);
    }

    public record CrystalProfile(CrystalTheme theme, int level, long seed) {
    }
}
