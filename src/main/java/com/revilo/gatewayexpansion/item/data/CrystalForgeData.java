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
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.CustomData;

public final class CrystalForgeData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String THEME_KEY = "theme";
    private static final String LEVEL_KEY = "level";
    private static final String SEED_KEY = "seed";
    private static final String ATTUNED_KEY = "attuned";

    private CrystalForgeData() {
    }

    public static CrystalProfile ensureProfile(ItemStack stack, int minLevel, int maxLevel, RandomSource random) {
        CompoundTag rootTag = getRootTag(stack);
        if (!rootTag.contains(THEME_KEY) || !rootTag.contains(LEVEL_KEY) || !rootTag.contains(SEED_KEY)) {
            int level = minLevel;
            long seed = random.nextLong();
            CrystalTheme theme = randomThemeForLevel(level, seed);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                CompoundTag updatedRoot = tag.getCompound(ROOT_KEY);
                updatedRoot.putString(THEME_KEY, theme.name());
                updatedRoot.putInt(LEVEL_KEY, level);
                updatedRoot.putLong(SEED_KEY, seed);
                updatedRoot.putBoolean(ATTUNED_KEY, false);
                tag.put(ROOT_KEY, updatedRoot);
            });
            syncModelData(stack, level);
            return new CrystalProfile(theme, level, seed);
        }
        CrystalProfile profile = readProfile(rootTag, minLevel, maxLevel);
        syncModelData(stack, profile.level());
        return profile;
    }

    public static CrystalProfile syncLevelToPlayer(ItemStack stack, int minLevel, int maxLevel, int playerLevel, RandomSource random) {
        CrystalProfile profile = ensureProfile(stack, minLevel, maxLevel, random);
        if (playerLevel < 0) {
            return profile;
        }

        int level = Mth.clamp(playerLevel, minLevel, maxLevel);
        CompoundTag rootTag = getRootTag(stack);
        boolean attuned = rootTag.contains(ATTUNED_KEY) && rootTag.getBoolean(ATTUNED_KEY);
        CrystalTheme theme = attuned ? profile.theme() : randomThemeForLevel(level, profile.seed());
        if (profile.level() == level && profile.theme() == theme) {
            return profile;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag updatedRoot = tag.getCompound(ROOT_KEY);
            updatedRoot.putString(THEME_KEY, theme.name());
            updatedRoot.putInt(LEVEL_KEY, level);
            updatedRoot.putLong(SEED_KEY, profile.seed());
            updatedRoot.putBoolean(ATTUNED_KEY, attuned);
            tag.put(ROOT_KEY, updatedRoot);
        });
        syncModelData(stack, level);
        return new CrystalProfile(theme, level, profile.seed());
    }

    public static CrystalProfile getProfile(ItemStack stack, int minLevel, int maxLevel) {
        return readProfile(getRootTag(stack), minLevel, maxLevel);
    }

    public static void attuneTheme(ItemStack stack, CrystalTheme theme) {
        CompoundTag rootTag = getRootTag(stack);
        int level = rootTag.contains(LEVEL_KEY) ? rootTag.getInt(LEVEL_KEY) : 0;
        long seed = rootTag.contains(SEED_KEY) ? rootTag.getLong(SEED_KEY) : (level * 31L + theme.ordinal());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag updatedRoot = tag.getCompound(ROOT_KEY);
            updatedRoot.putString(THEME_KEY, theme.name());
            updatedRoot.putInt(LEVEL_KEY, level);
            updatedRoot.putLong(SEED_KEY, seed);
            updatedRoot.putBoolean(ATTUNED_KEY, true);
            tag.put(ROOT_KEY, updatedRoot);
        });
        syncModelData(stack, level);
    }

    public static List<Component> buildCrystalTooltip(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        CompoundTag rootTag = getRootTag(stack);
        if (rootTag.contains(THEME_KEY) && rootTag.contains(ATTUNED_KEY) && rootTag.getBoolean(ATTUNED_KEY)) {
            CrystalTheme theme = CrystalTheme.valueOf(rootTag.getString(THEME_KEY));
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.theme", themedLabel(theme)));
            lines.addAll(themeSummary(theme));
        } else {
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.random_theme").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (rootTag.contains(LEVEL_KEY)) {
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.level", rootTag.getInt(LEVEL_KEY)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        return lines;
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
    }

    private static CrystalProfile readProfile(CompoundTag rootTag, int minLevel, int maxLevel) {
        int level = rootTag.contains(LEVEL_KEY) ? Mth.clamp(rootTag.getInt(LEVEL_KEY), minLevel, maxLevel) : minLevel;
        long seed = rootTag.contains(SEED_KEY) ? rootTag.getLong(SEED_KEY) : level * 31L;
        CrystalTheme theme = rootTag.contains(THEME_KEY) ? CrystalTheme.valueOf(rootTag.getString(THEME_KEY)) : randomThemeForLevel(level, seed);
        return new CrystalProfile(theme, level, seed);
    }

    private static Component themedLabel(CrystalTheme theme) {
        return theme.displayName().copy()
                .append(Component.literal(" "))
                .append(Component.literal("[alt]").withStyle(ChatFormatting.GRAY));
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
        }
        return lines;
    }

    private static Component themeStat(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static CrystalTheme randomThemeForLevel(int level, long seed) {
        RandomSource random = RandomSource.create(seed ^ ((long) level << 32) ^ 0x5F3759D5L);
        int undeadWeight;
        int netherWeight;
        int raiderWeight;
        int arcaneWeight;
        if (level >= 50) {
            undeadWeight = 45;
            netherWeight = 25;
            raiderWeight = 18;
            arcaneWeight = 12;
        } else if (level >= 30) {
            undeadWeight = 60;
            netherWeight = 25;
            raiderWeight = 15;
            arcaneWeight = 0;
        } else if (level >= 25) {
            undeadWeight = 75;
            netherWeight = 25;
            raiderWeight = 0;
            arcaneWeight = 0;
        } else {
            undeadWeight = 100;
            netherWeight = 0;
            raiderWeight = 0;
            arcaneWeight = 0;
        }

        int roll = random.nextInt(undeadWeight + netherWeight + raiderWeight + arcaneWeight);
        if ((roll -= undeadWeight) < 0) {
            return CrystalTheme.UNDEAD;
        }
        if ((roll -= netherWeight) < 0) {
            return CrystalTheme.NETHER;
        }
        if ((roll -= raiderWeight) < 0) {
            return CrystalTheme.RAIDER;
        }
        return CrystalTheme.ARCANE;
    }

    private static void syncModelData(ItemStack stack, int level) {
        int modelData = modelDataForLevel(level);
        CustomModelData existing = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (existing == null || existing.value() != modelData) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(modelData));
        }
    }

    private static int modelDataForLevel(int level) {
        if (level >= 81) {
            return 3;
        }
        if (level >= 51) {
            return 2;
        }
        return 1;
    }

    public record CrystalProfile(CrystalTheme theme, int level, long seed) {
    }
}
