package com.revilo.gatesofavarice.integration;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class ArsenalWeaponTraitHandler {

    private static final String ARSENAL_NAMESPACE = "arsenal";
    private static final int SLOWNESS_DURATION_TICKS = 60;
    private static final int FIRE_SECONDS = 4;
    private static final Set<String> SUPPORTED_SUFFIXES = Set.of(
            "broadsword",
            "dagger",
            "gaundao",
            "glaive",
            "hammer",
            "longsword",
            "machete");
    private static final Set<String> SLOWNESS_TIERS = Set.of(
            "lunarium",
            "ignite",
            "iridium",
            "mythril",
            "arcanium",
            "prismatic_steel");
    private static final Set<String> FIRE_TIERS = Set.of(
            "ignite",
            "iridium",
            "mythril",
            "arcanium",
            "prismatic_steel");

    private ArsenalWeaponTraitHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || event.getAmount() <= 0.0F) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || !attacker.isAlive()) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        ResourceLocation itemId = weapon.getItemHolder().unwrapKey().map(key -> key.location()).orElse(null);
        String tier = upgradedArsenalTier(itemId);
        if (tier == null) {
            return;
        }

        if (SLOWNESS_TIERS.contains(tier)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION_TICKS, 0), attacker);
        }
        if (FIRE_TIERS.contains(tier) && !target.fireImmune()) {
            target.igniteForSeconds(FIRE_SECONDS);
        }
    }

    private static String upgradedArsenalTier(ResourceLocation itemId) {
        if (itemId == null || !ARSENAL_NAMESPACE.equals(itemId.getNamespace())) {
            return null;
        }

        String path = itemId.getPath();
        for (String suffix : SUPPORTED_SUFFIXES) {
            String ending = "_" + suffix;
            if (!path.endsWith(ending)) {
                continue;
            }
            return path.substring(0, path.length() - ending.length());
        }
        return null;
    }
}
