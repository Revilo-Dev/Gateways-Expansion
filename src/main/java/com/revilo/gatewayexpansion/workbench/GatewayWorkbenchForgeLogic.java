package com.revilo.gatewayexpansion.workbench;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.integration.LevelUpIntegration;
import com.revilo.gatewayexpansion.item.AugmentItem;
import com.revilo.gatewayexpansion.item.CatalystItem;
import com.revilo.gatewayexpansion.item.CrystalItem;
import com.revilo.gatewayexpansion.item.data.AugmentDefinition;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import com.revilo.gatewayexpansion.item.data.AugmentStatEntry;
import com.revilo.gatewayexpansion.item.data.CatalystDefinition;
import com.revilo.gatewayexpansion.item.data.CatalystEffectEntry;
import com.revilo.gatewayexpansion.item.data.CrystalForgeData;
import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.Failure;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GatewayWorkbenchForgeLogic {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEBUG_ROOT = GatewayExpansion.MOD_ID;
    private static final String GATEWAY_ID_KEY = "gateway_id";
    private static final String GATEWAY_JSON_KEY = "gateway_json";
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String THEME_KEY = "theme";
    private static final String LEVEL_KEY = "level";
    private static final String PLAYER_LEVEL_KEY = "player_level";
    private static final String AUGMENTS_KEY = "augments";
    private static final String CATALYSTS_KEY = "catalysts";
    private static final String OVERLEVELED_KEY = "overleveled";

    private GatewayWorkbenchForgeLogic() {
    }

    public static boolean canForge(Container container) {
        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystal.getItem() instanceof CrystalItem)) {
            return false;
        }
        if (!container.getItem(GatewayWorkbenchSlots.OUTPUT_SLOT).isEmpty()) {
            return false;
        }
        return !GatewayWorkbenchSlots.collectAugments(container).isEmpty() || !GatewayWorkbenchSlots.collectCatalysts(container).isEmpty();
    }

    public static boolean forge(Player player, Container container) {
        if (!(player instanceof ServerPlayer serverPlayer) || !canForge(container)) {
            return false;
        }

        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystal.getItem() instanceof CrystalItem crystalItem)) {
            return false;
        }

        try {
            CrystalForgeData.CrystalProfile profile = CrystalForgeData.ensureProfile(
                    crystal,
                    crystalItem.crystalTier().minLevel(),
                    crystalItem.crystalTier().maxLevel(),
                    serverPlayer.serverLevel().random
            );
            ForgeState state = buildState(container, crystalItem.crystalTier(), profile);
            GeneratedGateway gatewayDefinition = generateGateway(state);
            validate(gatewayDefinition);

            NormalGateway gateway = gatewayDefinition.toGateway();
            ResourceLocation gatewayId = registerGeneratedGateway(gatewayDefinition.gatewayId(), gateway);
            syncGatewayRegistry(serverPlayer);

            int playerLevel = LevelUpIntegration.getPlayerLevel(serverPlayer);
            boolean overleveled = playerLevel >= 0 && profile.level() > playerLevel;
            ItemStack pearl = createPearl(gatewayDefinition, gatewayId, playerLevel, overleveled);
            consumeInputs(container);
            container.setItem(GatewayWorkbenchSlots.OUTPUT_SLOT, pearl);
            container.setChanged();

            GatewayExpansion.LOGGER.debug("Forged gateway {} for {}:\n{}", gatewayId, serverPlayer.getScoreboardName(), gatewayDefinition.gatewayJson());
            return true;
        } catch (Exception ex) {
            GatewayExpansion.LOGGER.error("Failed to forge gateway pearl for {}", player.getScoreboardName(), ex);
            player.sendSystemMessage(Component.literal("Gateway forge failed. Check the logs for details.").withStyle(ChatFormatting.RED));
            return false;
        }
    }

    public static PreviewData buildPreview(Player player, Container container) {
        ItemStack crystal = container.getItem(GatewayWorkbenchSlots.CRYSTAL_SLOT);
        if (!(crystal.getItem() instanceof CrystalItem crystalItem)) {
            return new PreviewData(0, "None", 0, -1, false, 0, 0, "Unavailable", 0, 0, "Unavailable", List.of());
        }

        CrystalForgeData.CrystalProfile profile = CrystalForgeData.getProfile(
                crystal,
                crystalItem.crystalTier().minLevel(),
                crystalItem.crystalTier().maxLevel()
        );
        ForgeState state = buildState(container, crystalItem.crystalTier(), profile);
        int playerLevel = LevelUpIntegration.getPlayerLevel(player);
        boolean overleveled = playerLevel >= 0 && profile.level() > playerLevel;
        List<String> lines = new ArrayList<>();
        lines.add("Theme: " + titleCase(profile.theme().name()));
        lines.add("Level: " + profile.level());
        if (playerLevel >= 0) {
            lines.add("Player LevelUP: " + playerLevel);
        }
        lines.add("Mob Count: +" + percent(state.populationScale - 1.0D));
        lines.add("Health: +" + percent(state.healthScale - 1.0D));
        lines.add("Reward: +" + percent(state.rewardMultiplier - 1.0D));
        lines.add("Elite Chance: " + Math.round(state.eliteChance * 100.0D) + "%");
        if (!state.augmentSummary.isEmpty()) {
            lines.add("Augments: " + String.join(", ", state.augmentSummary));
        }
        if (!state.catalystSummary.isEmpty()) {
            lines.add("Catalysts: " + String.join(", ", state.catalystSummary));
        }
        return new PreviewData(
                crystalItem.crystalTier().tier(),
                titleCase(profile.theme().name()),
                profile.level(),
                playerLevel,
                overleveled,
                state.augmentCount,
                state.catalystCount,
                difficultyLabel(state.difficultyEstimate),
                percent(state.rewardMultiplier - 1.0D),
                state.waveCount(),
                state.timePressureLabel(),
                lines
        );
    }

    private static ForgeState buildState(Container container, CrystalItem.CrystalTier crystalTier, CrystalForgeData.CrystalProfile profile) {
        ForgeState state = new ForgeState(crystalTier, profile, GatewayWorkbenchSlots.collectAugments(container).size(), GatewayWorkbenchSlots.collectCatalysts(container).size());

        for (ItemStack stack : GatewayWorkbenchSlots.collectAugments(container)) {
            if (stack.getItem() instanceof AugmentItem augmentItem) {
                applyAugment(state, augmentItem.definition());
            }
        }

        for (ItemStack stack : GatewayWorkbenchSlots.collectCatalysts(container)) {
            if (stack.getItem() instanceof CatalystItem catalystItem) {
                applyCatalyst(state, catalystItem.definition());
            }
        }

        state.finish();
        return state;
    }

    private static void applyAugment(ForgeState state, AugmentDefinition definition) {
        state.rewardMultiplier += definition.rewardBonusPercent() / 100.0D;
        state.rewardRolls += definition.rewardBonusPercent() >= 30 ? 1 : 0;
        state.tags.addAll(definition.tags());
        state.augmentSummary.add(titleCase(definition.id()));
        state.difficultyEstimate += switch (definition.difficultyTier()) {
            case EASY -> 6;
            case MEDIUM -> 12;
            case HARD -> 20;
            case EXTREME -> 30;
        };
        state.highestDifficulty = definition.difficultyTier().ordinal() > state.highestDifficulty.ordinal() ? definition.difficultyTier() : state.highestDifficulty;

        for (AugmentStatEntry statEntry : definition.statEntries()) {
            switch (statEntry.category()) {
                case POPULATION -> state.populationScale += 0.08D * statEntry.magnitude();
                case ELITE -> state.eliteChance += 0.04D * statEntry.magnitude();
                case SPEED -> {
                    state.speedScale += 0.05D * statEntry.magnitude();
                    state.setupTimeDelta -= 15 * statEntry.magnitude();
                }
                case HEALTH -> state.healthScale += 0.08D * statEntry.magnitude();
                case DAMAGE -> state.damageScale += 0.07D * statEntry.magnitude();
                case EFFECT -> state.effectPressure += statEntry.magnitude();
                case CHAOS -> {
                    state.chaosPressure += statEntry.magnitude();
                    state.extraWaveMutations += Math.max(1, statEntry.magnitude() / 2);
                }
                case LOOT -> {
                    state.rewardMultiplier += 0.05D * statEntry.magnitude();
                    state.entityLootRolls += statEntry.magnitude();
                }
                case TIME -> state.waveTimeDelta -= 20 * statEntry.magnitude();
                case WAVE -> state.bonusWaves += Math.max(1, statEntry.magnitude() / 2);
            }
        }
    }

    private static void applyCatalyst(ForgeState state, CatalystDefinition definition) {
        applyCatalystEffect(state, definition.positiveEffect(), true);
        applyCatalystEffect(state, definition.negativeEffect(), false);
        state.tags.addAll(definition.tags());
        state.catalystSummary.add(titleCase(definition.id()));
    }

    private static void applyCatalystEffect(ForgeState state, CatalystEffectEntry effect, boolean positive) {
        switch (effect.type()) {
            case WAVE_TIME -> state.waveTimeDelta += (int) Math.round(effect.magnitude());
            case SETUP_TIME -> state.setupTimeDelta += (int) Math.round(effect.magnitude());
            case HEALTH_MULTIPLIER -> state.healthScale += effect.magnitude();
            case DAMAGE_MULTIPLIER -> state.damageScale += effect.magnitude();
            case SPEED_MULTIPLIER -> state.speedScale += effect.magnitude();
            case ARMOR_BONUS -> state.armorBonus += effect.magnitude();
            case ELITE_CHANCE -> state.eliteChance += effect.magnitude();
            case REWARD_ROLLS -> state.rewardRolls += (int) Math.round(effect.magnitude());
            case REWARD_MULTIPLIER -> state.rewardMultiplier += effect.magnitude();
            case RARE_REWARD_CHANCE -> state.rareRewardChance += effect.magnitude();
        }
        state.difficultyEstimate += positive ? 5 : 10;
    }

    private static GeneratedGateway generateGateway(ForgeState state) {
        RandomSource random = RandomSource.create(state.profile.seed());
        List<EntityType<?>> pool = basePool(state.profile.theme(), state.profile.level());
        List<EntityType<?>> elitePool = elitePool(state.profile.theme());
        List<GeneratedWave> waves = new ArrayList<>();

        for (int waveIndex = 0; waveIndex < state.waveCount(); waveIndex++) {
            List<GeneratedEntity> entities = new ArrayList<>();
            List<GeneratedModifier> waveModifiers = new ArrayList<>();
            int groups = 1 + (waveIndex >= 2 ? 1 : 0) + random.nextInt(state.profile.level() >= 24 ? 2 : 1);

            for (int group = 0; group < groups; group++) {
                EntityType<?> type = pool.get(random.nextInt(pool.size()));
                int baseCount = 2 + state.crystalTier.tier() + waveIndex + random.nextInt(2);
                int count = Mth.clamp((int) Math.round(baseCount * state.populationScale), 1, 64);
                entities.add(new GeneratedEntity(EntityType.getKey(type), count, baseEntityModifiers(state), true));
            }

            if (random.nextFloat() < state.eliteChance) {
                EntityType<?> elite = elitePool.get(random.nextInt(elitePool.size()));
                entities.add(new GeneratedEntity(EntityType.getKey(elite), 1, eliteModifiers(state), true));
            }

            if (state.effectPressure > 0) {
                waveModifiers.add(GeneratedModifier.effect(themeEffect(state.profile.theme(), random), Math.max(0, state.effectPressure / 2)));
            }

            List<GeneratedReward> waveRewards = new ArrayList<>();
            if (state.entityLootRolls > 0) {
                EntityType<?> rewardEntity = pool.get(random.nextInt(pool.size()));
                waveRewards.add(GeneratedReward.entityLoot(EntityType.getKey(rewardEntity), state.entityLootRolls));
            }
            if (waveIndex == state.waveCount() - 1 || random.nextFloat() < 0.35F) {
                waveRewards.add(GeneratedReward.experience(5 + state.profile.level() + waveIndex * 2));
            }

            int maxWaveTime = Mth.clamp(360 + state.profile.level() * 10 + waveIndex * 30 + state.waveTimeDelta, 180, 1200);
            int setupTime = waveIndex == state.waveCount() - 1 ? 20 : Mth.clamp(120 + state.setupTimeDelta - waveIndex * 5, 40, 300);
            waves.add(new GeneratedWave(entities, waveModifiers, waveRewards, maxWaveTime, setupTime));
        }

        applyFinalRandomRolls(waves, state, random, pool, elitePool);

        List<GeneratedReward> rewards = new ArrayList<>();
        rewards.add(GeneratedReward.lootTable(themeLootTable(state.profile.theme()), Math.max(1, state.rewardRolls), themeRewardDescription(state.profile.theme())));
        rewards.add(GeneratedReward.experience(15 + state.profile.level() * 2));
        rewards.add(GeneratedReward.chanced(GeneratedReward.lootTable(themeRareLootTable(state.profile.theme()), 1, "Rare Cache"), (float) state.rareRewardChance));

        List<GeneratedFailure> failures = new ArrayList<>();
        failures.add(GeneratedFailure.mobEffect(ResourceLocation.withDefaultNamespace("weakness"), 200 + state.profile.level() * 5, state.crystalTier.tier() >= 4 ? 1 : 0));
        if (state.crystalTier.tier() >= 4) {
            failures.add(GeneratedFailure.chanced(GeneratedFailure.explosion(2.0F + state.crystalTier.tier() * 0.5F, false, false), 0.30F));
        }

        GeneratedRules rules = new GeneratedRules(
                8.0D + state.crystalTier.tier(),
                32.0D + state.crystalTier.tier() * 2.0D,
                false,
                false,
                false,
                true,
                false,
                0.0D,
                32.0D,
                0.0F,
                true,
                Math.max(1, 4 - state.crystalTier.tier() / 2)
        );

        ResourceLocation gatewayId = ResourceLocation.fromNamespaceAndPath(GatewayExpansion.MOD_ID, "forged_" + UUID.randomUUID().toString().replace('-', '_'));
        GeneratedGateway generated = new GeneratedGateway(
                gatewayId,
                generateName(state),
                gatewaySize(state.crystalTier.tier()),
                themeColor(state.profile.theme()),
                waves,
                rewards,
                failures,
                rules,
                state.profile.theme(),
                state.profile.level(),
                state.difficultyEstimate,
                List.copyOf(state.augmentSummary),
                List.copyOf(state.catalystSummary),
                ""
        );
        return generated.withJson(serializeGateway(generated.toGateway()));
    }

    private static List<GeneratedModifier> baseEntityModifiers(ForgeState state) {
        List<GeneratedModifier> modifiers = new ArrayList<>();
        if (state.healthScale > 1.0D) {
            modifiers.add(GeneratedModifier.attribute(attrId(Attributes.MAX_HEALTH), Operation.ADD_MULTIPLIED_TOTAL, state.healthScale - 1.0D));
        }
        if (state.damageScale > 1.0D) {
            modifiers.add(GeneratedModifier.attribute(attrId(Attributes.ATTACK_DAMAGE), Operation.ADD_MULTIPLIED_TOTAL, state.damageScale - 1.0D));
        }
        if (state.speedScale > 1.0D) {
            modifiers.add(GeneratedModifier.attribute(attrId(Attributes.MOVEMENT_SPEED), Operation.ADD_MULTIPLIED_TOTAL, state.speedScale - 1.0D));
        }
        if (state.armorBonus > 0.0D) {
            modifiers.add(GeneratedModifier.attribute(attrId(Attributes.ARMOR), Operation.ADD_VALUE, state.armorBonus));
        }
        return modifiers;
    }

    private static List<GeneratedModifier> eliteModifiers(ForgeState state) {
        List<GeneratedModifier> modifiers = new ArrayList<>(baseEntityModifiers(state));
        modifiers.add(GeneratedModifier.attribute(attrId(Attributes.MAX_HEALTH), Operation.ADD_MULTIPLIED_TOTAL, 0.50D));
        modifiers.add(GeneratedModifier.attribute(attrId(Attributes.ATTACK_DAMAGE), Operation.ADD_MULTIPLIED_TOTAL, 0.30D));
        modifiers.add(GeneratedModifier.attribute(attrId(Attributes.ARMOR), Operation.ADD_VALUE, 4.0D));
        return modifiers;
    }

    private static void applyFinalRandomRolls(List<GeneratedWave> waves, ForgeState state, RandomSource random, List<EntityType<?>> pool, List<EntityType<?>> elitePool) {
        int randomWaveMods = 1 + random.nextInt(state.profile.level() >= 25 ? 2 : 1) + state.extraWaveMutations;
        for (int index = 0; index < randomWaveMods; index++) {
            GeneratedWave wave = waves.get(random.nextInt(waves.size()));
            switch (random.nextInt(3)) {
                case 0 -> {
                    EntityType<?> extra = pool.get(random.nextInt(pool.size()));
                    wave.entities.add(new GeneratedEntity(EntityType.getKey(extra), Mth.clamp(1 + state.crystalTier.tier() + random.nextInt(3), 1, 10), List.of(), true));
                }
                case 1 -> wave.modifiers.add(GeneratedModifier.attribute(attrId(Attributes.MOVEMENT_SPEED), Operation.ADD_MULTIPLIED_TOTAL, 0.08D));
                default -> wave.setupTime = Mth.clamp(wave.setupTime + (random.nextBoolean() ? 20 : -20), 20, 300);
            }
        }

        GeneratedWave rewardWave = waves.get(random.nextInt(waves.size()));
        rewardWave.rewards.add(GeneratedReward.lootTable(themeLootTable(state.profile.theme()), 1, "Spoils"));

        if (state.profile.level() >= 35 || state.crystalTier.tier() >= 4) {
            GeneratedWave finalWave = waves.get(waves.size() - 1);
            EntityType<?> elite = elitePool.get(random.nextInt(elitePool.size()));
            finalWave.entities.add(new GeneratedEntity(EntityType.getKey(elite), 1, List.of(
                    GeneratedModifier.attribute(attrId(Attributes.MAX_HEALTH), Operation.ADD_MULTIPLIED_TOTAL, 0.75D),
                    GeneratedModifier.effect(themeEffect(state.profile.theme(), random), 1)
            ), true));
        }
    }

    private static ItemStack createPearl(GeneratedGateway gateway, ResourceLocation gatewayId, int playerLevel, boolean overleveled) {
        ItemStack pearl = new ItemStack(GatewayObjects.GATE_PEARL.value());
        GatePearlItem.setGate(pearl, GatewayRegistry.INSTANCE.holder(gatewayId));
        pearl.set(DataComponents.CUSTOM_NAME, Component.literal(gateway.name()).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gateway.color()))));
        pearl.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Theme: " + titleCase(gateway.theme().name())),
                Component.literal("Crystal Level: " + gateway.crystalLevel()),
                Component.literal(playerLevel >= 0 ? "Player LevelUP: " + playerLevel : "Player LevelUP: unavailable"),
                Component.literal("Waves: " + gateway.waves().size()),
                Component.literal("Difficulty: " + difficultyLabel(gateway.difficultyEstimate())),
                Component.literal(overleveled ? "Overleveled: Yes" : "Overleveled: No"),
                Component.literal("Augments: " + joinedOrNone(gateway.augmentSummary())),
                Component.literal("Catalysts: " + joinedOrNone(gateway.catalystSummary()))
        )));

        CustomData.update(DataComponents.CUSTOM_DATA, pearl, tag -> {
            CompoundTag root = tag.getCompound(DEBUG_ROOT);
            root.putString(GATEWAY_ID_KEY, gatewayId.toString());
            root.putString(GATEWAY_JSON_KEY, gateway.gatewayJson());
            root.putString(DIFFICULTY_KEY, difficultyLabel(gateway.difficultyEstimate()));
            root.putString(THEME_KEY, gateway.theme().name());
            root.putInt(LEVEL_KEY, gateway.crystalLevel());
            root.putInt(PLAYER_LEVEL_KEY, playerLevel);
            root.putBoolean(OVERLEVELED_KEY, overleveled);
            root.putString(AUGMENTS_KEY, joinedOrNone(gateway.augmentSummary()));
            root.putString(CATALYSTS_KEY, joinedOrNone(gateway.catalystSummary()));
            tag.put(DEBUG_ROOT, root);
        });
        return pearl;
    }

    private static ResourceLocation registerGeneratedGateway(ResourceLocation gatewayId, NormalGateway gateway) {
        DynamicRegistry.DataGenPopulator.runScoped(GatewayRegistry.INSTANCE, populator -> populator.register(gatewayId, gateway));
        return gatewayId;
    }

    private static void syncGatewayRegistry(ServerPlayer player) {
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

    private static void validate(GeneratedGateway gateway) {
        if (gateway.waves().isEmpty()) {
            throw new IllegalStateException("Gateway has no waves.");
        }
        for (GeneratedWave wave : gateway.waves()) {
            if (wave.entities.isEmpty()) {
                throw new IllegalStateException("Gateway has an empty wave.");
            }
            if (wave.maxWaveTime < 20 || wave.setupTime < 0) {
                throw new IllegalStateException("Gateway has invalid timing.");
            }
        }
    }

    private static String serializeGateway(NormalGateway gateway) {
        JsonElement element = NormalGateway.CODEC.encodeStart(JsonOps.INSTANCE, gateway).getOrThrow(IllegalStateException::new);
        return GSON.toJson(element);
    }

    private static List<EntityType<?>> basePool(CrystalTheme theme, int level) {
        return switch (theme) {
            case UNDEAD -> List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, level >= 18 ? EntityType.DROWNED : EntityType.ZOMBIE, level >= 26 ? EntityType.WITCH : EntityType.SKELETON);
            case BEAST -> List.of(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.WOLF, EntityType.POLAR_BEAR, level >= 28 ? EntityType.RAVAGER : EntityType.SPIDER);
            case ARCANE -> List.of(EntityType.SKELETON, EntityType.WITCH, EntityType.PILLAGER, EntityType.ENDERMAN, level >= 30 ? EntityType.EVOKER : EntityType.VEX);
            case NETHER -> List.of(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HOGLIN, level >= 28 ? EntityType.WITHER_SKELETON : EntityType.BLAZE);
        };
    }

    private static List<EntityType<?>> elitePool(CrystalTheme theme) {
        return switch (theme) {
            case UNDEAD -> List.of(EntityType.WITHER_SKELETON, EntityType.WITCH, EntityType.STRAY);
            case BEAST -> List.of(EntityType.RAVAGER, EntityType.POLAR_BEAR, EntityType.SPIDER);
            case ARCANE -> List.of(EntityType.EVOKER, EntityType.ENDERMAN, EntityType.VEX);
            case NETHER -> List.of(EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.PIGLIN_BRUTE);
        };
    }

    private static ResourceLocation themeEffect(CrystalTheme theme, RandomSource random) {
        return switch (theme) {
            case UNDEAD -> random.nextBoolean() ? ResourceLocation.withDefaultNamespace("resistance") : ResourceLocation.withDefaultNamespace("strength");
            case BEAST -> random.nextBoolean() ? ResourceLocation.withDefaultNamespace("speed") : ResourceLocation.withDefaultNamespace("jump_boost");
            case ARCANE -> random.nextBoolean() ? ResourceLocation.withDefaultNamespace("glowing") : ResourceLocation.withDefaultNamespace("regeneration");
            case NETHER -> random.nextBoolean() ? ResourceLocation.withDefaultNamespace("fire_resistance") : ResourceLocation.withDefaultNamespace("strength");
        };
    }

    private static ResourceLocation themeLootTable(CrystalTheme theme) {
        return switch (theme) {
            case UNDEAD -> ResourceLocation.withDefaultNamespace("chests/simple_dungeon");
            case BEAST -> ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft");
            case ARCANE -> ResourceLocation.withDefaultNamespace("chests/desert_pyramid");
            case NETHER -> ResourceLocation.withDefaultNamespace("chests/nether_bridge");
        };
    }

    private static ResourceLocation themeRareLootTable(CrystalTheme theme) {
        return switch (theme) {
            case UNDEAD -> ResourceLocation.withDefaultNamespace("chests/stronghold_library");
            case BEAST -> ResourceLocation.withDefaultNamespace("chests/jungle_temple");
            case ARCANE -> ResourceLocation.withDefaultNamespace("chests/ancient_city");
            case NETHER -> ResourceLocation.withDefaultNamespace("chests/bastion_treasure");
        };
    }

    private static String themeRewardDescription(CrystalTheme theme) {
        return switch (theme) {
            case UNDEAD -> "Crypt Cache";
            case BEAST -> "Predator Cache";
            case ARCANE -> "Arcane Cache";
            case NETHER -> "Infernal Cache";
        };
    }

    private static int themeColor(CrystalTheme theme) {
        return switch (theme) {
            case UNDEAD -> 0x6AA36A;
            case BEAST -> 0xB87B32;
            case ARCANE -> 0x3EA5D9;
            case NETHER -> 0xD9492B;
        };
    }

    private static Gateway.Size gatewaySize(int tier) {
        if (tier >= 5) {
            return Gateway.Size.LARGE;
        }
        if (tier >= 3) {
            return Gateway.Size.MEDIUM;
        }
        return Gateway.Size.SMALL;
    }

    private static String generateName(ForgeState state) {
        String lead = switch (state.profile.theme()) {
            case UNDEAD -> state.populationScale >= 1.25D ? "Swarming" : "Grim";
            case BEAST -> state.speedScale >= 1.15D ? "Feral" : "Ravenous";
            case ARCANE -> state.effectPressure > 0 ? "Mystic" : "Arcane";
            case NETHER -> state.eliteChance >= 0.18D ? "Brutal" : "Infernal";
        };
        String middle = switch (state.profile.theme()) {
            case UNDEAD -> "Undead";
            case BEAST -> "Beast";
            case ARCANE -> "Arcane";
            case NETHER -> "Nether";
        };
        String tail = state.tags.contains("elite") ? "Siege Gateway" : state.tags.contains("volatile") ? "Breach" : "Rift";
        return lead + " " + middle + " " + tail;
    }

    private static String difficultyLabel(int estimate) {
        if (estimate >= 95) {
            return "Extreme";
        }
        if (estimate >= 70) {
            return "Hard";
        }
        if (estimate >= 40) {
            return "Medium";
        }
        return "Easy";
    }

    private static int percent(double value) {
        return (int) Math.round(value * 100.0D);
    }

    private static ResourceLocation attrId(Holder<Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
    }

    private static Holder<Attribute> attrHolder(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(ResourceKey.create(Registries.ATTRIBUTE, id));
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

    public record PreviewData(
            int crystalTier,
            String crystalTheme,
            int crystalLevel,
            int playerLevel,
            boolean overleveled,
            int augmentCount,
            int catalystCount,
            String difficultyName,
            int rewardBonusPercent,
            int waves,
            String timePressure,
            List<String> previewLines
    ) {
        public PreviewData {
            previewLines = List.copyOf(previewLines);
        }
    }

    private static final class ForgeState {
        private final CrystalItem.CrystalTier crystalTier;
        private final CrystalForgeData.CrystalProfile profile;
        private final int augmentCount;
        private final int catalystCount;
        private final Set<String> tags = new LinkedHashSet<>();
        private final List<String> augmentSummary = new ArrayList<>();
        private final List<String> catalystSummary = new ArrayList<>();
        private double populationScale = 1.0D;
        private double healthScale = 1.0D;
        private double damageScale = 1.0D;
        private double speedScale = 1.0D;
        private double armorBonus = 0.0D;
        private double eliteChance = 0.05D;
        private double rewardMultiplier = 1.0D;
        private double rareRewardChance = 0.12D;
        private int rewardRolls = 1;
        private int entityLootRolls = 1;
        private int waveTimeDelta = 0;
        private int setupTimeDelta = 0;
        private int bonusWaves = 0;
        private int extraWaveMutations = 0;
        private int effectPressure = 0;
        private int chaosPressure = 0;
        private int difficultyEstimate;
        private AugmentDifficultyTier highestDifficulty = AugmentDifficultyTier.EASY;

        private ForgeState(CrystalItem.CrystalTier crystalTier, CrystalForgeData.CrystalProfile profile, int augmentCount, int catalystCount) {
            this.crystalTier = crystalTier;
            this.profile = profile;
            this.augmentCount = augmentCount;
            this.catalystCount = catalystCount;
            this.difficultyEstimate = crystalTier.tier() * 8 + profile.level();
        }

        private int waveCount() {
            int count = 3 + (this.crystalTier.tier() >= 3 ? 1 : 0) + (this.profile.level() >= 24 ? 1 : 0) + this.bonusWaves;
            return Mth.clamp(count, 3, 8);
        }

        private String timePressureLabel() {
            int total = this.waveTimeDelta + this.setupTimeDelta;
            if (total <= -80) {
                return "Severe";
            }
            if (total < 0) {
                return "High";
            }
            if (total > 80) {
                return "Relaxed";
            }
            return "Stable";
        }

        private void finish() {
            this.eliteChance = Mth.clamp(this.eliteChance, 0.02D, 0.45D);
            this.rewardMultiplier = Math.max(0.35D, this.rewardMultiplier);
            this.rareRewardChance = Mth.clamp(this.rareRewardChance, 0.05D, 0.85D);
            this.difficultyEstimate += percent(this.healthScale - 1.0D) / 4;
            this.difficultyEstimate += percent(this.damageScale - 1.0D) / 4;
            this.difficultyEstimate += percent(this.speedScale - 1.0D) / 5;
            this.difficultyEstimate += (int) Math.round(this.eliteChance * 60.0D);
            this.difficultyEstimate += this.waveCount() * 4;
        }
    }

    private record GeneratedGateway(
            ResourceLocation gatewayId,
            String name,
            Gateway.Size size,
            int color,
            List<GeneratedWave> waves,
            List<GeneratedReward> rewards,
            List<GeneratedFailure> failures,
            GeneratedRules rules,
            CrystalTheme theme,
            int crystalLevel,
            int difficultyEstimate,
            List<String> augmentSummary,
            List<String> catalystSummary,
            String gatewayJson
    ) {
        private GeneratedGateway withJson(String gatewayJson) {
            return new GeneratedGateway(this.gatewayId, this.name, this.size, this.color, this.waves, this.rewards, this.failures, this.rules, this.theme, this.crystalLevel, this.difficultyEstimate, this.augmentSummary, this.catalystSummary, gatewayJson);
        }

        private NormalGateway toGateway() {
            return NormalGateway.builder()
                    .size(this.size)
                    .color(this.color)
                    .waves(this.waves.stream().map(GeneratedWave::toWave).toList())
                    .keyRewards(this.rewards.stream().map(GeneratedReward::toReward).toList())
                    .failures(this.failures.stream().map(GeneratedFailure::toFailure).toList())
                    .rules(this.rules.toRules())
                    .build();
        }
    }

    private static final class GeneratedWave {
        private final List<GeneratedEntity> entities;
        private final List<GeneratedModifier> modifiers;
        private final List<GeneratedReward> rewards;
        private final int maxWaveTime;
        private int setupTime;

        private GeneratedWave(List<GeneratedEntity> entities, List<GeneratedModifier> modifiers, List<GeneratedReward> rewards, int maxWaveTime, int setupTime) {
            this.entities = new ArrayList<>(entities);
            this.modifiers = new ArrayList<>(modifiers);
            this.rewards = new ArrayList<>(rewards);
            this.maxWaveTime = maxWaveTime;
            this.setupTime = setupTime;
        }

        private Wave toWave() {
            return Wave.builder()
                    .entities(this.entities.stream().map(entity -> (dev.shadowsoffire.gateways.gate.WaveEntity) entity.toWaveEntity()).toList())
                    .modifiers(this.modifiers.stream().map(GeneratedModifier::toModifier).toList())
                    .rewards(this.rewards.stream().map(GeneratedReward::toReward).toList())
                    .maxWaveTime(this.maxWaveTime)
                    .setupTime(this.setupTime)
                    .build();
        }
    }

    private record GeneratedEntity(ResourceLocation entityId, int count, List<GeneratedModifier> modifiers, boolean finalizeSpawn) {
        private StandardWaveEntity toWaveEntity() {
            return StandardWaveEntity.builder(BuiltInRegistries.ENTITY_TYPE.get(this.entityId))
                    .count(this.count)
                    .addModifiers(this.modifiers.stream().map(GeneratedModifier::toModifier).toList())
                    .finalizeSpawn(this.finalizeSpawn)
                    .build();
        }
    }

    private enum ModifierKind {
        ATTRIBUTE,
        EFFECT
    }

    private record GeneratedModifier(ModifierKind kind, ResourceLocation id, Operation operation, double amount, int amplifier) {
        private static GeneratedModifier attribute(ResourceLocation id, Operation operation, double amount) {
            return new GeneratedModifier(ModifierKind.ATTRIBUTE, id, operation, amount, 0);
        }

        private static GeneratedModifier effect(ResourceLocation id, int amplifier) {
            return new GeneratedModifier(ModifierKind.EFFECT, id, Operation.ADD_VALUE, 0.0D, amplifier);
        }

        private WaveModifier toModifier() {
            return switch (this.kind) {
                case ATTRIBUTE -> WaveModifier.AttributeModifier.create(attrHolder(this.id), this.operation, (float) this.amount);
                case EFFECT -> WaveModifier.EffectModifier.create(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, this.id)), this.amplifier);
            };
        }
    }

    private enum RewardKind {
        LOOT_TABLE,
        ENTITY_LOOT,
        EXPERIENCE,
        CHANCED
    }

    private record GeneratedReward(RewardKind kind, ResourceLocation id, int amount, float chance, GeneratedReward nested, String desc) {
        private static GeneratedReward lootTable(ResourceLocation id, int rolls, String desc) {
            return new GeneratedReward(RewardKind.LOOT_TABLE, id, rolls, 1.0F, null, desc);
        }

        private static GeneratedReward entityLoot(ResourceLocation id, int rolls) {
            return new GeneratedReward(RewardKind.ENTITY_LOOT, id, rolls, 1.0F, null, "");
        }

        private static GeneratedReward experience(int amount) {
            return new GeneratedReward(RewardKind.EXPERIENCE, ResourceLocation.withDefaultNamespace("air"), amount, 1.0F, null, "");
        }

        private static GeneratedReward chanced(GeneratedReward nested, float chance) {
            return new GeneratedReward(RewardKind.CHANCED, ResourceLocation.withDefaultNamespace("air"), 0, chance, nested, "");
        }

        private Reward toReward() {
            return switch (this.kind) {
                case LOOT_TABLE -> new Reward.LootTableReward(this.id, Math.max(1, this.amount), this.desc);
                case ENTITY_LOOT -> new Reward.EntityLootReward(BuiltInRegistries.ENTITY_TYPE.get(this.id), null, Math.max(1, this.amount));
                case EXPERIENCE -> new Reward.ExperienceReward(this.amount, 5);
                case CHANCED -> new Reward.ChancedReward(this.nested.toReward(), this.chance);
            };
        }
    }

    private enum FailureKind {
        EXPLOSION,
        MOB_EFFECT,
        CHANCED
    }

    private record GeneratedFailure(FailureKind kind, ResourceLocation id, int duration, int amplifier, float strength, boolean fire, boolean blockDamage, float chance, GeneratedFailure nested) {
        private static GeneratedFailure mobEffect(ResourceLocation id, int duration, int amplifier) {
            return new GeneratedFailure(FailureKind.MOB_EFFECT, id, duration, amplifier, 0.0F, false, false, 1.0F, null);
        }

        private static GeneratedFailure explosion(float strength, boolean fire, boolean blockDamage) {
            return new GeneratedFailure(FailureKind.EXPLOSION, ResourceLocation.withDefaultNamespace("air"), 0, 0, strength, fire, blockDamage, 1.0F, null);
        }

        private static GeneratedFailure chanced(GeneratedFailure nested, float chance) {
            return new GeneratedFailure(FailureKind.CHANCED, ResourceLocation.withDefaultNamespace("air"), 0, 0, 0.0F, false, false, chance, nested);
        }

        private Failure toFailure() {
            return switch (this.kind) {
                case EXPLOSION -> new Failure.ExplosionFailure(this.strength, this.fire, this.blockDamage);
                case MOB_EFFECT -> new Failure.MobEffectFailure(BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, this.id)), this.duration, this.amplifier);
                case CHANCED -> new Failure.ChancedFailure(this.nested.toFailure(), this.chance);
            };
        }
    }

    private record GeneratedRules(
            double spawnRange,
            double leashRange,
            boolean allowDiscarding,
            boolean allowDimChange,
            boolean playerDamageOnly,
            boolean removeOnFailure,
            boolean failOnOutOfBounds,
            double spacing,
            double followRangeBoost,
            float defaultDropChance,
            boolean requiresNearbyPlayer,
            int lives
    ) {
        private GateRules toRules() {
            return GateRules.builder()
                    .spawnRange(this.spawnRange)
                    .leashRange(this.leashRange)
                    .allowDiscarding(this.allowDiscarding)
                    .allowDimChange(this.allowDimChange)
                    .playerDamageOnly(this.playerDamageOnly)
                    .removeOnFailure(this.removeOnFailure)
                    .failOnOutOfBounds(this.failOnOutOfBounds)
                    .spacing(this.spacing)
                    .followRangeBoost(this.followRangeBoost)
                    .defaultDropChance(this.defaultDropChance)
                    .requiresNearbyPlayer(this.requiresNearbyPlayer)
                    .lives(this.lives)
                    .build();
        }
    }
}
