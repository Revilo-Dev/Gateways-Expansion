package com.revilo.gatesofavarice.registry;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.effect.StabilityDrainEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, GatewayExpansion.MOD_ID);

    public static final DeferredHolder<MobEffect, StabilityDrainEffect> STABILITY_DRAIN = MOB_EFFECTS.register("stability_drain", StabilityDrainEffect::new);

    private ModMobEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
