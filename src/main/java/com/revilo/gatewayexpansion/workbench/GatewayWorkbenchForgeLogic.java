package com.revilo.gatewayexpansion.workbench;

import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.data.AugmentDefinition;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class GatewayWorkbenchForgeLogic {

    private GatewayWorkbenchForgeLogic() {
    }

    public static boolean canForge(Container container) {
        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystal.getItem() instanceof CrystalItem)) {
            return false;
        }
        return !GatewayWorkbenchSlots.collectAugments(container).isEmpty() || !GatewayWorkbenchSlots.collectCatalysts(container).isEmpty();
    }

    public static boolean forge(Container container) {
        if (!canForge(container)) {
            return false;
        }

        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        PreviewData previewData = buildPreview(container);
        CrystalForgeData.applyForgeData(
                crystal,
                previewData.augmentCount(),
                previewData.catalystCount(),
                previewData.difficultyName(),
                previewData.rewardBonusPercent(),
                previewData.previewLines()
        );

        consumeIngredients(container, GatewayWorkbenchSlots.CATALYST_SLOT_START, GatewayWorkbenchSlots.CATALYST_SLOT_COUNT);
        consumeIngredients(container, GatewayWorkbenchSlots.AUGMENT_SLOT_START, GatewayWorkbenchSlots.AUGMENT_SLOT_COUNT);
        container.setChanged();
        return true;
    }

    public static PreviewData buildPreview(Container container) {
        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        int crystalTier = crystal.getItem() instanceof CrystalItem crystalItem ? crystalItem.crystalTier().tier() : 0;
        List<ItemStack> augmentStacks = GatewayWorkbenchSlots.collectAugments(container);
        List<ItemStack> catalystStacks = GatewayWorkbenchSlots.collectCatalysts(container);

        AugmentDifficultyTier highestTier = AugmentDifficultyTier.EASY;
        int rewardBonus = catalystStacks.size() * 5;
        for (ItemStack augmentStack : augmentStacks) {
            if (augmentStack.getItem() instanceof AugmentItem augmentItem) {
                AugmentDefinition definition = augmentItem.definition();
                rewardBonus += definition.rewardBonusPercent();
                if (definition.difficultyTier().ordinal() > highestTier.ordinal()) {
                    highestTier = definition.difficultyTier();
                }
            }
        }

        int waves = Math.max(1, 3 + augmentStacks.size() / 2 + catalystStacks.size() / 3);
        String timePressure = catalystStacks.isEmpty() ? "Stable" : "+" + catalystStacks.size() + " pressure";

        List<String> previewLines = new ArrayList<>();
        previewLines.add("Crystal Tier: " + crystalTier);
        previewLines.add("Augments: " + augmentStacks.size());
        previewLines.add("Catalysts: " + catalystStacks.size());
        previewLines.add("Difficulty: " + highestTier.name());
        previewLines.add("Reward Bonus: +" + rewardBonus + "%");
        previewLines.add("Waves: " + waves);
        previewLines.add("Time Pressure: " + timePressure);

        return new PreviewData(
                crystalTier,
                augmentStacks.size(),
                catalystStacks.size(),
                highestTier.name(),
                rewardBonus,
                waves,
                timePressure,
                previewLines
        );
    }

    private static void consumeIngredients(Container container, int start, int count) {
        for (int slot = 0; slot < count; slot++) {
            int index = start + slot;
            ItemStack stack = container.getItem(index);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    container.setItem(index, ItemStack.EMPTY);
                } else {
                    container.setItem(index, stack);
                }
            }
        }
    }

    public record PreviewData(
            int crystalTier,
            int augmentCount,
            int catalystCount,
            String difficultyName,
            int rewardBonusPercent,
            int waves,
            String timePressure,
            List<String> previewLines
    ) {
        public PreviewData {
            previewLines = List.copyOf(previewLines);
        }
    }
}
