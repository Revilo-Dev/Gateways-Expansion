package com.revilo.gatesofavarice.item;

import com.revilo.gatesofavarice.item.data.LootRarity;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArcaneAppleItem extends LootMaterialItem {

    private final ResourceLocation effectId;
    private final int effectDurationTicks;
    private final int effectAmplifier;
    private final boolean foil;

    public ArcaneAppleItem(LootRarity rarity, Properties properties, ResourceLocation effectId, int effectDurationTicks, int effectAmplifier, boolean foil) {
        super(rarity, properties);
        this.effectId = effectId;
        this.effectDurationTicks = effectDurationTicks;
        this.effectAmplifier = effectAmplifier;
        this.foil = foil;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack consumed = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            Holder<net.minecraft.world.effect.MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, this.effectId)).orElse(null);
            if (effect != null) {
                livingEntity.addEffect(new MobEffectInstance(effect, this.effectDurationTicks, this.effectAmplifier), livingEntity);
            }
        }
        return consumed;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.foil || super.isFoil(stack);
    }
}
