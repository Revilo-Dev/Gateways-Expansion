package com.revilo.gatewayexpansion.item.data;

import com.revilo.gatewayexpansion.GatewayExpansion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CrystalForgeData {

    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String FORGED_KEY = "forged";
    private static final String AUGMENT_COUNT_KEY = "augment_count";
    private static final String CATALYST_COUNT_KEY = "catalyst_count";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String REWARD_KEY = "reward_bonus";
    private static final String PREVIEW_KEY = "preview_lines";

    private CrystalForgeData() {
    }

    public static boolean isForged(ItemStack stack) {
        CompoundTag rootTag = getRootTag(stack);
        return rootTag.getBoolean(FORGED_KEY);
    }

    public static int getAugmentCount(ItemStack stack) {
        return getRootTag(stack).getInt(AUGMENT_COUNT_KEY);
    }

    public static int getCatalystCount(ItemStack stack) {
        return getRootTag(stack).getInt(CATALYST_COUNT_KEY);
    }

    public static String getDifficulty(ItemStack stack) {
        return getRootTag(stack).getString(DIFFICULTY_KEY);
    }

    public static int getRewardBonus(ItemStack stack) {
        return getRootTag(stack).getInt(REWARD_KEY);
    }

    public static List<String> getPreviewLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        ListTag listTag = getRootTag(stack).getList(PREVIEW_KEY, StringTag.TAG_STRING);
        for (int index = 0; index < listTag.size(); index++) {
            lines.add(listTag.getString(index));
        }
        return lines;
    }

    public static void applyForgeData(ItemStack stack, int augmentCount, int catalystCount, String difficulty, int rewardBonus, List<String> previewLines) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag rootTag = tag.getCompound(ROOT_KEY);
            rootTag.putBoolean(FORGED_KEY, true);
            rootTag.putInt(AUGMENT_COUNT_KEY, augmentCount);
            rootTag.putInt(CATALYST_COUNT_KEY, catalystCount);
            rootTag.putString(DIFFICULTY_KEY, difficulty);
            rootTag.putInt(REWARD_KEY, rewardBonus);

            ListTag previewList = new ListTag();
            for (String previewLine : previewLines) {
                previewList.add(StringTag.valueOf(previewLine));
            }
            rootTag.put(PREVIEW_KEY, previewList);
            tag.put(ROOT_KEY, rootTag);
        });
    }

    public static List<Component> buildCrystalTooltip(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        if (!isForged(stack)) {
            return lines;
        }

        lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.forged").withStyle(ChatFormatting.LIGHT_PURPLE));
        lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.augments_applied", getAugmentCount(stack)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.catalysts_applied", getCatalystCount(stack)).withStyle(ChatFormatting.GRAY));

        String difficulty = getDifficulty(stack);
        if (!difficulty.isBlank()) {
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.difficulty", difficulty).withStyle(ChatFormatting.RED));
        }

        int rewardBonus = getRewardBonus(stack);
        if (rewardBonus > 0) {
            lines.add(Component.translatable("tooltip.gatewayexpansion.crystal.reward_bonus", rewardBonus).withStyle(ChatFormatting.GOLD));
        }

        for (String previewLine : getPreviewLines(stack)) {
            lines.add(Component.literal(previewLine).withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
    }
}
