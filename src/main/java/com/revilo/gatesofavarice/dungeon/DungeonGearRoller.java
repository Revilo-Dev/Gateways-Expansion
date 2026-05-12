package com.revilo.gatesofavarice.dungeon;

import com.revilo.gatesofavarice.item.MagnetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.core.RegistryAccess;

public final class DungeonGearRoller {

    private static final ResourceLocation ARMOR_POINTS_BONUS_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_armor_points_bonus");
    private static final ResourceLocation WEAPON_DAMAGE_BONUS_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_weapon_damage_bonus");
    private static final ResourceLocation WEAPON_DAMAGE_VAR_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_weapon_damage_variance");
    private static final ResourceLocation WEAPON_KB_VAR_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_weapon_knockback_variance");
    private static final ResourceLocation WEAPON_SPEED_VAR_ID = ResourceLocation.fromNamespaceAndPath("gatesofavarice", "dungeon_weapon_speed_variance");
    private static final double LEVEL_WEIGHT = 0.65D;
    private static final double TIME_WEIGHT = 0.35D;
    private static final double MAX_LEVEL_FOR_SCALING = 100.0D;
    private static final double MAX_MINUTES_FOR_SCALING = 15.0D;
    private static final double TICKS_PER_MINUTE = 1200.0D;

    private DungeonGearRoller() {
    }

    public static void rollAndBind(ItemStack stack, RandomSource random) {
        rollAndBind(stack, random, 1, 0L, null);
    }

    public static void rollAndBind(ItemStack stack, RandomSource random, int playerLevel, long timeInDungeonTicks) {
        rollAndBind(stack, random, playerLevel, timeInDungeonTicks, null);
    }

    public static void rollAndBind(ItemStack stack, RandomSource random, int playerLevel, long timeInDungeonTicks, RegistryAccess registryAccess) {
        if (stack.isEmpty()) {
            return;
        }
        if (!isDungeonGear(stack)) {
            DungeonBoundItems.markIfDungeonBound(stack);
            return;
        }

        DungeonBoundItems.markIfDungeonBound(stack);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        clearOldAffixesAndBonuses(stack);

        List<Component> lore = new ArrayList<>();
        int safeLevel = Math.max(1, playerLevel);
        long safeTicks = Math.max(0L, timeInDungeonTicks);
        if (stack.getItem() instanceof ArmorItem armorItem) {
            addArmorAffixes(stack, armorItem, random, lore, safeLevel, safeTicks);
        } else {
            addWeaponAffixes(stack, random, lore, safeLevel, safeTicks, registryAccess);
        }
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
    }

    private static void addArmorAffixes(ItemStack stack, ArmorItem armorItem, RandomSource random, List<Component> lore, int playerLevel, long timeInDungeonTicks) {
        double resist = rollPercent(random, 0.02D, 0.12D);
        double thorns = rollPercent(random, 0.04D, 0.22D);
        double health = rollPercent(random, 0.04D, 0.14D);
        double kbResist = rollPercent(random, 0.05D, 0.2D);
        double progress = levelAndTimeProgress(playerLevel, timeInDungeonTicks);
        double armorPoints = rollFlat(random, 0.5D + progress * 0.8D, 2.0D + progress * 4.0D);

        lore.add(line("Resistance", resist));
        lore.add(line("Thorns", thorns));
        lore.add(line("Health", health));
        lore.add(line("Knockback Resist", kbResist));
        lore.add(flatLine("Armor Points", armorPoints));
        addOrReplaceModifier(stack, Attributes.ARMOR, ARMOR_POINTS_BONUS_ID, armorPoints, EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot()));
    }

    private static void addWeaponAffixes(ItemStack stack, RandomSource random, List<Component> lore, int playerLevel, long timeInDungeonTicks, RegistryAccess registryAccess) {
        double damage = rollPercent(random, 0.06D, 0.2D);
        double knockback = rollPercent(random, 0.04D, 0.18D);
        double speed = rollPercent(random, 0.03D, 0.15D);
        double fire = rollPercent(random, 0.08D, 0.35D);
        double progress = levelAndTimeProgress(playerLevel, timeInDungeonTicks);
        double flatDamage = rollFlat(random, 0.4D + progress * 0.7D, 1.8D + progress * 4.8D);
        double damageVariance = rollFlat(random, -0.10D, 0.10D);
        double knockbackVariance = rollFlat(random, -0.10D, 0.10D);
        double speedVariance = rollFlat(random, -0.10D, 0.10D);

        lore.add(line("Damage", damage));
        lore.add(line("Knockback", knockback));
        lore.add(line("Attack Speed", speed));
        lore.add(line("Fire", fire));
        lore.add(flatLine("Weapon Damage", flatDamage));
        lore.add(varianceLine("Damage Var", damageVariance));
        lore.add(varianceLine("Knockback Var", knockbackVariance));
        lore.add(varianceLine("Attack Speed Var", speedVariance));
        addOrReplaceModifier(stack, Attributes.ATTACK_DAMAGE, WEAPON_DAMAGE_BONUS_ID, flatDamage, EquipmentSlotGroup.MAINHAND);
        addOrReplaceModifier(stack, Attributes.ATTACK_DAMAGE, WEAPON_DAMAGE_VAR_ID, damageVariance, EquipmentSlotGroup.MAINHAND, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addOrReplaceModifier(stack, Attributes.ATTACK_KNOCKBACK, WEAPON_KB_VAR_ID, knockbackVariance, EquipmentSlotGroup.MAINHAND, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        addOrReplaceModifier(stack, Attributes.ATTACK_SPEED, WEAPON_SPEED_VAR_ID, speedVariance, EquipmentSlotGroup.MAINHAND, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyRandomEnchantments(stack, random, playerLevel, registryAccess);
    }

    private static void clearOldAffixesAndBonuses(ItemStack stack) {
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers rebuilt = ItemAttributeModifiers.EMPTY;
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            ResourceLocation id = entry.modifier().id();
            if (!ARMOR_POINTS_BONUS_ID.equals(id)
                    && !WEAPON_DAMAGE_BONUS_ID.equals(id)
                    && !WEAPON_DAMAGE_VAR_ID.equals(id)
                    && !WEAPON_KB_VAR_ID.equals(id)
                    && !WEAPON_SPEED_VAR_ID.equals(id)) {
                rebuilt = rebuilt.withModifierAdded(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, rebuilt);
    }

    private static double rollPercent(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double rollFlat(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double levelAndTimeProgress(int playerLevel, long timeInDungeonTicks) {
        double levelFactor = Math.min(1.0D, playerLevel / MAX_LEVEL_FOR_SCALING);
        double minutesInDungeon = timeInDungeonTicks / TICKS_PER_MINUTE;
        double timeFactor = Math.min(1.0D, minutesInDungeon / MAX_MINUTES_FOR_SCALING);
        return levelFactor * LEVEL_WEIGHT + timeFactor * TIME_WEIGHT;
    }

    private static Component line(String label, double value) {
        return Component.literal(label + " +" + String.format(Locale.ROOT, "%.0f%%", value * 100.0D)).withStyle(ChatFormatting.AQUA);
    }

    private static Component flatLine(String label, double value) {
        return Component.literal(label + " +" + String.format(Locale.ROOT, "%.1f", value)).withStyle(ChatFormatting.GOLD);
    }

    private static Component varianceLine(String label, double value) {
        String sign = value >= 0.0D ? "+" : "";
        return Component.literal(label + " " + sign + String.format(Locale.ROOT, "%.0f%%", value * 100.0D))
                .withStyle(value >= 0.0D ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static void addOrReplaceModifier(ItemStack stack, Holder<Attribute> attribute, ResourceLocation id, double amount, EquipmentSlotGroup slotGroup) {
        addOrReplaceModifier(stack, attribute, id, amount, slotGroup, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void addOrReplaceModifier(ItemStack stack, Holder<Attribute> attribute, ResourceLocation id, double amount, EquipmentSlotGroup slotGroup,
            AttributeModifier.Operation operation) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        modifiers = modifiers.withModifierAdded(attribute, new AttributeModifier(id, amount, operation), slotGroup);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }

    private static void applyRandomEnchantments(ItemStack stack, RandomSource random, int playerLevel, RegistryAccess registryAccess) {
        if (registryAccess == null) {
            return;
        }
        int enchantPower = 6 + Math.min(24, Math.max(1, playerLevel) / 4);
        try {
            java.util.stream.Stream<Holder<Enchantment>> all = registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                    .listElements()
                    .map(holder -> (Holder<Enchantment>) holder);
            EnchantmentHelper.enchantItem(random, stack, enchantPower, all);
        } catch (Exception ignored) {
        }
    }

    public static boolean isDungeonGear(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MagnetItem
                || isArsenalWeapon(stack);
    }

    public static boolean isArsenalWeapon(ItemStack stack) {
        ResourceLocation id = stack.getItemHolder().unwrapKey().map(key -> key.location()).orElse(null);
        return id != null && "arsenal".equals(id.getNamespace()) && (id.getPath().contains("sword")
                || id.getPath().contains("dagger")
                || id.getPath().contains("machete")
                || id.getPath().contains("glaive")
                || id.getPath().contains("hammer")
                || id.getPath().contains("broadsword")
                || id.getPath().contains("longsword")
                || id.getPath().contains("gaundao"));
    }
}
