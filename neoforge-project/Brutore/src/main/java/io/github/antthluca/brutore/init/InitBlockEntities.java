package io.github.antthluca.brutore.init;

import java.util.function.Supplier;

import io.github.antthluca.brutore.Brutore;
import io.github.antthluca.brutore.blocks.entity.RawInatorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, Brutore.MODID);

    // Block Entities
    public static final Supplier<BlockEntityType<RawInatorBlockEntity>> RAW_INATOR_BE = BLOCK_ENTITIES.register(
            "raw_inator_be", () -> BlockEntityType.Builder.of(
                    RawInatorBlockEntity::new,
                    InitBlocks.RAW_INATOR.get()).build(null));
}