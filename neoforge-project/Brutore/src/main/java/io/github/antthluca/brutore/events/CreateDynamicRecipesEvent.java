package io.github.antthluca.brutore.events;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
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
                serverResources.getRecipeManager(),
                serverResources.getRegistryLookup()
        );

        event.addListener(dynamicReloader);
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD
                && currentServerResources != null) {
            injectDynamicRecipes(
                    currentServerResources.getRecipeManager(),
                    currentServerResources.getRegistryLookup());
        }
    }

    @SuppressWarnings("unchecked")
    private static void injectDynamicRecipes(RecipeManager recipeManager, HolderLookup.Provider registryAccess) {
        try {
            Field byNameField = RecipeManager.class.getDeclaredField("byName");
            Field byTypeField = RecipeManager.class.getDeclaredField("byType");
            byNameField.setAccessible(true);
            byTypeField.setAccessible(true);

            Map<ResourceLocation, RecipeHolder<?>> originalbyName = (Map<ResourceLocation, RecipeHolder<?>>) byNameField
                    .get(recipeManager);
            Multimap<RecipeType<?>, RecipeHolder<?>> originalByType = (Multimap<RecipeType<?>, RecipeHolder<?>>) byTypeField
                    .get(recipeManager);

            Map<ResourceLocation, RecipeHolder<?>> newbyName = new HashMap<>(originalbyName);

            for (RecipeHolder<?> holder : originalByType.get(RecipeType.SMELTING)) {
                processAndBuild(holder, newbyName, registryAccess);
            }

            for (RecipeHolder<?> holder : originalByType.get(RecipeType.BLASTING)) {
                processAndBuild(holder, newbyName, registryAccess);
            }

            ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipesByTypeBuilder = ImmutableMultimap
                    .builder();
            for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : newbyName.entrySet()) {
                recipesByTypeBuilder.put(entry.getValue().value().getType(), entry.getValue());
            }

            byNameField.set(recipeManager, newbyName);
            byTypeField.set(recipeManager, recipesByTypeBuilder.build());

            Brutore.LOGGER.info("Brutore: dynamic injected recipes: {}", newbyName.size());
        } catch (Exception e) {
            Brutore.LOGGER.error("Brutore: Critical error when injecting dynamic recipes: {}", e.getMessage());
        }
    }

    private static void processAndBuild(RecipeHolder<?> holder,
                                        Map<ResourceLocation, RecipeHolder<?>> newByName,
                                        HolderLookup.Provider registryAccess) {
        if (holder.value() instanceof AbstractCookingRecipe cookRecipe) {
            ItemStack output = cookRecipe.getResultItem(registryAccess);
            ItemStack input = cookRecipe.getIngredients().get(0).getItems()[0];

            if (!output.isEmpty() && input.is(C_ORES_TAG)) {
                ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                        Brutore.MODID,
                        holder.id().getPath() + "_to_raw_inator");

                RawInatorRecipe rawInatorRecipe = new RawInatorRecipe(
                        Ingredient.of(output.getItem()),
                        input);
                RecipeHolder<RawInatorRecipe> newHolder = new RecipeHolder<>(newId, rawInatorRecipe);

                newByName.put(newId, newHolder);
            }
        }
    }

    public static class DynamicRecipeReloader extends SimplePreparableReloadListener<Void> {
        private final RecipeManager recipeManager;
        private final HolderLookup.Provider registryAccess;

        // SUPER
        @Override
        protected Void prepare(ResourceManager resourceManager,
                               ProfilerFiller profilerFiller) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            CreateDynamicRecipesEvent.injectDynamicRecipes(recipeManager, registryAccess);
        }

        // MAIN
        public DynamicRecipeReloader(RecipeManager recipeManager, HolderLookup.Provider registryAccess) {
            this.recipeManager = recipeManager;
            this.registryAccess = registryAccess;
        }
    }
}