package io.github.antthluca.brutore.events;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.antthluca.brutore.Brutore;
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
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
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
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        ReloadableServerResources serverResources = event.getServerResources();
        currentServerResources = serverResources;
        PreparableReloadListener dynamicReloader = new DynamicRecipeReloader(
                serverResources.getRecipeManager());

        event.addListener(
                ResourceLocation.fromNamespaceAndPath(Brutore.MODID, "dynamic_recipes"),
                dynamicReloader);
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
            Field byKeyField = RecipeMap.class.getDeclaredField("byKey");
            Field byTypeField = RecipeMap.class.getDeclaredField("byType");
            byKeyField.setAccessible(true);
            byTypeField.setAccessible(true);

            Map<ResourceLocation, RecipeHolder<?>> originalByKey = (Map<ResourceLocation, RecipeHolder<?>>) byKeyField
                    .get(recipeManager.recipeMap());
            Multimap<RecipeType<?>, RecipeHolder<?>> originalByType = (Multimap<RecipeType<?>, RecipeHolder<?>>) byTypeField
                    .get(recipeManager.recipeMap());

            Map<ResourceLocation, RecipeHolder<?>> newByKey = new HashMap<>(originalByKey);

            for (RecipeHolder<?> holder : originalByType.get(RecipeType.SMELTING)) {
                processAndBuild(holder, newByKey);
            }

            for (RecipeHolder<?> holder : originalByType.get(RecipeType.BLASTING)) {
                processAndBuild(holder, newByKey);
            }

            ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipesByTypeBuilder = ImmutableMultimap
                    .builder();
            for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : newByKey.entrySet()) {
                recipesByTypeBuilder.put(entry.getValue().value().getType(), entry.getValue());
            }

            byKeyField.set(recipeManager.recipes, newByKey);
            byTypeField.set(recipeManager.recipes, recipesByTypeBuilder.build());

            Brutore.LOGGER.info("Brutore: dynamic injected recipes: {}", newByKey.size());
        } catch (Exception e) {
            Brutore.LOGGER.error("Brutore: Critical error when injecting dynamic recipes: {}", e.getMessage());
        }
    }

    private static void processAndBuild(RecipeHolder<?> holder,
            Map<ResourceLocation, RecipeHolder<?>> newByKey) {
        if (holder.value() instanceof AbstractCookingRecipe cookRecipe) {
            ItemStack output = cookRecipe.result;
            Holder<Item> input = cookRecipe.input().items().toList().get(0);

            if (!output.isEmpty() && input.is(C_ORES_TAG)) {
                ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                        Brutore.MODID,
                        holder.id().location().getPath() + "_to_raw_inator");
                ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, newId);

                RawInatorRecipe rawInatorRecipe = new RawInatorRecipe(
                        Ingredient.of(output.getItem()),
                        new ItemStack(input));
                RecipeHolder<RawInatorRecipe> newHolder = new RecipeHolder<>(recipeKey, rawInatorRecipe);

                newByKey.put(newId, newHolder);
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