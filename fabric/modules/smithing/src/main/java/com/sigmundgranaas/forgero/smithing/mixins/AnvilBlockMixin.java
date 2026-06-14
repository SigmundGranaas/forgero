package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AnvilBlock.class)
public abstract class AnvilBlockMixin implements BlockEntityProvider {
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SmithingAnvilBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			World world,
			BlockState state,
			BlockEntityType<T> type
	) {
		if (type == ModBlockEntities.SMITHING_ANVIL) {
			return (tickerWorld, tickerPos, tickerState, blockEntity) -> {
				if (blockEntity instanceof SmithingAnvilBlockEntity anvil) {
					anvil.tick();
				}
			};
		}

		return null;
	}
}
