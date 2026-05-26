package io.github.antthluca.brutore.init;

import io.github.antthluca.brutore.Brutore;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitBlockItems {
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(Brutore.MODID);

    // Block Items
    public static final DeferredItem<BlockItem> RAW_INATOR = BLOCK_ITEMS.registerSimpleBlockItem(
            "raw_inator", InitBlocks.RAW_INATOR);
}
