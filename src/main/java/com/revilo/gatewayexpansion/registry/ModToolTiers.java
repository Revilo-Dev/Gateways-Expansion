package com.revilo.gatewayexpansion.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModToolTiers {

    public static final Tier MANA_STEEL = new DerivedTier(Tiers.IRON, 0.95F, () -> Ingredient.of(ModItems.MANA_STEEL_INGOT.get()));
    public static final Tier ELIXRITE = new DerivedTier(Tiers.DIAMOND, 0.95F, () -> Ingredient.of(ModItems.ELIXRITE_INGOT.get()));

    private ModToolTiers() {
    }

    private record DerivedTier(Tier base, float speedMultiplier, java.util.function.Supplier<Ingredient> repairIngredient) implements Tier {
        @Override
        public int getUses() {
            return this.base.getUses();
        }

        @Override
        public float getSpeed() {
            return this.base.getSpeed() * this.speedMultiplier;
        }

        @Override
        public float getAttackDamageBonus() {
            return this.base.getAttackDamageBonus();
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return this.base.getIncorrectBlocksForDrops();
        }

        @Override
        public int getEnchantmentValue() {
            return this.base.getEnchantmentValue();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredient.get();
        }
    }
}
