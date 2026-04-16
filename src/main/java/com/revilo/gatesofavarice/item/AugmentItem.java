package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.augment.AugmentDefinition;
import com.revilo.gatesofavarice.augment.AugmentStackData;
import com.revilo.gatesofavarice.gateway.roll.ForgeEffect;
import com.revilo.gatesofavarice.gateway.roll.ForgeEffectType;
import com.revilo.gatesofavarice.integration.LevelUpIntegration;
import com.revilo.gatesofavarice.item.data.AugmentDifficultyTier;
import com.revilo.gatesofavarice.shop.GatewaySellValues;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AugmentItem extends Item {

    private static final Pattern EPIC_REWARD_PATTERN = Pattern.compile("\\+(\\d+) epic rewards?");
    private final AugmentDifficultyTier difficultyTier;

    public AugmentItem(AugmentDifficultyTier difficultyTier, Properties properties) {
        super(properties);
        this.difficultyTier = difficultyTier;
    }

    public AugmentDifficultyTier difficultyTier() {
        return this.difficultyTier;
    }

    public AugmentDefinition definition(ItemStack stack) {
        return AugmentStackData.getDefinition(stack, this.difficultyTier);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        AugmentDefinition definition = this.definition(stack);
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.augment.type").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.augment.difficulty", this.difficultyTier.displayName()).withStyle(ChatFormatting.GRAY));
        if (definition == null) {
            return;
        }
        for (var effect : definition.modifierEffects()) {
            tooltipComponents.add(Component.literal(effect.description()).withStyle(isNegativeTooltipEffect(effect) ? ChatFormatting.RED : ChatFormatting.GREEN));
        }
        for (ForgeEffect effect : definition.rewardEffects()) {
            appendRewardEffectTooltip(tooltipComponents, effect);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            int playerLevel = entity instanceof Player player
                    ? Math.max(LevelUpIntegration.getPlayerLevel(player), player.experienceLevel)
                    : -1;
            AugmentStackData.ensureDefinition(stack, this.difficultyTier, serverLevel.random, playerLevel);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private static boolean isNegativeTooltipEffect(ForgeEffect effect) {
        return effect.type() != ForgeEffectType.MOB_EFFECT
                || effect.referenceId() == null
                || !ResourceLocation.withDefaultNamespace("glowing").equals(effect.referenceId());
    }

    private static void appendRewardEffectTooltip(List<Component> tooltipComponents, ForgeEffect effect) {
        if (effect.type() != ForgeEffectType.EXTRA_RARE_REWARD_ROLLS) {
            tooltipComponents.add(Component.literal(effect.description()).withStyle(ChatFormatting.GOLD));
            return;
        }

        int rareRewards = Math.max(1, (int) Math.round(effect.value()));
        int epicRewards = (int) Math.round(effect.secondaryValue());
        if (epicRewards <= 0) {
            epicRewards = extractRewardCount(effect.description(), EPIC_REWARD_PATTERN, 1);
        }

        tooltipComponents.add(Component.literal("+" + rareRewards + " rare rewards").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("+" + epicRewards + " epic rewards").withStyle(ChatFormatting.GOLD));
        if (effect.description().contains("+1 legendary reward")) {
            tooltipComponents.add(Component.literal("+1 legendary reward").withStyle(ChatFormatting.GOLD));
        }
    }

    private static int extractRewardCount(String description, Pattern pattern, int fallback) {
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return fallback;
    }
}
