package com.revilo.gatesofavarice.integration;

import com.revilo.gatesofavarice.item.MagnetItem;
import com.revilo.gatesofavarice.registry.ModAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isMagnetEnabled(player)) {
            return;
        }

        MagnetItem magnet = strongestMagnet(player);
        if (magnet == null) {
            return;
        }

        pullNearbyItems(player, magnet);
    }

    public static void pullNearbyItems(LivingEntity entity, MagnetItem magnet) {
        if (entity.level().isClientSide || !isMagnetEnabled(entity)) {
            return;
        }

        double range = magnet.attractionRange();
        AABB area = entity.getBoundingBox().inflate(range);
        for (ItemEntity itemEntity : entity.level().getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive)) {
            pullItem(entity, itemEntity, magnet.attractionForce(), range);
        }
    }

    private static MagnetItem strongestMagnet(ServerPlayer player) {
        MagnetItem best = null;
        best = selectBetter(best, player.getMainHandItem());
        best = selectBetter(best, player.getOffhandItem());
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

        Vec3 pullDirection = direction.scale(1.0D / distance);
        double normalizedDistance = Math.min(distance / range, 1.0D);
        double pullStrength = force * (0.8D + ((1.0D - normalizedDistance) * 1.6D));
        double maxSpeed = 0.18D + (force * 1.8D);
        Vec3 desiredVelocity = pullDirection.scale(Math.min(pullStrength, maxSpeed));
        Vec3 blendedVelocity = itemEntity.getDeltaMovement().scale(0.45D).add(desiredVelocity.scale(0.55D));
        if (distance <= 0.85D) {
            blendedVelocity = blendedVelocity.scale(0.6D);
        }
        if (blendedVelocity.lengthSqr() > maxSpeed * maxSpeed) {
            blendedVelocity = blendedVelocity.normalize().scale(maxSpeed);
        }
        itemEntity.setDeltaMovement(blendedVelocity);
        itemEntity.hasImpulse = true;
        itemEntity.hurtMarked = true;
    }

    private static boolean isMagnetEnabled(LivingEntity entity) {
        return !(entity instanceof Player player) || player.getData(ModAttachments.MAGNET_ENABLED);
    }
}
