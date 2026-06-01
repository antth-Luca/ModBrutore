package io.github.antthluca.brutore.events;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import net.minecraft.core.HolderLookup;
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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Brutore.MODID)
public class CreateDynamicRecipesEvent {
    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        ReloadableServerResources serverResources = event.getServerResources();
        PreparableReloadListener dynamicReloader = new DynamicRecipeReloader(
                serverResources.getRecipeManager(),
                serverResources.getRegistryLookup()
        );

        event.addListener(
                ResourceLocation.fromNamespaceAndPath(Brutore.MODID, "dynamic_recipes"),
                dynamicReloader
        );
    }

    public static class DynamicRecipeReloader extends SimplePreparableReloadListener<Void> {
        private final RecipeManager recipeManager;
        private final HolderLookup.Provider registryAccess;

        // SUPER
        @Override
        protected Void prepare(ResourceManager resourceManager,
                               ProfilerFiller profilerFiller) { return null; }

        @Override
        @SuppressWarnings("unchecked")
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            TagKey<Item> C_ORES_TAG = TagKey.create(
                    Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores"));

            try {
                Field byNameField = RecipeManager.class.getDeclaredField("byName");
                Field byTypeField = RecipeManager.class.getDeclaredField("byType");
                byNameField.setAccessible(true);
                byTypeField.setAccessible(true);

                Map<ResourceLocation, RecipeHolder<?>> originalByName = (Map<ResourceLocation, RecipeHolder<?>>) byNameField.get(recipeManager);
                Multimap<RecipeType<?>, RecipeHolder<?>> originalByType = (Multimap<RecipeType<?>, RecipeHolder<?>>) byTypeField.get(recipeManager);

                Map<ResourceLocation, RecipeHolder<?>> newByName = new HashMap<>(originalByName);

                Collection<RecipeHolder<?>> smeltRecipes = originalByType.get(RecipeType.SMELTING);
                Collection<RecipeHolder<?>> blastRecipes = originalByType.get(RecipeType.BLASTING);

                for (RecipeHolder<?> holder : smeltRecipes) {
                    processAndBuild(holder, C_ORES_TAG, newByName);
                }

                for (RecipeHolder<?> holder : blastRecipes) {
                    processAndBuild(holder, C_ORES_TAG, newByName);
                }

                ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipesByTypeBuilder = ImmutableMultimap.builder();
                for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : newByName.entrySet()) {
                    recipesByTypeBuilder.put(entry.getValue().value().getType(), entry.getValue());
                }

                byNameField.set(recipeManager, newByName);
                byTypeField.set(recipeManager, recipesByTypeBuilder.build());

                Brutore.LOGGER.info("Brutore: Carregadas com sucesso {} receitas no total (incluindo dinâmicas).", newByName.size());
            } catch(Exception e) {
                Brutore.LOGGER.error("Erro crítico de reflexão ao injetar receitas dinâmicas: ", e);
            }
        }

        // MAIN
        public DynamicRecipeReloader(RecipeManager recipeManager, HolderLookup.Provider registryAccess) {
            this.recipeManager = recipeManager;
            this.registryAccess = registryAccess;
        }

        public void processAndBuild(RecipeHolder<?> holder,
                                    TagKey<Item> tag,
                                    Map<ResourceLocation, RecipeHolder<?>> newByName) {
            if (holder.value() instanceof AbstractCookingRecipe cookRecipe) {
                Ingredient input = cookRecipe.input();

                if (!input.isEmpty()) {
                    boolean hasOreTag = input.items()
                            .anyMatch(holderItem -> holderItem.is(tag));

                    if (hasOreTag) {
                        ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                                Brutore.MODID,
                                holder.id().location().getPath() + "_to_raw_inator"
                        );
                        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, newId);

                        ItemStack output = cookRecipe.result;

                        RawInatorRecipe rawInatorRecipe = new RawInatorRecipe(input, output.copy());
                        RecipeHolder<RawInatorRecipe> newHolder = new RecipeHolder<>(recipeKey, rawInatorRecipe);

                        newByName.put(newId, newHolder);
                    }
                }
            }
        }
    }
}