package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.catalyst.CatalystArchetype;
import com.revilo.gatesofavarice.catalyst.CatalystDefinition;
import com.revilo.gatesofavarice.catalyst.CatalystStackData;
import com.revilo.gatesofavarice.gateway.roll.ForgeEffect;
import com.revilo.gatesofavarice.gateway.roll.ForgeEffectType;
import com.revilo.gatesofavarice.integration.LevelUpIntegration;
import com.revilo.gatesofavarice.shop.GatewaySellValues;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CatalystItem extends Item {

    private static final Pattern EPIC_REWARD_PATTERN = Pattern.compile("\\+(\\d+) epic rewards?");
    private final CatalystArchetype archetype;

    public CatalystItem(CatalystArchetype archetype, Properties properties) {
        super(properties);
        this.archetype = archetype;
    }

    public CatalystArchetype archetype() {
        return this.archetype;
    }

    public CatalystDefinition definition(ItemStack stack) {
        return CatalystStackData.getDefinition(stack, this.archetype);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CatalystDefinition definition = this.definition(stack);
        GatewaySellValues.appendSellValueTooltip(stack, tooltipComponents);
        if (definition == null) {
            return;
        }
        appendPositiveEffectTooltip(tooltipComponents, definition.positiveEffect());
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.catalyst.negative", definition.negativeEffect().description()).withStyle(ChatFormatting.RED));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            int playerLevel = entity instanceof Player player
                    ? Math.max(LevelUpIntegration.getPlayerLevel(player), player.experienceLevel)
                    : -1;
            CatalystStackData.ensureDefinition(stack, this.archetype, serverLevel.random, playerLevel);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private static void appendPositiveEffectTooltip(List<Component> tooltipComponents, ForgeEffect effect) {
        if (effect.type() != ForgeEffectType.EXTRA_RARE_REWARD_ROLLS) {
            tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.catalyst.positive", effect.description()).withStyle(ChatFormatting.GREEN));
            return;
        }

        int rareRewards = Math.max(1, (int) Math.round(effect.value()));
        int epicRewards = (int) Math.round(effect.secondaryValue());
        if (epicRewards <= 0) {
            epicRewards = extractRewardCount(effect.description(), EPIC_REWARD_PATTERN, 1);
        }

        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.catalyst.positive", "+" + rareRewards + " rare rewards").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.catalyst.positive", "+" + epicRewards + " epic rewards").withStyle(ChatFormatting.GREEN));
        if (effect.description().contains("+1 legendary reward")) {
            tooltipComponents.add(Component.translatable("tooltip.gatesofavarice.catalyst.positive", "+1 legendary reward").withStyle(ChatFormatting.GREEN));
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
