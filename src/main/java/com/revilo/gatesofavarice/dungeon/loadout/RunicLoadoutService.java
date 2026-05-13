package com.revilo.gatesofavarice.dungeon.loadout;

import com.revilo.gatesofavarice.GatewayExpansion;
import com.revilo.gatesofavarice.config.GatewayExpansionConfig;
import com.revilo.gatesofavarice.dungeon.loadout.LoadoutModels.EffectSpec;
import com.revilo.gatesofavarice.dungeon.loadout.LoadoutModels.StatRollRange;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.component.CustomData;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

public final class RunicLoadoutService {
    private RunicLoadoutService() {}

    public static ItemStack createRunicPreset(ServerLevel level, ItemStack base, List<StatRollRange> stats, List<EffectSpec> effects, RandomSource random) {
        ItemStack stack = base.copy();
        applyLoadoutStats(level, stack, stats, random);
        applyLoadoutEffects(level, stack, effects, random);
        RuneSlots.syncUsedToContents(stack);
        return stack;
    }

    public static ItemStack applyLoadoutStats(ServerLevel level, ItemStack stack, List<StatRollRange> stats, RandomSource random) {
        RuneStats merged = RuneStats.get(stack);
        for (StatRollRange spec : stats) {
            RuneStatType type = RuneStatType.byId(spec.statId());
            if (type == null) {
                GatewayExpansion.LOGGER.warn("Unknown rune stat id in loadout preset: {}", spec.statId());
                continue;
            }
            float raw = spec.min() + random.nextFloat() * (spec.max() - spec.min());
            float value = raw * GatewayExpansionConfig.LOADOUT_STAT_ROLL_MULTIPLIER.get().floatValue();
            merged = RuneStats.combine(merged, RuneStats.single(type, value));
        }
        RuneStats.set(stack, merged);
        RuneSlots.syncUsedToContents(stack);
        return stack;
    }

    public static ItemStack applyLoadoutEffects(ServerLevel level, ItemStack stack, List<EffectSpec> effects, RandomSource random) {
        ItemEnchantments current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        for (EffectSpec effect : effects) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, effect.enchantmentId());
            Holder.Reference<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
            if (holder == null) {
                GatewayExpansion.LOGGER.warn("Unknown effect enchant id in loadout preset: {}", effect.enchantmentId());
                continue;
            }
            if (!RuneItem.isEffectEnchantment(holder)) {
                GatewayExpansion.LOGGER.warn("Rejected non-effect enchantment in loadout preset: {}", effect.enchantmentId());
                continue;
            }
            int levelRoll = effect.minLevel() + random.nextInt(Math.max(1, effect.maxLevel() - effect.minLevel() + 1));
            int clamped = RuneItem.clampEffectLevel(holder, levelRoll);
            mutable.set(holder, Math.max(mutable.getLevel(holder), clamped));
        }
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        RuneSlots.syncUsedToContents(stack);
        return stack;
    }

    public static void tagLoadoutIdentity(ItemStack stack, String loadoutId, String setName, String pieceId) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.getCompound(GatewayExpansion.MOD_ID);
            root.putString("loadout_id", loadoutId);
            root.putString("loadout_set_name", setName);
            root.putString("armor_piece", pieceId);
            tag.put(GatewayExpansion.MOD_ID, root);
        });
    }

    public static boolean isAllowedEffect(ResourceLocation enchantmentId) {
        return RuneItem.allowedEffectIds().contains(enchantmentId);
    }

    public static int clampEffectLevel(Holder<Enchantment> holder, int requestedLevel) {
        return RuneItem.clampEffectLevel(holder, requestedLevel);
    }

    public static void syncRunicSlots(ItemStack stack) {
        RuneSlots.syncUsedToContents(stack);
    }

    public static float cursedMultiplier(ItemStack stack) {
        return GearAttributes.cursedMultiplier(stack);
    }

    public static List<RuneStatType> parseTypes(List<String> ids) {
        ArrayList<RuneStatType> out = new ArrayList<>(ids.size());
        for (String id : ids) {
            RuneStatType type = RuneStatType.byId(id);
            if (type != null) out.add(type);
        }
        return out;
    }
}
