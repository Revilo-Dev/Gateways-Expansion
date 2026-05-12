package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.currency.MythicCoinWallet;
import com.revilo.gatesofavarice.entity.GatewayCrystalEntity;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRegistry;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRole;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolSet;
import com.revilo.gatesofavarice.integration.LevelUpIntegration;
import com.revilo.gatesofavarice.item.MythicCoinStackData;
import com.revilo.gatesofavarice.progression.ProgressionSystem;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
import com.revilo.gatesofavarice.network.DungeonCompletePayload;
import com.revilo.gatesofavarice.network.DungeonWaveHudPayload;
import com.revilo.gatesofavarice.registry.ModEntities;
import com.revilo.gatesofavarice.registry.ModItems;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DungeonRunManager {
    private static final ResourceLocation DUNGEON_EXIT_XP_SOURCE = ResourceLocation.fromNamespaceAndPath("levelup", "objective_complete");
    private static final ResourceLocation DUNGEON_EXIT_COIN_CASHOUT_SOURCE = ResourceLocation.fromNamespaceAndPath("levelup", "dungeon_coin_cashout");

    private static final Map<UUID, RunState> RUNS_BY_OWNER = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_TO_OWNER = new HashMap<>();
    private static final Map<UUID, PendingDeathRestore> PENDING_DEATH_RESTORES = new HashMap<>();

    private static final ResourceLocation MOB_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_health");
    private static final ResourceLocation MOB_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_damage");
    private static final ResourceLocation MOB_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_speed");
    private static final int MAX_REROLLS = 2;
    private static final int BASE_REROLL_COST = 100;
    private static final double BASE_ELITE_CHANCE = 0.06D;

    private static final List<Item> REGULAR_DROP_POOL = List.of(
            ModItems.GRIMSTONE.get(), ModItems.MYSTIC_ESSENCE.get(), ModItems.SCRAP_METAL.get(), ModItems.MANA_GEMS.get(),
            ModItems.MANA_STEEL_SCRAP.get(), ModItems.MAGNETITE_SCRAP.get(), ModItems.ARCANE_ESSENCE.get(), ModItems.MANASTONES.get(),
            ModItems.ELIXRITE_SCRAP.get(), ModItems.ASTRITE_SCRAP.get(), ModItems.SOLAR_SHARD.get(), ModItems.DARK_ESSENCE.get(),
            ModItems.RUSTY_COIN.get(), ModItems.HARDENED_FLESH.get()
    );
    private static final List<Item> COMMON_DROP_POOL = List.of(
            ModItems.GRIMSTONE.get(), ModItems.MYSTIC_ESSENCE.get(), ModItems.SCRAP_METAL.get(), ModItems.MANA_GEMS.get(),
            ModItems.MAGNETITE_SCRAP.get(), ModItems.ARCANE_ESSENCE.get(), ModItems.MANASTONES.get(), ModItems.RUSTY_COIN.get(), ModItems.HARDENED_FLESH.get()
    );
    private static final List<Item> UNCOMMON_DROP_POOL = List.of(
            ModItems.MANA_STEEL_SCRAP.get(), ModItems.ELIXRITE_SCRAP.get(), ModItems.ASTRITE_SCRAP.get(), ModItems.SOLAR_SHARD.get()
    );
    private static final List<Item> RARE_DROP_POOL = List.of(
            ModItems.DARK_ESSENCE.get(), ModItems.PRISMATIC_SHARD.get()
    );
    private static final List<Component> TAROT_ENEMY_LINES = List.of(
            Component.literal("+2 Hoard Mobs"), Component.literal("+3 Hoard Mobs"), Component.literal("+4 Hoard Mobs"),
            Component.literal("+2 Assassin Mobs"), Component.literal("+3 Archer Mobs"), Component.literal("+2 Tank Mobs"));
    private static final List<Component> TAROT_EFFECT_LINES = List.of(
            Component.literal("+8% Mob Damage"), Component.literal("+10% Mob Health"), Component.literal("+12% Mob Speed"),
            Component.literal("+15% Mob Resistance"), Component.literal("+8% Elite Chance"), Component.literal("+6% Spawn Rate"));
    private static final List<Component> TAROT_REWARD_LINES = List.of(
            Component.literal("+1 Reward Roll"), Component.literal("+2 Reward Rolls"), Component.literal("+20 Mythic Coins"),
            Component.literal("+1 Unlock Archetype"), Component.literal("+1 Dungeon Loot Burst"));
    private static final List<WeightedItem> WEAPON_POOL = buildWeaponPool();
    private static final List<LoadoutDefinition> LOADOUT_DEFINITIONS = buildLoadoutDefinitions();

    private DungeonRunManager() {}

    public static void enterFromGateway(ServerPlayer player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.computeIfAbsent(ownerId, RunState::new);
        run.server = player.server;
        if (run.runStartGameTime < 0L) {
            run.runStartGameTime = player.serverLevel().getGameTime();
        }
        run.participants.add(player.getUUID());
        PLAYER_TO_OWNER.put(player.getUUID(), ownerId);
        run.snapshots.putIfAbsent(player.getUUID(), PlayerSnapshot.capture(player));
        clearForDungeon(player);
        DungeonInstanceManager.teleportToDungeonInstance(player, ownerId);
        if (player.getUUID().equals(ownerId) && run.phase == RunPhase.SELECTING_TAROT) {
            rollTarotOptions(run, player.serverLevel().random);
            openWaveMenu(player, run);
        }
    }

    public static void exitViaBailPortal(ServerPlayer player, UUID ownerId, GatewayCrystalEntity portal) {
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run == null || run.exitPortalId != portal.getId()) return;
        PlayerSnapshot snapshot = run.snapshots.get(player.getUUID());
        if (snapshot == null) return;
        syncHudToPlayer(player, false, run.waveNumber, 0, 1);
        int levelPoints = awardDungeonExitProgression(player, run);
        List<ItemStack> rewards = collectDungeonRewards(player);
        ItemStack lootbox = createLootboxFromRewards(player, rewards);
        restoreSnapshot(player, snapshot);
        if (player.getUUID().equals(run.ownerId)) {
            closeOverworldEntryPortals(player.server, run.ownerId);
        }
        if (!lootbox.isEmpty() && !player.getInventory().add(lootbox)) player.drop(lootbox, false);
        DungeonInstanceManager.teleportToSavedLocation(player, snapshot.dimension, snapshot.returnPos, snapshot.yaw, snapshot.pitch);
        closeNearbyEntryPortals(player.server, snapshot.dimension, snapshot.returnPos, run.ownerId);
        int cashedOutCoins = cashoutRemainingCoinsOnDungeonExit(player);
        sendCompletionScreen(player, run, rewards, levelPoints, cashedOutCoins);
        PLAYER_TO_OWNER.remove(player.getUUID());
        run.participants.remove(player.getUUID());
        if (run.participants.isEmpty()) {
            finishAndCleanup(run);
        }
    }

    public static boolean handleWaveMenuClick(Player player, UUID ownerId, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run == null || !serverPlayer.getUUID().equals(run.ownerId)) return false;
        if (run.phase != RunPhase.SELECTING_TAROT && run.phase != RunPhase.SELECTING_LOOT) return false;

        if (run.phase == RunPhase.SELECTING_TAROT) {
            if (buttonId == DungeonWaveMenu.BAIL_BUTTON_ID) {
                spawnExitPortal(run, serverPlayer.serverLevel());
                run.phase = RunPhase.WAITING_EXIT;
                serverPlayer.closeContainer();
                return true;
            }
            if (buttonId < 0 || buttonId >= run.tarotOptions.size()) return false;
            applyTarot(run, run.tarotOptions.get(buttonId));
            if (run.waveNumber == 0) {
                rollLoadoutOptions(run, serverPlayer.serverLevel().random);
            } else {
                rollLootOptions(run, serverPlayer.serverLevel().random);
            }
            run.phase = RunPhase.SELECTING_LOOT;
            openWaveMenu(serverPlayer, run);
            return true;
        }

        if (buttonId == DungeonWaveMenu.REROLL_BUTTON_ID) {
            if (run.selectingLoadout) return false;
            if (run.rerollsUsed >= MAX_REROLLS) return false;
            int cost = BASE_REROLL_COST << run.rerollsUsed;
            if (!MythicCoinWallet.spend(serverPlayer, cost)) return false;
            run.rerollsUsed++;
            rollLootOptions(run, serverPlayer.serverLevel().random);
            openWaveMenu(serverPlayer, run);
            return true;
        }
        if (buttonId == DungeonWaveMenu.SKIP_BUTTON_ID) {
            if (run.selectingLoadout) return false;
            startWave(run);
            serverPlayer.closeContainer();
            return true;
        }
        if (run.selectingLoadout) {
            if (buttonId < 0 || buttonId >= run.loadoutOptions.size()) return false;
            grantLoadout(serverPlayer, run.loadoutOptions.get(buttonId), serverPlayer.serverLevel().random);
        } else {
            if (buttonId < 0 || buttonId >= run.lootOptions.size()) return false;
            grantLoot(serverPlayer, run, run.lootOptions.get(buttonId), serverPlayer.serverLevel().random);
        }
        startWave(run);
        serverPlayer.closeContainer();
        return true;
    }

    public static boolean isWaveMenuValid(Player player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.get(ownerId);
        return run != null && run.participants.contains(player.getUUID());
    }

    public static boolean isPlayerInActiveRun(Player player) {
        return getRunForPlayer(player) != null;
    }

    public static void rollAndBindForActiveRun(ServerPlayer player, ItemStack stack, RandomSource random) {
        RunState run = getRunForPlayer(player);
        int playerLevel = getEffectivePlayerLevel(player);
        long timeInDungeonTicks = 0L;
        if (run != null && run.runStartGameTime >= 0L) {
            timeInDungeonTicks = Math.max(0L, player.serverLevel().getGameTime() - run.runStartGameTime);
        }
        DungeonGearRoller.rollAndBind(stack, random, playerLevel, timeInDungeonTicks, player.registryAccess());
    }

    public static int recoverStalledShops(MinecraftServer server) {
        int recovered = 0;
        for (RunState run : RUNS_BY_OWNER.values()) {
            if (run.phase != RunPhase.SHOP) {
                continue;
            }
            ServerLevel dungeon = getDungeonLevel(run);
            if (dungeon == null) {
                continue;
            }
            Entity shop = run.shopkeeperId >= 0 ? dungeon.getEntity(run.shopkeeperId) : null;
            if (shop != null) {
                continue;
            }
            if (ensureShopkeeper(run, dungeon)) {
                recovered++;
            }
        }
        return recovered;
    }
    public static boolean canOwnerStartNextWaveFromShop(ServerPlayer player, int shopkeeperEntityId) {
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        if (ownerId == null) return false;
        RunState run = RUNS_BY_OWNER.get(ownerId);
        return run != null && run.phase == RunPhase.SHOP && player.getUUID().equals(run.ownerId) && run.shopkeeperId == shopkeeperEntityId;
    }
    public static boolean startNextWaveFromShop(ServerPlayer player, int shopkeeperEntityId) {
        if (!canOwnerStartNextWaveFromShop(player, shopkeeperEntityId)) return false;
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        if (ownerId == null) return false;
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run == null) return false;
        Entity shop = player.serverLevel().getEntity(shopkeeperEntityId);
        if (shop != null) shop.discard();
        run.shopkeeperId = -1;
        run.phase = RunPhase.SELECTING_TAROT;
        run.rerollsUsed = 0;
        rollTarotOptions(run, player.serverLevel().random);
        openWaveMenu(player, run);
        return true;
    }
    public static boolean handleLoadoutMenuClick(Player player, UUID ownerId, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (!isLoadoutMenuValid(serverPlayer, ownerId) || buttonId < 0 || buttonId > 2) {
            return false;
        }
        applyLoadout(serverPlayer, buttonId);
        serverPlayer.closeContainer();
        return true;
    }

    public static boolean isLoadoutMenuValid(Player player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.get(ownerId);
        return run != null && run.ownerId.equals(ownerId) && run.participants.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RUNS_BY_OWNER.isEmpty()) return;
        ArrayList<UUID> empty = new ArrayList<>();
        ServerLevel dungeonLevel = event.getServer().getLevel(ModDimensions.DUNGEON_LEVEL);
        if (dungeonLevel != null) {
            dungeonLevel.setDayTime(18000L);
        }
        for (RunState run : RUNS_BY_OWNER.values()) {
            ServerLevel dungeon = dungeonLevel;
            if (dungeon == null) continue;
            DungeonInstanceManager.keepInstanceAlive(run.ownerId, dungeon);

            if (run.phase == RunPhase.SELECTING_TAROT && run.tarotOptions.isEmpty()) {
                rollTarotOptions(run, dungeon.random);
                ServerPlayer owner = run.online(run.ownerId);
                if (owner != null) openWaveMenu(owner, run);
            } else if (run.phase == RunPhase.IN_WAVE) {
                tickWave(run, dungeon);
            }

            if (run.participants.isEmpty()) empty.add(run.ownerId);
        }
        for (UUID ownerId : empty) {
            RunState run = RUNS_BY_OWNER.get(ownerId);
            if (run != null) finishAndCleanup(run);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        float damage = Math.max(0.0F, event.getNewDamage());
        if (damage <= 0.0F) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer player) {
            RunState run = getRunForPlayer(player);
            if (run != null) {
                run.damageDealt += damage;
            }
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            RunState run = getRunForPlayer(player);
            if (run != null) {
                run.damageReceived += damage;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID ownerId = PLAYER_TO_OWNER.remove(player.getUUID());
            if (ownerId == null) return;
            RunState run = RUNS_BY_OWNER.get(ownerId);
            if (run == null) return;
            PlayerSnapshot snapshot = run.snapshots.get(player.getUUID());
            if (snapshot != null) {
                PENDING_DEATH_RESTORES.put(player.getUUID(), new PendingDeathRestore(snapshot));
            }
            run.participants.remove(player.getUUID());
            if (player.getUUID().equals(run.ownerId)) {
                closeOverworldEntryPortals(player.server, run.ownerId);
            }
            clearForDungeon(player);
            syncHudToPlayer(player, false, run.waveNumber, 0, 1);
            if (run.liveParticipants().isEmpty()) {
                finishAndCleanup(run);
            }
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity dead) || dead.level().isClientSide) return;
        RunState run = findRunByTrackedMob(dead.getUUID());
        if (run == null) return;
        trackDungeonKillProgress(run, dead);
        run.mobsKilled++;
        run.aliveMobs.remove(dead.getUUID());
        if (dead.level() instanceof ServerLevel level) spawnDungeonMobDrops(level, dead, run);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && PENDING_DEATH_RESTORES.containsKey(player.getUUID())) {
            event.getDrops().clear();
            return;
        }
        if (findRunByTrackedMob(event.getEntity().getUUID()) != null) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingDeathRestore pending = PENDING_DEATH_RESTORES.remove(player.getUUID());
        if (pending == null) {
            return;
        }
        restoreSnapshot(player, pending.snapshot());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID ownerId = PLAYER_TO_OWNER.remove(player.getUUID());
        if (ownerId == null) return;
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run != null) {
            run.participants.remove(player.getUUID());
            if (player.getUUID().equals(run.ownerId)) {
                closeOverworldEntryPortals(player.server, run.ownerId);
            }
            if (run.participants.isEmpty()) finishAndCleanup(run);
        }
    }

    private static void startWave(RunState run) {
        run.waveNumber++;
        run.phase = RunPhase.IN_WAVE;
        run.aliveMobs.clear();
        run.spawnCooldown = 0;
        run.tarotOptions = List.of();
        run.lootOptions = List.of();
        run.loadoutOptions = List.of();
        run.selectingLoadout = false;
        int extraPlayers = Math.max(0, run.liveParticipants().size() - 1);
        int avgLevel = averageParticipantLevel(run);
        double progressionDifficulty = ProgressionSystem.dungeonDifficultyScalar(avgLevel, Math.max(1, run.waveNumber));
        int baseCount = 6 + run.waveNumber * 2 + extraPlayers * 3;
        run.toSpawn = Math.max(4, (int) Math.round(baseCount * run.enemyCountMultiplier * progressionDifficulty));
        run.waveTotalMobs = run.toSpawn;
        run.currentWavePools = buildWavePools(run);
        for (ServerPlayer participant : run.liveParticipants()) {
            participant.displayClientMessage(Component.literal("Wave " + run.waveNumber).withStyle(ChatFormatting.GOLD), false);
        }
        syncHud(run, true);
    }

    private static void tickWave(RunState run, ServerLevel level) {
        run.aliveMobs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return !(entity instanceof LivingEntity living) || !living.isAlive();
        });
        if (run.toSpawn > 0) {
            if (run.spawnCooldown > 0) run.spawnCooldown--;
            else {
                int batch = Math.min(run.toSpawn, 2 + level.random.nextInt(2));
                for (int i = 0; i < batch; i++) spawnOneMob(run, level);
                run.toSpawn -= batch;
                run.spawnCooldown = 10;
            }
        }
        if (run.toSpawn <= 0 && run.aliveMobs.isEmpty()) completeWave(run, level); else syncHud(run, true);
    }

    private static void completeWave(RunState run, ServerLevel level) {
        int avgLevel = averageParticipantLevel(run);
        int wave = Math.max(1, run.waveNumber);
        int scaledCoins = ProgressionSystem.dungeonCoinReward(avgLevel, wave, run.quantityBonusModifier);
        run.coinsEarned += Math.max(5, scaledCoins);
        for (ServerPlayer player : run.liveParticipants()) {
            MythicCoinWallet.addRaw(player, Math.max(5, scaledCoins));
        }
        int lootRolls = computeWaveLootRolls(run, avgLevel, level.random);
        spawnWaveLootBurst(run, level, lootRolls);
        for (ServerPlayer player : run.liveParticipants()) {
            player.setHealth(player.getMaxHealth());
        }
        ensureShopkeeper(run, level);
        run.phase = RunPhase.SHOP;
        syncHud(run, false);
    }

    private static void spawnExitPortal(RunState run, ServerLevel level) {
        if (run.exitPortalId >= 0) return;
        GatewayCrystalEntity portal = ModEntities.GATEWAY_CRYSTAL.get().create(level);
        if (portal == null) return;
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        portal.setOwnerId(run.ownerId);
        portal.setReturnPortal(true);
        portal.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 23.5D, 0.0F, 0.0F);
        if (level.addFreshEntity(portal)) run.exitPortalId = portal.getId();
    }

    private static boolean ensureShopkeeper(RunState run, ServerLevel level) {
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        ServerPlayer owner = run.online(run.ownerId);
        Player summoner = owner;
        var shop = ShopkeeperManager.spawnShopkeeper(level, center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, summoner);
        if (shop == null) {
            run.shopkeeperId = -1;
            return false;
        }
        shop.setInvulnerable(true);
        run.shopkeeperId = shop.getId();
        return true;
    }

    private static void spawnWaveLootBurst(RunState run, ServerLevel level, int rolls) {
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        for (int i = 0; i < rolls; i++) {
            Item item = pickScaledDrop(run, averageParticipantLevel(run), level.random);
            int stackCount = Math.max(1, 1 + level.random.nextInt(2) + Math.max(0, run.waveNumber / 5) + (int) Math.floor(run.quantityBonusModifier * 2.0D));
            ItemStack stack = new ItemStack(item, stackCount);
            ItemEntity entity = new ItemEntity(level, center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, stack);
            entity.setDeltaMovement(level.random.nextDouble() * 0.3D - 0.15D, 0.25D, level.random.nextDouble() * 0.3D - 0.15D);
            entity.setNoPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    private static void spawnOneMob(RunState run, ServerLevel level) {
        WaveArchetype archetype = run.unlockedArchetypes.get(level.random.nextInt(run.unlockedArchetypes.size()));
        EnemyPoolSet pools = run.currentWavePools.get(archetype);
        EntityType<?> type = pickEntityType(level.random, pools, archetype);
        if (type == null) return;
        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity mob)) return;
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        double x = center.getX() + 0.5D + (level.random.nextDouble() * 24.0D - 12.0D);
        double z = center.getZ() + 0.5D + (level.random.nextDouble() * 24.0D - 12.0D);
        double y = center.getY() + 1.0D;
        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        if (mob instanceof Mob aiMob) aiMob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.EVENT, (SpawnGroupData) null);
        applyMobScaling(mob, run.damageMultiplier, run.speedMultiplier);
        if (rollElite(run, level.random)) {
            applyEliteVariant(mob);
        }
        if (level.addFreshEntity(mob)) run.aliveMobs.add(mob.getUUID());
    }

    private static void rollTarotOptions(RunState run, RandomSource random) {
        ArrayList<TarotOption> rolled = new ArrayList<>();
        for (int i = 0; i < 4; i++) rolled.add(TarotOption.random(random));
        run.tarotOptions = List.copyOf(rolled);
    }

    private static void rollLootOptions(RunState run, RandomSource random) {
        run.selectingLoadout = false;
        int avgLevel = averageParticipantLevel(run);
        ArrayList<LootOption> rolled = new ArrayList<>();
        rolled.add(LootOption.weapon(random, avgLevel));
        rolled.add(LootOption.armor(random));
        rolled.add(LootOption.magnet(random, avgLevel));
        rolled.add(LootOption.coins(random, avgLevel));
        run.lootOptions = List.copyOf(rolled);
        run.loadoutOptions = List.of();
    }

    private static void rollLoadoutOptions(RunState run, RandomSource random) {
        run.selectingLoadout = true;
        ArrayList<LoadoutOption> rolled = new ArrayList<>();
        ArrayList<LoadoutDefinition> eligible = new ArrayList<>(LOADOUT_DEFINITIONS);
        int avgLevel = averageParticipantLevel(run);
        for (int i = 0; i < 3 && !eligible.isEmpty(); i++) {
            LoadoutDefinition definition = eligible.remove(random.nextInt(eligible.size()));
            ItemStack primary = new ItemStack(pickLoadoutWeapon(random, avgLevel, definition.primaryKind()));
            ItemStack secondary = new ItemStack(pickLoadoutWeapon(random, avgLevel, definition.secondaryKind()));
            rolled.add(new LoadoutOption(
                    Component.literal(definition.name() + " loadout"),
                    Component.literal("Armor: " + definition.armorName() + "\n" + String.join("\n", definition.traits()) + "\nFood: " + definition.foodSummary()),
                    new ItemStack(definition.head()),
                    new ItemStack(definition.chest()),
                    new ItemStack(definition.legs()),
                    new ItemStack(definition.feet()),
                    primary,
                    secondary,
                    pickLeveledMagnet(random, avgLevel),
                    definition.food()));
        }
        run.loadoutOptions = List.copyOf(rolled);
        run.lootOptions = List.of();
    }

    private static void applyTarot(RunState run, TarotOption option) {
        run.enemyCountMultiplier += option.enemyCountBonus;
        run.damageMultiplier += option.damageBonus;
        run.speedMultiplier += option.speedBonus;
        run.extraRewardRolls += option.rewardRollBonus;
        run.quantityBonusModifier += option.quantityBonus;
        run.rarityBonusModifier += option.rarityBonus;
        run.eliteChanceBonus += option.eliteChanceBonus;
        if (option.unlockArchetype != null && !run.unlockedArchetypes.contains(option.unlockArchetype)) run.unlockedArchetypes.add(option.unlockArchetype);
    }

    private static void grantLoot(ServerPlayer player, RunState run, LootOption option, RandomSource random) {
        ItemStack stack = option.stack().copy();
        if (stack.isEmpty()) return;
        int playerLevel = getEffectivePlayerLevel(player);
        long timeInDungeonTicks = Math.max(0L, player.serverLevel().getGameTime() - run.runStartGameTime);
        DungeonGearRoller.rollAndBind(stack, random, playerLevel, timeInDungeonTicks, player.registryAccess());
        if (stack.getItem() instanceof ArmorItem armorItem) {
            player.setItemSlot(armorItem.getEquipmentSlot(), stack);
        } else if (DungeonBoundItems.isWeapon(stack)) {
            grantSecondaryWeapon(player, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void grantLoadout(ServerPlayer player, LoadoutOption loadout, RandomSource random) {
        clearForDungeon(player);
        int playerLevel = getEffectivePlayerLevel(player);
        long timeTicks = 0L;
        ItemStack head = loadout.head().copy();
        ItemStack chest = loadout.chest().copy();
        ItemStack legs = loadout.legs().copy();
        ItemStack feet = loadout.feet().copy();
        ItemStack primary = loadout.primary().copy();
        ItemStack secondary = loadout.secondary().copy();
        ItemStack utility = loadout.utility().copy();
        DungeonGearRoller.rollAndBind(head, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(chest, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(legs, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(feet, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(primary, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(secondary, random, playerLevel, timeTicks, player.registryAccess());
        DungeonGearRoller.rollAndBind(utility, random, playerLevel, timeTicks, player.registryAccess());
        DungeonBoundItems.markPrimaryWeapon(primary);
        DungeonBoundItems.markSecondaryWeapon(secondary);
        player.setItemSlot(EquipmentSlot.HEAD, head);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.LEGS, legs);
        player.setItemSlot(EquipmentSlot.FEET, feet);
        player.getInventory().armor.set(3, head.copy());
        player.getInventory().armor.set(2, chest.copy());
        player.getInventory().armor.set(1, legs.copy());
        player.getInventory().armor.set(0, feet.copy());
        addRoleAware(player, primary);
        addRoleAware(player, secondary);
        player.getInventory().add(utility);
        for (ItemStack food : loadout.food()) {
            player.getInventory().add(food.copy());
        }
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    public static void grantPrimaryWeapon(ServerPlayer player, ItemStack stack) {
        DungeonBoundItems.markPrimaryWeapon(stack);
        addRoleAware(player, stack);
    }

    private static void grantSecondaryWeapon(ServerPlayer player, ItemStack stack) {
        DungeonBoundItems.markSecondaryWeapon(stack);
        addRoleAware(player, stack);
    }

    private static void addRoleAware(ServerPlayer player, ItemStack stack) {
        if (!DungeonBoundItems.replaceRoleWeapon(player, stack) && !player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static List<LoadoutDefinition> buildLoadoutDefinitions() {
        return List.of(
                loadout("Assassin", "dagger", "dagger", "Shadow Leather Set", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                        List.of("Very high speed", "Low defence", "Small crit bonus", "Low health"), List.of(new ItemStack(Items.GOLDEN_APPLE, 2), new ItemStack(Items.COOKED_PORKCHOP, 16))),
                loadout("Knight", "longsword", "crossbow", "Steel Knight Set", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                        List.of("High defence", "Medium speed", "Medium health boost", "Small thorns"), List.of(new ItemStack(Items.COOKED_BEEF, 16), new ItemStack(Items.GOLDEN_CARROT, 8))),
                loadout("Berserker", "axe", "machete", "Rage Plate Set", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                        List.of("Medium defence", "Medium speed", "High health boost", "Low ability power"), List.of(new ItemStack(Items.COOKED_BEEF, 24), new ItemStack(Items.GOLDEN_APPLE, 1))),
                loadout("Vanguard", "hammer", "broadsword", "Fortress Set", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                        List.of("Very high defence", "Very low speed", "Massive health boost", "Medium thorns"), List.of(new ItemStack(Items.RABBIT_STEW, 6), new ItemStack(Items.COOKED_BEEF, 12))),
                loadout("Samurai", "gaundao", "dagger", "Windwalker Set", Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                        List.of("High speed", "Medium defence", "Small ability power", "Medium health"), List.of(new ItemStack(ModItems.ARCANE_APPLE.get(), 2), new ItemStack(Items.COOKED_SALMON, 16), new ItemStack(Items.GOLDEN_CARROT, 12))),
                loadout("Reaper", "glaive", "dagger", "Soulbound Set", Items.CHAINMAIL_HELMET, Items.IRON_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.IRON_BOOTS,
                        List.of("Medium defence", "Medium speed", "High thorns", "Medium ability power"), List.of(new ItemStack(ModItems.ARCANE_APPLE.get(), 3), new ItemStack(Items.BEETROOT_SOUP, 8))),
                loadout("Ranger", "bow", "machete", "Hunter Set", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                        List.of("High speed", "Low-medium defence", "Small ability power", "Low health boost"), List.of(new ItemStack(ModItems.ARCANE_APPLE.get(), 1), new ItemStack(Items.COOKED_CHICKEN, 16), new ItemStack(Items.SWEET_BERRIES, 32))),
                loadout("Marksman", "crossbow", "longsword", "Sharpshooter Set", Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
                        List.of("Medium defence", "Medium speed", "Medium ability power", "Small health boost"), List.of(new ItemStack(ModItems.ARCANE_APPLE.get(), 2), new ItemStack(Items.GOLDEN_CARROT, 16), new ItemStack(Items.PUMPKIN_PIE, 8))),
                loadout("Gladiator", "broadsword", "dagger", "Arena Set", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                        List.of("Medium-high defence", "Medium speed", "Medium thorns", "Medium health boost"), List.of(new ItemStack(Items.COOKED_BEEF, 20), new ItemStack(Items.GOLDEN_APPLE, 1))),
                loadout("Spellblade", "longsword", "glaive", "Arcane Set", Items.GOLDEN_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_BOOTS,
                        List.of("Medium defence", "Medium speed", "High ability power", "Small health boost"), List.of(new ItemStack(ModItems.ARCANE_APPLE.get(), 4), new ItemStack(Items.GOLDEN_CARROT, 16), new ItemStack(Items.HONEY_BOTTLE, 6))),
                loadout("Warlord", "hammer", "crossbow", "Tyrant Set", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                        List.of("Very high defence", "High thorns", "High health boost", "Very low speed"), List.of(new ItemStack(Items.COOKED_MUTTON, 24), new ItemStack(Items.GOLDEN_APPLE, 3))),
                loadout("Nomad", "machete", "bow", "Traveler Set", Items.LEATHER_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                        List.of("Very high speed", "Low defence", "Small health boost", "Low ability power"), List.of(new ItemStack(Items.BREAD, 24), new ItemStack(Items.COOKED_COD, 12)))
        );
    }

    private static LoadoutDefinition loadout(String name, String primaryKind, String secondaryKind, String armorName, Item head, Item chest, Item legs, Item feet, List<String> traits, List<ItemStack> food) {
        return new LoadoutDefinition(name, primaryKind, secondaryKind, armorName, head, chest, legs, feet, traits, food);
    }

    private static List<WeightedItem> buildWeaponPool() {
        ArrayList<WeightedItem> pool = new ArrayList<>();
        addWeighted(pool, "gatewayexpansion:mana_steel_paxel", 3);
        addWeighted(pool, "gatewayexpansion:elixrite_paxel", 4);
        addWeighted(pool, "gatewayexpansion:astrite_paxel", 5);
        addWeighted(pool, "gatewayexpansion:lunarium_paxel", 6);
        addWeighted(pool, "gatewayexpansion:ignite_paxel", 7);
        addWeighted(pool, "gatewayexpansion:iridium_paxel", 8);
        addWeighted(pool, "gatewayexpansion:mythril_paxel", 9);
        addWeighted(pool, "gatewayexpansion:arcanium_paxel", 10);
        addWeighted(pool, "gatewayexpansion:prismatic_steel_paxel", 11);
        addWeighted(pool, "gatewayexpansion:mana_steel_sword", 7);
        addWeighted(pool, "gatewayexpansion:elixrite_sword", 7);
        addWeighted(pool, "gatewayexpansion:astrite_sword", 8);
        addWeighted(pool, "gatewayexpansion:lunarium_sword", 8);
        addWeighted(pool, "gatewayexpansion:ignite_sword", 9);
        addWeighted(pool, "gatewayexpansion:iridium_sword", 9);
        addWeighted(pool, "gatewayexpansion:mythril_sword", 10);
        addWeighted(pool, "gatewayexpansion:arcanium_sword", 10);
        addWeighted(pool, "gatewayexpansion:prismatic_steel_sword", 12);

        addWeighted(pool, "arsenal:mana_steel_broadsword", 7);
        addWeighted(pool, "arsenal:elixrite_broadsword", 7);
        addWeighted(pool, "arsenal:astrite_broadsword", 8);
        addWeighted(pool, "arsenal:lunarium_broadsword", 8);
        addWeighted(pool, "arsenal:ignite_broadsword", 9);
        addWeighted(pool, "arsenal:iridium_broadsword", 9);
        addWeighted(pool, "arsenal:mythril_broadsword", 10);
        addWeighted(pool, "arsenal:arcanium_broadsword", 10);
        addWeighted(pool, "arsenal:prismatic_steel_broadsword", 11);
        addWeighted(pool, "arsenal:mana_steel_dagger", 7);
        addWeighted(pool, "arsenal:elixrite_dagger", 7);
        addWeighted(pool, "arsenal:astrite_dagger", 8);
        addWeighted(pool, "arsenal:lunarium_dagger", 8);
        addWeighted(pool, "arsenal:ignite_dagger", 9);
        addWeighted(pool, "arsenal:iridium_dagger", 9);
        addWeighted(pool, "arsenal:mythril_dagger", 10);
        addWeighted(pool, "arsenal:arcanium_dagger", 10);
        addWeighted(pool, "arsenal:prismatic_steel_dagger", 11);
        addWeighted(pool, "arsenal:mana_steel_gaundao", 7);
        addWeighted(pool, "arsenal:elixrite_gaundao", 7);
        addWeighted(pool, "arsenal:astrite_gaundao", 8);
        addWeighted(pool, "arsenal:lunarium_gaundao", 8);
        addWeighted(pool, "arsenal:ignite_gaundao", 9);
        addWeighted(pool, "arsenal:iridium_gaundao", 9);
        addWeighted(pool, "arsenal:mythril_gaundao", 10);
        addWeighted(pool, "arsenal:arcanium_gaundao", 10);
        addWeighted(pool, "arsenal:prismatic_steel_gaundao", 11);
        addWeighted(pool, "arsenal:mana_steel_glaive", 7);
        addWeighted(pool, "arsenal:elixrite_glaive", 7);
        addWeighted(pool, "arsenal:astrite_glaive", 8);
        addWeighted(pool, "arsenal:lunarium_glaive", 8);
        addWeighted(pool, "arsenal:ignite_glaive", 9);
        addWeighted(pool, "arsenal:iridium_glaive", 9);
        addWeighted(pool, "arsenal:mythril_glaive", 10);
        addWeighted(pool, "arsenal:arcanium_glaive", 10);
        addWeighted(pool, "arsenal:prismatic_steel_glaive", 11);
        addWeighted(pool, "arsenal:mana_steel_hammer", 7);
        addWeighted(pool, "arsenal:elixrite_hammer", 7);
        addWeighted(pool, "arsenal:astrite_hammer", 8);
        addWeighted(pool, "arsenal:lunarium_hammer", 8);
        addWeighted(pool, "arsenal:ignite_hammer", 9);
        addWeighted(pool, "arsenal:iridium_hammer", 9);
        addWeighted(pool, "arsenal:mythril_hammer", 10);
        addWeighted(pool, "arsenal:arcanium_hammer", 10);
        addWeighted(pool, "arsenal:prismatic_steel_hammer", 11);
        addWeighted(pool, "arsenal:mana_steel_longsword", 7);
        addWeighted(pool, "arsenal:elixrite_longsword", 7);
        addWeighted(pool, "arsenal:astrite_longsword", 8);
        addWeighted(pool, "arsenal:lunarium_longsword", 8);
        addWeighted(pool, "arsenal:ignite_longsword", 9);
        addWeighted(pool, "arsenal:iridium_longsword", 9);
        addWeighted(pool, "arsenal:mythril_longsword", 10);
        addWeighted(pool, "arsenal:arcanium_longsword", 10);
        addWeighted(pool, "arsenal:prismatic_steel_longsword", 11);
        addWeighted(pool, "arsenal:mana_steel_machete", 7);
        addWeighted(pool, "arsenal:elixrite_machete", 7);
        addWeighted(pool, "arsenal:astrite_machete", 8);
        addWeighted(pool, "arsenal:lunarium_machete", 8);
        addWeighted(pool, "arsenal:ignite_machete", 9);
        addWeighted(pool, "arsenal:iridium_machete", 9);
        addWeighted(pool, "arsenal:mythril_machete", 10);
        addWeighted(pool, "arsenal:arcanium_machete", 10);
        addWeighted(pool, "arsenal:prismatic_steel_machete", 11);

        addWeighted(pool, "gatewayexpansion:mana_steel_magnet", 2);
        addWeighted(pool, "gatewayexpansion:elixrite_magnet", 2);
        addWeighted(pool, "gatewayexpansion:astrite_magnet", 2);
        addWeighted(pool, "gatewayexpansion:lunarium_magnet", 2);
        addWeighted(pool, "gatewayexpansion:ignite_magnet", 2);
        addWeighted(pool, "gatewayexpansion:iridium_magnet", 2);
        addWeighted(pool, "gatewayexpansion:mythril_magnet", 2);
        addWeighted(pool, "gatewayexpansion:arcanium_magnet", 2);
        addWeighted(pool, "gatewayexpansion:prismatic_steel_magnet", 3);
        return List.copyOf(pool);
    }

    private static void addWeighted(List<WeightedItem> pool, String id, int weight) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null || weight <= 0) {
            return;
        }
        BuiltInRegistries.ITEM.getOptional(key).ifPresent(item -> pool.add(new WeightedItem(item, weight)));
    }

    private static Item pickWeightedWeapon(RandomSource random) {
        return pickWeightedWeapon(random, 100);
    }

    private static Item pickWeightedWeapon(RandomSource random, int playerLevel) {
        if (WEAPON_POOL.isEmpty()) {
            return Items.IRON_SWORD;
        }
        int safeLevel = Math.max(1, playerLevel);
        ArrayList<WeightedItem> eligible = new ArrayList<>();
        for (WeightedItem entry : WEAPON_POOL) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.item());
            int tier = weaponTier(id);
            if (safeLevel < 10) {
                if (tier == 1) {
                    eligible.add(new WeightedItem(entry.item(), Math.max(1, entry.weight() * 10)));
                } else if (tier == 2) {
                    eligible.add(new WeightedItem(entry.item(), Math.max(1, entry.weight() / 6)));
                }
                continue;
            }
            if (tier <= 2 && safeLevel < 20) {
                eligible.add(entry);
                continue;
            }
            if (tier <= 3 && safeLevel < 35) {
                eligible.add(entry);
                continue;
            }
            if (tier <= 4 && safeLevel < 50) {
                eligible.add(entry);
                continue;
            }
            if (tier <= 5 || safeLevel >= 50) {
                eligible.add(entry);
            }
        }
        if (eligible.isEmpty()) {
            eligible.addAll(WEAPON_POOL);
        }
        int totalWeight = 0;
        for (WeightedItem entry : eligible) totalWeight += entry.weight();
        int roll = random.nextInt(totalWeight);
        int cursor = 0;
        for (WeightedItem entry : eligible) {
            cursor += entry.weight();
            if (roll < cursor) {
                return entry.item();
            }
        }
        return eligible.getLast().item();
    }

    private static Item pickLoadoutWeapon(RandomSource random, int playerLevel, String kind) {
        if ("bow".equals(kind)) {
            return Items.BOW;
        }
        if ("crossbow".equals(kind)) {
            return Items.CROSSBOW;
        }
        if ("axe".equals(kind)) {
            return pickWeaponByPath(random, playerLevel, "paxel");
        }
        return pickWeaponByPath(random, playerLevel, kind);
    }

    private static Item pickWeaponByPath(RandomSource random, int playerLevel, String pathNeedle) {
        int safeLevel = Math.max(1, playerLevel);
        ArrayList<WeightedItem> eligible = new ArrayList<>();
        for (WeightedItem entry : WEAPON_POOL) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.item());
            if (id == null || !id.getPath().contains(pathNeedle)) {
                continue;
            }
            int tier = weaponTier(id);
            if ((safeLevel < 10 && tier > 2) || (safeLevel < 20 && tier > 2) || (safeLevel < 35 && tier > 3) || (safeLevel < 50 && tier > 4)) {
                continue;
            }
            int weight = Math.max(1, entry.weight());
            if (safeLevel < 10 && tier == 1) {
                weight *= 10;
            } else if (safeLevel < 10 && tier == 2) {
                weight = Math.max(1, weight / 6);
            }
            eligible.add(new WeightedItem(entry.item(), weight));
        }
        if (eligible.isEmpty()) {
            return pickWeightedWeapon(random, playerLevel);
        }
        int totalWeight = 0;
        for (WeightedItem entry : eligible) totalWeight += entry.weight();
        int roll = random.nextInt(totalWeight);
        int cursor = 0;
        for (WeightedItem entry : eligible) {
            cursor += entry.weight();
            if (roll < cursor) {
                return entry.item();
            }
        }
        return eligible.getLast().item();
    }

    private static int weaponTier(ResourceLocation id) {
        if (id == null) return 1;
        String p = id.getPath();
        if (p.contains("mana_steel")) return 1;
        if (p.contains("elixrite")) return 2;
        if (p.contains("astrite") || p.contains("lunarium")) return 3;
        if (p.contains("ignite") || p.contains("iridium")) return 4;
        return 5;
    }

    private static String shortName(Item item) {
        return new ItemStack(item).getHoverName().getString();
    }

    private static ItemStack pickLeveledMagnet(RandomSource random, int playerLevel) {
        int level = Math.max(1, playerLevel);
        Item item;
        if (level < 15) {
            item = resolveItem("gatewayexpansion:mana_steel_magnet");
        } else if (level < 30) {
            item = random.nextFloat() < 0.8F ? resolveItem("gatewayexpansion:mana_steel_magnet") : resolveItem("gatewayexpansion:elixrite_magnet");
        } else if (level < 45) {
            item = random.nextBoolean() ? resolveItem("gatewayexpansion:elixrite_magnet") : resolveItem("gatewayexpansion:astrite_magnet");
        } else if (level < 60) {
            item = random.nextBoolean() ? resolveItem("gatewayexpansion:lunarium_magnet") : resolveItem("gatewayexpansion:ignite_magnet");
        } else if (level < 75) {
            item = random.nextBoolean() ? resolveItem("gatewayexpansion:iridium_magnet") : resolveItem("gatewayexpansion:mythril_magnet");
        } else if (level < 90) {
            item = resolveItem("gatewayexpansion:arcanium_magnet");
        } else {
            item = resolveItem("gatewayexpansion:prismatic_steel_magnet");
        }
        if (item == null) {
            item = resolveItem("gatewayexpansion:mana_steel_magnet");
        }
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static Item resolveItem(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(key);
    }

    private static int getEffectivePlayerLevel(ServerPlayer player) {
        return LevelUpIntegration.getEffectiveLevel(player);
    }

    private static void applyLoadout(ServerPlayer player, int loadoutId) {
        clearForDungeon(player);
        switch (loadoutId) {
            case 0 -> equipVanguard(player);
            case 1 -> equipRanger(player);
            default -> equipSpellblade(player);
        }
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void equipVanguard(ServerPlayer player) {
        player.getInventory().add(new ItemStack(Items.IRON_SWORD));
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        equipArmorSet(player, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
    }

    private static void equipRanger(ServerPlayer player) {
        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(player.registryAccess().holderOrThrow(Enchantments.POWER), 1);
        player.getInventory().add(bow);
        player.getInventory().add(new ItemStack(Items.ARROW, 48));
        player.getInventory().add(new ItemStack(Items.STONE_SWORD));
        equipArmorSet(player, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
    }

    private static void equipSpellblade(ServerPlayer player) {
        player.getInventory().add(new ItemStack(Items.TRIDENT));
        player.getInventory().add(new ItemStack(Items.IRON_AXE));
        equipArmorSet(player, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
    }

    private static void equipArmorSet(ServerPlayer player, Item helmet, Item chest, Item legs, Item boots) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmet));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
    }

    private static RunState getRunForPlayer(Player player) {
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        return ownerId == null ? null : RUNS_BY_OWNER.get(ownerId);
    }

    private static RunState findRunByTrackedMob(UUID mobId) {
        for (RunState run : RUNS_BY_OWNER.values()) if (run.aliveMobs.contains(mobId)) return run;
        return null;
    }

    private static void spawnDungeonMobDrops(ServerLevel level, LivingEntity dead, RunState run) {
        int avgLevel = averageParticipantLevel(run);
        int wave = Math.max(1, run.waveNumber);
        int rolls = Math.max(1, 1 + wave / 3 + (int) Math.floor(run.quantityBonusModifier));
        for (int i = 0; i < rolls; i++) {
            Item item = pickScaledDrop(run, avgLevel, level.random);
            dead.spawnAtLocation(new ItemStack(item, 1 + level.random.nextInt(2) + Math.max(0, wave / 8)));
        }
        int coinValue = 2 + wave * 2 + avgLevel / 8 + (int) Math.floor(run.quantityBonusModifier * 3.0D);
        for (int i = 0; i < 2 + wave / 5; i++) {
            dead.spawnAtLocation(new ItemStack(ModItems.MYTHIC_COIN.get(), Math.max(1, coinValue / Math.max(1, 2 + wave / 5))));
        }
    }

    private static int computeWaveLootRolls(RunState run, int avgLevel, RandomSource random) {
        int base = ProgressionSystem.dungeonLootRolls(avgLevel, Math.max(1, run.waveNumber), run.extraRewardRolls, run.quantityBonusModifier);
        return base + random.nextInt(2 + Math.max(1, Math.max(1, run.waveNumber) / 6));
    }

    private static Item pickScaledDrop(RunState run, int avgLevel, RandomSource random) {
        double rarityRoll = random.nextDouble();
        double waveFactor = Math.min(0.30D, run.waveNumber * 0.012D);
        double levelFactor = Math.min(0.20D, avgLevel / 500.0D);
        double rarityChanceBonus = Math.max(0.0D, run.rarityBonusModifier);
        double rareChance = 0.04D + waveFactor + levelFactor + rarityChanceBonus;
        double uncommonChance = 0.22D + waveFactor * 0.8D + levelFactor * 0.6D + rarityChanceBonus * 0.6D;
        if (rarityRoll < rareChance) {
            return RARE_DROP_POOL.get(random.nextInt(RARE_DROP_POOL.size()));
        }
        if (rarityRoll < rareChance + uncommonChance) {
            return UNCOMMON_DROP_POOL.get(random.nextInt(UNCOMMON_DROP_POOL.size()));
        }
        return COMMON_DROP_POOL.get(random.nextInt(COMMON_DROP_POOL.size()));
    }

    private static int averageParticipantLevel(RunState run) {
        List<ServerPlayer> players = run.liveParticipants();
        if (players.isEmpty()) {
            return 1;
        }
        int total = 0;
        for (ServerPlayer player : players) {
            total += getEffectivePlayerLevel(player);
        }
        return Math.max(1, total / players.size());
    }

    private static void trackDungeonKillProgress(RunState run, LivingEntity dead) {
        LivingEntity killer = dead.getKillCredit();
        if (!(killer instanceof ServerPlayer serverPlayer) || !run.participants.contains(serverPlayer.getUUID())) {
            return;
        }
        double healthScore = Math.max(1.0D, dead.getMaxHealth()) * 0.08D;
        double damageScore = 1.0D;
        if (dead.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            damageScore += Math.max(0.0D, dead.getAttribute(Attributes.ATTACK_DAMAGE).getValue()) * 1.25D;
        }
        double killScore = healthScore + damageScore;
        run.killScoreByPlayer.merge(serverPlayer.getUUID(), killScore, Double::sum);
    }

    private static int awardDungeonExitProgression(ServerPlayer player, RunState run) {
        if (!run.awardedExitXp.add(player.getUUID())) {
            return 0;
        }
        double score = run.killScoreByPlayer.getOrDefault(player.getUUID(), 0.0D);
        int xp = ProgressionSystem.levelXpFromDungeonKillScore(score);
        LevelUpIntegration.awardXp(player, xp, DUNGEON_EXIT_XP_SOURCE);
        run.experienceEarned += xp;
        return xp;
    }

    private static EntityType<?> pickEntityType(RandomSource random, EnemyPoolSet pools, WaveArchetype archetype) {
        return switch (archetype) {
            case UNDEAD, HORDE -> pools.pick(random, EnemyPoolRole.HOARD, EnemyPoolRole.TANK);
            case ASSASSIN -> pools.pick(random, EnemyPoolRole.ASSASSIN, EnemyPoolRole.HOARD);
            case ARCHER -> pools.pick(random, EnemyPoolRole.ARCHER, EnemyPoolRole.HOARD);
            case TANK -> pools.pick(random, EnemyPoolRole.TANK, EnemyPoolRole.HOARD);
        };
    }

    private static Map<WaveArchetype, EnemyPoolSet> buildWavePools(RunState run) {
        Map<WaveArchetype, EnemyPoolSet> pools = new HashMap<>();
        int level = Math.max(1, 10 + run.waveNumber * 2);
        for (WaveArchetype archetype : run.unlockedArchetypes) {
            CrystalTheme theme = switch (archetype) {
                case UNDEAD -> CrystalTheme.UNDEAD;
                case HORDE, TANK -> CrystalTheme.BEAST;
                case ASSASSIN, ARCHER -> CrystalTheme.RAIDER;
            };
            pools.put(archetype, EnemyPoolRegistry.create(theme, level));
        }
        return pools;
    }

    private static void applyMobScaling(LivingEntity mob, double damageMultiplier, double speedMultiplier) {
        removeMobModifiers(mob);
        addMobModifier(mob, Attributes.MAX_HEALTH, MOB_HEALTH_MODIFIER_ID, 0.15D);
        addMobModifier(mob, Attributes.ATTACK_DAMAGE, MOB_DAMAGE_MODIFIER_ID, Math.max(0.0D, damageMultiplier - 1.0D));
        addMobModifier(mob, Attributes.MOVEMENT_SPEED, MOB_SPEED_MODIFIER_ID, Math.max(0.0D, speedMultiplier - 1.0D));
        mob.setHealth(mob.getMaxHealth());
    }

    private static boolean rollElite(RunState run, RandomSource random) {
        double chance = Math.max(0.0D, Math.min(0.85D, BASE_ELITE_CHANCE + run.eliteChanceBonus));
        return random.nextDouble() < chance;
    }

    private static void applyEliteVariant(LivingEntity mob) {
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                    new AttributeModifier(ResourceLocation.fromNamespaceAndPath("gatesofavarice", "elite_health_x2"), 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                    new AttributeModifier(ResourceLocation.fromNamespaceAndPath("gatesofavarice", "elite_damage_x2"), 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.setHealth(mob.getMaxHealth());
        MutableComponent icon = Component.literal("◆ ELITE ◆").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        mob.setCustomName(icon);
        mob.setCustomNameVisible(true);
    }

    private static void removeMobModifiers(LivingEntity mob) {
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) mob.getAttribute(Attributes.MAX_HEALTH).removeModifier(MOB_HEALTH_MODIFIER_ID);
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) mob.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MOB_DAMAGE_MODIFIER_ID);
        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) mob.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOB_SPEED_MODIFIER_ID);
    }

    private static void addMobModifier(LivingEntity mob, Holder<Attribute> attribute, ResourceLocation id, double value) {
        if (value <= 0.0D || mob.getAttribute(attribute) == null) return;
        mob.getAttribute(attribute).addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void openWaveMenu(ServerPlayer owner, RunState run) {
        List<DungeonWaveMenu.WaveOptionView> views;
        if (run.phase == RunPhase.SELECTING_TAROT) {
            views = run.tarotOptions.stream().map(option -> new DungeonWaveMenu.WaveOptionView(option.title, option.details, 100, 100, ItemStack.EMPTY, ItemStack.EMPTY)).toList();
        } else if (run.selectingLoadout) {
            views = run.loadoutOptions.stream().map(option -> new DungeonWaveMenu.WaveOptionView(option.title, option.details, 100, 100, option.primary().copy(), option.secondary().copy())).toList();
        } else {
            views = run.lootOptions.stream().map(option -> new DungeonWaveMenu.WaveOptionView(option.title, option.details, 100, 100, option.stack().copy(), ItemStack.EMPTY)).toList();
        }
        int rerollsLeft = Math.max(0, MAX_REROLLS - run.rerollsUsed);
        int rerollCost = rerollsLeft <= 0 ? 0 : BASE_REROLL_COST << run.rerollsUsed;
        int stage = run.phase == RunPhase.SELECTING_TAROT ? 0 : (run.selectingLoadout ? 2 : 1);
        List<Component> changes = runChangeSummary(run);
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new DungeonWaveMenu(containerId, inventory, run.ownerId, run.waveNumber + 1, true, stage, rerollsLeft, rerollCost, views, changes),
                Component.translatable("screen.gatesofavarice.dungeon_wave.title", run.waveNumber + 1)
        );
        owner.openMenu(provider, buffer -> DungeonWaveMenu.writePayload(buffer, run.ownerId, run.waveNumber + 1, true, stage, rerollsLeft, rerollCost, views, changes));
    }

    private static List<Component> runChangeSummary(RunState run) {
        return List.of(
                Component.literal(String.format(java.util.Locale.ROOT, "+spawn chance %.0f%%", Math.max(0.0D, (run.enemyCountMultiplier - 1.0D) * 100.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+mob speed %.0f%%", Math.max(0.0D, (run.speedMultiplier - 1.0D) * 100.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+mob health %.0f%%", Math.max(0.0D, (run.enemyCountMultiplier - 1.0D) * 60.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+mob damage %.0f%%", Math.max(0.0D, (run.damageMultiplier - 1.0D) * 100.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+elite spawns %.0f%%", run.eliteChanceBonus * 100.0D)),
                Component.literal(String.format(java.util.Locale.ROOT, "+quantity %.0f%%", run.quantityBonusModifier * 100.0D)),
                Component.literal(String.format(java.util.Locale.ROOT, "+rarity %.0f%%", run.rarityBonusModifier * 100.0D)),
                Component.literal(String.format(java.util.Locale.ROOT, "+coins %.0f%%", Math.max(0.0D, (run.quantityBonusModifier * 0.5D + run.rarityBonusModifier * 0.5D) * 100.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+xp %.0f%%", Math.max(0.0D, run.quantityBonusModifier * 100.0D))),
                Component.literal(String.format(java.util.Locale.ROOT, "+levels %.0f%%", Math.max(0.0D, run.rarityBonusModifier * 100.0D)))
        );
    }

    private static void syncHudToPlayer(ServerPlayer player, boolean active, int wave, int remaining, int total) {
        PacketDistributor.sendToPlayer(player, new DungeonWaveHudPayload(active, wave, remaining, total));
    }

    private static void finishAndCleanup(RunState run) {
        syncHud(run, false);
        closeOverworldEntryPortals(run.server, run.ownerId);
        ServerLevel dungeon = getDungeonLevel(run);
        if (run.shopkeeperId >= 0) {
            if (dungeon != null) {
                Entity shop = dungeon.getEntity(run.shopkeeperId);
                if (shop != null) shop.discard();
            }
        }
        if (run.exitPortalId >= 0) {
            if (dungeon != null) {
                Entity portal = dungeon.getEntity(run.exitPortalId);
                if (portal != null) portal.discard();
            }
        }
        DungeonInstanceManager.cleanupInstance(run.ownerId, dungeon);
        RUNS_BY_OWNER.remove(run.ownerId);
    }

    private static void closeOverworldEntryPortals(MinecraftServer server, UUID ownerId) {
        if (server == null || ownerId == null) {
            return;
        }
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }
        for (GatewayCrystalEntity gateway : overworld.getEntitiesOfClass(
                GatewayCrystalEntity.class,
                new net.minecraft.world.phys.AABB(-30000000, -1024, -30000000, 30000000, 2048, 30000000),
                entity -> !entity.isReturnPortal() && ownerId.equals(entity.getOwnerId()))) {
            gateway.discard();
        }
    }

    private static void closeNearbyEntryPortals(MinecraftServer server, ResourceKey<Level> dimension, BlockPos center, UUID ownerId) {
        if (server == null || center == null || dimension == null) {
            return;
        }
        ServerLevel level = server.getLevel(dimension);
        if (level == null) {
            return;
        }
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                center.getX() - 24, center.getY() - 12, center.getZ() - 24,
                center.getX() + 24, center.getY() + 24, center.getZ() + 24);
        for (GatewayCrystalEntity gateway : level.getEntitiesOfClass(
                GatewayCrystalEntity.class,
                area,
                entity -> !entity.isReturnPortal() && (ownerId.equals(entity.getOwnerId()) || entity.getOwnerId() == null))) {
            gateway.discard();
        }
    }

    private static ServerLevel getDungeonLevel(RunState run) {
        return run.server == null ? null : run.server.getLevel(ModDimensions.DUNGEON_LEVEL);
    }

    private static void clearForDungeon(ServerPlayer player) {
        player.getInventory().clearContent();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static List<ItemStack> collectDungeonRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();
        collectDungeonRewards(player.getInventory().items, rewards);
        collectDungeonRewards(player.getInventory().armor, rewards);
        collectDungeonRewards(player.getInventory().offhand, rewards);
        return List.copyOf(rewards);
    }

    private static void collectDungeonRewards(List<ItemStack> source, List<ItemStack> rewards) {
        for (ItemStack stack : source) {
            if (!stack.isEmpty() && !DungeonBoundItems.isDungeonBound(stack)) {
                rewards.add(stack.copy());
            }
        }
    }

    private static ItemStack createLootboxFromRewards(ServerPlayer player, List<ItemStack> rewards) {
        ItemStack lootbox = new ItemStack(ModItems.LOOTBOX.get());
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (ItemStack stack : rewards) {
            if (!stack.isEmpty()) {
                list.add(stack.saveOptional(player.registryAccess()));
            }
        }
        if (list.isEmpty()) return ItemStack.EMPTY;
        net.minecraft.nbt.CompoundTag all = lootbox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        net.minecraft.nbt.CompoundTag root = all.getCompound("gatesofavarice");
        root.put("lootbox_loot", list);
        all.put("gatesofavarice", root);
        lootbox.set(DataComponents.CUSTOM_DATA, CustomData.of(all));
        return lootbox;
    }

    private static void restoreSnapshot(ServerPlayer player, PlayerSnapshot snapshot) {
        for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().items.set(i, snapshot.items.get(i).copy());
        for (int i = 0; i < player.getInventory().armor.size(); i++) player.getInventory().armor.set(i, snapshot.armor.get(i).copy());
        for (int i = 0; i < player.getInventory().offhand.size(); i++) player.getInventory().offhand.set(i, snapshot.offhand.get(i).copy());
        player.getInventory().selected = snapshot.selectedSlot;
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static int cashoutRemainingCoinsOnDungeonExit(ServerPlayer player) {
        int coins = MythicCoinWallet.get(player);
        if (coins <= 0) {
            return 0;
        }
        MythicCoinWallet.set(player, 0);
        long xp = Math.max(1L, coins);
        if (!LevelUpIntegration.awardXp(player, xp, DUNGEON_EXIT_COIN_CASHOUT_SOURCE)) {
            player.giveExperienceLevels(Math.max(1, coins / 100));
        }
        return coins;
    }

    private static void sendCompletionScreen(ServerPlayer player, RunState run, List<ItemStack> rewards, int levelPoints, int cashedOutCoins) {
        long now = player.serverLevel().getGameTime();
        long elapsedTicks = run.runStartGameTime < 0L ? 0L : Math.max(0L, now - run.runStartGameTime);
        run.experienceEarned += cashedOutCoins;
        PacketDistributor.sendToPlayer(player, new DungeonCompletePayload(
                Math.max(0, run.waveNumber),
                elapsedTicks,
                levelPoints,
                cashedOutCoins,
                run.mobsKilled,
                Math.round(run.damageDealt),
                Math.round(run.damageReceived),
                levelPoints + cashedOutCoins,
                (int) Math.round(run.rarityBonusModifier * 100.0D),
                (int) Math.round(run.quantityBonusModifier * 100.0D),
                (int) Math.round(Math.max(0.0D, (run.enemyCountMultiplier - 1.0D) * 60.0D)),
                (int) Math.round(Math.max(0.0D, (run.damageMultiplier - 1.0D) * 100.0D)),
                rewards
        ));
    }

    private static void syncHud(RunState run, boolean active) {
        int remaining = Math.max(0, run.toSpawn + run.aliveMobs.size());
        int total = Math.max(1, run.waveTotalMobs);
        DungeonWaveHudPayload payload = new DungeonWaveHudPayload(active, run.waveNumber, remaining, total);
        for (ServerPlayer participant : run.liveParticipants()) PacketDistributor.sendToPlayer(participant, payload);
    }

    private enum RunPhase { SELECTING_TAROT, SELECTING_LOOT, IN_WAVE, SHOP, WAITING_EXIT }

    private enum WaveArchetype { UNDEAD, HORDE, ASSASSIN, ARCHER, TANK }

    private static final class RunState {
        private final UUID ownerId;
        private MinecraftServer server;
        private final Set<UUID> participants = new HashSet<>();
        private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();
        private RunPhase phase = RunPhase.SELECTING_TAROT;
        private int waveNumber = 0;
        private Set<UUID> aliveMobs = new HashSet<>();
        private int toSpawn = 0;
        private int waveTotalMobs = 0;
        private int spawnCooldown = 0;
        private int exitPortalId = -1;
        private int shopkeeperId = -1;
        private List<WaveArchetype> unlockedArchetypes = new ArrayList<>(List.of(WaveArchetype.UNDEAD));
        private Map<WaveArchetype, EnemyPoolSet> currentWavePools = new HashMap<>();
        private List<TarotOption> tarotOptions = List.of();
        private List<LootOption> lootOptions = List.of();
        private List<LoadoutOption> loadoutOptions = List.of();
        private boolean selectingLoadout = false;
        private int rerollsUsed = 0;
        private double enemyCountMultiplier = 1.0D;
        private double damageMultiplier = 1.0D;
        private double speedMultiplier = 1.0D;
        private double eliteChanceBonus = 0.0D;
        private double quantityBonusModifier = 0.0D;
        private double rarityBonusModifier = 0.0D;
        private int extraRewardRolls = 0;
        private long runStartGameTime = -1L;
        private final Map<UUID, Double> killScoreByPlayer = new HashMap<>();
        private final Set<UUID> awardedExitXp = new HashSet<>();
        private int mobsKilled = 0;
        private int coinsEarned = 0;
        private int experienceEarned = 0;
        private float damageDealt = 0.0F;
        private float damageReceived = 0.0F;

        private RunState(UUID ownerId) {
            this.ownerId = ownerId;
            this.participants.add(ownerId);
        }

        private ServerPlayer online(UUID playerId) {
            return this.server == null ? null : this.server.getPlayerList().getPlayer(playerId);
        }

        private List<ServerPlayer> liveParticipants() {
            ArrayList<ServerPlayer> players = new ArrayList<>();
            for (UUID uuid : this.participants) {
                ServerPlayer player = this.online(uuid);
                if (player != null && player.isAlive() && player.level().dimension() == ModDimensions.DUNGEON_LEVEL) players.add(player);
            }
            return players;
        }
    }

    private static final class PlayerSnapshot {
        private final ResourceKey<Level> dimension;
        private final BlockPos returnPos;
        private final float yaw;
        private final float pitch;
        private final List<ItemStack> items;
        private final List<ItemStack> armor;
        private final List<ItemStack> offhand;
        private final int selectedSlot;

        private PlayerSnapshot(ResourceKey<Level> dimension, BlockPos returnPos, float yaw, float pitch, List<ItemStack> items, List<ItemStack> armor, List<ItemStack> offhand, int selectedSlot) {
            this.dimension = dimension;
            this.returnPos = returnPos;
            this.yaw = yaw;
            this.pitch = pitch;
            this.items = items;
            this.armor = armor;
            this.offhand = offhand;
            this.selectedSlot = selectedSlot;
        }

        private static PlayerSnapshot capture(ServerPlayer player) {
            return new PlayerSnapshot(
                    player.level().dimension(),
                    player.blockPosition(),
                    player.getYRot(),
                    player.getXRot(),
                    copyStacks(player.getInventory().items),
                    copyStacks(player.getInventory().armor),
                    copyStacks(player.getInventory().offhand),
                    player.getInventory().selected
            );
        }

        private static List<ItemStack> copyStacks(List<ItemStack> source) {
            ArrayList<ItemStack> copied = new ArrayList<>(source.size());
            for (ItemStack stack : source) copied.add(stack.copy());
            return copied;
        }
    }

    private record PendingDeathRestore(PlayerSnapshot snapshot) {
    }

    private static final class TarotOption {
        private final Component title;
        private final Component details;
        private final double enemyCountBonus;
        private final double damageBonus;
        private final double speedBonus;
        private final double eliteChanceBonus;
        private final double quantityBonus;
        private final double rarityBonus;
        private final int rewardRollBonus;
        private final WaveArchetype unlockArchetype;

        private TarotOption(Component title, Component details, double enemyCountBonus, double damageBonus, double speedBonus, double eliteChanceBonus, double quantityBonus,
                double rarityBonus,
                int rewardRollBonus, WaveArchetype unlockArchetype) {
            this.title = title;
            this.details = details;
            this.enemyCountBonus = enemyCountBonus;
            this.damageBonus = damageBonus;
            this.speedBonus = speedBonus;
            this.eliteChanceBonus = eliteChanceBonus;
            this.quantityBonus = quantityBonus;
            this.rarityBonus = rarityBonus;
            this.rewardRollBonus = rewardRollBonus;
            this.unlockArchetype = unlockArchetype;
        }

        private static TarotOption random(RandomSource random) {
            int enemyLines = 1 + random.nextInt(3);
            int combatLines = 1 + random.nextInt(3);
            int rewardLines = 1 + random.nextInt(3);
            double scaleEnemies = switch (enemyLines) { case 1 -> 1.8D; case 2 -> 1.2D; default -> 0.85D; };
            double scaleCombat = switch (combatLines) { case 1 -> 1.8D; case 2 -> 1.2D; default -> 0.85D; };
            double scaleRewards = switch (rewardLines) { case 1 -> 1.8D; case 2 -> 1.2D; default -> 0.85D; };
            WaveArchetype[] values = WaveArchetype.values();
            WaveArchetype archetype = values[random.nextInt(values.length)];
            double enemyBonus = 0.0D;
            double damage = 0.0D;
            double speed = 0.0D;
            double eliteChance = 0.0D;
            double quantity = 0.0D;
            double rarity = 0.0D;
            int rewards = 0;
            ArrayList<String> lines = new ArrayList<>();
            ArrayList<String> enemyPool = new ArrayList<>(List.of("+hoard mobs", "+tank", "+archers", "+assassins", "+elite spawns"));
            for (int i = 0; i < enemyLines && !enemyPool.isEmpty(); i++) {
                String type = enemyPool.remove(random.nextInt(enemyPool.size()));
                int value = Math.max(1, (int) Math.round((2 + random.nextInt(5)) * scaleEnemies));
                lines.add("+" + value + " " + type.substring(1));
                enemyBonus += value * 0.025D;
                if (type.contains("elite")) eliteChance += value / 100.0D;
            }
            lines.add("-------------------");
            ArrayList<String> combatPool = new ArrayList<>(List.of("mob speed", "mob health", "mob damage", "mob resistance", "mob regen"));
            for (int i = 0; i < combatLines && !combatPool.isEmpty(); i++) {
                String type = combatPool.remove(random.nextInt(combatPool.size()));
                int value = Math.max(1, (int) Math.round((4 + random.nextInt(9)) * scaleCombat));
                if ("mob regen".equals(type)) value = Math.max(1, Math.min(2, value / 6));
                lines.add("+" + value + "% " + type);
                if (type.contains("speed")) speed += value / 100.0D;
                if (type.contains("damage")) damage += value / 100.0D;
                if (type.contains("health")) enemyBonus += value / 200.0D;
            }
            lines.add("-------------------");
            ArrayList<String> rewardPool = new ArrayList<>(List.of("coins", "levels", "quantity", "rarity", "xp"));
            for (int i = 0; i < rewardLines && !rewardPool.isEmpty(); i++) {
                String type = rewardPool.remove(random.nextInt(rewardPool.size()));
                int value = Math.max(1, (int) Math.round((3 + random.nextInt(8)) * scaleRewards));
                lines.add("+" + value + "% " + type);
                if (type.contains("quantity") || type.contains("coins")) quantity += value / 100.0D;
                if (type.contains("rarity") || type.contains("levels") || type.equals("xp")) rarity += value / 100.0D;
                if (type.contains("levels")) rewards += Math.max(1, value / 5);
            }
            return new TarotOption(Component.empty(), Component.literal(String.join("\n", lines)), enemyBonus, damage, speed, eliteChance, quantity, rarity, rewards, archetype);
        }
    }

    private static final class LootOption {
        private final Component title;
        private final Component details;
        private final ItemStack stack;

        private LootOption(Component title, Component details, ItemStack stack) {
            this.title = title;
            this.details = details;
            this.stack = stack;
        }

        private ItemStack stack() { return this.stack; }

        private static LootOption weapon(RandomSource random, int playerLevel) {
            ItemStack stack = new ItemStack(pickWeightedWeapon(random, playerLevel));
            String name = stack.getHoverName().getString();
            return new LootOption(Component.literal("Weapon"), Component.literal(name), stack);
        }

        private static LootOption armor(RandomSource random) {
            ItemStack stack = switch (random.nextInt(4)) {
                case 0 -> new ItemStack(Items.IRON_HELMET);
                case 1 -> new ItemStack(Items.IRON_CHESTPLATE);
                case 2 -> new ItemStack(Items.IRON_LEGGINGS);
                default -> new ItemStack(Items.IRON_BOOTS);
            };
            return new LootOption(Component.literal("Armor"), Component.literal(stack.getHoverName().getString()), stack);
        }

        private static LootOption buff(RandomSource random) {
            ItemStack stack = random.nextBoolean() ? new ItemStack(Items.GOLDEN_APPLE, 16) : new ItemStack(ModItems.ARCANE_APPLE.get(), 8);
            String name = stack.getHoverName().getString() + " x" + stack.getCount();
            return new LootOption(Component.literal("Item"), Component.literal(name), stack);
        }

        private static LootOption coins(RandomSource random, int playerLevel) {
            int base = 100 + Math.max(1, playerLevel) * 8;
            int variance = 20 + random.nextInt(81);
            int value = base + variance;
            ItemStack stack = MythicCoinStackData.createStack(value);
            return new LootOption(Component.literal("Item Upgrade"), Component.literal("Mythic Coins x" + value), stack);
        }

        private static LootOption magnet(RandomSource random, int playerLevel) {
            ItemStack stack = pickLeveledMagnet(random, playerLevel);
            if (stack.isEmpty()) {
                return buff(random);
            }
            String name = stack.getHoverName().getString();
            return new LootOption(Component.literal("Item Upgrade"), Component.literal(name), stack);
        }

        private static LootOption mystery(RandomSource random, int playerLevel) {
            return switch (random.nextInt(5)) {
                case 0 -> weapon(random, playerLevel);
                case 1 -> armor(random);
                case 2 -> buff(random);
                case 3 -> magnet(random, playerLevel);
                default -> coins(random, playerLevel);
            };
        }
    }

    private record WeightedItem(Item item, int weight) {
    }

    private record LoadoutOption(
            Component title,
            Component details,
            ItemStack head,
            ItemStack chest,
            ItemStack legs,
            ItemStack feet,
            ItemStack primary,
            ItemStack secondary,
            ItemStack utility,
            List<ItemStack> food
    ) {
    }

    private record LoadoutDefinition(
            String name,
            String primaryKind,
            String secondaryKind,
            String armorName,
            Item head,
            Item chest,
            Item legs,
            Item feet,
            List<String> traits,
            List<ItemStack> food
    ) {
        private String foodSummary() {
            ArrayList<String> parts = new ArrayList<>();
            for (ItemStack stack : this.food) {
                parts.add(stack.getHoverName().getString() + " x" + stack.getCount());
            }
            return String.join(", ", parts);
        }
    }
}
