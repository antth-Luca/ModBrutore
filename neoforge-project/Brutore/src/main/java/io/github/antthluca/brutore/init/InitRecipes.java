package io.github.antthluca.brutore.init;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(
            Registries.RECIPE_SERIALIZER, Brutore.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(
            Registries.RECIPE_TYPE, Brutore.MODID);

    // Serializers
    public static final DeferredHolder<RecipeSerializer<?>,
            RecipeSerializer<RawInatorRecipe>> RAW_INATOR_SERIALIZER = SERIALIZERS.register(
                    "raw_inator_serializer", RawInatorRecipe.Serializer::new);

    // Types
    public static final DeferredHolder<RecipeType<?>,
            RecipeType<RawInatorRecipe>> RAW_INATOR_TYPE = TYPES.register(
                    "raw_inator_type", () -> new RecipeType<RawInatorRecipe>() {
                        @Override
                public String toString() { return "raw_inator_type" }
            });
}
