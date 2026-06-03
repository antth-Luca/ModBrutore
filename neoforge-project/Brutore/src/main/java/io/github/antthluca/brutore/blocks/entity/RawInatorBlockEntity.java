package io.github.antthluca.brutore.blocks.entity;

import io.github.antthluca.brutore.init.InitBlockEntities;
import io.github.antthluca.brutore.init.InitRecipes;
import io.github.antthluca.brutore.recipes.custom.RawInatorRecipe;
import io.github.antthluca.brutore.recipes.input.RawInatorRecipeInput;
import io.github.antthluca.brutore.screens.menu.RawInatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RawInatorBlockEntity extends BlockEntity
        implements MenuProvider, ICapabilityProvider<BlockCapability<?, Direction>, Direction, Object> {
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    public static final String tagNameProgress = "raw_inator.progress";
    public static final String tagNameMaxProgress = "raw_inator.max_progress";
    public static final int DEFAULT_PROGRESS = 0;
    public static final int DEFAULT_MAX_PROGRESS = 72;
    public static final int LAVA_PER_ITEM = 100;
    public static final int MAX_LAVA = 4000;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int progress = DEFAULT_PROGRESS;
    private int maxProgress = DEFAULT_MAX_PROGRESS;
    public final FluidTank lavaTank = new FluidTank(MAX_LAVA) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final ResourceHandler<FluidResource> fluidHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            return FluidResource.of(lavaTank.getFluid());
        }

        @Override
        public long getAmountAsLong(int slot) {
            return lavaTank.getFluidAmount();
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return lavaTank.getCapacity();
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return resource.getFluid() == net.minecraft.world.level.material.Fluids.LAVA;
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return lavaTank.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return lavaTank.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
    };

    public RawInatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(InitBlockEntities.RAW_INATOR_BE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> RawInatorBlockEntity.this.progress;
                    case 1 -> RawInatorBlockEntity.this.maxProgress;
                    case 2 -> RawInatorBlockEntity.this.lavaTank.getFluidAmount();
                    case 3 -> RawInatorBlockEntity.this.lavaTank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> RawInatorBlockEntity.this.progress = value;
                    case 1 -> RawInatorBlockEntity.this.maxProgress = value;
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    // SUPER
    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.translatable("block.brutore.raw_inator");
    }

    @Override
    public Object getCapability(BlockCapability<?, Direction> cap, Direction side) {
        if (cap == Capabilities.Fluid.BLOCK) {
            return fluidHandler;
        }
        return null;
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RawInatorMenu(i, inventory, this, this.data);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        itemHandler.serialize(output);
        lavaTank.serialize(output);
        output.putInt(tagNameProgress, progress);
        output.putInt(tagNameMaxProgress, maxProgress);

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input);
        lavaTank.deserialize(input);
        progress = input.getIntOr(tagNameProgress, 0);
        maxProgress = input.getIntOr(tagNameMaxProgress, 0);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // MAIN
    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for (int c = 0; c < itemHandler.getSlots(); c++) {
            inv.setItem(c, itemHandler.getStackInSlot(c));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (hasRecipe() && hasEnoughLava()) {
            increaseCraftingProcess();
            setChanged(level, pos, state);

            if (hasCraftingFinished()) {
                craftItem();
                resetProcess();
            }
        } else {
            resetProcess();
        }
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<RawInatorRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) {
            return false;
        }

        ItemStack output = recipe.get().value().output();
        return canInsertAmountIntoOutputSlot(output.getCount())
                && canInsertItemIntoOutputSlot(output)
                && hasEnoughLava();
    }

    private boolean hasEnoughLava() {
        return lavaTank.getFluidAmount() >= LAVA_PER_ITEM;
    }

    private void increaseCraftingProcess() {
        progress++;
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void craftItem() {
        Optional<RecipeHolder<RawInatorRecipe>> recipe = getCurrentRecipe();
        ItemStack output = recipe.get().value().output();

        if (!hasEnoughLava()) {
            return;
        }

        lavaTank.drain(LAVA_PER_ITEM, IFluidHandler.FluidAction.EXECUTE);
        itemHandler.extractItem(INPUT_SLOT, 1, false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(output.getItem(),
                itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + output.getCount()));
    }

    private void resetProcess() {
        progress = DEFAULT_PROGRESS;
        maxProgress = DEFAULT_MAX_PROGRESS;
    }

    private Optional<RecipeHolder<RawInatorRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        return serverLevel.recipeAccess()
                .getRecipeFor(InitRecipes.RAW_INATOR_TYPE.get(),
                        new RawInatorRecipeInput(itemHandler.getStackInSlot(INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64
                : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }
}
