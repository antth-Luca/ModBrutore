package io.github.antthluca.brutore.recipes.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.antthluca.brutore.init.InitRecipes;
import io.github.antthluca.brutore.recipes.input.RawInatorRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record RawInatorRecipe(Ingredient input, ItemStack output) implements Recipe<RawInatorRecipeInput> {
    // SUPER
    @Override
    public boolean matches(RawInatorRecipeInput rawInatorRecipeInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return input.test(rawInatorRecipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(RawInatorRecipeInput rawInatorRecipeInput, HolderLookup.Provider provider) { return output.copy(); }

    @Override
    public RecipeSerializer<? extends Recipe<RawInatorRecipeInput>> getSerializer() { return InitRecipes.RAW_INATOR_SERIALIZER.get(); }

    @Override
    public RecipeType<? extends Recipe<RawInatorRecipeInput>> getType() {
        return InitRecipes.RAW_INATOR_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.create(input); }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    // IMPLEMENTS
    public static class Serializer implements RecipeSerializer<RawInatorRecipe> {
        public static final MapCodec<RawInatorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(RawInatorRecipe::input),
                ItemStack.CODEC.fieldOf("result").forGetter(RawInatorRecipe::output)
        ).apply(inst, RawInatorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RawInatorRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, RawInatorRecipe::input,
                        ItemStack.STREAM_CODEC, RawInatorRecipe::output,
                        RawInatorRecipe::new);

        @Override
        public MapCodec<RawInatorRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RawInatorRecipe> streamCodec() { return STREAM_CODEC; }
    }

    // MAIN
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }
}
