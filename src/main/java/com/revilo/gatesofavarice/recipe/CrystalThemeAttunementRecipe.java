package com.revilo.gatesofavarice.recipe;

import com.revilo.gatesofavarice.item.CrystalItem;
import com.revilo.gatesofavarice.item.data.CrystalForgeData;
import com.revilo.gatesofavarice.item.data.CrystalTheme;
import com.revilo.gatesofavarice.registry.ModItems;
import com.revilo.gatesofavarice.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CrystalThemeAttunementRecipe extends CustomRecipe {

    public CrystalThemeAttunementRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !resolveTheme(input).stack().isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Match match = resolveTheme(input);
        if (match.stack().isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.stack().copyWithCount(1);
        CrystalForgeData.attuneTheme(result, match.theme());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRYSTAL_THEME_ATTUNEMENT.get();
    }

    private static Match resolveTheme(CraftingInput input) {
        if (input.width() < 3 || input.height() < 3) {
            return Match.EMPTY;
        }

        ItemStack center = input.getItem(4);
        if (!(center.getItem() instanceof CrystalItem)) {
            return Match.EMPTY;
        }

        CrystalTheme theme = null;
        for (int slot = 0; slot < input.size(); slot++) {
            if (slot == 4) {
                continue;
            }
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                return Match.EMPTY;
            }
            CrystalTheme slotTheme = themeForItem(stack.getItem());
            if (slotTheme == null) {
                return Match.EMPTY;
            }
            if (theme == null) {
                theme = slotTheme;
            } else if (theme != slotTheme) {
                return Match.EMPTY;
            }
        }

        if (theme == null) {
            return Match.EMPTY;
        }
        return new Match(center, theme);
    }

    private static CrystalTheme themeForItem(Item item) {
        if (item == ModItems.HARDENED_FLESH.get()) {
            return CrystalTheme.UNDEAD;
        }
        if (item == ModItems.RUSTY_COIN.get()) {
            return CrystalTheme.RAIDER;
        }
        if (item == ModItems.ARCANE_ESSENCE.get()) {
            return CrystalTheme.ARCANE;
        }
        if (item == ModItems.SOLAR_SHARD.get()) {
            return CrystalTheme.NETHER;
        }
        return null;
    }

    private record Match(ItemStack stack, CrystalTheme theme) {
        private static final Match EMPTY = new Match(ItemStack.EMPTY, CrystalTheme.UNDEAD);
    }
}
