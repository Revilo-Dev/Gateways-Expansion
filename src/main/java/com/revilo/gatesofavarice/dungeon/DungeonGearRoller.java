package com.revilo.gatesofavarice.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;

public final class DungeonGearRoller {

    private DungeonGearRoller() {
    }

    public static void rollAndBind(ItemStack stack, RandomSource random) {
        if (stack.isEmpty()) {
            return;
        }
        if (!isDungeonGear(stack)) {
            DungeonBoundItems.markIfDungeonBound(stack);
            return;
        }

        DungeonBoundItems.markIfDungeonBound(stack);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        clearOldAffixes(stack);

        List<Component> lore = new ArrayList<>();
        if (stack.getItem() instanceof ArmorItem) addArmorAffixes(random, lore);
        else addWeaponAffixes(random, lore);
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
    }

    private static void addArmorAffixes(RandomSource random, List<Component> lore) {
        double resist = rollPercent(random, 0.02D, 0.12D);
        double thorns = rollPercent(random, 0.04D, 0.22D);
        double health = rollPercent(random, 0.04D, 0.14D);
        double kbResist = rollPercent(random, 0.05D, 0.2D);

        lore.add(line("Resistance", resist));
        lore.add(line("Thorns", thorns));
        lore.add(line("Health", health));
        lore.add(line("Knockback Resist", kbResist));
    }

    private static void addWeaponAffixes(RandomSource random, List<Component> lore) {
        double damage = rollPercent(random, 0.06D, 0.2D);
        double knockback = rollPercent(random, 0.04D, 0.18D);
        double speed = rollPercent(random, 0.03D, 0.15D);
        double fire = rollPercent(random, 0.08D, 0.35D);

        lore.add(line("Damage", damage));
        lore.add(line("Knockback", knockback));
        lore.add(line("Attack Speed", speed));
        lore.add(line("Fire", fire));
    }

    private static void clearOldAffixes(ItemStack stack) {
        stack.set(DataComponents.LORE, ItemLore.EMPTY);
    }

    private static double rollPercent(RandomSource random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static Component line(String label, double value) {
        return Component.literal(label + " +" + String.format(Locale.ROOT, "%.0f%%", value * 100.0D)).withStyle(ChatFormatting.AQUA);
    }

    public static boolean isDungeonGear(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
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
