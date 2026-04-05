package com.revilo.gatewayexpansion.shop;

import com.revilo.gatewayexpansion.currency.MythicCoinWallet;
import com.revilo.gatewayexpansion.entity.GatekeeperEntity;
import com.revilo.gatewayexpansion.gateway.builder.GatewayForgeService;
import com.revilo.gatewayexpansion.integration.LevelUpIntegration;
import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.item.LootMaterialItem;
import com.revilo.gatewayexpansion.item.data.LootRarity;
import com.revilo.gatewayexpansion.menu.ShopkeeperMenu;
import com.revilo.gatewayexpansion.registry.ModEntities;
import com.revilo.gatewayexpansion.registry.ModItems;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.event.GateEvent;
import dev.shadowsoffire.gateways.GatewayObjects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ShopkeeperManager {

    private static final String SHOPKEEPER_KEY = "gatewayexpansion.shopkeeper";
    private static final String TEMP_TRADE_KEY = "gatewayexpansion.temp_trades";
    private static final String STOCK_KEY = "gatewayexpansion.stock";
    private static final String REROLL_COUNT_KEY = "gatewayexpansion.reroll_count";
    private static final String SHOP_GATEWAY_ANIMATION_KEY = "gatewayexpansion.shop_gateway_animation";
    private static final String SHOP_GATEWAY_TRADER_ID = "gatewayexpansion.shop_gateway_trader";
    private static final String SHOP_GATEWAY_TRADER_SPAWNED = "gatewayexpansion.shop_gateway_trader_spawned";
    private static final String SHOP_GATEWAY_ENTITY_ID = "gatewayexpansion.shop_gateway_entity";
    private static final int MAX_REROLLS = 3;
    private static final int BASE_REROLL_COST = 100;
    private static final int SHOP_GATEWAY_ANIMATION_TICKS = 50;
    private static final double COIN_ATTRACTION_RANGE = 7.5D;
    private static final double COIN_ATTRACTION_FORCE = 0.022D;
    private static final java.util.List<DeferredHolder<net.minecraft.world.item.Item, LootMaterialItem>> GATEWAY_DROP_ITEMS = java.util.List.of(
            ModItems.GRIMSTONE,
            ModItems.MYSTIC_ESSENCE,
            ModItems.SCRAP_METAL,
            ModItems.MANA_GEMS,
            ModItems.MANA_STEEL_SCRAP,
            ModItems.ARCANE_ESSENCE,
            ModItems.MANASTONES,
            ModItems.ELIXRITE_SCRAP,
            ModItems.SOLAR_CRYSTAL,
            ModItems.PRISMATIC_DIAMOND,
            ModItems.DARK_ESSENCE,
            ModItems.PRISMATIC_CORE
    );
    private static final LootMaterialItem[] GATEWAY_DROP_POOL = GATEWAY_DROP_ITEMS.stream().map(DeferredHolder::get).toArray(LootMaterialItem[]::new);
    private static final int GATEWAY_DROP_TOTAL_WEIGHT = java.util.Arrays.stream(GATEWAY_DROP_POOL).mapToInt(item -> item.rarity().weight()).sum();

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
        spawnGatewayLoot(gate, 2 + serverLevel.random.nextInt(2));
        Player summoner = gate.summonerOrClosest();
        spawnShopkeeper(serverLevel, gate.getX(), gate.getY() + 0.5D, gate.getZ(), summoner);
    }

    @SubscribeEvent
    public static void onWaveCompleted(GateEvent.WaveEnd event) {
        GatewayEntity gate = event.getEntity();
        if (gate.level() instanceof ServerLevel serverLevel) {
            int waveBurst = 8 + serverLevel.random.nextInt(5) + Math.max(0, gate.getWave());
            spawnCoins(gate, waveBurst);
            spawnGatewayLoot(gate, 1 + serverLevel.random.nextInt(2));
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
                    GatekeeperEntity trader = spawnShopkeeper(serverLevel, gatewayEntity.getX(), gatewayEntity.getY() + 0.5D, gatewayEntity.getZ(), summoner);
                    if (trader != null) {
                        gatewayEntity.getPersistentData().putBoolean(SHOP_GATEWAY_TRADER_SPAWNED, true);
                        gatewayEntity.getPersistentData().putUUID(SHOP_GATEWAY_TRADER_ID, trader.getUUID());
                        trader.getPersistentData().putUUID(SHOP_GATEWAY_ENTITY_ID, gatewayEntity.getUUID());
                        gatewayEntity.remove(Entity.RemovalReason.DISCARDED);
                    }
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
        if (!(target instanceof GatekeeperEntity trader) || !isShopkeeper(trader) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        MenuProvider provider = new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ShopkeeperMenu(containerId, inventory, trader.getId()),
                Component.translatable("entity.gatewayexpansion.shopkeeper"));
        player.openMenu(provider, buffer -> buffer.writeInt(trader.getId()));
    }

    @SubscribeEvent
    public static void onShopkeeperDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof GatekeeperEntity trader) || trader.level().isClientSide || !isShopkeeper(trader)) {
            return;
        }

        CompoundTag traderData = trader.getPersistentData();
        if (!traderData.hasUUID(SHOP_GATEWAY_ENTITY_ID) || !(trader.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity linkedEntity = serverLevel.getEntity(traderData.getUUID(SHOP_GATEWAY_ENTITY_ID));
        if (linkedEntity instanceof GatewayEntity gatewayEntity && isGatewayAnimation(gatewayEntity) && !gatewayEntity.isRemoved()) {
            gatewayEntity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public static GatekeeperEntity spawnShopkeeper(Level level, double x, double y, double z, Player summoner) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        GatekeeperEntity trader = ModEntities.GATEKEEPER.get().create(serverLevel);
        if (trader == null) {
            return null;
        }

        trader.moveTo(x, y, z, summoner == null ? 0.0F : summoner.getYRot(), 0.0F);
        trader.setCustomName(Component.translatable("entity.gatewayexpansion.shopkeeper").withStyle(ChatFormatting.GOLD));
        trader.setCustomNameVisible(true);
        trader.setPersistenceRequired();
        trader.setHealth(1.0F);
        trader.getPersistentData().putBoolean(SHOPKEEPER_KEY, true);
        rollVisibleOffers(trader, serverLevel.random, getPlayerLevel(summoner));
        return serverLevel.addFreshEntity(trader) ? trader : null;
    }

    public static boolean isShopkeeper(GatekeeperEntity trader) {
        return trader.getPersistentData().getBoolean(SHOPKEEPER_KEY);
    }

    public static void markGatewayAnimation(GatewayEntity entity) {
        entity.getPersistentData().putBoolean(SHOP_GATEWAY_ANIMATION_KEY, true);
    }

    public static boolean isGatewayAnimation(GatewayEntity entity) {
        return entity.getPersistentData().getBoolean(SHOP_GATEWAY_ANIMATION_KEY);
    }

    public static java.util.List<ShopOfferDefinition> getOffers(GatekeeperEntity trader) {
        java.util.List<ShopOfferDefinition> offers = buildOffers(trader.getPersistentData().getIntArray(TEMP_TRADE_KEY));
        return offers.size() > ShopkeeperMenu.GRID_SLOT_COUNT ? offers.subList(0, ShopkeeperMenu.GRID_SLOT_COUNT) : offers;
    }

    public static int getMaxRerolls() {
        return MAX_REROLLS;
    }

    public static int getRerollCost(GatekeeperEntity trader) {
        int rerollCount = getRerollCount(trader);
        if (rerollCount >= MAX_REROLLS) {
            return 0;
        }
        return BASE_REROLL_COST << rerollCount;
    }

    public static int getRerollCount(GatekeeperEntity trader) {
        return Math.max(0, trader.getPersistentData().getInt(REROLL_COUNT_KEY));
    }

    public static int[] getTempOfferIndexes(GatekeeperEntity trader) {
        return trader.getPersistentData().getIntArray(TEMP_TRADE_KEY);
    }

    public static int[] getOfferStocks(GatekeeperEntity trader) {
        int[] stocks = trader.getPersistentData().getIntArray(STOCK_KEY);
        if (stocks.length == ShopkeeperMenu.GRID_SLOT_COUNT) {
            return stocks;
        }

        int[] normalized = new int[ShopkeeperMenu.GRID_SLOT_COUNT];
        System.arraycopy(stocks, 0, normalized, 0, Math.min(stocks.length, normalized.length));
        return normalized;
    }

    public static boolean rerollOffers(ServerPlayer player, GatekeeperEntity trader) {
        int rerollCount = Math.max(0, trader.getPersistentData().getInt(REROLL_COUNT_KEY));
        if (rerollCount >= MAX_REROLLS) {
            return false;
        }

        int rerollCost = getRerollCost(trader);
        if (!MythicCoinWallet.spend(player, rerollCost)) {
            return false;
        }

        rollVisibleOffers(trader, player.getRandom(), getPlayerLevel(player));
        CompoundTag tag = trader.getPersistentData();
        tag.putInt(REROLL_COUNT_KEY, rerollCount + 1);
        return true;
    }

    public static boolean consumeStock(GatekeeperEntity trader, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= ShopkeeperMenu.GRID_SLOT_COUNT) {
            return false;
        }

        int[] stocks = getOfferStocks(trader);
        if (stocks[slotIndex] <= 0) {
            return false;
        }

        stocks[slotIndex]--;
        trader.getPersistentData().putIntArray(STOCK_KEY, stocks);
        return true;
    }

    public static void restoreStock(GatekeeperEntity trader, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= ShopkeeperMenu.GRID_SLOT_COUNT) {
            return;
        }

        int[] stocks = getOfferStocks(trader);
        stocks[slotIndex]++;
        trader.getPersistentData().putIntArray(STOCK_KEY, stocks);
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

    private static void spawnGatewayLoot(GatewayEntity gate, int rolls) {
        RandomSource random = gate.level().random;
        for (int index = 0; index < rolls; index++) {
            LootMaterialItem item = rollGatewayLoot(gate, random);
            if (item == null) {
                continue;
            }

            int stackSize = switch (item.rarity()) {
                case COMMON -> 2 + random.nextInt(3);
                case UNCOMMON -> 1 + random.nextInt(2);
                case RARE, EPIC, LEGENDARY, UNIQUE -> 1;
            };

            ItemEntity itemEntity = new ItemEntity(gate.level(), gate.getX(), gate.getY() + 1.0D, gate.getZ(), new ItemStack(item, stackSize));
            itemEntity.setDeltaMovement(
                    random.nextDouble() * 0.24D - 0.12D,
                    0.22D + random.nextDouble() * 0.10D,
                    random.nextDouble() * 0.24D - 0.12D);
            itemEntity.setNoPickUpDelay();
            gate.level().addFreshEntity(itemEntity);
        }
    }

    private static LootMaterialItem rollGatewayLoot(GatewayEntity gate, RandomSource random) {
        LootMaterialItem[] pool = getDropPoolForGate(gate);
        int totalWeight = java.util.Arrays.stream(pool).mapToInt(item -> getGatewayDropWeight(item, gate)).sum();
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (LootMaterialItem item : pool) {
            roll -= getGatewayDropWeight(item, gate);
            if (roll < 0) {
                return item;
            }
        }

        return pool[0];
    }

    private static void rollVisibleOffers(GatekeeperEntity trader, RandomSource random, int playerLevel) {
        int tempCount = Math.min(ShopkeeperMenu.GRID_SLOT_COUNT, ShopOfferDefinition.ALL_OFFERS.size());
        int[] picks = pickTempOfferIndexes(random, tempCount, playerLevel);
        trader.getPersistentData().putIntArray(TEMP_TRADE_KEY, picks);
        trader.getPersistentData().putIntArray(STOCK_KEY, rollStocks(buildOffers(picks), random));
    }

    private static int[] rollStocks(java.util.List<ShopOfferDefinition> offers, RandomSource random) {
        int[] stocks = new int[ShopkeeperMenu.GRID_SLOT_COUNT];
        for (int index = 0; index < stocks.length; index++) {
            if (index >= offers.size()) {
                stocks[index] = 0;
                continue;
            }

            stocks[index] = rollStockForOffer(offers.get(index), random);
        }
        return stocks;
    }

    private static int rollStockForOffer(ShopOfferDefinition offer, RandomSource random) {
        ItemStack preview = offer.previewStack();
        if (preview.is(ModItems.GRIMSTONE.get()) || preview.is(ModItems.MYSTIC_ESSENCE.get()) || preview.is(ModItems.SCRAP_METAL.get())) {
            return 32 + random.nextInt(17);
        }
        if (preview.is(ModItems.MANA_STEEL_SCRAP.get()) || preview.is(ModItems.ELIXRITE_SCRAP.get())) {
            return 1 + random.nextInt(7);
        }
        if (preview.is(ModItems.MANA_STEEL_INGOT.get()) || preview.is(ModItems.ELIXRITE_INGOT.get())) {
            return 1 + random.nextInt(2);
        }
        if (preview.is(Items.IRON_INGOT) || preview.is(Items.GOLD_INGOT)) {
            return 6 + random.nextInt(7);
        }
        if (preview.is(Items.DIAMOND)) {
            return 2 + random.nextInt(4);
        }
        if (preview.is(Items.GOLDEN_APPLE)) {
            return 1 + random.nextInt(3);
        }
        if (preview.is(Items.NETHERITE_SCRAP)) {
            return 1 + random.nextInt(2);
        }
        if (isRunicOffer(preview)) {
            return preview.is(runicItem("enhanced_rune")) ? 1 + random.nextInt(2) : 1;
        }
        if (preview.is(ModItems.MANA_GEMS.get()) || preview.is(ModItems.ARCANE_ESSENCE.get()) || preview.is(ModItems.MANASTONES.get())) {
            return 18 + random.nextInt(11);
        }
        if (preview.is(ModItems.SOLAR_CRYSTAL.get()) || preview.is(ModItems.DARK_ESSENCE.get())) {
            return 10 + random.nextInt(7);
        }
        if (preview.is(ModItems.PRISMATIC_DIAMOND.get())) {
            return 6 + random.nextInt(5);
        }
        if (preview.is(ModItems.PRISMATIC_CORE.get())) {
            return 3 + random.nextInt(3);
        }
        if (preview.getItem() instanceof com.revilo.gatewayexpansion.item.CrystalItem) {
            return 2 + random.nextInt(3);
        }
        if (preview.getItem() instanceof com.revilo.gatewayexpansion.item.AugmentItem || preview.getItem() instanceof com.revilo.gatewayexpansion.item.CatalystItem) {
            return 4 + random.nextInt(5);
        }
        return 8 + random.nextInt(7);
    }

    private static java.util.List<ShopOfferDefinition> buildOffers(int[] tempIndexes) {
        java.util.List<ShopOfferDefinition> offers = new java.util.ArrayList<>(tempIndexes.length);
        for (int tempIndex : tempIndexes) {
            if (tempIndex >= 0 && tempIndex < ShopOfferDefinition.ALL_OFFERS.size()) {
                offers.add(ShopOfferDefinition.ALL_OFFERS.get(tempIndex));
            }
        }
        return offers;
    }

    private static int[] pickTempOfferIndexes(RandomSource random, int tempCount, int playerLevel) {
        int[] eligible = ShopOfferDefinition.ALL_OFFERS.stream()
                .filter(offer -> offer.requiredLevel() <= playerLevel)
                .mapToInt(ShopOfferDefinition.ALL_OFFERS::indexOf)
                .toArray();
        int poolSize = eligible.length;
        if (poolSize == 0) {
            return new int[0];
        }

        int[] pool = new int[poolSize];
        for (int index = 0; index < poolSize; index++) {
            pool[index] = eligible[index];
        }

        int pickCount = Math.min(tempCount, poolSize);
        for (int index = 0; index < pickCount; index++) {
            int swapIndex = index + random.nextInt(poolSize - index);
            int selected = pool[swapIndex];
            pool[swapIndex] = pool[index];
            pool[index] = selected;
        }
        return java.util.Arrays.copyOf(pool, pickCount);
    }

    private static LootMaterialItem[] getDropPoolForGate(GatewayEntity gate) {
        if (gate == null || GatewayForgeService.getGatewayCrystalTier(gate.getGateway()) >= 4) {
            return GATEWAY_DROP_POOL;
        }

        return java.util.Arrays.stream(GATEWAY_DROP_POOL)
                .filter(item -> item != ModItems.PRISMATIC_CORE.get())
                .toArray(LootMaterialItem[]::new);
    }

    private static int getGatewayDropWeight(LootMaterialItem item, GatewayEntity gate) {
        if (item == ModItems.MANA_STEEL_SCRAP.get()) {
            return isLevel20PlusGate(gate) ? LootRarity.COMMON.weight() : LootRarity.UNCOMMON.weight();
        }
        if (item == ModItems.ELIXRITE_SCRAP.get()) {
            return isLevel20PlusGate(gate) ? LootRarity.UNCOMMON.weight() : LootRarity.RARE.weight();
        }
        return item.rarity().weight();
    }

    private static boolean isLevel20PlusGate(GatewayEntity gate) {
        return gate != null && GatewayForgeService.getGatewayCrystalTier(gate.getGateway()) >= 2;
    }

    private static boolean isRunicOffer(ItemStack stack) {
        return ModCompat.isAnyLoaded("runic") && stack.getItem().builtInRegistryHolder().key().location().getNamespace().equals("runic");
    }

    private static net.minecraft.world.item.Item runicItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("runic", path));
    }

    private static int getPlayerLevel(Player player) {
        if (player == null) {
            return 0;
        }
        int integratedLevel = LevelUpIntegration.getPlayerLevel(player);
        return integratedLevel >= 0 ? integratedLevel : player.experienceLevel;
    }
}
