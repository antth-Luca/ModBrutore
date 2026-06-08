package io.github.antthluca.brutore.datagen;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.init.InitBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BrutoreBlockTagsProvider extends BlockTagsProvider {
    public BrutoreBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(InitBlocks.RAW_INATOR.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(InitBlocks.RAW_INATOR.get());
    }
}
