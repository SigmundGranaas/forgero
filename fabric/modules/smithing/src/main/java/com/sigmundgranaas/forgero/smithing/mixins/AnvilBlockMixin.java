package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.block.entity.custom.SmithingAnvilBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOGGER = LoggerFactory.getLogger("Forgero/SmithingAnvilBlockMixin");

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		LOGGER.info("Creating SmithingAnvilBlockEntity at {}", pos);
		return new SmithingAnvilBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			World world, BlockState state, BlockEntityType<T> type
	) {
		if (type == ModBlockEntities.SMITHING_ANVIL) {
			return (w, p, s, be) -> {
				if (be instanceof SmithingAnvilBlockEntity anvil) {
					anvil.tick();
				}
			};
		}
		return null;
	}
}

