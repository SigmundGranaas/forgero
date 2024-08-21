package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SmithingAnvilBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public ItemStack getRenderStack(){
        return this.getStack(0);
    }

    public void setInventory(DefaultedList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, items.get(i));
        }
    }

    @Override
    public void markDirty() {
        if(!world.isClient()) {
            PacketByteBuf data = PacketByteBufs.create();
            data.writeInt(items.size());
            for(int i = 0; i < items.size(); i++) {
                data.writeItemStack(items.get(i));
            }
            data.writeBlockPos(getPos());

            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
                ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
            }
        }

        super.markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMITHING_ANVIL, pos, state);



    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        super.writeNbt(nbt);
    }


    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        Inventories.writeNbt(nbtCompound, this.items, true);
        return nbtCompound;
    }

    /**
     * Update the block entity listeners so the resultInventory gets updated on the client.
     * This is done because the item renderer needs to access the resultInventory clientside.
     */

    private void updateListeners() {
        var world = this.getWorld();
        if (world == null) return;

        this.markDirty();

        if (world instanceof ServerWorld serverWorld) {
            // Sync block entity with client
            final Chunk chunk = world.getChunk(pos);
            final Packet<?> packet = toUpdatePacket();

            serverWorld.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(
                    chunk.getPos(), false).forEach(player -> player.networkHandler.sendPacket(packet));
        }
    }


}


