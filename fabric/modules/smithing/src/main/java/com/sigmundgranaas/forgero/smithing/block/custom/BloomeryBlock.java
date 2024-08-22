package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BloomeryBlock extends AbstractBloomeryBlock {
	public BloomeryBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BloomeryBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return BloomeryBlock.checkType(world, type, ModBlockEntities.BLOOMERY);
	}

	@Override
	protected void openScreen(@NotNull World world, @NotNull BlockPos blockPosition, @Nullable PlayerEntity player) {
		if (player == null) {
			return;
		}

		@Nullable BlockEntity blockEntity = world.getBlockEntity(blockPosition);
		if (!(blockEntity instanceof BloomeryBlockEntity bloomeryBlockEntity)) {
			return;
		}

		player.openHandledScreen(bloomeryBlockEntity);
	}

	@Override
	public void randomDisplayTick(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @NotNull Random random) {
		if (!blockState.get(LIT)) {
			return;
		}

		double d = (double) blockPosition.getX() + 0.5;
		double e = blockPosition.getY();
		double f = (double) blockPosition.getZ() + 0.5;
		if (random.nextDouble() < 0.1) {
			world.playSound(d, e, f, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
		}

		Direction direction = blockState.get(FACING);
		Direction.Axis axis = direction.getAxis();
		double g = 0.52;
		double h = random.nextDouble() * 0.6 - 0.3;
		double i = axis == Direction.Axis.X ? (double) direction.getOffsetX() * g : h;
		double j = random.nextDouble() * 6.0 / 16.0;
		double k = axis == Direction.Axis.Z ? (double) direction.getOffsetZ() * g : h;
		world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0, 0.0, 0.0);
		world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0, 0.0, 0.0);
	}
}
