package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.currency.MythicCoinWallet;
import com.revilo.gatesofavarice.entity.GatewayCrystalEntity;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRegistry;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRole;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolSet;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
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
import net.minecraft.network.chat.Component;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DungeonRunManager {

    private static final Map<UUID, RunState> RUNS_BY_OWNER = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_TO_OWNER = new HashMap<>();

    private static final ResourceLocation MOB_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_health");
    private static final ResourceLocation MOB_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_damage");
    private static final ResourceLocation MOB_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_speed");
    private static final int MAX_REROLLS = 2;
    private static final int BASE_REROLL_COST = 100;

    private static final List<Item> REGULAR_DROP_POOL = List.of(
            ModItems.GRIMSTONE.get(), ModItems.MYSTIC_ESSENCE.get(), ModItems.SCRAP_METAL.get(), ModItems.MANA_GEMS.get(),
            ModItems.MANA_STEEL_SCRAP.get(), ModItems.MAGNETITE_SCRAP.get(), ModItems.ARCANE_ESSENCE.get(), ModItems.MANASTONES.get(),
            ModItems.ELIXRITE_SCRAP.get(), ModItems.ASTRITE_SCRAP.get(), ModItems.SOLAR_SHARD.get(), ModItems.DARK_ESSENCE.get(),
            ModItems.RUSTY_COIN.get(), ModItems.HARDENED_FLESH.get()
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

    private DungeonRunManager() {}

    public static void enterFromGateway(ServerPlayer player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.computeIfAbsent(ownerId, RunState::new);
        run.server = player.server;
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
        ItemStack lootbox = createLootboxFromDungeonInventory(player);
        restoreSnapshot(player, snapshot);
        if (!lootbox.isEmpty() && !player.getInventory().add(lootbox)) player.drop(lootbox, false);
        DungeonInstanceManager.teleportToSavedLocation(player, snapshot.dimension, snapshot.returnPos, snapshot.yaw, snapshot.pitch);
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
            rollLootOptions(run, serverPlayer.serverLevel().random);
            run.phase = RunPhase.SELECTING_LOOT;
            openWaveMenu(serverPlayer, run);
            return true;
        }

        if (buttonId == DungeonWaveMenu.REROLL_BUTTON_ID) {
            if (run.rerollsUsed >= MAX_REROLLS) return false;
            int cost = BASE_REROLL_COST << run.rerollsUsed;
            if (!MythicCoinWallet.spend(serverPlayer, cost)) return false;
            run.rerollsUsed++;
            rollLootOptions(run, serverPlayer.serverLevel().random);
            openWaveMenu(serverPlayer, run);
            return true;
        }
        if (buttonId == DungeonWaveMenu.SKIP_BUTTON_ID) {
            startWave(run);
            serverPlayer.closeContainer();
            return true;
        }
        if (buttonId < 0 || buttonId >= run.lootOptions.size()) return false;
        grantLoot(serverPlayer, run.lootOptions.get(buttonId), serverPlayer.serverLevel().random);
        startWave(run);
        serverPlayer.closeContainer();
        return true;
    }

    public static boolean isWaveMenuValid(Player player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.get(ownerId);
        return run != null && run.participants.contains(player.getUUID());
    }

    public static boolean isPlayerInActiveRun(Player player) {
        return PLAYER_TO_OWNER.containsKey(player.getUUID()) && RUNS_BY_OWNER.containsKey(PLAYER_TO_OWNER.get(player.getUUID()));
    }

    public static int recoverStalledShops(MinecraftServer server) { return 0; }
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
    public static boolean handleLoadoutMenuClick(Player player, UUID ownerId, int buttonId) { return false; }
    public static boolean isLoadoutMenuValid(Player player, UUID ownerId) { return false; }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RUNS_BY_OWNER.isEmpty()) return;
        ArrayList<UUID> empty = new ArrayList<>();
        for (RunState run : RUNS_BY_OWNER.values()) {
            ServerLevel dungeon = event.getServer().getLevel(ModDimensions.DUNGEON_LEVEL);
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
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID ownerId = PLAYER_TO_OWNER.remove(player.getUUID());
            if (ownerId == null) return;
            RunState run = RUNS_BY_OWNER.get(ownerId);
            if (run == null) return;
            run.participants.remove(player.getUUID());
            if (run.liveParticipants().isEmpty()) {
                finishAndCleanup(run);
            }
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity dead) || dead.level().isClientSide) return;
        RunState run = findRunByTrackedMob(dead.getUUID());
        if (run == null) return;
        run.aliveMobs.remove(dead.getUUID());
        if (dead.level() instanceof ServerLevel level) spawnDungeonMobDrops(level, dead, run.waveNumber);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID ownerId = PLAYER_TO_OWNER.remove(player.getUUID());
        if (ownerId == null) return;
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run != null) {
            run.participants.remove(player.getUUID());
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
        int extraPlayers = Math.max(0, run.liveParticipants().size() - 1);
        int baseCount = 6 + run.waveNumber * 2 + extraPlayers * 3;
        run.toSpawn = Math.max(4, (int) Math.round(baseCount * run.enemyCountMultiplier));
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
        for (ServerPlayer player : run.liveParticipants()) MythicCoinWallet.addRaw(player, 1);
        spawnWaveLootBurst(run, level, 3 + level.random.nextInt(3));
        for (ServerPlayer player : run.liveParticipants()) {
            player.setHealth(player.getMaxHealth());
        }
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        ServerPlayer owner = run.online(run.ownerId);
        Player summoner = owner;
        var shop = ShopkeeperManager.spawnShopkeeper(level, center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, summoner);
        if (shop != null) shop.setInvulnerable(true);
        run.shopkeeperId = shop == null ? -1 : shop.getId();
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

    private static void spawnWaveLootBurst(RunState run, ServerLevel level, int rolls) {
        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        for (int i = 0; i < rolls; i++) {
            Item item = REGULAR_DROP_POOL.get(level.random.nextInt(REGULAR_DROP_POOL.size()));
            ItemStack stack = new ItemStack(item, 1 + level.random.nextInt(2));
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
        if (level.addFreshEntity(mob)) run.aliveMobs.add(mob.getUUID());
    }

    private static void rollTarotOptions(RunState run, RandomSource random) {
        ArrayList<TarotOption> rolled = new ArrayList<>();
        for (int i = 0; i < 4; i++) rolled.add(TarotOption.random(random));
        run.tarotOptions = List.copyOf(rolled);
    }

    private static void rollLootOptions(RunState run, RandomSource random) {
        ArrayList<LootOption> rolled = new ArrayList<>();
        rolled.add(LootOption.weapon(random));
        rolled.add(LootOption.armor(random));
        rolled.add(LootOption.buff(random));
        rolled.add(LootOption.mystery(random));
        run.lootOptions = List.copyOf(rolled);
    }

    private static void applyTarot(RunState run, TarotOption option) {
        run.enemyCountMultiplier += option.enemyCountBonus;
        run.damageMultiplier += option.damageBonus;
        run.speedMultiplier += option.speedBonus;
        run.extraRewardRolls += option.rewardRollBonus;
        if (option.unlockArchetype != null && !run.unlockedArchetypes.contains(option.unlockArchetype)) run.unlockedArchetypes.add(option.unlockArchetype);
    }

    private static void grantLoot(ServerPlayer player, LootOption option, RandomSource random) {
        ItemStack stack = option.create(random);
        if (stack.isEmpty()) return;
        DungeonGearRoller.rollAndBind(stack, random);
        if (stack.getItem() == Items.IRON_HELMET || stack.getItem() == Items.CHAINMAIL_HELMET || stack.getItem() == Items.GOLDEN_HELMET || stack.getItem() == Items.DIAMOND_HELMET) {
            player.setItemSlot(EquipmentSlot.HEAD, stack);
        } else if (stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_CHESTPLATE || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.DIAMOND_CHESTPLATE) {
            player.setItemSlot(EquipmentSlot.CHEST, stack);
        } else if (stack.getItem() == Items.IRON_LEGGINGS || stack.getItem() == Items.CHAINMAIL_LEGGINGS || stack.getItem() == Items.GOLDEN_LEGGINGS || stack.getItem() == Items.DIAMOND_LEGGINGS) {
            player.setItemSlot(EquipmentSlot.LEGS, stack);
        } else if (stack.getItem() == Items.IRON_BOOTS || stack.getItem() == Items.CHAINMAIL_BOOTS || stack.getItem() == Items.GOLDEN_BOOTS || stack.getItem() == Items.DIAMOND_BOOTS) {
            player.setItemSlot(EquipmentSlot.FEET, stack);
        } else if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static RunState findRunByTrackedMob(UUID mobId) {
        for (RunState run : RUNS_BY_OWNER.values()) if (run.aliveMobs.contains(mobId)) return run;
        return null;
    }

    private static void spawnDungeonMobDrops(ServerLevel level, LivingEntity dead, int waveNumber) {
        int rolls = 1 + level.random.nextInt(2);
        for (int i = 0; i < rolls; i++) {
            Item item = REGULAR_DROP_POOL.get(level.random.nextInt(REGULAR_DROP_POOL.size()));
            dead.spawnAtLocation(new ItemStack(item, 1 + level.random.nextInt(2)));
        }
    }

    private static EntityType<?> pickEntityType(RandomSource random, EnemyPoolSet pools, WaveArchetype archetype) {
        return switch (archetype) {
            case UNDEAD -> pools.pick(random, EnemyPoolRole.THEME, EnemyPoolRole.MELEE);
            case HORDE -> pools.pick(random, EnemyPoolRole.MELEE, EnemyPoolRole.TANK);
            case ASSASSIN -> pools.pick(random, EnemyPoolRole.FAST, EnemyPoolRole.ELITE);
            case ARCHER -> pools.pick(random, EnemyPoolRole.RANGED, EnemyPoolRole.THEME);
            case TANK -> pools.pick(random, EnemyPoolRole.TANK, EnemyPoolRole.MELEE);
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
            views = run.tarotOptions.stream().map(option -> new DungeonWaveMenu.WaveOptionView(option.title, option.details, 100, 100)).toList();
        } else {
            views = run.lootOptions.stream().map(option -> new DungeonWaveMenu.WaveOptionView(option.title, option.details, 100, 100)).toList();
        }
        int rerollsLeft = Math.max(0, MAX_REROLLS - run.rerollsUsed);
        int rerollCost = rerollsLeft <= 0 ? 0 : BASE_REROLL_COST << run.rerollsUsed;
        int stage = run.phase == RunPhase.SELECTING_TAROT ? 0 : 1;
        List<Component> changes = runChangeSummary(run);
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new DungeonWaveMenu(containerId, inventory, run.ownerId, run.waveNumber + 1, true, stage, rerollsLeft, rerollCost, views, changes),
                Component.translatable("screen.gatesofavarice.dungeon_wave.title", run.waveNumber + 1)
        );
        owner.openMenu(provider, buffer -> DungeonWaveMenu.writePayload(buffer, run.ownerId, run.waveNumber + 1, true, stage, rerollsLeft, rerollCost, views, changes));
    }

    private static List<Component> runChangeSummary(RunState run) {
        return List.of(
                Component.literal(String.format(java.util.Locale.ROOT, "Enemy Count x%.2f", run.enemyCountMultiplier)),
                Component.literal(String.format(java.util.Locale.ROOT, "Enemy Damage x%.2f", run.damageMultiplier)),
                Component.literal(String.format(java.util.Locale.ROOT, "Enemy Speed x%.2f", run.speedMultiplier)),
                Component.literal("Extra Rewards +" + run.extraRewardRolls),
                Component.literal("Unlocked Archetypes: " + run.unlockedArchetypes.size())
        );
    }

    private static void finishAndCleanup(RunState run) {
        syncHud(run, false);
        if (run.shopkeeperId >= 0) {
            ServerLevel dungeon = run.server == null ? null : run.server.getLevel(ModDimensions.DUNGEON_LEVEL);
            if (dungeon != null) {
                Entity shop = dungeon.getEntity(run.shopkeeperId);
                if (shop != null) shop.discard();
            }
        }
        if (run.exitPortalId >= 0) {
            ServerLevel dungeon = run.server == null ? null : run.server.getLevel(ModDimensions.DUNGEON_LEVEL);
            if (dungeon != null) {
                Entity portal = dungeon.getEntity(run.exitPortalId);
                if (portal != null) portal.discard();
            }
        }
        ServerLevel dungeonLevel = run.server == null ? null : run.server.getLevel(ModDimensions.DUNGEON_LEVEL);
        DungeonInstanceManager.cleanupInstance(run.ownerId, dungeonLevel);
        RUNS_BY_OWNER.remove(run.ownerId);
    }

    private static void clearForDungeon(ServerPlayer player) {
        player.getInventory().clearContent();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static ItemStack createLootboxFromDungeonInventory(ServerPlayer player) {
        ItemStack lootbox = new ItemStack(ModItems.LOOTBOX.get());
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) list.add(stack.saveOptional(player.registryAccess()));
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty()) list.add(stack.saveOptional(player.registryAccess()));
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty()) list.add(stack.saveOptional(player.registryAccess()));
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
        private int rerollsUsed = 0;
        private double enemyCountMultiplier = 1.0D;
        private double damageMultiplier = 1.0D;
        private double speedMultiplier = 1.0D;
        private int extraRewardRolls = 0;

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

    private static final class TarotOption {
        private final Component title;
        private final Component details;
        private final double enemyCountBonus;
        private final double damageBonus;
        private final double speedBonus;
        private final int rewardRollBonus;
        private final WaveArchetype unlockArchetype;

        private TarotOption(Component title, Component details, double enemyCountBonus, double damageBonus, double speedBonus, int rewardRollBonus, WaveArchetype unlockArchetype) {
            this.title = title;
            this.details = details;
            this.enemyCountBonus = enemyCountBonus;
            this.damageBonus = damageBonus;
            this.speedBonus = speedBonus;
            this.rewardRollBonus = rewardRollBonus;
            this.unlockArchetype = unlockArchetype;
        }

        private static TarotOption random(RandomSource random) {
            int enemies = 2 + random.nextInt(8);
            int dmg = 6 + random.nextInt(20);
            int spd = 5 + random.nextInt(18);
            int rewards = 1 + random.nextInt(3);
            WaveArchetype[] values = WaveArchetype.values();
            WaveArchetype archetype = values[random.nextInt(values.length)];
            Component title = TAROT_ENEMY_LINES.get(random.nextInt(TAROT_ENEMY_LINES.size()));
            Component details = Component.literal(TAROT_EFFECT_LINES.get(random.nextInt(TAROT_EFFECT_LINES.size())).getString()
                    + ", " + TAROT_REWARD_LINES.get(random.nextInt(TAROT_REWARD_LINES.size())).getString()
                    + ", +" + dmg + "% dmg, +" + spd + "% speed");
            return new TarotOption(title, details, enemies * 0.03D, dmg / 100.0D, spd / 100.0D, rewards, archetype);
        }
    }

    private static final class LootOption {
        private final Component title;
        private final Component details;
        private final java.util.function.Function<RandomSource, ItemStack> factory;

        private LootOption(Component title, Component details, java.util.function.Function<RandomSource, ItemStack> factory) {
            this.title = title;
            this.details = details;
            this.factory = factory;
        }

        private ItemStack create(RandomSource random) { return this.factory.apply(random); }

        private static LootOption weapon(RandomSource random) {
            return new LootOption(Component.literal("Weapon Upgrade"), Component.literal("Random weapon"), r -> switch (r.nextInt(4)) {
                case 0 -> new ItemStack(ModItems.MANA_STEEL_SWORD.get());
                case 1 -> new ItemStack(ModItems.ELIXRITE_SWORD.get());
                case 2 -> new ItemStack(Items.IRON_SWORD);
                default -> new ItemStack(Items.BOW);
            });
        }

        private static LootOption armor(RandomSource random) {
            return new LootOption(Component.literal("Dungeon Armor"), Component.literal("Random iron armor piece"), r -> switch (r.nextInt(4)) {
                case 0 -> new ItemStack(Items.IRON_HELMET);
                case 1 -> new ItemStack(Items.IRON_CHESTPLATE);
                case 2 -> new ItemStack(Items.IRON_LEGGINGS);
                default -> new ItemStack(Items.IRON_BOOTS);
            });
        }

        private static LootOption buff(RandomSource random) {
            return new LootOption(Component.literal("Buff Bundle"), Component.literal("Golden / Arcane apples"), r -> {
                if (r.nextBoolean()) return new ItemStack(Items.GOLDEN_APPLE, 3);
                return new ItemStack(ModItems.ARCANE_APPLE.get(), 2);
            });
        }

        private static LootOption mystery(RandomSource random) {
            return new LootOption(Component.literal("Mystery"), Component.literal("Random item type"), r -> switch (r.nextInt(3)) {
                case 0 -> weapon(r).create(r);
                case 1 -> armor(r).create(r);
                default -> buff(r).create(r);
            });
        }
    }
}
