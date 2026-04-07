package com.revilo.gatewayexpansion.gateway;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.entity.NormalGatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class GatewayPartyScaling {

    private static final String EXTRA_PLAYERS_KEY = "gatewayexpansion.party_extra_players";
    private static final String BONUS_WAVES_KEY = "gatewayexpansion.party_bonus_waves";
    private static final String APPLIED_EXTRA_PLAYERS_KEY = "gatewayexpansion.party_applied_extra_players";
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "party_health");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "party_damage");
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "party_speed");
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatewayexpansion", "party_armor");

    private GatewayPartyScaling() {
    }

    @SubscribeEvent
    public static void onGatewayOpened(GateEvent.Opened event) {
        GatewayEntity gate = event.getEntity();
        int extraPlayers = countExtraPlayers(gate);
        CompoundTag data = gate.getPersistentData();
        data.putInt(EXTRA_PLAYERS_KEY, extraPlayers);
        data.putInt(BONUS_WAVES_KEY, extraPlayers);
    }

    @SubscribeEvent
    public static void onGatewayTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof GatewayEntity gate) || gate.level().isClientSide || !(gate.level() instanceof ServerLevel)) {
            return;
        }

        int extraPlayers = countExtraPlayers(gate);
        CompoundTag data = gate.getPersistentData();
        int previousExtraPlayers = Math.max(0, data.getInt(EXTRA_PLAYERS_KEY));
        if (extraPlayers > previousExtraPlayers) {
            data.putInt(BONUS_WAVES_KEY, data.getInt(BONUS_WAVES_KEY) + (extraPlayers - previousExtraPlayers));
            scaleActiveWaveEntities(gate, extraPlayers);
        }
        data.putInt(EXTRA_PLAYERS_KEY, Math.max(previousExtraPlayers, extraPlayers));

        if (gate instanceof NormalGatewayEntity normalGate
                && data.getInt(BONUS_WAVES_KEY) > 0
                && !normalGate.isWaveActive()
                && normalGate.getActiveEnemies() == 0
                && normalGate.getWave() >= normalGate.getGateway().getNumWaves()) {
            normalGate.getEntityData().set(GatewayEntity.WAVE, normalGate.getGateway().getNumWaves() - 1);
            data.putInt(BONUS_WAVES_KEY, data.getInt(BONUS_WAVES_KEY) - 1);
        }
    }

    @SubscribeEvent
    public static void onWaveEntitySpawned(GateEvent.WaveEntitySpawned event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }

        int extraPlayers = Math.max(0, event.getEntity().getPersistentData().getInt(EXTRA_PLAYERS_KEY));
        scaleEntity(event.getWaveEntity(), extraPlayers);
    }

    public static int getExtraPlayers(GatewayEntity gate) {
        return gate == null ? 0 : Math.max(0, gate.getPersistentData().getInt(EXTRA_PLAYERS_KEY));
    }

    public static double getDifficultyMultiplier(GatewayEntity gate) {
        return 1.0D + (getExtraPlayers(gate) * 0.5D);
    }

    private static int countExtraPlayers(GatewayEntity gate) {
        double range = gate.getGateway().rules().leashRange() + 8.0D;
        AABB area = gate.getBoundingBox().inflate(range);
        List<Player> players = gate.level().getEntitiesOfClass(Player.class, area, player -> player.isAlive() && !player.isSpectator());
        return Math.max(0, players.size() - 1);
    }

    private static void scaleActiveWaveEntities(GatewayEntity gate, int extraPlayers) {
        double range = gate.getGateway().rules().leashRange() + 16.0D;
        AABB area = gate.getBoundingBox().inflate(range);
        List<LivingEntity> entities = gate.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> GatewayEntity.getOwner(entity) == gate);
        for (LivingEntity entity : entities) {
            scaleEntity(entity, extraPlayers);
        }
    }

    private static void scaleEntity(LivingEntity entity, int extraPlayers) {
        CompoundTag data = entity.getPersistentData();
        int applied = Math.max(0, data.getInt(APPLIED_EXTRA_PLAYERS_KEY));
        if (applied == extraPlayers) {
            return;
        }

        double oldMaxHealth = entity.getMaxHealth();
        applyModifier(entity.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER_ID, extraPlayers <= 0 ? null : new AttributeModifier(HEALTH_MODIFIER_ID, extraPlayers * 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        applyModifier(entity.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER_ID, extraPlayers <= 0 ? null : new AttributeModifier(DAMAGE_MODIFIER_ID, extraPlayers * 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        applyModifier(entity.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_MODIFIER_ID, extraPlayers <= 0 ? null : new AttributeModifier(SPEED_MODIFIER_ID, extraPlayers * 0.08D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        applyModifier(entity.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER_ID, extraPlayers <= 0 ? null : new AttributeModifier(ARMOR_MODIFIER_ID, extraPlayers * 0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        double newMaxHealth = entity.getMaxHealth();
        if (oldMaxHealth > 0.0D && newMaxHealth > 0.0D) {
            float ratio = (float) (entity.getHealth() / oldMaxHealth);
            entity.setHealth(Math.min(entity.getMaxHealth(), Math.max(1.0F, (float) (newMaxHealth * ratio))));
        }
        data.putInt(APPLIED_EXTRA_PLAYERS_KEY, extraPlayers);
    }

    private static void applyModifier(AttributeInstance attribute, ResourceLocation modifierId, AttributeModifier modifier) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(modifierId);
        if (modifier != null) {
            attribute.addPermanentModifier(modifier);
        }
    }
}
