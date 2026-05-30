package io.github.antthluca.brutore.events;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import io.github.antthluca.brutore.recipes.input.RawInatorRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.*;

@EventBusSubscriber(modid = Brutore.MODID)
public class CreateDynamicRecipesEvent {
    @SubscribeEvent
    public void onAddReloadListeners(AddServerReloadListenersEvent event) {
        ReloadableServerResources serverResources = event.getServerResources();
        PreparableReloadListener dynamicReloader = new DynamicRecipeReloader(
                serverResources.getRecipeManager(),
                serverResources.getRegistryLookup()
        );

        event.addListener(dynamicReloader);
    }

    public class DynamicRecipeReloader extends SimplePreparableReloadListener<Void> {
        private static final TagKey<Item> C_ORES_TAG = TagKey.create(
                BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath("c", "ores"));

        private final RecipeManager recipeManager;
        private final HolderLookup.Provider registryAccess;

        // SUPER
        @Override
        protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            Multimap<RecipeType<?>, RecipeHolder<?>> dynamicRecipesByType = ArrayListMultimap.create();
            Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> dynamicRecipesByKey = new HashMap<>();

            RecipeMap allRecipes = recipeManager.recipeMap();
            Collection<RecipeHolder<SmeltingRecipe>> smeltRecipes = allRecipes.byType(RecipeType.SMELTING);
            Collection<RecipeHolder<BlastingRecipe>> blastRecipes = allRecipes.byType(RecipeType.BLASTING);

            for (RecipeHolder<SmeltingRecipe> holder : smeltRecipes) {
                processAndBuild(holder, dynamicRecipesByType, dynamicRecipesByKey);
            }

            for (RecipeHolder<BlastingRecipe> holder : blastRecipes) {
                processAndBuild(holder, dynamicRecipesByType, dynamicRecipesByKey);
            }

            if (!dynamicRecipesByKey.isEmpty()) {
                try {
                    allRecipes.byType.putAll(dynamicRecipesByType);
                    allRecipes.byKey.putAll(dynamicRecipesByKey);
                } catch(Exception e) {
                    Brutore.LOGGER.error("Error injecting recipes via RecipeMap: ", e);
                }
            }
        }

        // MAIN
        public DynamicRecipeReloader(RecipeManager recipeManager, HolderLookup.Provider registryAccess) {
            this.recipeManager = recipeManager;
            this.registryAccess = registryAccess;
        }

        public void processAndBuild(RecipeHolder<? extends AbstractCookingRecipe> holder,
                                    Multimap<RecipeType<?>, RecipeHolder<?>> byType,
                                    Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
            AbstractCookingRecipe cookRecipe = holder.value();
            Ingredient input = cookRecipe.input();

            if (!input.isEmpty()) {
                for (Holder<Item> itemHolder : input.getValues()) {
                    if (itemHolder.is(C_ORES_TAG)) {

                        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(
                                Registries.RECIPE,
                                ResourceLocation.fromNamespaceAndPath(
                                        Brutore.MODID,
                                        holder.id().registry().getPath() + "_to_raw_inator"
                                )
                        );

                        ItemStack output = cookRecipe.result;

                        RawInatorRecipe rawInatorRecipe = new RawInatorRecipe(input, output.copy());

                        RecipeHolder<RawInatorRecipe> newHolder = new RecipeHolder<>(recipeKey, rawInatorRecipe);

                        byType.put(rawInatorRecipe.getType(), newHolder);
                        byKey.put(recipeKey, newHolder);

                        break;
        }
    }
}
