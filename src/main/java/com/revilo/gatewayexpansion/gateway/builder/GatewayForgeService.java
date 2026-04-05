package com.revilo.gatewayexpansion.gateway.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.augment.AugmentDefinition;
import com.revilo.gatewayexpansion.augment.AugmentStackData;
import com.revilo.gatewayexpansion.catalyst.CatalystArchetype;
import com.revilo.gatewayexpansion.catalyst.CatalystDefinition;
import com.revilo.gatewayexpansion.catalyst.CatalystStackData;
import com.revilo.gatewayexpansion.gateway.GatewayThemeProfile;
import com.revilo.gatewayexpansion.gateway.pool.EnemyPoolRegistry;
import com.revilo.gatewayexpansion.gateway.pool.EnemyPoolRole;
import com.revilo.gatewayexpansion.gateway.pool.EnemyPoolSet;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import com.revilo.gatewayexpansion.integration.LevelUpIntegration;
import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import com.revilo.gatewayexpansion.registry.ModItems;
import com.revilo.gatewayexpansion.workbench.GatewayWorkbenchSlots;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.BossEventSettings;
import dev.shadowsoffire.gateways.gate.GateRules;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.StandardWaveEntity;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.gateways.gate.normal.NormalGateway;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.reload.ReloadListenerPayloads;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GatewayForgeService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final CatalystArchetype[] CATALYST_ARCHETYPES = CatalystArchetype.values();
    private static final boolean RUNIC_LOADED = ModCompat.isAnyLoaded("runic");
    private static final String ROOT_KEY = GatewayExpansion.MOD_ID;
    private static final String GATEWAY_ID_KEY = "gateway_id";
    private static final String GATEWAY_JSON_KEY = "gateway_json";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String THEME_KEY = "theme";
    private static final String LEVEL_KEY = "level";
    private static final String PLAYER_LEVEL_KEY = "player_level";
    private static final String OVERLEVELED_KEY = "overleveled";
    private static final String SUMMARY_KEY = "summary";
    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String TIER_KEY = "tier";
    private static final Map<ResourceLocation, String> GENERATED_GATEWAY_NAMES = new HashMap<>();
    private static final Map<ResourceLocation, Integer> GENERATED_GATEWAY_TIERS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> GENERATED_GATEWAY_LEVELS = new HashMap<>();

    private GatewayForgeService() {
    }

    public static boolean canForge(net.minecraft.world.entity.player.Player player, Container container) {
        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystal.getItem() instanceof CrystalItem crystalItem)) {
            return false;
        }
        if (!container.getItem(GatewayWorkbenchSlots.OUTPUT_SLOT).isEmpty()) {
            return false;
        }
        if (player == null) {
            return true;
        }

        CrystalForgeData.CrystalProfile profile = CrystalForgeData.getProfile(
                crystal,
                crystalItem.crystalTier().minLevel(),
                crystalItem.crystalTier().maxLevel()
        );
        return !LevelUpIntegration.isCrystalOverleveled(player, profile.level());
    }

    public static GatewayPreview buildPreview(net.minecraft.world.entity.player.Player player, Container container) {
        ItemStack crystalStack = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystalStack.getItem() instanceof CrystalItem crystalItem)) {
            return new GatewayPreview(0, "None", 0, -1, false, 0, 0, "Unavailable", 0, 0, "Unavailable", List.of());
        }

        CrystalForgeData.CrystalProfile profile = CrystalForgeData.getProfile(crystalStack, crystalItem.crystalTier().minLevel(), crystalItem.crystalTier().maxLevel());
        ForgeState state = buildState(container, crystalItem.crystalTier(), profile);
        int playerLevel = LevelUpIntegration.getPlayerLevel(player);
        boolean overleveled = playerLevel >= 0 && profile.level() > playerLevel;

        List<String> previewLines = List.of(
                "Theme: " + titleCase(profile.theme().name()),
                "Level: " + profile.level(),
                "Waves: " + state.waveCount(),
                "Difficulty: " + difficultyLabel(state.difficultyEstimate),
                "Reward Direction: " + signedPercent(state.rewardMultiplier - 1.0D),
                "Augments: " + joinedOrNone(state.augmentSummary),
                "Catalysts: " + joinedOrNone(state.catalystSummary)
        );

        return new GatewayPreview(
                crystalItem.crystalTier().tier(),
                titleCase(profile.theme().name()),
                profile.level(),
                playerLevel,
                overleveled,
                state.augmentSummary.size(),
                state.catalystSummary.size(),
                difficultyLabel(state.difficultyEstimate),
                percent(state.rewardMultiplier - 1.0D),
                state.waveCount(),
                state.timePressureLabel(),
                previewLines
        );
    }

    public static ItemStack forge(ServerPlayer player, Container container) {
        ItemStack crystalStack = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystalStack.getItem() instanceof CrystalItem crystalItem)) {
            throw new IllegalStateException("Cannot forge without a crystal.");
        }

        CrystalForgeData.CrystalProfile currentProfile = CrystalForgeData.getProfile(
                crystalStack,
                crystalItem.crystalTier().minLevel(),
                crystalItem.crystalTier().maxLevel()
        );
        if (LevelUpIntegration.isCrystalOverleveled(player, currentProfile.level())) {
            throw new IllegalStateException("Cannot forge an overleveled crystal.");
        }

        int playerLevel = LevelUpIntegration.getPlayerLevel(player);
        CrystalForgeData.CrystalProfile profile = CrystalForgeData.syncLevelToPlayer(
                crystalStack,
                crystalItem.crystalTier().minLevel(),
                crystalItem.crystalTier().maxLevel(),
                playerLevel,
                player.serverLevel().random
        );

        ForgeState state = buildState(container, crystalItem.crystalTier(), profile);
        boolean overleveled = playerLevel >= 0 && profile.level() > playerLevel;

        applyFinalRandomStage(state);
        GatewayBuildResult result = generateGateway(state, playerLevel, overleveled);
        registerGeneratedGateway(result.gatewayId(), result.gateway());
        GENERATED_GATEWAY_NAMES.put(result.gatewayId(), result.name());
        GENERATED_GATEWAY_TIERS.put(result.gatewayId(), result.crystalTier());
        GENERATED_GATEWAY_LEVELS.put(result.gatewayId(), result.crystalLevel());
        persistGeneratedGateway(player.serverLevel(), result.gatewayId(), result.gatewayJson(), result.name(), result.crystalTier(), result.crystalLevel());
        syncGatewayRegistry(player);

        ItemStack pearl = createPearl(result);
        consumeInputs(container);
        container.setItem(GatewayWorkbenchSlots.OUTPUT_SLOT, pearl);
        container.setChanged();
        GatewayExpansion.LOGGER.debug("Forged gateway {} for {}", result.gatewayId(), player.getScoreboardName());
        GatewayExpansion.LOGGER.debug("Gateway JSON:\n{}", result.gatewayJson());
        return pearl;
    }

    public static boolean restoreGatewayFromPearl(ItemStack stack) {
        return restoreGatewayFromPearl(stack, null);
    }

    public static boolean restoreGatewayFromPearl(ItemStack stack, ServerLevel serverLevel) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(GATEWAY_ID_KEY) || !root.contains(GATEWAY_JSON_KEY)) {
            return false;
        }

        ResourceLocation gatewayId = ResourceLocation.parse(root.getString(GATEWAY_ID_KEY));
        int crystalLevel = root.contains(LEVEL_KEY) ? root.getInt(LEVEL_KEY) : 0;
        int crystalTier = root.contains(TIER_KEY) ? root.getInt(TIER_KEY) : inferCrystalTierFromLevel(crystalLevel);
        String displayName = root.contains(DISPLAY_NAME_KEY) ? root.getString(DISPLAY_NAME_KEY) : "Lv " + crystalLevel + " Gateway";
        String gatewayJson = root.getString(GATEWAY_JSON_KEY);
        if (serverLevel != null) {
            persistGeneratedGateway(serverLevel, gatewayId, gatewayJson, displayName, crystalTier, crystalLevel);
        }
        return restoreGeneratedGateway(gatewayId, gatewayJson, displayName, crystalTier, crystalLevel);
    }

    public static void syncGatewayRegistry(ServerPlayer player) {
        for (ServerPlayer target : player.getServer().getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(target, new ReloadListenerPayloads.Start("gateways"));
            for (ResourceLocation key : GatewayRegistry.INSTANCE.getKeys()) {
                Gateway gateway = GatewayRegistry.INSTANCE.getValue(key);
                if (gateway != null) {
                    PacketDistributor.sendToPlayer(target, new ReloadListenerPayloads.Content<>("gateways", key, gateway));
                }
            }
            PacketDistributor.sendToPlayer(target, new ReloadListenerPayloads.End("gateways"));
        }
    }

    private static ForgeState buildState(Container container, CrystalItem.CrystalTier tier, CrystalForgeData.CrystalProfile profile) {
        ForgeState state = new ForgeState(tier, profile);
        for (ItemStack stack : GatewayWorkbenchSlots.collectAugments(container)) {
            if (stack.getItem() instanceof AugmentItem augmentItem) {
                AugmentDefinition definition = AugmentStackData.getDefinition(stack, augmentItem.difficultyTier());
                if (definition != null) {
                    state.augmentSummary.add(definition.title());
                    applyEffects(state, definition.modifierEffects(), true);
                    applyEffect(state, definition.rewardEffect(), true);
                }
            }
        }
        for (ItemStack stack : GatewayWorkbenchSlots.collectCatalysts(container)) {
            if (stack.getItem() instanceof CatalystItem catalystItem) {
                CatalystDefinition definition = CatalystStackData.getDefinition(stack, catalystItem.archetype());
                if (definition != null) {
                    state.catalystSummary.add(definition.title());
                    applyEffect(state, definition.positiveEffect(), false);
                    applyEffect(state, definition.negativeEffect(), false);
                }
            }
        }
        state.finish();
        return state;
    }

    private static void applyFinalRandomStage(ForgeState state) {
        RandomSource random = RandomSource.create(state.profile.seed() ^ 0x51A7L);
        GatewayThemeProfile theme = GatewayThemeProfile.forTheme(state.profile.theme(), state.profile.level());
        int effectRolls = state.profile.level() >= 34 ? 2 : 1;
        for (int i = 0; i < effectRolls; i++) {
            ForgeEffect effect = switch (state.profile.theme()) {
                case UNDEAD -> random.nextBoolean()
                        ? ForgeEffect.of(ForgeEffectType.THEMED_REINFORCEMENTS, 1, "Final roll: undead reinforcements")
                        : ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.withDefaultNamespace("strength"), 0, 0.0D, "Final roll: stronger undead");
                case BEAST -> random.nextBoolean()
                        ? ForgeEffect.of(ForgeEffectType.SPEED_MULTIPLIER, 0.08D, "Final roll: feral speed")
                        : ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.withDefaultNamespace("jump_boost"), 0, 0.0D, "Final roll: leaping beasts");
                case ARCANE -> random.nextBoolean()
                        ? ForgeEffect.of(ForgeEffectType.RANGED_PACKS, 1, "Final roll: extra ranged pressure")
                        : ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.withDefaultNamespace("glowing"), 0, 0.0D, "Final roll: revealing magic");
                case NETHER -> random.nextBoolean()
                        ? ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.withDefaultNamespace("fire_resistance"), 0, 0.0D, "Final roll: infernal resilience")
                        : ForgeEffect.of(ForgeEffectType.DAMAGE_MULTIPLIER, 0.08D, "Final roll: hellfire damage");
                case RAIDER -> random.nextBoolean()
                        ? ForgeEffect.of(ForgeEffectType.RANGED_PACKS, 1, "Final roll: coordinated volley")
                        : ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.withDefaultNamespace("hero_of_the_village"), 0, 0.0D, "Final roll: raider momentum");
            };
            applyEffect(state, effect, false);
            state.finalRollSummary.add(effect.description());
        }
        ForgeEffect rewardRoll = random.nextBoolean()
                ? ForgeEffect.of(ForgeEffectType.REWARD_MULTIPLIER, 0.08D + Math.min(0.10D, state.profile.level() / 250.0D), "Final roll: enhanced payout")
                : ForgeEffect.of(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "Final roll: extra final reward");
        applyEffect(state, rewardRoll, false);
        state.finalRollSummary.add(rewardRoll.description());
        if (state.profile.level() >= 30 && random.nextFloat() < 0.45F) {
            ForgeEffect highRoll = ForgeEffect.of(ForgeEffectType.MINIBOSS_CHANCE, 0.15D, "Final roll: themed miniboss chance");
            applyEffect(state, highRoll, false);
            state.finalRollSummary.add(highRoll.description());
        }
        if (state.profile.level() >= 36 && random.nextFloat() < 0.35F) {
            ForgeEffect highRoll = ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, theme.effects().get(random.nextInt(theme.effects().size())), 0, 0.0D, "Final roll: thematic effect");
            applyEffect(state, highRoll, false);
            state.finalRollSummary.add(highRoll.description());
        }
    }

    private static GatewayBuildResult generateGateway(ForgeState state, int playerLevel, boolean overleveled) {
        GatewayThemeProfile theme = GatewayThemeProfile.forTheme(state.profile.theme(), state.profile.level());
        GatewayThemeProfile.GateTypeProfile gateType = theme.gateType(state.profile.seed());
        EnemyPoolSet enemyPools = EnemyPoolRegistry.create(state.profile.theme(), state.profile.level());
        RandomSource random = RandomSource.create(state.profile.seed());
        int waveCount = state.waveCount();
        int bossWaveIndex = selectBossWaveIndex(state, enemyPools, random);
        boolean bossWaveEnabled = bossWaveIndex >= 0;
        List<Wave> waves = new ArrayList<>();
        for (int waveIndex = 0; waveIndex < waveCount; waveIndex++) {
            if (waveIndex == bossWaveIndex) {
                waves.add(buildBossWave(state, theme, enemyPools, random, waveCount));
                continue;
            }

            Wave.Builder builder = Wave.builder();
            int packs = 1 + waveIndex / 2 + state.densePacks + (waveIndex >= waveCount - 2 ? 1 : 0);
            for (int pack = 0; pack < packs; pack++) {
                EntityType<?> primary = pickEnemy(enemyPools, random, EnemyPoolRole.THEME, EnemyPoolRole.MELEE);
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(primary), state).count(Mth.clamp(baseCount(state, waveIndex, random), 1, 64)).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.supportInterval > 0 && ((waveIndex + 1) % state.supportInterval == 0)) {
                for (int i = 0; i < state.supportCount; i++) {
                    builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.SUPPORT, EnemyPoolRole.MELEE)), state).count(1).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
                }
            }
            if (state.rangedPacks > 0 && random.nextFloat() < 0.45F + (0.10F * state.rangedPacks)) {
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.RANGED, EnemyPoolRole.MELEE)), state).count(1 + state.rangedPacks).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.mixedPackCount > 0 && random.nextFloat() < 0.35F) {
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.FAST, EnemyPoolRole.SUPPORT)), state).count(state.mixedPackCount).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.reinforcementRolls > 0 && random.nextFloat() < 0.25F * state.reinforcementRolls) {
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.TANK, EnemyPoolRole.MELEE)), state).count(2 + state.reinforcementRolls).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (shouldAddElite(state, waveIndex, random)) {
                int eliteCount = 1 + (waveIndex == waveCount - 1 ? state.finalWaveEliteCount : 0);
                for (int i = 0; i < eliteCount; i++) {
                    builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.ELITE, EnemyPoolRole.MELEE)), state).count(1).addModifiers(eliteModifiers(state, random)).finalizeSpawn(true).build());
                }
            }
            if (waveIndex >= waveCount - 2 && random.nextFloat() < state.minibossChance) {
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.ELITE, EnemyPoolRole.TANK)), state).count(1).addModifiers(minibossModifiers(state)).finalizeSpawn(true).build());
            }
            for (WaveModifier modifier : globalWaveModifiers(state)) {
                builder.modifier(modifier);
            }
            if (state.entityLootRolls > 0 && (waveIndex == waveCount - 1 || random.nextFloat() < 0.40F)) {
                builder.reward(new Reward.EntityLootReward(pickEnemy(enemyPools, random, EnemyPoolRole.MELEE, EnemyPoolRole.THEME), null, state.entityLootRolls));
            }
            addWaveRewardDrops(builder, state, theme, waveIndex, waveCount, random);
            builder.reward(new Reward.ExperienceReward(waveExperienceReward(state), 5));
            builder.maxWaveTime(Mth.clamp(1020 + state.profile.level() * 6 + state.waveTimeDelta + (waveIndex >= waveCount - 2 ? state.lateWaveTimeBonus : 0), 220, 1800));
            builder.setupTime(computeSetupTime(state));
            waves.add(builder.build());
        }

        List<Reward> rewards = buildRewards(state, theme, gateType, bossWaveEnabled);
        List<Failure> failures = buildFailures(state);
        GateRules rules = GateRules.builder()
                .spawnRange(8.0D + state.crystalTier.tier())
                .leashRange(32.0D + state.crystalTier.tier() * 2.0D)
                .removeOnFailure(true)
                .requiresNearbyPlayer(true)
                .followRangeBoost(24.0D + state.profile.level() / 3.0D)
                .lives(bossWaveEnabled ? 1 : Math.max(1, 4 - state.crystalTier.tier() / 2))
                .build();
        NormalGateway gateway = NormalGateway.builder()
                .size(gatewaySize(state.crystalTier.tier()))
                .color(theme.color())
                .waves(waves)
                .keyRewards(rewards)
                .failures(failures)
                .rules(rules)
                .bossSettings(new BossEventSettings(BossEventSettings.Mode.NAME_PLATE, false))
                .build();
        validate(waves, rewards);
        if (bossWaveEnabled && !state.finalRollSummary.contains("Boss encounter")) {
            state.finalRollSummary.add("Boss encounter");
        }

        ResourceLocation gatewayId = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "forged_" + UUID.randomUUID().toString().replace('-', '_'));
        String gatewayJson = serializeGateway(gateway);
        List<String> debugLines = List.of(
                "theme=" + state.profile.theme(),
                "gateType=" + gateType.id(),
                "level=" + state.profile.level(),
                "difficulty=" + difficultyLabel(state.difficultyEstimate),
                "waves=" + waveCount,
                "bossWave=" + bossWaveEnabled,
                "rewardMultiplier=" + state.rewardMultiplier,
                "finalRolls=" + state.finalRollSummary
        );
        return new GatewayBuildResult(
                gatewayId,
                generateName(state, theme, gateType),
                state.profile.theme(),
                theme.color(),
                state.profile.level(),
                state.crystalTier.tier(),
                playerLevel,
                overleveled,
                state.waveCount(),
                state.difficultyEstimate,
                state.rewardMultiplier,
                state.augmentSummary,
                state.catalystSummary,
                state.finalRollSummary,
                debugLines,
                gateway,
                gatewayJson
        );
    }

    private static List<Reward> buildRewards(ForgeState state, GatewayThemeProfile theme, GatewayThemeProfile.GateTypeProfile gateType, boolean bossWaveEnabled) {
        List<Reward> rewards = new ArrayList<>();
        RandomSource random = RandomSource.create(state.profile.seed() ^ 0xA661L);
        int bossBonus = bossWaveEnabled ? 2 : 0;
        int baseRolls = Math.max(1, 1 + state.finalRewardRolls + bossBonus + (int) Math.floor(Math.max(0.0D, (state.rewardMultiplier - 1.0D) * 3.5D)));
        rewards.add(new Reward.LootTableReward(theme.commonLoot(), baseRolls, theme.finalDescKey()));
        rewards.add(new Reward.LootTableReward(gateType.bonusLoot(), 1 + state.crystalTier.tier() / 3, gateType.descKey()));
        rewards.add(new Reward.StackReward(createAugmentRewardStack(state, Math.max(0, state.waveCount() - 1), random)));
        if (state.crystalTier.tier() >= 3 || state.profile.level() >= 30) {
            rewards.add(new Reward.StackReward(createAugmentRewardStack(state, state.waveCount(), random)));
        }
        if (state.rewardMultiplier > 1.0D && state.lootTableBonusChance > 0.0D) {
            rewards.add(new Reward.ChancedReward(new Reward.LootTableReward(theme.commonLoot(), 1, theme.finalDescKey()), (float) state.lootTableBonusChance));
        }
        for (int i = 0; i < Math.max(0, state.rareRewardRolls); i++) {
            rewards.add(new Reward.ChancedReward(new Reward.LootTableReward(theme.rareLoot(), 1, theme.rareDescKey()), 0.35F + (state.profile.level() / 180.0F)));
        }
        rewards.add(new Reward.ExperienceReward(finalExperienceReward(state), 5));
        return rewards;
    }

    private static int selectBossWaveIndex(ForgeState state, EnemyPoolSet enemyPools, RandomSource random) {
        if (state.profile.level() < 35 || state.difficultyEstimate < 80 || !enemyPools.hasBosses()) {
            return -1;
        }
        return random.nextFloat() < 0.28F ? state.waveCount() - 1 : -1;
    }

    private static Wave buildBossWave(ForgeState state, GatewayThemeProfile theme, EnemyPoolSet enemyPools, RandomSource random, int waveCount) {
        Wave.Builder builder = Wave.builder();
        EntityType<?> boss = enemyPools.pickBoss(random);
        builder.entity(configureWaveEntity(StandardWaveEntity.builder(boss), state).count(1).addModifiers(bossModifiers(state)).finalizeSpawn(true).build());

        if (random.nextFloat() < 0.65F) {
            builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.SUPPORT, EnemyPoolRole.RANGED)), state).count(2 + state.crystalTier.tier() / 2).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
        }
        if (random.nextFloat() < 0.45F) {
            builder.entity(configureWaveEntity(StandardWaveEntity.builder(pickEnemy(enemyPools, random, EnemyPoolRole.MELEE, EnemyPoolRole.THEME)), state).count(3 + state.crystalTier.tier()).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
        }

        for (WaveModifier modifier : globalWaveModifiers(state)) {
            builder.modifier(modifier);
        }
        builder.reward(new Reward.StackReward(createAugmentRewardStack(state, waveCount - 1, random)));
        builder.reward(new Reward.StackReward(createCatalystRewardStack(random)));
        builder.reward(new Reward.LootTableReward(theme.rareLoot(), 1, theme.rareDescKey()));
        builder.reward(new Reward.ExperienceReward(waveExperienceReward(state), 5));
        builder.maxWaveTime(Mth.clamp(7200 + state.profile.level() * 20 + state.waveTimeDelta, 3600, 12000));
        builder.setupTime(computeSetupTime(state));
        return builder.build();
    }

    private static StandardWaveEntity.Builder configureWaveEntity(StandardWaveEntity.Builder builder, ForgeState state) {
        if (state.profile.theme() != CrystalTheme.UNDEAD) {
            return builder;
        }
        return builder.nbt(GatewayForgeService::applyUndeadEquipment);
    }

    private static CompoundTag applyUndeadEquipment(CompoundTag tag) {
        ListTag armorItems = new ListTag();
        armorItems.add(new CompoundTag());
        armorItems.add(new CompoundTag());
        armorItems.add(new CompoundTag());
        armorItems.add(ItemStack.OPTIONAL_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, new ItemStack(Items.LEATHER_HELMET))
                .result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new));
        tag.put("ArmorItems", armorItems);

        ListTag armorDropChances = new ListTag();
        for (int i = 0; i < 4; i++) {
            armorDropChances.add(net.minecraft.nbt.FloatTag.valueOf(0.0F));
        }
        tag.put("ArmorDropChances", armorDropChances);
        return tag;
    }

    private static List<Failure> buildFailures(ForgeState state) {
        List<Failure> failures = new ArrayList<>();
        failures.add(new Failure.MobEffectFailure(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.withDefaultNamespace("weakness"))), 180 + state.profile.level() * 5, state.crystalTier.tier() >= 4 ? 1 : 0));
        if (state.dangerousFinalWave || state.crystalTier.tier() >= 4) {
            failures.add(new Failure.ChancedFailure(new Failure.ExplosionFailure(2.0F + state.crystalTier.tier() * 0.4F, false, false), 0.25F));
        }
        return failures;
    }

    private static int computeSetupTime(ForgeState state) {
        int baseSetupTime = 100 + state.profile.level() * 40;
        int adjustedSetupTime = baseSetupTime + state.setupTimeDelta - (state.shorterDenser ? 20 : 0);
        return Math.max(100, adjustedSetupTime);
    }

    private static int finalExperienceReward(ForgeState state) {
        return Math.max(1, state.profile.level() * 50 + state.experienceBonus);
    }

    private static int waveExperienceReward(ForgeState state) {
        return Math.max(1, (int) Math.round(finalExperienceReward(state) * 0.25D));
    }

    private static void addWaveRewardDrops(Wave.Builder builder, ForgeState state, GatewayThemeProfile theme, int waveIndex, int waveCount, RandomSource random) {
        float levelBoost = Math.min(0.30F, state.profile.level() * 0.006F);
        float augmentChance = Math.min(0.98F, 0.40F + state.crystalTier.tier() * 0.09F + waveIndex * 0.08F + levelBoost);
        if (random.nextFloat() < augmentChance) {
            builder.reward(new Reward.StackReward(createAugmentRewardStack(state, waveIndex, random)));
        }
        if ((state.crystalTier.tier() >= 3 || state.profile.level() >= 25) && random.nextFloat() < Math.min(0.75F, 0.18F + levelBoost * 0.9F + waveIndex * 0.05F)) {
            builder.reward(new Reward.StackReward(createAugmentRewardStack(state, waveIndex + 1, random)));
        }

        int tierRolls = Math.max(1, state.crystalTier.tier() >= 4 && waveIndex >= waveCount - 2 ? 2 : 1);
        if (state.profile.level() >= 30) {
            tierRolls++;
        }
        if (state.profile.level() >= 50 && waveIndex >= waveCount - 2) {
            tierRolls++;
        }
        builder.reward(new Reward.LootTableReward(
                ResourceLocation.fromNamespaceAndPath("gatewayexpansion", (RUNIC_LOADED ? "rewards/waves/tier_" : "rewards/waves_fallback/tier_") + state.crystalTier.tier()),
                tierRolls,
                theme.waveDescKey()
        ));
        if (random.nextFloat() < Math.min(0.9F, 0.45F + state.crystalTier.tier() * 0.08F)) {
            builder.reward(new Reward.LootTableReward(theme.waveLoot(), 1, theme.themedWaveDescKey()));
        }

        float catalystChance = Math.min(0.90F, 0.10F + state.crystalTier.tier() * 0.05F + waveIndex * 0.05F + (levelBoost * 0.75F));
        if (random.nextFloat() < catalystChance) {
            builder.reward(new Reward.StackReward(createCatalystRewardStack(random)));
        }
        if (state.profile.level() >= 45 && random.nextFloat() < Math.min(0.40F, levelBoost * 0.55F + waveIndex * 0.025F)) {
            builder.reward(new Reward.StackReward(createCatalystRewardStack(random)));
        }
    }

    private static ItemStack createAugmentRewardStack(ForgeState state, int waveIndex, RandomSource random) {
        AugmentDifficultyTier tier = selectAugmentTier(state, waveIndex);
        ItemStack stack = new ItemStack(switch (tier) {
            case EASY -> ModItems.EASY_AUGMENT.get();
            case MEDIUM -> ModItems.MEDIUM_AUGMENT.get();
            case HARD -> ModItems.HARD_AUGMENT.get();
            case EXTREME -> ModItems.EXTREME_AUGMENT.get();
        });
        AugmentStackData.ensureDefinition(stack, tier, random);
        return stack;
    }

    private static AugmentDifficultyTier selectAugmentTier(ForgeState state, int waveIndex) {
        int rewardScore = state.profile.level() + state.crystalTier.tier() * 8 + waveIndex * 6;
        if (rewardScore >= 62) {
            return AugmentDifficultyTier.EXTREME;
        }
        if (rewardScore >= 42) {
            return AugmentDifficultyTier.HARD;
        }
        if (rewardScore >= 24) {
            return AugmentDifficultyTier.MEDIUM;
        }
        return AugmentDifficultyTier.EASY;
    }

    private static ItemStack createCatalystRewardStack(RandomSource random) {
        CatalystArchetype archetype = CATALYST_ARCHETYPES[random.nextInt(CATALYST_ARCHETYPES.length)];
        ItemStack stack = new ItemStack(switch (archetype) {
            case TIME -> ModItems.TIME_CATALYST.get();
            case STAT -> ModItems.STAT_CATALYST.get();
            case LOOT -> ModItems.LOOT_CATALYST.get();
            case VOLATILE -> ModItems.HIGHRISK_CATALYST.get();
        });
        CatalystStackData.ensureDefinition(stack, archetype, random);
        return stack;
    }

    private static List<WaveModifier> globalWaveModifiers(ForgeState state) {
        List<WaveModifier> modifiers = new ArrayList<>();
        for (ResourceLocation id : state.mobEffects) {
            modifiers.add(WaveModifier.EffectModifier.create(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, id)), 0));
        }
        return modifiers;
    }

    private static List<WaveModifier> baseModifiers(ForgeState state) {
        List<WaveModifier> modifiers = new ArrayList<>();
        addAttribute(modifiers, Attributes.MAX_HEALTH, state.healthMultiplier);
        addAttribute(modifiers, Attributes.ATTACK_DAMAGE, state.damageMultiplier);
        addAttribute(modifiers, Attributes.MOVEMENT_SPEED, state.speedMultiplier);
        if (state.armorBonus != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, (float) state.armorBonus));
        }
        if (state.knockbackResistance != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, (float) state.knockbackResistance));
        }
        if (state.lifeSteal != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(ALObjects.Attributes.LIFE_STEAL, Operation.ADD_VALUE, (float) state.lifeSteal));
        }
        if (state.armorPierce != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, (float) state.armorPierce));
        }
        if (state.projectileDamage != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(ALObjects.Attributes.PROJECTILE_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, (float) state.projectileDamage));
        }
        return modifiers;
    }

    private static List<WaveModifier> eliteModifiers(ForgeState state, RandomSource random) {
        List<WaveModifier> modifiers = new ArrayList<>(baseModifiers(state));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.40F));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.25F));
        if (random.nextFloat() < 0.5F) {
            modifiers.add(WaveModifier.EffectModifier.create(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.withDefaultNamespace("strength"))), 0));
        }
        return modifiers;
    }

    private static List<WaveModifier> minibossModifiers(ForgeState state) {
        List<WaveModifier> modifiers = new ArrayList<>(eliteModifiers(state, RandomSource.create(state.profile.seed() + 19L)));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.55F));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 6.0F));
        return modifiers;
    }

    private static List<WaveModifier> bossModifiers(ForgeState state) {
        List<WaveModifier> modifiers = new ArrayList<>(minibossModifiers(state));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.MAX_HEALTH, Operation.ADD_MULTIPLIED_TOTAL, 0.85F));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, 0.45F));
        modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ARMOR, Operation.ADD_VALUE, 8.0F));
        return modifiers;
    }

    private static void addAttribute(List<WaveModifier> modifiers, Holder<Attribute> attribute, double multiplier) {
        if (multiplier != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(attribute, Operation.ADD_MULTIPLIED_TOTAL, (float) multiplier));
        }
    }

    private static boolean shouldAddElite(ForgeState state, int waveIndex, RandomSource random) {
        if (state.eliteEvery > 0 && (waveIndex + 1) % state.eliteEvery == 0) {
            return true;
        }
        return random.nextFloat() < state.eliteChance + (waveIndex * 0.02F) + state.eliteUpgradeChance;
    }

    private static int baseCount(ForgeState state, int waveIndex, RandomSource random) {
        int count = 2 + state.crystalTier.tier() + waveIndex + state.minionsPerWave + (state.shorterDenser ? 2 : 0);
        count += random.nextInt(2 + Math.max(0, state.profile.level() / 18));
        return count;
    }

    private static EntityType<?> pickEnemy(EnemyPoolSet enemyPools, RandomSource random, EnemyPoolRole primary, EnemyPoolRole fallback) {
        EntityType<?> type = enemyPools.pick(random, primary, fallback);
        if (type == null) {
            throw new IllegalStateException("Unable to pick enemy for " + primary + " with fallback " + fallback);
        }
        return type;
    }

    private static ItemStack createPearl(GatewayBuildResult result) {
        ItemStack pearl = new ItemStack(GatewayObjects.GATE_PEARL.value());
        GatePearlItem.setGate(pearl, GatewayRegistry.INSTANCE.holder(result.gatewayId()));
        pearl.set(DataComponents.CUSTOM_NAME, Component.literal(result.name()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(result.color()))));
        pearl.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Theme: " + titleCase(result.theme().name())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(result.color()))),
                Component.literal("Crystal Level: " + result.crystalLevel()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))),
                Component.literal("Difficulty: " + difficultyLabel(result.difficultyEstimate())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(difficultyColor(result.difficultyEstimate())))),
                Component.literal("Reward Direction: " + signedPercent(result.rewardMultiplier() - 1.0D)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0C24B))),
                Component.literal("Final Rolls: " + joinedOrNone(result.finalRollSummary())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xD8D8D8)))
        )));
        CustomData.update(DataComponents.CUSTOM_DATA, pearl, tag -> {
            CompoundTag root = tag.getCompound(ROOT_KEY);
            root.putString(GATEWAY_ID_KEY, result.gatewayId().toString());
            root.putString(GATEWAY_JSON_KEY, result.gatewayJson());
            root.putString(DISPLAY_NAME_KEY, result.name());
            root.putString(DIFFICULTY_KEY, difficultyLabel(result.difficultyEstimate()));
            root.putString(THEME_KEY, result.theme().name());
            root.putInt(LEVEL_KEY, result.crystalLevel());
            root.putInt(TIER_KEY, result.crystalTier());
            root.putInt(PLAYER_LEVEL_KEY, result.playerLevel());
            root.putBoolean(OVERLEVELED_KEY, result.overleveled());
            root.putString(SUMMARY_KEY, String.join(" | ", result.debugLines()));
            tag.put(ROOT_KEY, root);
        });
        return pearl;
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(ROOT_KEY);
    }

    public static void restorePersistedGateways(ServerLevel level) {
        for (Map.Entry<ResourceLocation, GeneratedGatewayStorage.StoredGateway> entry : GeneratedGatewayStorage.get(level).entries().entrySet()) {
            restoreGeneratedGateway(entry.getKey(), entry.getValue().json(), entry.getValue().displayName(), entry.getValue().crystalTier(), entry.getValue().crystalLevel());
        }
    }

    private static ResourceLocation registerGeneratedGateway(ResourceLocation gatewayId, NormalGateway gateway) {
        DynamicRegistry.DataGenPopulator.runScoped(GatewayRegistry.INSTANCE, populator -> populator.register(gatewayId, gateway));
        GENERATED_GATEWAY_NAMES.putIfAbsent(gatewayId, "Gateway");
        return gatewayId;
    }

    public static String getGatewayDisplayName(Gateway gateway) {
        ResourceLocation id = GatewayRegistry.INSTANCE.getKey(gateway);
        if (id == null) {
            return null;
        }
        return GENERATED_GATEWAY_NAMES.get(id);
    }

    public static int getGatewayCrystalTier(Gateway gateway) {
        ResourceLocation id = GatewayRegistry.INSTANCE.getKey(gateway);
        if (id == null) {
            return 0;
        }
        return GENERATED_GATEWAY_TIERS.getOrDefault(id, 0);
    }

    public static int getGatewayLevel(Gateway gateway) {
        ResourceLocation id = GatewayRegistry.INSTANCE.getKey(gateway);
        if (id == null) {
            return 0;
        }
        return GENERATED_GATEWAY_LEVELS.getOrDefault(id, 0);
    }

    private static void consumeInputs(Container container) {
        consumeSlot(container, GatewayWorkbenchSlots.CRYSTAL_SLOT);
        consumeRange(container, GatewayWorkbenchSlots.CATALYST_SLOT_START, GatewayWorkbenchSlots.CATALYST_SLOT_COUNT);
        consumeRange(container, GatewayWorkbenchSlots.AUGMENT_SLOT_START, GatewayWorkbenchSlots.AUGMENT_SLOT_COUNT);
    }

    private static void consumeRange(Container container, int start, int count) {
        for (int index = 0; index < count; index++) {
            consumeSlot(container, start + index);
        }
    }

    private static void consumeSlot(Container container, int slot) {
        ItemStack stack = container.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            container.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
    }

    private static void validate(List<Wave> waves, List<Reward> rewards) {
        if (waves.isEmpty()) throw new IllegalStateException("Gateway has no waves.");
        for (Wave wave : waves) {
            if (wave.entities().isEmpty()) throw new IllegalStateException("Gateway has empty wave.");
            if (wave.maxWaveTime() < 20 || wave.setupTime() < 0) throw new IllegalStateException("Gateway has invalid timing.");
        }
        if (rewards.isEmpty()) throw new IllegalStateException("Gateway has no rewards.");
    }

    private static String serializeGateway(NormalGateway gateway) {
        JsonElement element = NormalGateway.CODEC.encodeStart(JsonOps.INSTANCE, gateway).getOrThrow(IllegalStateException::new);
        return GSON.toJson(element);
    }

    private static boolean restoreGeneratedGateway(ResourceLocation gatewayId, String gatewayJson, String displayName, int crystalTier, int crystalLevel) {
        if (GatewayRegistry.INSTANCE.getValue(gatewayId) != null) {
            GENERATED_GATEWAY_NAMES.putIfAbsent(gatewayId, displayName);
            GENERATED_GATEWAY_TIERS.putIfAbsent(gatewayId, crystalTier);
            GENERATED_GATEWAY_LEVELS.putIfAbsent(gatewayId, crystalLevel);
            return false;
        }

        JsonElement json = JsonParser.parseString(gatewayJson);
        NormalGateway gateway = NormalGateway.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(IllegalStateException::new);
        registerGeneratedGateway(gatewayId, gateway);
        GENERATED_GATEWAY_NAMES.put(gatewayId, displayName);
        GENERATED_GATEWAY_TIERS.put(gatewayId, crystalTier);
        GENERATED_GATEWAY_LEVELS.put(gatewayId, crystalLevel);
        return true;
    }

    private static void persistGeneratedGateway(ServerLevel level, ResourceLocation gatewayId, String gatewayJson, String displayName, int crystalTier, int crystalLevel) {
        GeneratedGatewayStorage.get(level).put(gatewayId, gatewayJson, displayName, crystalTier, crystalLevel);
    }

    private static int inferCrystalTierFromLevel(int crystalLevel) {
        if (crystalLevel >= 90) return 5;
        if (crystalLevel >= 70) return 4;
        if (crystalLevel >= 50) return 3;
        if (crystalLevel >= 20) return 2;
        return crystalLevel > 0 ? 1 : 0;
    }

    private static Gateway.Size gatewaySize(int tier) {
        if (tier >= 5) return Gateway.Size.LARGE;
        if (tier >= 3) return Gateway.Size.MEDIUM;
        return Gateway.Size.SMALL;
    }

    private static String generateName(ForgeState state, GatewayThemeProfile theme, GatewayThemeProfile.GateTypeProfile gateType) {
        return "Lv " + state.profile.level() + " Gateway";
    }

    private static void applyEffects(ForgeState state, List<ForgeEffect> effects, boolean augment) {
        for (ForgeEffect effect : effects) {
            applyEffect(state, effect, augment);
        }
    }

    private static void applyEffect(ForgeState state, ForgeEffect effect, boolean augment) {
        switch (effect.type()) {
            case ADD_MINIONS_PER_WAVE -> state.minionsPerWave += (int) Math.round(effect.value());
            case SUPPORT_PACK_EVERY -> {
                state.supportInterval = state.supportInterval == 0 ? (int) Math.round(effect.value()) : Math.min(state.supportInterval, (int) Math.round(effect.value()));
                state.supportCount += Math.max(1, (int) Math.round(effect.secondaryValue()));
            }
            case BONUS_WAVES -> state.bonusWaves += (int) Math.round(effect.value());
            case EXTEND_LATE_WAVES -> state.lateWaveTimeBonus += (int) Math.round(effect.value());
            case SHORTER_DENSER_WAVES -> {
                state.shorterDenser = true;
                state.densePacks += 1;
                state.waveTimeDelta -= 80;
                if (state.bonusWaves > 0) state.bonusWaves -= 1;
            }
            case ELITE_EVERY -> state.eliteEvery = state.eliteEvery == 0 ? (int) Math.round(effect.value()) : Math.min(state.eliteEvery, (int) Math.round(effect.value()));
            case ELITE_CHANCE -> state.eliteChance += (float) effect.value();
            case ELITE_UPGRADE_CHANCE -> state.eliteUpgradeChance += (float) effect.value();
            case FINAL_WAVE_ELITES -> state.finalWaveEliteCount += (int) Math.round(effect.value());
            case HEALTH_MULTIPLIER -> state.healthMultiplier += effect.value();
            case DAMAGE_MULTIPLIER -> state.damageMultiplier += effect.value();
            case SPEED_MULTIPLIER -> state.speedMultiplier += effect.value();
            case ARMOR_BONUS -> state.armorBonus += effect.value();
            case KNOCKBACK_RESISTANCE -> state.knockbackResistance += effect.value();
            case LIFE_STEAL -> state.lifeSteal += effect.value();
            case ARMOR_PIERCE -> state.armorPierce += effect.value();
            case PROJECTILE_DAMAGE -> state.projectileDamage += effect.value();
            case MOB_EFFECT -> {
                if (effect.referenceId() != null) state.mobEffects.add(effect.referenceId());
            }
            case THEMED_REINFORCEMENTS -> state.reinforcementRolls += (int) Math.round(effect.value());
            case MIXED_PACKS -> state.mixedPackCount += (int) Math.round(effect.value());
            case RANGED_PACKS -> state.rangedPacks += (int) Math.round(effect.value());
            case MINIBOSS_CHANCE -> state.minibossChance += (float) effect.value();
            case NAMED_ELITE_CHANCE -> state.namedEliteChance += (float) effect.value();
            case DANGEROUS_FINAL_WAVE -> state.dangerousFinalWave = true;
            case WAVE_TIME_DELTA -> state.waveTimeDelta += (int) Math.round(effect.value());
            case SETUP_TIME_DELTA -> state.setupTimeDelta += (int) Math.round(effect.value());
            case REWARD_MULTIPLIER -> state.rewardMultiplier += effect.value();
            case EXTRA_RARE_REWARD_ROLLS -> state.rareRewardRolls += (int) Math.round(effect.value());
            case EXTRA_FINAL_REWARD_ROLLS -> state.finalRewardRolls += (int) Math.round(effect.value());
            case EXTRA_ENTITY_LOOT_ROLLS -> state.entityLootRolls += (int) Math.round(effect.value());
            case BONUS_EXPERIENCE -> state.experienceBonus += (int) Math.round(effect.value());
            case BONUS_LOOT_TABLE_CHANCE -> state.lootTableBonusChance += effect.value();
        }
        state.difficultyEstimate += augment ? 8 : (effect.value() >= 0 ? 4 : -2);
    }

    private static String difficultyLabel(int estimate) {
        if (estimate >= 95) return "Extreme";
        if (estimate >= 70) return "Hard";
        if (estimate >= 40) return "Medium";
        return "Easy";
    }

    private static int difficultyColor(int estimate) {
        if (estimate >= 95) return 0xFF5555;
        if (estimate >= 70) return 0xFFAA00;
        if (estimate >= 40) return 0xFFFF55;
        return 0x55FF55;
    }

    private static int percent(double value) {
        return (int) Math.round(value * 100.0D);
    }

    private static String signedPercent(double value) {
        int rounded = percent(value);
        return (rounded > 0 ? "+" : "") + rounded + "%";
    }

    private static String titleCase(String value) {
        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static String joinedOrNone(List<String> values) {
        return values.isEmpty() ? "None" : String.join(", ", values);
    }

    private static double naturalHealthMultiplier(int level) {
        if (level <= 10) {
            return 0.0D;
        }
        if (level <= 50) {
            return 0.15D + ((level - 10) / 40.0D) * 0.95D;
        }
        if (level <= 100) {
            return 1.10D + ((level - 50) / 50.0D) * 1.90D;
        }
        return 3.0D;
    }

    private static double naturalDamageMultiplier(int level) {
        if (level <= 10) {
            return 0.0D;
        }
        if (level <= 50) {
            return 0.10D + ((level - 10) / 40.0D) * 0.90D;
        }
        if (level <= 100) {
            return 1.0D + ((level - 50) / 50.0D) * 3.0D;
        }
        return 4.0D;
    }

    private static final class ForgeState {
        private final CrystalItem.CrystalTier crystalTier;
        private final CrystalForgeData.CrystalProfile profile;
        private final List<String> augmentSummary = new ArrayList<>();
        private final List<String> catalystSummary = new ArrayList<>();
        private final List<String> finalRollSummary = new ArrayList<>();
        private final Set<ResourceLocation> mobEffects = new LinkedHashSet<>();
        private int minionsPerWave;
        private int supportInterval;
        private int supportCount;
        private int bonusWaves;
        private int lateWaveTimeBonus;
        private boolean shorterDenser;
        private int densePacks;
        private int eliteEvery;
        private float eliteChance = 0.06F;
        private float eliteUpgradeChance;
        private int finalWaveEliteCount;
        private double healthMultiplier;
        private double damageMultiplier;
        private double speedMultiplier;
        private double armorBonus;
        private double knockbackResistance;
        private double lifeSteal;
        private double armorPierce;
        private double projectileDamage;
        private int reinforcementRolls;
        private int mixedPackCount;
        private int rangedPacks;
        private float minibossChance;
        private float namedEliteChance;
        private boolean dangerousFinalWave;
        private int waveTimeDelta;
        private int setupTimeDelta;
        private double rewardMultiplier = 1.0D;
        private int rareRewardRolls = 1;
        private int finalRewardRolls = 1;
        private int entityLootRolls = 1;
        private int experienceBonus;
        private double lootTableBonusChance;
        private int difficultyEstimate;

        private ForgeState(CrystalItem.CrystalTier crystalTier, CrystalForgeData.CrystalProfile profile) {
            this.crystalTier = crystalTier;
            this.profile = profile;
            this.healthMultiplier = naturalHealthMultiplier(profile.level());
            this.damageMultiplier = naturalDamageMultiplier(profile.level());
            this.difficultyEstimate = crystalTier.tier() * 8 + profile.level();
        }

        private int waveCount() {
            int base = 3 + (this.crystalTier.tier() >= 3 ? 1 : 0) + (this.profile.level() >= 24 ? 1 : 0) + (this.profile.level() >= 38 ? 1 : 0);
            return Mth.clamp(base + this.bonusWaves, 3, 9);
        }

        private String timePressureLabel() {
            int total = this.waveTimeDelta + this.setupTimeDelta;
            if (total <= -120) return "Severe";
            if (total < -40) return "High";
            if (total > 120) return "Relaxed";
            return "Stable";
        }

        private void finish() {
            this.healthMultiplier = Mth.clamp(this.healthMultiplier, -0.20D, 3.75D);
            this.damageMultiplier = Mth.clamp(this.damageMultiplier, -0.20D, 4.75D);
            this.speedMultiplier = Mth.clamp(this.speedMultiplier, -0.10D, 0.40D);
            this.eliteChance = Mth.clamp(this.eliteChance, 0.02F, 0.60F);
            this.rewardMultiplier = Math.max(0.40D, this.rewardMultiplier);
            this.entityLootRolls = Math.max(0, this.entityLootRolls);
            this.finalRewardRolls = Math.max(0, this.finalRewardRolls);
            this.rareRewardRolls = Math.max(0, this.rareRewardRolls);
            this.difficultyEstimate += this.waveCount() * 5;
            this.difficultyEstimate += percent(this.healthMultiplier) / 3;
            this.difficultyEstimate += percent(this.damageMultiplier) / 3;
            this.difficultyEstimate += percent(this.speedMultiplier) / 4;
            this.difficultyEstimate += this.finalWaveEliteCount * 6;
            this.difficultyEstimate += this.dangerousFinalWave ? 12 : 0;
            this.difficultyEstimate += this.mobEffects.size() * 4;
        }
    }
}
