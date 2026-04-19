package com.revilo.gatesofavarice.integration;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class GatewayFailureEvents {

    private static final String BREACH_TRIGGERED_KEY = "gatesofavarice.breach_triggered";
    private static final int BREACH_MIN_PLAYER_LEVEL = 50;

    private GatewayFailureEvents() {
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }

        List<GatewayEntity> gateways = player.level().getEntitiesOfClass(GatewayEntity.class, player.getBoundingBox().inflate(100.0D));
        for (GatewayEntity gateway : gateways) {
            if (gateway.isRemoved()) {
                continue;
            }
            tryTriggerBreach(gateway, player);
            gateway.setRemainingLives(1);
            gateway.playerDied(player);
        }
    }

    @SubscribeEvent
    public static void onGateFailed(GateEvent.Failed event) {
        GatewayEntity gateway = event.getEntity();
        if (gateway == null || gateway.level().isClientSide || gateway.isRemoved()) {
            return;
        }

        Player player = gateway.summonerOrClosest();
        if (player instanceof ServerPlayer serverPlayer) {
            tryTriggerBreach(gateway, serverPlayer);
            return;
        }

        if (gateway.level() instanceof ServerLevel serverLevel) {
            Player nearest = serverLevel.getNearestPlayer(gateway, 96.0D);
            if (nearest instanceof ServerPlayer nearestServerPlayer) {
                tryTriggerBreach(gateway, nearestServerPlayer);
            }
        }
    }

    private static void tryTriggerBreach(GatewayEntity gateway, ServerPlayer player) {
        if (gateway.getPersistentData().getBoolean(BREACH_TRIGGERED_KEY)) {
            return;
        }

        if (LevelUpIntegration.getPlayerLevel(player) < BREACH_MIN_PLAYER_LEVEL) {
            return;
        }

        if (!(gateway.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        gateway.getPersistentData().putBoolean(BREACH_TRIGGERED_KEY, true);
        RandomSource random = serverLevel.random;
        boolean spawnTank = random.nextBoolean();
        if (spawnTank) {
            spawnTankBreach(serverLevel, gateway, random);
            announceBreach(serverLevel, gateway, "Gateway Breach: A tank enemy has emerged!");
        } else {
            spawnChallengingWave(serverLevel, gateway, random);
            announceBreach(serverLevel, gateway, "Gateway Breach: An extra wave spills out!");
        }
    }

    private static void spawnChallengingWave(ServerLevel level, GatewayEntity gateway, RandomSource random) {
        int spawns = 4 + random.nextInt(4);
        for (int i = 0; i < spawns; i++) {
            EntityType<? extends Monster> type = switch (random.nextInt(5)) {
                case 0 -> EntityType.ZOMBIE;
                case 1 -> EntityType.SKELETON;
                case 2 -> EntityType.SPIDER;
                case 3 -> EntityType.VINDICATOR;
                default -> EntityType.PILLAGER;
            };

            Monster mob = type.create(level);
            if (mob == null) {
                continue;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 3.0D + random.nextDouble() * 5.0D;
            double x = gateway.getX() + Math.cos(angle) * distance;
            double z = gateway.getZ() + Math.sin(angle) * distance;
            double y = gateway.getY();
            mob.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);

            if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
                double baseHealth = mob.getAttributeValue(Attributes.MAX_HEALTH);
                mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * 1.35D);
                mob.setHealth((float) mob.getAttributeValue(Attributes.MAX_HEALTH));
            }
            if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                double baseDamage = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(4.0D, baseDamage * 1.25D));
            }

            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 0));
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 20, 0));
            mob.setAggressive(true);
            markBreachEntity(mob);
            level.addFreshEntity(mob);
        }
    }

    private static void spawnTankBreach(ServerLevel level, GatewayEntity gateway, RandomSource random) {
        Ravager ravager = EntityType.RAVAGER.create(level);
        if (ravager == null) {
            spawnFallbackTank(level, gateway, random);
            return;
        }

        placeNearGateway(ravager, gateway, random);
        if (ravager.getAttribute(Attributes.MAX_HEALTH) != null) {
            ravager.getAttribute(Attributes.MAX_HEALTH).setBaseValue(220.0D);
            ravager.setHealth(220.0F);
        }
        if (ravager.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            ravager.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20.0D);
        }
        if (ravager.getAttribute(Attributes.ARMOR) != null) {
            ravager.getAttribute(Attributes.ARMOR).setBaseValue(14.0D);
        }
        if (ravager.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
            ravager.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        }
        ravager.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 1));
        ravager.setCustomName(Component.literal("Gateway Breach").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        ravager.setCustomNameVisible(true);
        markBreachEntity(ravager);
        level.addFreshEntity(ravager);
    }

    private static void spawnFallbackTank(ServerLevel level, GatewayEntity gateway, RandomSource random) {
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            return;
        }
        placeNearGateway(zombie, gateway, random);
        if (zombie.getAttribute(Attributes.MAX_HEALTH) != null) {
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(180.0D);
            zombie.setHealth(180.0F);
        }
        if (zombie.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(18.0D);
        }
        if (zombie.getAttribute(Attributes.ARMOR) != null) {
            zombie.getAttribute(Attributes.ARMOR).setBaseValue(12.0D);
        }
        zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 0));
        zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 30, 1));
        zombie.setCustomName(Component.literal("Gateway Breach").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        zombie.setCustomNameVisible(true);
        markBreachEntity(zombie);
        level.addFreshEntity(zombie);
    }

    private static void placeNearGateway(Mob mob, GatewayEntity gateway, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = 4.0D + random.nextDouble() * 4.0D;
        double x = gateway.getX() + Math.cos(angle) * distance;
        double z = gateway.getZ() + Math.sin(angle) * distance;
        double y = gateway.getY();
        mob.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
    }

    private static void announceBreach(ServerLevel level, GatewayEntity gateway, String message) {
        Component line = Component.literal(message).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        level.players().stream()
                .filter(player -> player.distanceToSqr(gateway) <= Mth.square(96.0D))
                .forEach(player -> player.sendSystemMessage(line));
    }

    private static void markBreachEntity(Mob mob) {
        mob.getPersistentData().putBoolean("gatesofavarice.gateway_breach_spawn", true);
        if (mob instanceof AbstractSkeleton skeleton) {
            skeleton.setCanPickUpLoot(false);
        } else if (mob instanceof Spider spider) {
            spider.setCanPickUpLoot(false);
        }
    }
}
