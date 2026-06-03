package io.github.antthluca.brutore.blocks.custom;

import com.mojang.serialization.MapCodec;
import io.github.antthluca.brutore.blocks.entity.RawInatorBlockEntity;
import io.github.antthluca.brutore.init.InitBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class RawInatorBlock extends BaseEntityBlock {
    public static final MapCodec<RawInatorBlock> CODEC = simpleCodec(RawInatorBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final Component CONTAINER_TITLE = Component.translatable("block.brutore.raw_inator");

    // SUPER
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RawInatorBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (tryHandleLavaBucket(player, hand, level, pos, stack)) {
                return InteractionResult.SUCCESS;
            }

            if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, Direction.UP)) {
                return InteractionResult.SUCCESS;
            }

            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof RawInatorBlockEntity rawInatorBE) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(rawInatorBE, CONTAINER_TITLE), pos);
            } else {
                throw new IllegalStateException("Our container provider is missing!");
            }
        }

        return InteractionResult.SUCCESS;
    }

    private boolean tryHandleLavaBucket(Player player, InteractionHand hand, Level level, BlockPos pos,
            ItemStack stack) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof RawInatorBlockEntity rawInatorBE)) {
            return false;
        }

        if (stack.getItem() == Items.LAVA_BUCKET
                && rawInatorBE.lavaTank.getFluidAmount() < rawInatorBE.lavaTank.getCapacity()) {
            int inserted = rawInatorBE.lavaTank.fill(new FluidStack(Fluids.LAVA, 1000),
                    IFluidHandler.FluidAction.EXECUTE);
            if (inserted > 0) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (!player.getInventory().add(new ItemStack(Items.BUCKET))) {
                        player.drop(new ItemStack(Items.BUCKET), false);
                    }
                }
                rawInatorBE.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                return true;
            }
        }

        if (stack.getItem() == Items.BUCKET && rawInatorBE.lavaTank.getFluidAmount() >= 1000) {
            int drained = rawInatorBE.lavaTank.drain(1000, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if (drained > 0) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (!player.getInventory().add(new ItemStack(Items.LAVA_BUCKET))) {
                        player.drop(new ItemStack(Items.LAVA_BUCKET), false);
                    }
                }
                rawInatorBE.setChanged();
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
                return true;
            }
        }

        return false;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null
                : createTickerHelper(blockEntityType, InitBlockEntities.RAW_INATOR_BE.get(),
                        (bLevel, bPos, bState, blockEntity) -> blockEntity.tick(bLevel, bPos, bState));
    }

    // MAIN
    public RawInatorBlock(Properties prop) {
        super(prop);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }
}
