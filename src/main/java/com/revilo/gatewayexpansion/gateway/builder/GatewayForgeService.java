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
    private static final String COIN_REWARD_MULTIPLIER_KEY = "coin_reward_multiplier";
    private static final String LEVEL_XP_MULTIPLIER_KEY = "level_xp_multiplier";
    private static final String EXPERIENCE_REWARD_MULTIPLIER_KEY = "experience_reward_multiplier";
    private static final Map<ResourceLocation, String> GENERATED_GATEWAY_NAMES = new HashMap<>();
    private static final Map<ResourceLocation, Integer> GENERATED_GATEWAY_TIERS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> GENERATED_GATEWAY_LEVELS = new HashMap<>();
    private static final Map<ResourceLocation, Double> GENERATED_GATEWAY_COIN_MULTIPLIERS = new HashMap<>();
    private static final Map<ResourceLocation, Double> GENERATED_GATEWAY_LEVEL_XP_MULTIPLIERS = new HashMap<>();
    private static final ItemStack[] LEATHER_ARMOR_SET = createArmorSet(Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET);
    private static final ItemStack[] CHAINMAIL_ARMOR_SET = createArmorSet(Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET);
    private static final ItemStack[] IRON_ARMOR_SET = createArmorSet(Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET);
    private static final ItemStack[] DIAMOND_ARMOR_SET = createArmorSet(Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET);
    private static final ItemStack[] NETHERITE_ARMOR_SET = createArmorSet(Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET);
    private static final ItemStack[] SWORD_SET = createWeaponSet(Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
    private static final ItemStack[] AXE_SET = createWeaponSet(Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);

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

        List<String> previewLines = buildRewardSummary(state);

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
        GENERATED_GATEWAY_COIN_MULTIPLIERS.put(result.gatewayId(), result.coinRewardMultiplier());
        GENERATED_GATEWAY_LEVEL_XP_MULTIPLIERS.put(result.gatewayId(), result.levelXpMultiplier());
        persistGeneratedGateway(player.serverLevel(), result);
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
        double coinRewardMultiplier = root.contains(COIN_REWARD_MULTIPLIER_KEY) ? root.getDouble(COIN_REWARD_MULTIPLIER_KEY) : 1.0D;
        double levelXpMultiplier = root.contains(LEVEL_XP_MULTIPLIER_KEY) ? root.getDouble(LEVEL_XP_MULTIPLIER_KEY) : 1.0D;
        double experienceRewardMultiplier = root.contains(EXPERIENCE_REWARD_MULTIPLIER_KEY) ? root.getDouble(EXPERIENCE_REWARD_MULTIPLIER_KEY) : 1.0D;
        String displayName = root.contains(DISPLAY_NAME_KEY) ? root.getString(DISPLAY_NAME_KEY) : "Lv " + crystalLevel + " Gateway";
        String gatewayJson = root.getString(GATEWAY_JSON_KEY);
        if (serverLevel != null) {
            persistGeneratedGateway(serverLevel, gatewayId, gatewayJson, displayName, crystalTier, crystalLevel, coinRewardMultiplier, levelXpMultiplier, experienceRewardMultiplier);
        }
        return restoreGeneratedGateway(gatewayId, gatewayJson, displayName, crystalTier, crystalLevel, coinRewardMultiplier, levelXpMultiplier, experienceRewardMultiplier);
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
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(primary), primary, state).count(Mth.clamp(baseCount(state, waveIndex, random), 1, 64)).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.supportInterval > 0 && ((waveIndex + 1) % state.supportInterval == 0)) {
                for (int i = 0; i < state.supportCount; i++) {
                    EntityType<?> supportType = pickEnemy(enemyPools, random, EnemyPoolRole.SUPPORT, EnemyPoolRole.MELEE);
                    builder.entity(configureWaveEntity(StandardWaveEntity.builder(supportType), supportType, state).count(1).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
                }
            }
            if (state.rangedPacks > 0 && random.nextFloat() < 0.45F + (0.10F * state.rangedPacks)) {
                EntityType<?> rangedType = pickEnemy(enemyPools, random, EnemyPoolRole.RANGED, EnemyPoolRole.MELEE);
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(rangedType), rangedType, state).count(1 + state.rangedPacks).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.mixedPackCount > 0 && random.nextFloat() < 0.35F) {
                EntityType<?> mixedType = pickEnemy(enemyPools, random, EnemyPoolRole.FAST, EnemyPoolRole.SUPPORT);
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(mixedType), mixedType, state).count(state.mixedPackCount).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (state.reinforcementRolls > 0 && random.nextFloat() < 0.25F * state.reinforcementRolls) {
                EntityType<?> reinforcementType = pickEnemy(enemyPools, random, EnemyPoolRole.TANK, EnemyPoolRole.MELEE);
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(reinforcementType), reinforcementType, state).count(2 + state.reinforcementRolls).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
            }
            if (shouldAddElite(state, waveIndex, random)) {
                int eliteCount = 1 + (waveIndex == waveCount - 1 ? state.finalWaveEliteCount : 0);
                for (int i = 0; i < eliteCount; i++) {
                    EntityType<?> eliteType = pickEnemy(enemyPools, random, EnemyPoolRole.ELITE, EnemyPoolRole.MELEE);
                    builder.entity(configureWaveEntity(StandardWaveEntity.builder(eliteType), eliteType, state).count(1).addModifiers(eliteModifiers(state, random)).finalizeSpawn(true).build());
                }
            }
            if (waveIndex >= waveCount - 2 && random.nextFloat() < state.minibossChance) {
                EntityType<?> minibossType = pickEnemy(enemyPools, random, EnemyPoolRole.ELITE, EnemyPoolRole.TANK);
                builder.entity(configureWaveEntity(StandardWaveEntity.builder(minibossType), minibossType, state).count(1).addModifiers(minibossModifiers(state)).finalizeSpawn(true).build());
            }
            for (WaveModifier modifier : globalWaveModifiers(state)) {
                builder.modifier(modifier);
            }
            addWaveRewardDrops(builder, state, theme, waveIndex, waveCount, random);
            builder.reward(new Reward.ExperienceReward(waveExperienceReward(state), 5));
            builder.maxWaveTime(computeWaveTimeLimit(state, waveIndex, waveCount, packs));
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
                "coinMultiplier=" + state.coinRewardMultiplier,
                "levelXpMultiplier=" + state.levelXpMultiplier,
                "experienceMultiplier=" + state.experienceRewardMultiplier,
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
                state.coinRewardMultiplier,
                state.levelXpMultiplier,
                state.experienceRewardMultiplier,
                state.rarityRewardMultiplier,
                Math.max(0, state.rareRewardRolls),
                Math.max(1, state.finalRewardRolls),
                finalExperienceReward(state),
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
        rewards.add(new Reward.StackReward(createAugmentRewardStack(state, Math.max(0, state.waveCount() - 1), random)));
        if (state.crystalTier.tier() >= 3 || state.profile.level() >= 30) {
            rewards.add(new Reward.StackReward(createAugmentRewardStack(state, state.waveCount(), random)));
        }
        if (state.rewardMultiplier > 1.0D && state.lootTableBonusChance > 0.0D) {
            rewards.add(new Reward.ChancedReward(new Reward.LootTableReward(theme.commonLoot(), 1, theme.finalDescKey()), (float) state.lootTableBonusChance));
        }
        float rareRewardChance = Mth.clamp((0.35F + (state.profile.level() / 180.0F)) * (float) state.rarityRewardMultiplier, 0.10F, 0.95F);
        for (int i = 0; i < Math.max(0, state.rareRewardRolls); i++) {
            rewards.add(new Reward.ChancedReward(new Reward.LootTableReward(theme.rareLoot(), 1, theme.rareDescKey()), rareRewardChance));
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
        builder.entity(configureWaveEntity(StandardWaveEntity.builder(boss), boss, state).count(1).addModifiers(bossModifiers(state)).finalizeSpawn(true).build());

        if (random.nextFloat() < 0.65F) {
            EntityType<?> supportType = pickEnemy(enemyPools, random, EnemyPoolRole.SUPPORT, EnemyPoolRole.RANGED);
            builder.entity(configureWaveEntity(StandardWaveEntity.builder(supportType), supportType, state).count(2 + state.crystalTier.tier() / 2).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
        }
        if (random.nextFloat() < 0.45F) {
            EntityType<?> meleeType = pickEnemy(enemyPools, random, EnemyPoolRole.MELEE, EnemyPoolRole.THEME);
            builder.entity(configureWaveEntity(StandardWaveEntity.builder(meleeType), meleeType, state).count(3 + state.crystalTier.tier()).addModifiers(baseModifiers(state)).finalizeSpawn(true).build());
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

    private static StandardWaveEntity.Builder configureWaveEntity(StandardWaveEntity.Builder builder, EntityType<?> entityType, ForgeState state) {
        return builder.nbt(tag -> applySpawnOverrides(tag, entityType, state));
    }

    private static CompoundTag applySpawnOverrides(CompoundTag tag, EntityType<?> entityType, ForgeState state) {
        tag.putString("DeathLootTable", "minecraft:empty");
        if (entityType == EntityType.PHANTOM) {
            ListTag activeEffects = tag.contains("ActiveEffects", 9) ? tag.getList("ActiveEffects", 10) : new ListTag();
            CompoundTag effect = new CompoundTag();
            effect.putString("id", "minecraft:fire_resistance");
            effect.putInt("amplifier", 0);
            effect.putInt("duration", 12000);
            effect.putBoolean("ambient", false);
            effect.putBoolean("show_particles", false);
            effect.putBoolean("show_icon", false);
            activeEffects.add(effect);
            tag.put("ActiveEffects", activeEffects);
        }
        if (!supportsArmorProgression(entityType)) {
            return tag;
        }

        RandomSource random = RandomSource.create(state.profile.seed() ^ BuiltInRegistries.ENTITY_TYPE.getKey(entityType).hashCode() ^ tag.hashCode());
        float progress = armorProgress(state.profile.level());
        ListTag armorItems = ensureArmorItems(tag);
        boolean undeadCapOnly = state.profile.theme() == CrystalTheme.UNDEAD;
        if (undeadCapOnly) {
            armorItems.set(3, encodeItemStack(new ItemStack(Items.LEATHER_HELMET)));
        }

        if (progress > 0.0F && random.nextFloat() <= Mth.lerp(progress, 0.12F, 1.0F)) {
            ItemStack[] armorSet = selectArmorSet(state.profile.level(), random);
            for (int slot = 0; slot < 4; slot++) {
                if (undeadCapOnly && slot == 3) {
                    continue;
                }
                boolean equipSlot = state.profile.level() >= 80 || random.nextFloat() < Mth.lerp(progress, 0.35F, 1.0F);
                armorItems.set(slot, equipSlot ? encodeItemStack(armorSet[slot]) : new CompoundTag());
            }
        }
        tag.put("ArmorItems", armorItems);

        ListTag armorDropChances = new ListTag();
        for (int i = 0; i < 4; i++) {
            armorDropChances.add(net.minecraft.nbt.FloatTag.valueOf(0.0F));
        }
        tag.put("ArmorDropChances", armorDropChances);
        if (supportsWeaponProgression(entityType)) {
            float weaponProgress = weaponProgress(state.profile.level());
            if (weaponProgress > 0.0F && random.nextFloat() < Mth.lerp(weaponProgress, 0.22F, 0.90F)) {
                ListTag handItems = ensureHandItems(tag);
                if (handItems.getCompound(0).isEmpty()) {
                    handItems.set(0, encodeItemStack(selectWeapon(entityType, state.profile.level(), random)));
                    tag.put("HandItems", handItems);
                }
                ListTag handDropChances = ensureHandDropChances(tag);
                handDropChances.set(0, net.minecraft.nbt.FloatTag.valueOf(0.0F));
                handDropChances.set(1, net.minecraft.nbt.FloatTag.valueOf(0.0F));
                tag.put("HandDropChances", handDropChances);
            }
        }
        return tag;
    }

    private static ListTag ensureArmorItems(CompoundTag tag) {
        ListTag armorItems = tag.getList("ArmorItems", net.minecraft.nbt.Tag.TAG_COMPOUND);
        while (armorItems.size() < 4) {
            armorItems.add(new CompoundTag());
        }
        return armorItems;
    }

    private static ListTag ensureHandItems(CompoundTag tag) {
        ListTag handItems = tag.getList("HandItems", net.minecraft.nbt.Tag.TAG_COMPOUND);
        while (handItems.size() < 2) {
            handItems.add(new CompoundTag());
        }
        return handItems;
    }

    private static ListTag ensureHandDropChances(CompoundTag tag) {
        ListTag handDropChances = tag.getList("HandDropChances", net.minecraft.nbt.Tag.TAG_FLOAT);
        while (handDropChances.size() < 2) {
            handDropChances.add(net.minecraft.nbt.FloatTag.valueOf(0.0F));
        }
        return handDropChances;
    }

    private static List<Failure> buildFailures(ForgeState state) {
        return List.of();
    }

    private static int computeSetupTime(ForgeState state) {
        int adjustedSetupTime = 100 + state.setupTimeDelta - (state.shorterDenser ? 20 : 0);
        return Math.max(20, adjustedSetupTime);
    }

    private static int computeWaveTimeLimit(ForgeState state, int waveIndex, int waveCount, int packs) {
        int base = 1020 + state.profile.level() * 6 + state.waveTimeDelta + (waveIndex >= waveCount - 2 ? state.lateWaveTimeBonus : 0);
        int densityBonus = packs * 100 + Math.max(0, state.minionsPerWave) * 35;
        if (state.profile.level() >= 18) {
            densityBonus += 260 + Math.min(140, (state.profile.level() - 18) * 10);
        }
        return Mth.clamp(base + densityBonus, 220, 2400);
    }

    private static boolean supportsArmorProgression(EntityType<?> entityType) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return path.contains("zombie")
                || path.contains("skeleton")
                || path.contains("piglin")
                || path.contains("pillager")
                || path.contains("vindicator")
                || path.contains("evoker")
                || path.contains("illusioner")
                || path.contains("bogged");
    }

    private static boolean supportsWeaponProgression(EntityType<?> entityType) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return path.contains("zombie")
                || path.contains("husk")
                || path.contains("drowned")
                || path.contains("piglin")
                || path.contains("vindicator");
    }

    private static float armorProgress(int level) {
        return Mth.clamp((level - 15) / 65.0F, 0.0F, 1.0F);
    }

    private static float weaponProgress(int level) {
        return Mth.clamp((level - 25) / 55.0F, 0.0F, 1.0F);
    }

    private static ItemStack[] selectArmorSet(int level, RandomSource random) {
        if (level >= 80) {
            return NETHERITE_ARMOR_SET;
        }

        float tierProgress = armorProgress(level) * 4.0F;
        int baseTier = Mth.floor(tierProgress);
        float upgradeChance = tierProgress - baseTier;
        if (random.nextFloat() < upgradeChance) {
            baseTier++;
        }
        return switch (Mth.clamp(baseTier, 0, 4)) {
            case 0 -> LEATHER_ARMOR_SET;
            case 1 -> CHAINMAIL_ARMOR_SET;
            case 2 -> IRON_ARMOR_SET;
            case 3 -> DIAMOND_ARMOR_SET;
            default -> NETHERITE_ARMOR_SET;
        };
    }

    private static ItemStack[] createArmorSet(net.minecraft.world.item.Item boots, net.minecraft.world.item.Item leggings, net.minecraft.world.item.Item chestplate, net.minecraft.world.item.Item helmet) {
        return new ItemStack[] {
                new ItemStack(boots),
                new ItemStack(leggings),
                new ItemStack(chestplate),
                new ItemStack(helmet)
        };
    }

    private static ItemStack[] createWeaponSet(net.minecraft.world.item.Item low, net.minecraft.world.item.Item mid, net.minecraft.world.item.Item high, net.minecraft.world.item.Item top) {
        return new ItemStack[] {
                new ItemStack(low),
                new ItemStack(mid),
                new ItemStack(high),
                new ItemStack(top)
        };
    }

    private static ItemStack selectWeapon(EntityType<?> entityType, int level, RandomSource random) {
        ItemStack[] set = prefersAxe(entityType) ? AXE_SET : SWORD_SET;
        if (level >= 80) {
            return set[3].copy();
        }

        float tierProgress = weaponProgress(level) * 3.0F;
        int baseTier = Mth.floor(tierProgress);
        float upgradeChance = tierProgress - baseTier;
        if (random.nextFloat() < upgradeChance) {
            baseTier++;
        }
        return set[Mth.clamp(baseTier, 0, 3)].copy();
    }

    private static boolean prefersAxe(EntityType<?> entityType) {
        return entityType == EntityType.VINDICATOR || entityType == EntityType.PIGLIN_BRUTE;
    }

    private static CompoundTag encodeItemStack(ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, stack.copy())
                .result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new);
    }

    private static int finalExperienceReward(ForgeState state) {
        int baseReward = Math.max(1, state.profile.level() * 18 + state.experienceBonus);
        return Math.max(1, (int) Math.round(baseReward * state.experienceRewardMultiplier));
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
        if (state.flatDamageBonus != 0.0D) {
            modifiers.add(WaveModifier.AttributeModifier.create(Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, (float) state.flatDamageBonus));
        }
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
        if (state.profile.level() >= 18) {
            count += 2 + (state.profile.level() >= 28 ? 1 : 0);
        }
        count += random.nextInt(2 + Math.max(1, state.profile.level() / 14));
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
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Theme: " + titleCase(result.theme().name())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(result.color()))));
        lore.add(Component.literal("Crystal Level: " + result.crystalLevel()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));
        lore.add(Component.literal("Difficulty: " + difficultyLabel(result.difficultyEstimate())).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(difficultyColor(result.difficultyEstimate())))));
        for (String line : buildRewardSummary(result)) {
            lore.add(Component.literal(line).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xF0C24B))));
        }
        pearl.set(DataComponents.LORE, new ItemLore(lore));
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
            root.putDouble(COIN_REWARD_MULTIPLIER_KEY, result.coinRewardMultiplier());
            root.putDouble(LEVEL_XP_MULTIPLIER_KEY, result.levelXpMultiplier());
            root.putDouble(EXPERIENCE_REWARD_MULTIPLIER_KEY, result.experienceRewardMultiplier());
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
            restoreGeneratedGateway(
                    entry.getKey(),
                    entry.getValue().json(),
                    entry.getValue().displayName(),
                    entry.getValue().crystalTier(),
                    entry.getValue().crystalLevel(),
                    entry.getValue().coinRewardMultiplier(),
                    entry.getValue().levelXpMultiplier(),
                    entry.getValue().experienceRewardMultiplier());
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

    public static double getGatewayCoinRewardMultiplier(Gateway gateway) {
        ResourceLocation id = GatewayRegistry.INSTANCE.getKey(gateway);
        if (id == null) {
            return 1.0D;
        }
        return GENERATED_GATEWAY_COIN_MULTIPLIERS.getOrDefault(id, 1.0D);
    }

    public static double getGatewayLevelXpMultiplier(Gateway gateway) {
        ResourceLocation id = GatewayRegistry.INSTANCE.getKey(gateway);
        if (id == null) {
            return 1.0D;
        }
        return GENERATED_GATEWAY_LEVEL_XP_MULTIPLIERS.getOrDefault(id, 1.0D);
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

    private static boolean restoreGeneratedGateway(ResourceLocation gatewayId, String gatewayJson, String displayName, int crystalTier, int crystalLevel, double coinRewardMultiplier, double levelXpMultiplier, double experienceRewardMultiplier) {
        if (GatewayRegistry.INSTANCE.getValue(gatewayId) != null) {
            GENERATED_GATEWAY_NAMES.putIfAbsent(gatewayId, displayName);
            GENERATED_GATEWAY_TIERS.putIfAbsent(gatewayId, crystalTier);
            GENERATED_GATEWAY_LEVELS.putIfAbsent(gatewayId, crystalLevel);
            GENERATED_GATEWAY_COIN_MULTIPLIERS.putIfAbsent(gatewayId, coinRewardMultiplier);
            GENERATED_GATEWAY_LEVEL_XP_MULTIPLIERS.putIfAbsent(gatewayId, levelXpMultiplier);
            return false;
        }

        JsonElement json = JsonParser.parseString(gatewayJson);
        NormalGateway gateway = NormalGateway.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(IllegalStateException::new);
        registerGeneratedGateway(gatewayId, gateway);
        GENERATED_GATEWAY_NAMES.put(gatewayId, displayName);
        GENERATED_GATEWAY_TIERS.put(gatewayId, crystalTier);
        GENERATED_GATEWAY_LEVELS.put(gatewayId, crystalLevel);
        GENERATED_GATEWAY_COIN_MULTIPLIERS.put(gatewayId, coinRewardMultiplier);
        GENERATED_GATEWAY_LEVEL_XP_MULTIPLIERS.put(gatewayId, levelXpMultiplier);
        return true;
    }

    private static void persistGeneratedGateway(ServerLevel level, GatewayBuildResult result) {
        persistGeneratedGateway(
                level,
                result.gatewayId(),
                result.gatewayJson(),
                result.name(),
                result.crystalTier(),
                result.crystalLevel(),
                result.coinRewardMultiplier(),
                result.levelXpMultiplier(),
                result.experienceRewardMultiplier());
    }

    private static void persistGeneratedGateway(ServerLevel level, ResourceLocation gatewayId, String gatewayJson, String displayName, int crystalTier, int crystalLevel, double coinRewardMultiplier, double levelXpMultiplier, double experienceRewardMultiplier) {
        GeneratedGatewayStorage.get(level).put(gatewayId, gatewayJson, displayName, crystalTier, crystalLevel, coinRewardMultiplier, levelXpMultiplier, experienceRewardMultiplier);
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
            case COIN_REWARD_MULTIPLIER -> state.coinRewardMultiplier *= Math.max(1.0D, effect.value());
            case LEVEL_XP_MULTIPLIER -> state.levelXpMultiplier *= Math.max(1.0D, effect.value());
            case EXPERIENCE_REWARD_MULTIPLIER -> state.experienceRewardMultiplier *= Math.max(1.0D, effect.value());
            case RARITY_REWARD_MULTIPLIER -> state.rarityRewardMultiplier *= Math.max(1.0D, effect.value());
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

    private static List<String> buildRewardSummary(ForgeState state) {
        return List.of(
                Math.max(0, state.rareRewardRolls) + " Rare item drops",
                Math.max(1, state.finalRewardRolls) + " Epic item drops",
                signedPercent(state.rarityRewardMultiplier - 1.0D) + " item rarity",
                signedPercent(state.rewardMultiplier - 1.0D) + " item quantity",
                "x" + formatMultiplier(state.levelXpMultiplier) + " Levels",
                finalExperienceReward(state) + " XP"
        );
    }

    private static List<String> buildRewardSummary(GatewayBuildResult result) {
        return List.of(
                result.rareRewardDrops() + " Rare item drops",
                result.epicRewardDrops() + " Epic item drops",
                signedPercent(result.rarityRewardMultiplier() - 1.0D) + " item rarity",
                signedPercent(result.rewardMultiplier() - 1.0D) + " item quantity",
                "x" + formatMultiplier(result.levelXpMultiplier()) + " Levels",
                result.finalExperienceReward() + " XP"
        );
    }

    private static String formatMultiplier(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static double naturalHealthMultiplier(int level) {
        if (level <= 10) {
            return 0.04D * level;
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
            return 0.05D * level;
        }
        if (level <= 50) {
            return 0.10D + ((level - 10) / 40.0D) * 0.90D;
        }
        if (level <= 100) {
            return 1.0D + ((level - 50) / 50.0D) * 3.0D;
        }
        return 4.0D;
    }

    private static double naturalFlatDamageBonus(int level) {
        if (level <= 0) {
            return 0.0D;
        }
        if (level <= 10) {
            return level / 10.0D;
        }
        if (level <= 50) {
            return 1.0D + ((level - 10) / 40.0D) * 1.5D;
        }
        if (level <= 100) {
            return 2.5D + ((level - 50) / 50.0D) * 2.5D;
        }
        return 5.0D;
    }

    private static double naturalArmorBonus(int level) {
        if (level <= 0) {
            return 0.0D;
        }
        if (level <= 10) {
            return 0.10D * level;
        }
        if (level <= 50) {
            return 1.0D + ((level - 10) / 40.0D) * 2.0D;
        }
        if (level <= 100) {
            return 3.0D + ((level - 50) / 50.0D) * 3.0D;
        }
        return 6.0D;
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
        private double flatDamageBonus;
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
        private double coinRewardMultiplier = 1.0D;
        private double levelXpMultiplier = 1.0D;
        private double experienceRewardMultiplier = 1.0D;
        private double rarityRewardMultiplier = 1.0D;
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
            this.flatDamageBonus = naturalFlatDamageBonus(profile.level());
            this.armorBonus = naturalArmorBonus(profile.level());
            this.difficultyEstimate = crystalTier.tier() * 8 + profile.level();
        }

        private int waveCount() {
            int base = 3 + (this.profile.level() >= 18 ? 1 : 0) + (this.crystalTier.tier() >= 3 ? 1 : 0) + (this.profile.level() >= 38 ? 1 : 0);
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
