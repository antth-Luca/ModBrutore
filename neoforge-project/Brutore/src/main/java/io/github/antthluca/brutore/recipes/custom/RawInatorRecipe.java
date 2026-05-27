package io.github.antthluca.brutore.recipes.custom;

import io.github.antthluca.brutore.recipes.input.RawInatorRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record RawInatorRecipe(Ingredient input, ItemStack output) implements Recipe<RawInatorRecipeInput> {
    @Override
    public boolean matches(RawInatorRecipeInput rawInatorRecipeInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RawInatorRecipeInput rawInatorRecipeInput, HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RawInatorRecipeInput>> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<? extends Recipe<RawInatorRecipeInput>> getType() {
        return null;
    }

    @Override
    public PlacementInfo placementInfo() {
        return null;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }
}
