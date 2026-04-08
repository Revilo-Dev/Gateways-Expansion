package com.revilo.gatewayexpansion.integration;

import com.revilo.gatewayexpansion.item.MagnetItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MagnetHandler {

    private MagnetHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 2 != 0) {
            return;
        }

        MagnetItem magnet = strongestMagnet(player);
        if (magnet == null) {
            return;
        }

        pullNearbyItems(player, magnet);
    }

    public static void pullNearbyItems(LivingEntity entity, MagnetItem magnet) {
        if (entity.level().isClientSide) {
            return;
        }

        double range = magnet.attractionRange();
        AABB area = entity.getBoundingBox().inflate(range);
        for (ItemEntity itemEntity : entity.level().getEntitiesOfClass(ItemEntity.class, area, item -> item.isAlive() && !item.hasPickUpDelay())) {
            pullItem(entity, itemEntity, magnet.attractionForce(), range);
        }
    }

    private static MagnetItem strongestMagnet(ServerPlayer player) {
        MagnetItem best = null;
        for (ItemStack stack : player.getInventory().items) {
            best = selectBetter(best, stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            best = selectBetter(best, stack);
        }
        return best;
    }

    private static MagnetItem selectBetter(MagnetItem current, ItemStack stack) {
        if (!(stack.getItem() instanceof MagnetItem magnet)) {
            return current;
        }
        if (current == null) {
            return magnet;
        }
        if (magnet.bonusRange() > current.bonusRange()) {
            return magnet;
        }
        if (magnet.bonusRange() == current.bonusRange() && magnet.pullSpeed() > current.pullSpeed()) {
            return magnet;
        }
        return current;
    }

    private static void pullItem(LivingEntity entity, ItemEntity itemEntity, double force, double range) {
        Vec3 target = entity.position().add(0.0D, entity.getEyeHeight() * 0.4D, 0.0D);
        Vec3 direction = target.subtract(itemEntity.position());
        double distance = direction.length();
        if (distance <= 0.001D || distance > range) {
            return;
        }

        double scaledForce = (1.0D - (distance / range)) * force;
        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(direction.normalize().scale(scaledForce)));
        itemEntity.hurtMarked = true;
    }
}
