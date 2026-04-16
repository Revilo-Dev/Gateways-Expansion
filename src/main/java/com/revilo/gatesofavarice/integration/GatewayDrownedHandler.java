package com.revilo.gatesofavarice.integration;

import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class GatewayDrownedHandler {

    private static final String GATEWAY_DROWNED_KEY = "gatesofavarice.gateway_drowned";
    private static final String AI_PATCHED_KEY = "gatesofavarice.gateway_drowned_ai_patched";

    private GatewayDrownedHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Drowned drowned)) {
            return;
        }

        var data = drowned.getPersistentData();
        if (!data.getBoolean(GATEWAY_DROWNED_KEY) || data.getBoolean(AI_PATCHED_KEY)) {
            return;
        }

        drowned.goalSelector.addGoal(1, new ZombieAttackGoal(drowned, 1.0D, false));
        drowned.targetSelector.addGoal(0, new HurtByTargetGoal(drowned));
        drowned.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(drowned, Player.class, true));
        data.putBoolean(AI_PATCHED_KEY, true);
    }
}
