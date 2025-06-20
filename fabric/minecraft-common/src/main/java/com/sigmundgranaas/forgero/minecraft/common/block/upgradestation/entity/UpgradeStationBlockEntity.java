package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationScreenHandler;
import com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class UpgradeStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private static final @NotNull String COMPOSITE_INVENTORY_NBT_KEY = "CompositeInventory";
    
    // Single slot inventory to hold the tool/weapon being upgraded
    private final @NotNull SimpleInventory compositeInventory = new SimpleInventory(1);

    public UpgradeStationBlockEntity(@NotNull BlockPos blockPosition, @NotNull BlockState blockState) {
        super(BlockEntityRegistry.UPGRADE_STATION_BLOCK_ENTITY, blockPosition, blockState);
        
        this.compositeInventory.addListener(inventory -> {
            markDirty();
            // Synchronize inventory changes to the client
            if (world != null && !world.isClient()) {
                world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
            }
        });
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.forgero.upgrade_station");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (this.world != null) {
            // This ensures a clean handler is created each time
            return new UpgradeStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(this.world, this.pos));
        }
        return null;
    }

    @Override
    public void writeNbt(@NotNull NbtCompound nbt) {
        // Save inventory data
        nbt.put(COMPOSITE_INVENTORY_NBT_KEY, this.compositeInventory.toNbtList());
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        // Load inventory data
        if (nbt.contains(COMPOSITE_INVENTORY_NBT_KEY)) {
            this.compositeInventory.readNbtList(nbt.getList(COMPOSITE_INVENTORY_NBT_KEY, NbtElement.COMPOUND_TYPE));
        }
        super.readNbt(nbt);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Override
    public @NotNull Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public @NotNull SimpleInventory getCompositeInventory() {
        return this.compositeInventory;
    }

    public ItemStack getRenderInventory() {
        return this.compositeInventory.getStack(0);
    }

    // Ensure the item is synced when it changes
    public void setInventoryStack(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }

        // Check if the stack is actually different to avoid unnecessary updates
        ItemStack currentStack = this.compositeInventory.getStack(0);
        if (!ItemStack.areEqual(currentStack, stack)) {
            this.compositeInventory.setStack(0, stack);
            this.markDirty();

            // Force sync to client
            if (this.world != null && !this.world.isClient) {
                this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        // Ensure we sync whenever marked dirty
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }
}
