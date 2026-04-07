package com.revilo.gatewayexpansion.registry;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModToolTiers {

    public static final Tier MANA_STEEL = new DerivedTier(Tiers.IRON, 0.95F, () -> Ingredient.of(ModItems.MANA_STEEL_INGOT.get()));
    public static final Tier ELIXRITE = new DerivedTier(Tiers.DIAMOND, 0.95F, () -> Ingredient.of(ModItems.ELIXRITE_INGOT.get()));
    public static final Tier ASTRITE = new DerivedTier(Tiers.DIAMOND, 1.0F, () -> Ingredient.of(ModItems.ASTRITE_INGOT.get()));
    public static final Tier LUNARIUM = new DerivedTier(Tiers.NETHERITE, 1.0F, () -> Ingredient.of(ModItems.LUNARIUM_INGOT.get()));
    public static final Tier IGNITE = new DerivedTier(Tiers.NETHERITE, 1.10F, 1.05F, 0.5F, 2, () -> Ingredient.of(ModItems.IGNITE_INGOT.get()));
    public static final Tier IRIDIUM = new DerivedTier(Tiers.NETHERITE, 1.20F, 1.10F, 1.0F, 4, () -> Ingredient.of(ModItems.IRIDIUM_INGOT.get()));
    public static final Tier MYTHRIL = new DerivedTier(Tiers.NETHERITE, 1.30F, 1.15F, 1.5F, 6, () -> Ingredient.of(ModItems.MYTHRIL_INGOT.get()));
    public static final Tier ARCANIUM = new DerivedTier(Tiers.NETHERITE, 1.45F, 1.22F, 2.0F, 8, () -> Ingredient.of(ModItems.ARCANIUM_INGOT.get()));
    public static final Tier PRISMATIC_STEEL = new DerivedTier(Tiers.NETHERITE, 1.65F, 1.30F, 3.0F, 12, () -> Ingredient.of(ModItems.PRISMATIC_STEEL_INGOT.get()));

    private ModToolTiers() {
    }

    private record DerivedTier(
            Tier base,
            float useMultiplier,
            float speedMultiplier,
            float attackBonus,
            int enchantmentBonus,
            java.util.function.Supplier<Ingredient> repairIngredient) implements Tier {

        private DerivedTier(Tier base, float speedMultiplier, java.util.function.Supplier<Ingredient> repairIngredient) {
            this(base, 1.0F, speedMultiplier, 0.0F, 0, repairIngredient);
        }

        @Override
        public int getUses() {
            return Math.max(this.base.getUses(), Math.round(this.base.getUses() * this.useMultiplier));
        }

        @Override
        public float getSpeed() {
            return this.base.getSpeed() * this.speedMultiplier;
        }

        @Override
        public float getAttackDamageBonus() {
            return this.base.getAttackDamageBonus() + this.attackBonus;
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return this.base.getIncorrectBlocksForDrops();
        }

        @Override
        public int getEnchantmentValue() {
            return this.base.getEnchantmentValue() + this.enchantmentBonus;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredient.get();
        }
    }
}
