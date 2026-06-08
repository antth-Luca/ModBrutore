package io.github.antthluca.brutore.events;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.init.InitRecipes;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import net.minecraft.core.Holder;
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
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Brutore.MODID)
public class CreateDynamicRecipesEvent {
    private static final TagKey<Item> C_ORES_TAG = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores"));

    private static ReloadableServerResources currentServerResources;

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        ReloadableServerResources serverResources = event.getServerResources();
        currentServerResources = serverResources;
        PreparableReloadListener dynamicReloader = new DynamicRecipeReloader(
                serverResources.getRecipeManager());

        event.addListener(dynamicReloader);
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD
                && currentServerResources != null) {
            injectDynamicRecipes(currentServerResources.getRecipeManager());
        }
    }

    @SuppressWarnings("unchecked")
    private static void injectDynamicRecipes(RecipeManager recipeManager) {
        try {
            Field recipesField = RecipeManager.class.getDeclaredField("recipes");
            recipesField.setAccessible(true);

            Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> originalRecipes =
                    (Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>>) recipesField.get(recipeManager);

            Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> newRecipes = new HashMap<>();
            for (Map.Entry<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> entry : originalRecipes.entrySet()) {
                newRecipes.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }

            Map<ResourceLocation, RecipeHolder<?>> rawInatorMap = newRecipes.computeIfAbsent(
                    InitRecipes.RAW_INATOR_TYPE.get(), k -> new HashMap<>());

            Map<ResourceLocation, RecipeHolder<?>> smeltingRecipes = newRecipes.get(RecipeType.SMELTING);
            if (smeltingRecipes != null) {
                for (RecipeHolder<?> holder : smeltingRecipes.values()) {
                    processAndBuild(holder, rawInatorMap);
                }
            }

            Map<ResourceLocation, RecipeHolder<?>> blastingRecipes = newRecipes.get(RecipeType.BLASTING);
            if (blastingRecipes != null) {
                for (RecipeHolder<?> holder : blastingRecipes.values()) {
                    processAndBuild(holder, rawInatorMap);
                }
            }

            recipesField.set(recipeManager, newRecipes);

            Brutore.LOGGER.info("Brutore: dynamic injected recipes: {}", newRecipes.size());
        } catch (Exception e) {
            Brutore.LOGGER.error("Brutore: Critical error when injecting dynamic recipes: {}", e.getMessage());
        }
    }

    private static void processAndBuild(RecipeHolder<?> holder,
            Map<ResourceLocation, RecipeHolder<?>> rawInatorMap) {
        if (holder.value() instanceof AbstractCookingRecipe cookRecipe) {
            ItemStack output = cookRecipe.getResultItem(null);
            ItemStack input = cookRecipe.getIngredients().get(0).getItems()[0];

            if (!output.isEmpty() && input.is(C_ORES_TAG)) {
                ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                        Brutore.MODID,
                        holder.id().getPath() + "_to_raw_inator");

                RawInatorRecipe rawInatorRecipe = new RawInatorRecipe(
                        Ingredient.of(output.getItem()),
                        new ItemStack(input.getItem()));
                RecipeHolder<RawInatorRecipe> newHolder = new RecipeHolder<>(newId, rawInatorRecipe);

                rawInatorMap.put(newId, newHolder);
            }
        }
    }

    public static class DynamicRecipeReloader extends SimplePreparableReloadListener<Void> {
        private final RecipeManager recipeManager;

        // SUPER
        @Override
        protected Void prepare(ResourceManager resourceManager,
                ProfilerFiller profilerFiller) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            CreateDynamicRecipesEvent.injectDynamicRecipes(recipeManager);
        }

        // MAIN
        public DynamicRecipeReloader(RecipeManager recipeManager) {
            this.recipeManager = recipeManager;
        }
    }
}