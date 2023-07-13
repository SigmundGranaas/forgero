package com.sigmundgranaas.forgero.fabric.blockentity.assemblystation;

import java.util.ArrayList;
import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationBlock;
import com.sigmundgranaas.forgero.fabric.block.assemblystation.AssemblyStationScreenHandler;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	public static final Identifier IDENTIFIER = new Identifier(Forgero.NAMESPACE, "assembly_station_block_entity");
	public static final BlockEntityType<AssemblyStationBlockEntity> ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE, IDENTIFIER, FabricBlockEntityTypeBuilder.create(
					AssemblyStationBlockEntity::new,
					AssemblyStationBlock.ASSEMBLY_STATION_BLOCK
			).build());
	public SimpleInventory disassemblyInventory = new SimpleInventory(1);
	public SimpleInventory resultInventory = new SimpleInventory(9);

	public AssemblyStationBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPos, blockState);

		updateListeners();
		resultInventory.addListener(inv -> updateListeners());
		disassemblyInventory.addListener(inv -> updateListeners());
	}

	public List<ItemStack> getItems() {
		var list = new ArrayList<ItemStack>();
		ItemStack disassemblyStack = disassemblyInventory.getStack(0);

		if (!disassemblyStack.isEmpty()) {
			list.add(disassemblyStack);
		} else {
			list.add(new ItemStack(Items.AIR));
		}

		for (int i = 0; i < resultInventory.size(); i++) {
			ItemStack inventoryStack = resultInventory.getStack(i);
			if (!inventoryStack.isEmpty()) {
				list.add(inventoryStack);
			} else {
				list.add(new ItemStack(Items.AIR));
			}
		}

		return list;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		resultInventory.clear();
		disassemblyInventory.clear();
		super.readNbt(nbt);
		Inventories.readNbt(nbt, resultInventory.stacks);

		if (nbt.contains("DisassemblyItems")) {
			Inventories.readNbt(nbt.getCompound("DisassemblyItems"), disassemblyInventory.stacks);
		}

		updateListeners();
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		Inventories.writeNbt(nbt, resultInventory.stacks);

		NbtCompound disassemblyNbt = new NbtCompound();
		Inventories.writeNbt(disassemblyNbt, disassemblyInventory.stacks);
		nbt.put("DisassemblyItems", disassemblyNbt);

		super.writeNbt(nbt);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new AssemblyStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos),
				resultInventory, disassemblyInventory
		);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(getCachedState().getBlock().getTranslationKey());
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}


	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return super.createNbt();
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
