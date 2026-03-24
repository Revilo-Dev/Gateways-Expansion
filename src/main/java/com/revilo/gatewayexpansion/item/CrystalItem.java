package com.revilo.gatewayexpansion.item;

import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CrystalItem extends Item {

    private final CrystalTier crystalTier;

    public CrystalItem(CrystalTier crystalTier, Properties properties) {
        super(properties);
        this.crystalTier = crystalTier;
    }

    public CrystalTier crystalTier() {
        return this.crystalTier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.crystal.description").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.gatewayexpansion.crystal.tier", this.crystalTier.tier()).withStyle(ChatFormatting.AQUA));
        tooltipComponents.addAll(CrystalForgeData.buildCrystalTooltip(stack));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level instanceof ServerLevel serverLevel) {
            CrystalForgeData.ensureProfile(stack, this.crystalTier.minLevel(), this.crystalTier.maxLevel(), serverLevel.random);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    public record CrystalTier(int tier, int minLevel, int maxLevel) {
    }
}
