package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.gateway.builder.GatewayForgeService;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class GatewayThornsHandler {

    private GatewayThornsHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || event.getSource().is(DamageTypes.THORNS)) {
            return;
        }

        GatewayEntity gatewayEntity = GatewayEntity.getOwner(target);
        if (gatewayEntity == null) {
            return;
        }

        double thornsDamage = GatewayForgeService.getGatewayThornsDamage(gatewayEntity.getGateway());
        if (thornsDamage <= 0.0D || !(event.getSource().getEntity() instanceof LivingEntity attacker) || !attacker.isAlive()) {
            return;
        }

        attacker.hurt(target.damageSources().thorns(target), (float) thornsDamage);
    }
}
