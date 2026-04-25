package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRegistry;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolRole;
import com.revilo.gatesofavarice.gateway.pool.EnemyPoolSet;
import com.revilo.gatesofavarice.item.MythicCoinStackData;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import com.revilo.gatesofavarice.menu.DungeonWaveMenu;
import com.revilo.gatesofavarice.network.DungeonWaveHudPayload;
import com.revilo.gatesofavarice.registry.ModAttachments;
import com.revilo.gatesofavarice.registry.ModItems;
import com.revilo.gatesofavarice.shop.ShopkeeperManager;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DungeonRunManager {

    private static final Map<UUID, RunState> RUNS_BY_OWNER = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_TO_OWNER = new HashMap<>();

    private static final ResourceLocation PLAYER_HEALTH_DEBUFF_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_health_debuff");
    private static final ResourceLocation MOB_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_health");
    private static final ResourceLocation MOB_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_damage");
    private static final ResourceLocation MOB_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_wave_speed");

    private static final List<Item> EXTERNAL_REWARD_POOL = List.of(
            ModItems.GRIMSTONE.get(), ModItems.MYSTIC_ESSENCE.get(), ModItems.SCRAP_METAL.get(), ModItems.UPGRADE_BASE.get(),
            ModItems.MANA_GEMS.get(), ModItems.MANA_STEEL_SCRAP.get(), ModItems.MAGNETITE_SCRAP.get(), ModItems.ARCANE_ESSENCE.get(),
            ModItems.MANASTONES.get(), ModItems.ELIXRITE_SCRAP.get(), ModItems.MAGNETITE_INGOT.get(), ModItems.ASTRITE_SCRAP.get(),
            ModItems.SOLAR_SHARD.get(), ModItems.PRISMATIC_SHARD.get(), ModItems.PRISMATIC_DIAMOND.get(), ModItems.LUNARIUM_SCRAP.get(),
            ModItems.DARK_ESSENCE.get(), ModItems.PRISMATIC_CORE.get()
    );

    private DungeonRunManager() {
    }

    public static void enterFromGateway(ServerPlayer player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.computeIfAbsent(ownerId, RunState::new);
        run.server = player.server;
        run.participants.add(player.getUUID());
        PLAYER_TO_OWNER.put(player.getUUID(), ownerId);

        if (!run.snapshots.containsKey(player.getUUID())) {
            run.snapshots.put(player.getUUID(), PlayerSnapshot.capture(player));
            clearForDungeon(player);
        }

        DungeonInstanceManager.teleportToDungeonInstance(player, ownerId);
        if (player.getUUID().equals(ownerId) && run.phase == RunPhase.SELECTING) {
            if (run.options.isEmpty()) {
                run.options = rollWaveOptions(run, player.serverLevel().random);
            }
            openWaveMenu(player, run);
        }
    }

    public static boolean handleWaveMenuClick(Player player, UUID ownerId, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run == null || run.phase != RunPhase.SELECTING || !serverPlayer.getUUID().equals(run.ownerId)) {
            return false;
        }

        if (buttonId == DungeonWaveMenu.BAIL_BUTTON_ID) {
            endRun(run, true);
            return true;
        }
        if (buttonId < 0 || buttonId >= run.options.size()) {
            return false;
        }

        startWave(run, run.options.get(buttonId));
        serverPlayer.closeContainer();
        return true;
    }

    public static boolean isWaveMenuValid(Player player, UUID ownerId) {
        RunState run = RUNS_BY_OWNER.get(ownerId);
        return run != null && run.phase == RunPhase.SELECTING && run.participants.contains(player.getUUID());
    }

    public static void onShopClosedByOwner(ServerPlayer player, int shopkeeperEntityId) {
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        if (ownerId == null) {
            return;
        }
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run == null || run.phase != RunPhase.SHOP || !player.getUUID().equals(run.ownerId) || run.shopkeeperId != shopkeeperEntityId) {
            return;
        }

        Entity shop = player.serverLevel().getEntity(shopkeeperEntityId);
        if (shop != null) {
            shop.discard();
        }
        run.shopkeeperId = -1;
        run.phase = RunPhase.SELECTING;
        run.options = rollWaveOptions(run, player.serverLevel().random);
        openWaveMenu(player, run);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RUNS_BY_OWNER.isEmpty()) {
            return;
        }

        ArrayList<UUID> toFail = new ArrayList<>();
        for (RunState run : RUNS_BY_OWNER.values()) {
            ServerLevel dungeon = event.getServer().getLevel(ModDimensions.DUNGEON_LEVEL);
            if (dungeon == null) {
                continue;
            }

            if (run.phase == RunPhase.SELECTING && run.options.isEmpty()) {
                run.options = rollWaveOptions(run, dungeon.random);
                ServerPlayer owner = run.online(run.ownerId);
                if (owner != null) {
                    openWaveMenu(owner, run);
                }
            }
            else if (run.phase == RunPhase.IN_WAVE) {
                tickWave(run, dungeon);
            }

            if (run.participants.isEmpty()) {
                toFail.add(run.ownerId);
            }
        }

        for (UUID ownerId : toFail) {
            RunState run = RUNS_BY_OWNER.get(ownerId);
            if (run != null) {
                endRun(run, false);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        if (ownerId == null) {
            return;
        }
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run != null) {
            endRun(run, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID ownerId = PLAYER_TO_OWNER.get(player.getUUID());
        if (ownerId == null) {
            return;
        }
        RunState run = RUNS_BY_OWNER.get(ownerId);
        if (run != null) {
            run.participants.remove(player.getUUID());
            if (player.getUUID().equals(run.ownerId)) {
                endRun(run, false);
            }
        }
    }

    private static void startWave(RunState run, WaveOption option) {
        run.waveNumber++;
        run.phase = RunPhase.IN_WAVE;
        run.activeOption = option;
        run.options = List.of();
        run.aliveMobs.clear();
        run.spawnCooldown = 0;

        List<ServerPlayer> players = run.liveParticipants();
        int extraPlayers = Math.max(0, players.size() - 1);
        int baseCount = 6 + run.waveNumber * 2 + extraPlayers * 3;
        run.toSpawn = Math.max(4, (int) Math.round(baseCount * option.mobCountMultiplier));
        run.waveTotalMobs = run.toSpawn;

        applyWavePlayerDebuff(players, option);
        for (ServerPlayer participant : players) {
            participant.displayClientMessage(Component.literal("Wave " + run.waveNumber + ": ").withStyle(ChatFormatting.GOLD).append(option.title), false);
        }
        syncHud(run, true);
    }

    private static void tickWave(RunState run, ServerLevel level) {
        run.aliveMobs.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return !(entity instanceof LivingEntity living) || !living.isAlive();
        });

        if (run.toSpawn > 0) {
            if (run.spawnCooldown > 0) {
                run.spawnCooldown--;
            }
            else {
                int batch = Math.min(run.toSpawn, 2 + level.random.nextInt(2));
                for (int i = 0; i < batch; i++) {
                    spawnOneMob(run, level);
                }
                run.toSpawn -= batch;
                run.spawnCooldown = 10;
            }
        }

        if (run.toSpawn <= 0 && run.aliveMobs.isEmpty()) {
            completeWave(run, level);
            syncHud(run, false);
        }
        else {
            syncHud(run, true);
        }
    }

    private static void completeWave(RunState run, ServerLevel level) {
        removeWavePlayerDebuff(run.liveParticipants());
        grantWaveRewards(run, level);

        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        ServerPlayer owner = run.online(run.ownerId);
        Player summoner = owner;
        var shop = ShopkeeperManager.spawnShopkeeper(level, center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, summoner);
        run.shopkeeperId = shop == null ? -1 : shop.getId();
        run.phase = RunPhase.SHOP;
        syncHud(run, false);
    }

    private static void grantWaveRewards(RunState run, ServerLevel level) {
        WaveOption option = run.activeOption;
        if (option == null) {
            return;
        }

        List<ServerPlayer> players = run.liveParticipants();
        int extraPlayers = Math.max(0, players.size() - 1);
        double partyRewardMultiplier = 1.0D + extraPlayers * 0.35D;

        int coinValue = Math.max(5, (int) Math.round((10 + run.waveNumber * 3) * option.inDungeonRewardMultiplier * partyRewardMultiplier));
        for (ServerPlayer player : players) {
            ItemStack coins = MythicCoinStackData.createStack(coinValue);
            if (!player.getInventory().add(coins)) {
                player.drop(coins, false);
            }
            if (level.random.nextFloat() < 0.12F) {
                ItemStack pearl = new ItemStack(ModItems.STABILITY_PEARL.get());
                if (!player.getInventory().add(pearl)) {
                    player.drop(pearl, false);
                }
            }
        }

        int externalRolls = Math.max(1, (int) Math.round((1 + run.waveNumber * 0.5D) * option.externalRewardMultiplier * partyRewardMultiplier));
        for (int i = 0; i < externalRolls; i++) {
            Item item = EXTERNAL_REWARD_POOL.get(level.random.nextInt(EXTERNAL_REWARD_POOL.size()));
            int count = 1 + level.random.nextInt(2 + Math.max(0, run.waveNumber / 4));
            run.externalRewards.add(new ItemStack(item, count));
        }
    }

    private static void spawnOneMob(RunState run, ServerLevel level) {
        WaveOption option = run.activeOption;
        if (option == null) {
            return;
        }

        EntityType<?> type = pickEntityType(level.random, option.poolSet, option.archetype);
        if (type == null) {
            return;
        }

        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity mob)) {
            return;
        }

        BlockPos center = DungeonInstanceManager.instanceCenter(run.ownerId);
        double x = center.getX() + 0.5D + (level.random.nextDouble() * 24.0D - 12.0D);
        double z = center.getZ() + 0.5D + (level.random.nextDouble() * 24.0D - 12.0D);
        double y = center.getY() + 1.0D;
        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);

        int extraPlayers = Math.max(0, run.liveParticipants().size() - 1);
        double power = (1.0D + extraPlayers * 0.5D) * option.mobPowerMultiplier;
        applyMobScaling(mob, power, option.damageMultiplier, option.speedMultiplier);

        if (level.addFreshEntity(mob)) {
            run.aliveMobs.add(mob.getUUID());
            Player target = level.getNearestPlayer(mob, 40.0D);
            if (target != null && mob instanceof Mob aiMob) {
                aiMob.setTarget(target);
            }
        }
    }

    private static EntityType<?> pickEntityType(RandomSource random, EnemyPoolSet pools, WaveArchetype archetype) {
        return switch (archetype) {
            case UNDEAD -> pools.pick(random, EnemyPoolRole.THEME, EnemyPoolRole.MELEE);
            case HORDE -> random.nextBoolean()
                    ? pools.pick(random, EnemyPoolRole.MELEE, EnemyPoolRole.TANK)
                    : pools.pick(random, EnemyPoolRole.THEME, EnemyPoolRole.MELEE);
            case ASSASSIN -> random.nextBoolean()
                    ? pools.pick(random, EnemyPoolRole.FAST, EnemyPoolRole.ELITE)
                    : pools.pick(random, EnemyPoolRole.RANGED, EnemyPoolRole.FAST);
        };
    }

    private static void applyMobScaling(LivingEntity mob, double power, double damageMultiplier, double speedMultiplier) {
        removeMobModifiers(mob);
        addMobModifier(mob, Attributes.MAX_HEALTH, MOB_HEALTH_MODIFIER_ID, Math.max(0.0D, power - 1.0D));
        addMobModifier(mob, Attributes.ATTACK_DAMAGE, MOB_DAMAGE_MODIFIER_ID, Math.max(0.0D, (power * damageMultiplier) - 1.0D));
        addMobModifier(mob, Attributes.MOVEMENT_SPEED, MOB_SPEED_MODIFIER_ID, Math.max(0.0D, speedMultiplier - 1.0D));
        mob.setHealth(mob.getMaxHealth());
    }

    private static void removeMobModifiers(LivingEntity mob) {
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).removeModifier(MOB_HEALTH_MODIFIER_ID);
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MOB_DAMAGE_MODIFIER_ID);
        }
        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOB_SPEED_MODIFIER_ID);
        }
    }

    private static void addMobModifier(LivingEntity mob, Holder<Attribute> attribute, ResourceLocation id, double value) {
        if (value <= 0.0D || mob.getAttribute(attribute) == null) {
            return;
        }
        mob.getAttribute(attribute).addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void applyWavePlayerDebuff(List<ServerPlayer> players, WaveOption option) {
        removeWavePlayerDebuff(players);
        if (option.playerHealthPenalty <= 0.0D) {
            return;
        }
        AttributeModifier modifier = new AttributeModifier(PLAYER_HEALTH_DEBUFF_ID, -option.playerHealthPenalty, AttributeModifier.Operation.ADD_VALUE);
        for (ServerPlayer player : players) {
            if (player.getAttribute(Attributes.MAX_HEALTH) != null) {
                player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        }
    }

    private static void removeWavePlayerDebuff(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            if (player.getAttribute(Attributes.MAX_HEALTH) != null) {
                player.getAttribute(Attributes.MAX_HEALTH).removeModifier(PLAYER_HEALTH_DEBUFF_ID);
            }
        }
    }

    private static List<WaveOption> rollWaveOptions(RunState run, RandomSource random) {
        List<WaveArchetype> archetypes = new ArrayList<>(List.of(WaveArchetype.UNDEAD, WaveArchetype.HORDE, WaveArchetype.ASSASSIN));
        java.util.Collections.shuffle(archetypes, new java.util.Random(random.nextLong()));
        ArrayList<WaveOption> options = new ArrayList<>(3);
        for (WaveArchetype archetype : archetypes) {
            options.add(createWaveOption(archetype, run.waveNumber, random));
        }
        return List.copyOf(options);
    }

    private static WaveOption createWaveOption(WaveArchetype archetype, int waveNumber, RandomSource random) {
        EnumSet<WaveModifier> modifiers = EnumSet.noneOf(WaveModifier.class);
        int modifierCount = 1 + random.nextInt(2);
        WaveModifier[] all = WaveModifier.values();
        for (int i = 0; i < modifierCount; i++) {
            modifiers.add(all[random.nextInt(all.length)]);
        }

        double mobPower = 1.0D + waveNumber * 0.04D + random.nextDouble() * 0.2D;
        double mobCountMultiplier = 0.9D + random.nextDouble() * 0.5D;
        double inDungeonRewardMultiplier = 0.85D + random.nextDouble() * 0.9D;
        double externalRewardMultiplier = 0.85D + random.nextDouble() * 0.9D;
        double damageMultiplier = modifiers.contains(WaveModifier.DAMAGE_UP) ? 1.2D : 1.0D;
        double speedMultiplier = modifiers.contains(WaveModifier.SPEED_UP) ? 1.2D : 1.0D;
        double playerHealthPenalty = modifiers.contains(WaveModifier.PLAYER_HEALTH_DOWN) ? 4.0D : 0.0D;

        CrystalTheme theme = switch (archetype) {
            case UNDEAD -> CrystalTheme.UNDEAD;
            case HORDE -> CrystalTheme.BEAST;
            case ASSASSIN -> CrystalTheme.RAIDER;
        };
        EnemyPoolSet poolSet = EnemyPoolRegistry.create(theme, Math.max(1, 10 + waveNumber * 2));

        Component details = Component.literal(archetype.description);
        for (WaveModifier modifier : modifiers) {
            details = details.copy().append(Component.literal(" | " + modifier.label).withStyle(ChatFormatting.AQUA));
        }

        return new WaveOption(
                archetype,
                Component.literal(archetype.title),
                details,
                poolSet,
                mobPower,
                mobCountMultiplier,
                inDungeonRewardMultiplier,
                externalRewardMultiplier,
                damageMultiplier,
                speedMultiplier,
                playerHealthPenalty
        );
    }

    private static void openWaveMenu(ServerPlayer owner, RunState run) {
        List<DungeonWaveMenu.WaveOptionView> views = run.options.stream()
                .map(option -> new DungeonWaveMenu.WaveOptionView(
                        option.title,
                        option.details,
                        (int) Math.round(option.inDungeonRewardMultiplier * 100.0D),
                        (int) Math.round(option.externalRewardMultiplier * 100.0D)
                ))
                .toList();

        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new DungeonWaveMenu(containerId, inventory, run.ownerId, run.waveNumber + 1, true, views),
                Component.translatable("screen.gatesofavarice.dungeon_wave.title", run.waveNumber + 1)
        );
        owner.openMenu(provider, buffer -> DungeonWaveMenu.writePayload(buffer, run.ownerId, run.waveNumber + 1, true, views));
    }

    private static void clearForDungeon(ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) inv.items.set(i, ItemStack.EMPTY);
        for (int i = 0; i < inv.armor.size(); i++) inv.armor.set(i, ItemStack.EMPTY);
        for (int i = 0; i < inv.offhand.size(); i++) inv.offhand.set(i, ItemStack.EMPTY);
        inv.selected = 0;
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.setData(ModAttachments.MYTHIC_COINS, 0);
        player.setData(ModAttachments.COIN_MULTIPLIER, 1.0F);
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void restoreSnapshot(ServerPlayer player, PlayerSnapshot snapshot) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) inv.items.set(i, snapshot.items.get(i).copy());
        for (int i = 0; i < inv.armor.size(); i++) inv.armor.set(i, snapshot.armor.get(i).copy());
        for (int i = 0; i < inv.offhand.size(); i++) inv.offhand.set(i, snapshot.offhand.get(i).copy());
        inv.selected = snapshot.selectedSlot;
        player.setData(ModAttachments.MYTHIC_COINS, snapshot.mythicCoins);
        player.setData(ModAttachments.COIN_MULTIPLIER, snapshot.coinMultiplier);
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void endRun(RunState run, boolean bailed) {
        syncHud(run, false);
        for (UUID playerId : new HashSet<>(run.participants)) {
            ServerPlayer player = run.online(playerId);
            PlayerSnapshot snapshot = run.snapshots.get(playerId);
            PLAYER_TO_OWNER.remove(playerId);
            if (player == null || snapshot == null) {
                continue;
            }

            removeWavePlayerDebuff(List.of(player));
            restoreSnapshot(player, snapshot);
            if (bailed) {
                for (ItemStack rewardStack : run.externalRewards) {
                    ItemStack reward = rewardStack.copy();
                    if (!player.getInventory().add(reward)) {
                        player.drop(reward, false);
                    }
                }
                player.displayClientMessage(Component.translatable("message.gatesofavarice.dungeon.bailed").withStyle(ChatFormatting.GREEN), false);
            }
            else {
                player.displayClientMessage(Component.translatable("message.gatesofavarice.dungeon.failed").withStyle(ChatFormatting.RED), false);
            }

            DungeonInstanceManager.teleportToSavedLocation(player, snapshot.dimension, snapshot.returnPos, snapshot.yaw, snapshot.pitch);
        }

        if (run.shopkeeperId >= 0) {
            ServerPlayer owner = run.online(run.ownerId);
            if (owner != null) {
                Entity shop = owner.serverLevel().getEntity(run.shopkeeperId);
                if (shop != null) {
                    shop.discard();
                }
            }
        }

        RUNS_BY_OWNER.remove(run.ownerId);
    }

    private enum RunPhase {
        SELECTING,
        IN_WAVE,
        SHOP
    }

    private enum WaveArchetype {
        UNDEAD("Undead Wave", "Undead frontline with sustained pressure"),
        HORDE("Horde Wave", "Dense swarm with higher body count"),
        ASSASSIN("Assassin Wave", "Fast lethal units with burst pressure");

        private final String title;
        private final String description;

        WaveArchetype(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private enum WaveModifier {
        DAMAGE_UP("+20% Mob Damage"),
        SPEED_UP("+20% Mob Speed"),
        PLAYER_HEALTH_DOWN("-2 Hearts Player Health");

        private final String label;

        WaveModifier(String label) {
            this.label = label;
        }
    }

    private record WaveOption(
            WaveArchetype archetype,
            Component title,
            Component details,
            EnemyPoolSet poolSet,
            double mobPowerMultiplier,
            double mobCountMultiplier,
            double inDungeonRewardMultiplier,
            double externalRewardMultiplier,
            double damageMultiplier,
            double speedMultiplier,
            double playerHealthPenalty
    ) {
    }

    private static final class RunState {
        private final UUID ownerId;
        private MinecraftServer server;
        private final Set<UUID> participants = new HashSet<>();
        private final Map<UUID, PlayerSnapshot> snapshots = new HashMap<>();
        private final List<ItemStack> externalRewards = new ArrayList<>();
        private RunPhase phase = RunPhase.SELECTING;
        private int waveNumber = 0;
        private List<WaveOption> options = List.of();
        private WaveOption activeOption;
        private final Set<UUID> aliveMobs = new HashSet<>();
        private int toSpawn = 0;
        private int waveTotalMobs = 0;
        private int spawnCooldown = 0;
        private int shopkeeperId = -1;

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
                if (player != null && player.isAlive() && player.level().dimension() == ModDimensions.DUNGEON_LEVEL) {
                    players.add(player);
                }
            }
            return players;
        }
    }

    private static void syncHud(RunState run, boolean active) {
        int remaining = Math.max(0, run.toSpawn + run.aliveMobs.size());
        int total = Math.max(1, run.waveTotalMobs);
        DungeonWaveHudPayload payload = new DungeonWaveHudPayload(active, run.waveNumber, remaining, total);
        for (ServerPlayer participant : run.liveParticipants()) {
            PacketDistributor.sendToPlayer(participant, payload);
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
        private final int mythicCoins;
        private final float coinMultiplier;

        private PlayerSnapshot(ResourceKey<Level> dimension, BlockPos returnPos, float yaw, float pitch, List<ItemStack> items, List<ItemStack> armor, List<ItemStack> offhand, int selectedSlot, int mythicCoins, float coinMultiplier) {
            this.dimension = dimension;
            this.returnPos = returnPos;
            this.yaw = yaw;
            this.pitch = pitch;
            this.items = items;
            this.armor = armor;
            this.offhand = offhand;
            this.selectedSlot = selectedSlot;
            this.mythicCoins = mythicCoins;
            this.coinMultiplier = coinMultiplier;
        }

        private static PlayerSnapshot capture(ServerPlayer player) {
            Inventory inv = player.getInventory();
            return new PlayerSnapshot(
                    player.level().dimension(),
                    player.blockPosition(),
                    player.getYRot(),
                    player.getXRot(),
                    copyStacks(inv.items),
                    copyStacks(inv.armor),
                    copyStacks(inv.offhand),
                    inv.selected,
                    player.getData(ModAttachments.MYTHIC_COINS),
                    player.getData(ModAttachments.COIN_MULTIPLIER)
            );
        }

        private static List<ItemStack> copyStacks(List<ItemStack> source) {
            ArrayList<ItemStack> copied = new ArrayList<>(source.size());
            for (ItemStack stack : source) {
                copied.add(stack.copy());
            }
            return copied;
        }
    }
}
