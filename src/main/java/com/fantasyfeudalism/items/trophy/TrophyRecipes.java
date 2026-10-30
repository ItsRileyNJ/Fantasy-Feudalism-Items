package com.fantasyfeudalism.items.trophy;
import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;
public final class TrophyRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FeudalItems.ID);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<TrophyRecipe>> SERIALIZER = REGISTRY.register("trophy", () -> new SimpleCraftingRecipeSerializer<>(TrophyRecipe::new));
}
