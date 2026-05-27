package io.github.antthluca.brutore.init;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.blocks.RawInatorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Brutore.MODID);

    // Blocks
    public static final DeferredBlock<Block> RAW_INATOR = BLOCKS.registerBlock(
            "raw_inator", (prop) -> new RawInatorBlock(prop
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.5F, 1200.0F)
                    .sound(SoundType.ANVIL)
                    .pushReaction(PushReaction.BLOCK)
            ));
}
