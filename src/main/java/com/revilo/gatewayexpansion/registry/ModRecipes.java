package com.revilo.gatewayexpansion.registry;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.recipe.CrystalThemeAttunementRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, GatewayExpansion.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrystalThemeAttunementRecipe>> CRYSTAL_THEME_ATTUNEMENT =
            RECIPE_SERIALIZERS.register("crystal_theme_attunement", () -> new SimpleCraftingRecipeSerializer<>(CrystalThemeAttunementRecipe::new));

    private ModRecipes() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
