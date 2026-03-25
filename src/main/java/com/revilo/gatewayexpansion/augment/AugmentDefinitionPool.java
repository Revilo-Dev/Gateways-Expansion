package com.revilo.gatewayexpansion.augment;

import com.revilo.gatewayexpansion.gateway.roll.ForgeEffect;
import com.revilo.gatewayexpansion.gateway.roll.ForgeEffectType;
import com.revilo.gatewayexpansion.item.data.AugmentDifficultyTier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class AugmentDefinitionPool {

    private static final Map<String, AugmentDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<AugmentDifficultyTier, List<AugmentDefinition>> BY_TIER = new LinkedHashMap<>();

    static {
        register(a("grave_tide", "Grave Tide", AugmentDifficultyTier.EASY,  tags("population", "undead"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 2, "+2 minions per wave")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "+10% loot")));
        register(a("swarm_wake", "Swarm Wake", AugmentDifficultyTier.EASY, tags("population", "swarm"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 3, "+3 minions per wave")),
                fx(ForgeEffectType.EXTRA_ENTITY_LOOT_ROLLS, 1, "+1 entity loot roll")));
        register(a("pack_call", "Pack Call", AugmentDifficultyTier.EASY, tags("support", "beast"),
                List.of(dual(ForgeEffectType.SUPPORT_PACK_EVERY, 2, 1, "+1 support mob every 2 waves")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.12D, "+12% loot")));
        register(a("pressure_spike", "Pressure Spike", AugmentDifficultyTier.EASY, tags("tempo"),
                List.of(fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Shorter gateway, denser waves")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll")));
        register(a("march_of_bones", "March of Bones", AugmentDifficultyTier.EASY, tags("wave", "undead"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 1, "+1 wave")),
                fx(ForgeEffectType.BONUS_EXPERIENCE, 10, "+10 bonus experience")));
        register(a("acid_fangs", "Acid Fangs", AugmentDifficultyTier.EASY, tags("effect", "beast"),
                List.of(effect("minecraft:poison", 0, "+Poison pressure")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "+10% loot")));
        register(a("hex_surge", "Hex Surge", AugmentDifficultyTier.EASY, tags("effect", "arcane"),
                List.of(effect("minecraft:glowing", 0, "+Glowing enemies")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("cinder_rush", "Cinder Rush", AugmentDifficultyTier.EASY, tags("speed", "nether"),
                List.of(fx(ForgeEffectType.SPEED_MULTIPLIER, 0.10D, "+10% movement speed")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.10D, "+10% loot")));

        register(a("late_harvest", "Late Harvest", AugmentDifficultyTier.MEDIUM, tags("wave", "reward"),
                List.of(fx(ForgeEffectType.EXTEND_LATE_WAVES, 50, "Extended later waves")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll")));
        register(a("bolstered_dead", "Bolstered Dead", AugmentDifficultyTier.MEDIUM, tags("health", "undead"),
                List.of(fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.18D, "+18% health")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.15D, "+15% loot")));
        register(a("ambush_lines", "Ambush Lines", AugmentDifficultyTier.MEDIUM, tags("ranged", "arcane"),
                List.of(fx(ForgeEffectType.RANGED_PACKS, 1, "More ranged enemies"), fx(ForgeEffectType.PROJECTILE_DAMAGE, 0.15D, "+15% ranged damage")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("predator_stride", "Predator Stride", AugmentDifficultyTier.MEDIUM, tags("speed", "beast"),
                List.of(fx(ForgeEffectType.SPEED_MULTIPLIER, 0.15D, "+15% speed"), effect("minecraft:speed", 0, "Enemies gain Speed")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.15D, "+15% loot")));
        register(a("iron_advance", "Iron Advance", AugmentDifficultyTier.MEDIUM, tags("elite", "armor"),
                List.of(fx(ForgeEffectType.ARMOR_BONUS, 4, "+4 armor"), fx(ForgeEffectType.ELITE_CHANCE, 0.08D, "+8% elite chance")),
                fx(ForgeEffectType.EXTRA_ENTITY_LOOT_ROLLS, 1, "+1 entity loot roll")));
        register(a("grave_command", "Grave Command", AugmentDifficultyTier.MEDIUM, tags("elite", "undead"),
                List.of(fx(ForgeEffectType.ELITE_EVERY, 2, "+1 elite every 2 waves")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("chaos_breach", "Chaos Breach", AugmentDifficultyTier.MEDIUM, tags("chaos"),
                List.of(fx(ForgeEffectType.MIXED_PACKS, 1, "Mixed enemy pool additions"), fx(ForgeEffectType.THEMED_REINFORCEMENTS, 1, "Random reinforcements")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.18D, "+18% loot")));
        register(a("burning_raiders", "Burning Raiders", AugmentDifficultyTier.MEDIUM, tags("nether", "damage"),
                List.of(effect("minecraft:fire_resistance", 0, "Enemies resist fire"), fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.15D, "+15% damage")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 1, "+1 final reward roll")));

        register(a("siege_lords", "Siege Lords", AugmentDifficultyTier.HARD, tags("elite", "finale"),
                List.of(fx(ForgeEffectType.FINAL_WAVE_ELITES, 2, "+2 stronger final wave units"), fx(ForgeEffectType.ELITE_CHANCE, 0.12D, "+12% elite chance")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("reaper_arms", "Reaper Arms", AugmentDifficultyTier.HARD, tags("damage", "undead"),
                List.of(fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.22D, "+22% damage"), fx(ForgeEffectType.ARMOR_PIERCE, 2, "+2 armor pierce")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.20D, "+20% loot")));
        register(a("unyielding_hide", "Unyielding Hide", AugmentDifficultyTier.HARD, tags("beast", "defense"),
                List.of(fx(ForgeEffectType.KNOCKBACK_RESISTANCE, 0.25D, "+25% knockback resistance"), fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.20D, "+20% health")),
                fx(ForgeEffectType.EXTRA_ENTITY_LOOT_ROLLS, 2, "+2 entity loot rolls")));
        register(a("void_wardens", "Void Wardens", AugmentDifficultyTier.HARD, tags("arcane", "elite"),
                List.of(effect("minecraft:regeneration", 0, "Enemies regenerate"), fx(ForgeEffectType.ELITE_UPGRADE_CHANCE, 0.18D, "Elite upgrades on some waves")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("hellbound_march", "Hellbound March", AugmentDifficultyTier.HARD, tags("nether", "wave"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 2, "+2 waves")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("venom_furnace", "Venom Furnace", AugmentDifficultyTier.HARD, tags("effect", "damage"),
                List.of(effect("minecraft:poison", 1, "Stronger poison"), fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.18D, "+18% damage")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.18D, "+18% loot")));
        register(a("blood_tithe", "Blood Tithe", AugmentDifficultyTier.HARD, tags("lifesteal"),
                List.of(fx(ForgeEffectType.LIFE_STEAL, 0.10D, "+10% life steal")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 1, "+1 rare reward roll")));
        register(a("crossfire_choir", "Crossfire Choir", AugmentDifficultyTier.HARD, tags("ranged", "arcane"),
                List.of(fx(ForgeEffectType.RANGED_PACKS, 2, "Heavy ranged pressure"), fx(ForgeEffectType.PROJECTILE_DAMAGE, 0.25D, "+25% ranged damage")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.22D, "+22% loot")));

        register(a("cataclysm_engine", "Cataclysm Engine", AugmentDifficultyTier.EXTREME, tags("extreme", "chaos"),
                List.of(fx(ForgeEffectType.BONUS_WAVES, 2, "+2 waves"), fx(ForgeEffectType.THEMED_REINFORCEMENTS, 2, "Heavy random reinforcements"), fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Dangerous final wave bonus")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("nightmare_legion", "Nightmare Legion", AugmentDifficultyTier.EXTREME, tags("extreme", "elite"),
                List.of(fx(ForgeEffectType.ELITE_EVERY, 1, "+1 elite every wave"), fx(ForgeEffectType.FINAL_WAVE_ELITES, 3, "+3 final wave elites"), fx(ForgeEffectType.HEALTH_MULTIPLIER, 0.28D, "+28% health")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("ashen_stampede", "Ashen Stampede", AugmentDifficultyTier.EXTREME, tags("beast", "nether"),
                List.of(fx(ForgeEffectType.ADD_MINIONS_PER_WAVE, 4, "+4 minions per wave"), fx(ForgeEffectType.SPEED_MULTIPLIER, 0.22D, "+22% speed"), fx(ForgeEffectType.SHORTER_DENSER_WAVES, 1, "Dense encounter")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.25D, "+25% loot")));
        register(a("deathless_coven", "Deathless Coven", AugmentDifficultyTier.EXTREME, tags("undead", "effect"),
                List.of(effect("minecraft:strength", 0, "Enemies gain Strength"), effect("minecraft:regeneration", 0, "Enemies regenerate"), fx(ForgeEffectType.MINIBOSS_CHANCE, 0.20D, "Occasional miniboss chance")),
                fx(ForgeEffectType.EXTRA_RARE_REWARD_ROLLS, 2, "+2 rare reward rolls")));
        register(a("shattered_observatory", "Shattered Observatory", AugmentDifficultyTier.EXTREME, tags("arcane", "special"),
                List.of(fx(ForgeEffectType.NAMED_ELITE_CHANCE, 0.30D, "Named enemy injection"), fx(ForgeEffectType.RANGED_PACKS, 2, "Arcane ranged pressure"), effect("minecraft:slowness", 0, "Slowing attacks")),
                fx(ForgeEffectType.EXTRA_FINAL_REWARD_ROLLS, 2, "+2 final reward rolls")));
        register(a("infernal_overrun", "Infernal Overrun", AugmentDifficultyTier.EXTREME, tags("nether", "finale"),
                List.of(fx(ForgeEffectType.DAMAGE_MULTIPLIER, 0.25D, "+25% damage"), fx(ForgeEffectType.ARMOR_PIERCE, 3, "+3 armor pierce"), fx(ForgeEffectType.DANGEROUS_FINAL_WAVE, 1, "Infernal final wave")),
                fx(ForgeEffectType.REWARD_MULTIPLIER, 0.28D, "+28% loot")));
    }

    private AugmentDefinitionPool() {
    }

    public static AugmentDefinition getById(String id) {
        return BY_ID.get(id);
    }

    public static AugmentDefinition random(AugmentDifficultyTier tier, RandomSource random) {
        List<AugmentDefinition> entries = BY_TIER.getOrDefault(tier, List.of());
        return entries.get(random.nextInt(entries.size()));
    }

    public static AugmentDefinition fallback(AugmentDifficultyTier tier) {
        return BY_TIER.getOrDefault(tier, List.of()).getFirst();
    }

    private static void register(AugmentDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_TIER.computeIfAbsent(definition.difficultyTier(), ignored -> new ArrayList<>()).add(definition);
    }

    private static AugmentDefinition a(String id, String title, AugmentDifficultyTier tier, Set<String> tags, List<ForgeEffect> modifiers, ForgeEffect reward) {
        return new AugmentDefinition(id, title, tier, modifiers, reward, tags);
    }

    private static ForgeEffect fx(ForgeEffectType type, double value, String description) {
        return ForgeEffect.of(type, value, description);
    }

    private static ForgeEffect dual(ForgeEffectType type, double value, double secondaryValue, String description) {
        return ForgeEffect.dual(type, value, secondaryValue, description);
    }

    private static ForgeEffect effect(String effectId, int amplifier, String description) {
        return ForgeEffect.ref(ForgeEffectType.MOB_EFFECT, ResourceLocation.parse(effectId), amplifier, 0.0D, description);
    }

    private static Set<String> tags(String... tags) {
        return Set.of(tags);
    }
}
