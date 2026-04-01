package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.menu.ShopkeeperMenu;
import com.revilo.gatewayexpansion.registry.ModItems;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import dev.shadowsoffire.gateways.GatewayObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class ShopkeeperManager {

    private static final String SHOPKEEPER_KEY = "gatewayexpansion.shopkeeper";
    private static final String TEMP_TRADE_KEY = "gatewayexpansion.temp_trades";
    private static final String SHOP_GATEWAY_ANIMATION_KEY = "gatewayexpansion.shop_gateway_animation";
    private static final String SHOP_GATEWAY_TRADER_ID = "gatewayexpansion.shop_gateway_trader";
    private static final String SHOP_GATEWAY_TRADER_SPAWNED = "gatewayexpansion.shop_gateway_trader_spawned";
    private static final int SHOPKEEPER_LIFETIME_TICKS = 20 * 60;
    private static final int SHOP_GATEWAY_ANIMATION_TICKS = 50;
    private static final int SHOP_GATEWAY_EXPIRE_TICKS = 20 * 60 * 5;
    private static final double COIN_ATTRACTION_RANGE = 7.5D;
    private static final double COIN_ATTRACTION_FORCE = 0.022D;

    private ShopkeeperManager() {
    }

    @SubscribeEvent
    public static void onGateCompleted(GateEvent.Completed event) {
        GatewayEntity gate = event.getEntity();
        if (!(gate.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int completionBurst = 14 + serverLevel.random.nextInt(9) + (gate.getWave() * 3);
        spawnCoins(gate, completionBurst);
        Player summoner = gate.summonerOrClosest();
        spawnShopkeeper(serverLevel, gate.getX(), gate.getY() + 0.5D, gate.getZ(), summoner);
    }

    @SubscribeEvent
    public static void onWaveCompleted(GateEvent.WaveEnd event) {
        GatewayEntity gate = event.getEntity();
        if (gate.level() instanceof ServerLevel serverLevel) {
            int waveBurst = 8 + serverLevel.random.nextInt(5) + Math.max(0, gate.getWave());
            spawnCoins(gate, waveBurst);
        }
    }

    @SubscribeEvent
    public static void onCoinPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();
        if (!stack.is(ModItems.MYTHIC_COIN.get())) {
            return;
        }

        event.setCanPickup(TriState.FALSE);
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        MythicCoinWallet.add(player, stack.getCount());
        player.take(itemEntity, stack.getCount());
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.25F, 1.1F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 0.6D, player.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.0D);
        }
        itemEntity.discard();
    }

    @SubscribeEvent
    public static void onCoinTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof GatewayEntity gatewayEntity && isGatewayAnimation(gatewayEntity)) {
            if (gatewayEntity.level() instanceof ServerLevel serverLevel) {
                if (!gatewayEntity.getPersistentData().getBoolean(SHOP_GATEWAY_TRADER_SPAWNED) && gatewayEntity.tickCount >= SHOP_GATEWAY_ANIMATION_TICKS) {
                    Player summoner = gatewayEntity.summonerOrClosest();
                    WanderingTrader trader = spawnShopkeeper(serverLevel, gatewayEntity.getX(), gatewayEntity.getY() + 0.5D, gatewayEntity.getZ(), summoner, SHOP_GATEWAY_EXPIRE_TICKS);
                    if (trader != null) {
                        gatewayEntity.getPersistentData().putBoolean(SHOP_GATEWAY_TRADER_SPAWNED, true);
                        gatewayEntity.getPersistentData().putUUID(SHOP_GATEWAY_TRADER_ID, trader.getUUID());
                    }
                }

                if (gatewayEntity.tickCount >= SHOP_GATEWAY_EXPIRE_TICKS) {
                    if (gatewayEntity.getPersistentData().hasUUID(SHOP_GATEWAY_TRADER_ID)) {
                        Entity trader = serverLevel.getEntity(gatewayEntity.getPersistentData().getUUID(SHOP_GATEWAY_TRADER_ID));
                        if (trader != null) {
                            trader.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                    gatewayEntity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            return;
        }

        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();
        if (!stack.is(ModItems.MYTHIC_COIN.get())) {
            return;
        }

        Vec3 motion = itemEntity.getDeltaMovement();
        itemEntity.setDeltaMovement(motion.x, motion.y + 0.02D, motion.z);

        Player target = itemEntity.level().getNearestPlayer(itemEntity, COIN_ATTRACTION_RANGE);
        if (target != null) {
            Vec3 direction = new Vec3(
                    target.getX() - itemEntity.getX(),
                    target.getY() + target.getEyeHeight() * 0.45D - itemEntity.getY(),
                    target.getZ() - itemEntity.getZ());
            double distance = direction.length();
            if (distance > 0.001D) {
                double pull = (1.0D - (distance / COIN_ATTRACTION_RANGE)) * COIN_ATTRACTION_FORCE;
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(direction.normalize().scale(pull)));
            }
        }

        if (itemEntity.level() instanceof ServerLevel serverLevel && itemEntity.tickCount % 4 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    itemEntity.getX(),
                    itemEntity.getY() + 0.08D,
                    itemEntity.getZ(),
                    3,
                    0.12D,
                    0.04D,
                    0.12D,
                    0.01D);
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!(target instanceof WanderingTrader trader) || !isShopkeeper(trader) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        MenuProvider provider = new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ShopkeeperMenu(containerId, inventory, trader.getId()),
                Component.translatable("entity.gatewayexpansion.shopkeeper"));
        player.openMenu(provider, buffer -> buffer.writeInt(trader.getId()));
    }

    public static boolean spawnShopkeeper(Level level, double x, double y, double z, Player summoner) {
        return spawnShopkeeper(level, x, y, z, summoner, SHOPKEEPER_LIFETIME_TICKS) != null;
    }

    public static WanderingTrader spawnShopkeeper(Level level, double x, double y, double z, Player summoner, int despawnDelay) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        WanderingTrader trader = EntityType.WANDERING_TRADER.create(serverLevel);
        if (trader == null) {
            return null;
        }

        trader.moveTo(x, y, z, summoner == null ? 0.0F : summoner.getYRot(), 0.0F);
        trader.setNoAi(true);
        trader.setCustomName(Component.translatable("entity.gatewayexpansion.shopkeeper").withStyle(ChatFormatting.GOLD));
        trader.setCustomNameVisible(true);
        trader.setDespawnDelay(despawnDelay);
        trader.getPersistentData().putBoolean(SHOPKEEPER_KEY, true);
        rollTempTrades(trader, serverLevel.random);
        if (trader.getAttribute(Attributes.MAX_HEALTH) != null) {
            trader.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1.0D);
        }
        trader.setHealth(1.0F);
        return serverLevel.addFreshEntity(trader) ? trader : null;
    }

    public static boolean isShopkeeper(WanderingTrader trader) {
        return trader.getPersistentData().getBoolean(SHOPKEEPER_KEY);
    }

    public static void markGatewayAnimation(GatewayEntity entity) {
        entity.getPersistentData().putBoolean(SHOP_GATEWAY_ANIMATION_KEY, true);
    }

    public static boolean isGatewayAnimation(GatewayEntity entity) {
        return entity.getPersistentData().getBoolean(SHOP_GATEWAY_ANIMATION_KEY);
    }

    public static java.util.List<ShopOfferDefinition> getOffers(WanderingTrader trader) {
        java.util.List<ShopOfferDefinition> offers = new java.util.ArrayList<>(ShopOfferDefinition.CORE_OFFERS);
        CompoundTag tag = trader.getPersistentData();
        if (tag.contains(TEMP_TRADE_KEY)) {
            int[] tempIndexes = tag.getIntArray(TEMP_TRADE_KEY);
            for (int tempIndex : tempIndexes) {
                if (tempIndex >= 0 && tempIndex < ShopOfferDefinition.TEMP_OFFERS.size()) {
                    offers.add(ShopOfferDefinition.TEMP_OFFERS.get(tempIndex));
                }
            }
        }
        return offers;
    }

    private static void spawnCoins(GatewayEntity gate, int amount) {
        RandomSource random = gate.level().random;
        int remaining = Math.max(0, amount);
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 3 + random.nextInt(5));
            remaining -= stackSize;
            ItemEntity itemEntity = new ItemEntity(gate.level(), gate.getX(), gate.getY() + 1.0D, gate.getZ(), new ItemStack(ModItems.MYTHIC_COIN.get(), stackSize));
            itemEntity.setDeltaMovement(
                    random.nextDouble() * 0.38D - 0.19D,
                    0.28D + random.nextDouble() * 0.20D,
                    random.nextDouble() * 0.38D - 0.19D);
            itemEntity.setNoPickUpDelay();
            gate.level().addFreshEntity(itemEntity);
        }
    }

    private static void rollTempTrades(WanderingTrader trader, RandomSource random) {
        java.util.List<Integer> pool = new java.util.ArrayList<>();
        for (int index = 0; index < ShopOfferDefinition.TEMP_OFFERS.size(); index++) {
            pool.add(index);
        }
        java.util.Collections.shuffle(pool, new java.util.Random(random.nextLong()));
        trader.getPersistentData().putIntArray(TEMP_TRADE_KEY, new int[] {pool.get(0), pool.get(1)});
    }
}
