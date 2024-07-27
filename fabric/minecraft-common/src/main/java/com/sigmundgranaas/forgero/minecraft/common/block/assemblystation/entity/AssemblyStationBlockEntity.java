package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.*;
import static com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry.ASSEMBLY_STATION_BLOCK_ENTITY;

public class AssemblyStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
	private final @NotNull SimpleInventory disassemblyInventory = new SimpleInventory(DISASSEMBLY_INVENTORY_SIZE);
	private final @NotNull SimpleInventory resultInventory = new SimpleInventory(RESULT_INVENTORY_SIZE);

	public AssemblyStationBlockEntity(@NotNull BlockPos blockPosition, @NotNull BlockState blockState) {
		super(ASSEMBLY_STATION_BLOCK_ENTITY, blockPosition, blockState);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(ASSEMBLY_STATION_TRANSLATION_KEY);
	}

	@Override
	public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new AssemblyStationScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos),
				getDisassemblyInventory(), getResultInventory()
		);
	}

	public @NotNull SimpleInventory getDisassemblyInventory() {
		return disassemblyInventory;
	}

	public @NotNull SimpleInventory getResultInventory() {
		return resultInventory;
	}
}
