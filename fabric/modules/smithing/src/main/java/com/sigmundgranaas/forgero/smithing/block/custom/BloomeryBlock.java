package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BloomeryBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	public BloomeryBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, false));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BloomeryBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : checkType(type, ModBlockEntities.BLOOMERY, BloomeryBlockEntity::serverTick);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity entity = world.getBlockEntity(pos);
			if (entity instanceof BloomeryBlockEntity bloomery) {
				ItemStack heldItem = player.getStackInHand(hand);

				// Handle item insertion/extraction
				if (!heldItem.isEmpty()) {
					// Try to insert items
					ItemStack remaining = bloomery.insertItem(heldItem);
					if (remaining.getCount() != heldItem.getCount()) {
						player.setStackInHand(hand, remaining);
						return ActionResult.SUCCESS;
					}
				} else {
					// Try to extract items when hand is empty
					ItemStack extracted = bloomery.extractItem();
					if (!extracted.isEmpty()) {
						player.setStackInHand(hand, extracted);
						return ActionResult.SUCCESS;
					}
				}
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 1.4;
			double z = pos.getZ() + 0.5;

			// Sound effects
			if (random.nextFloat() < 0.1f) {
				// Main crackling/burning sound
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
						0.3f + random.nextFloat() * 0.2f, // Volume: 0.3-0.5
						0.8f + random.nextFloat() * 0.4f, // Pitch: 0.8-1.2
						false);
			}

			// Occasional furnace burning sounds
			if (random.nextFloat() < 0.05f) {
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS,
						0.2f + random.nextFloat() * 0.1f, // Volume: 0.2-0.3
						0.9f + random.nextFloat() * 0.2f, // Pitch: 0.9-1.1
						false);
			}

			// Rare lava popping sounds for dramatic effect
			if (random.nextFloat() < 0.02f) {
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
						0.1f + random.nextFloat() * 0.1f, // Volume: 0.1-0.2
						1.2f + random.nextFloat() * 0.3f, // Pitch: 1.2-1.5
						false);
			}

			// Campfire smoke particles
			if (random.nextFloat() < 0.8f) {
				// Main smoke column
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
						x + (random.nextDouble() - 0.5) * 0.3,
						y + 0.1,
						z + (random.nextDouble() - 0.5) * 0.3,
						0.0, 0.07, 0.0);
			}

			// Additional smoke variation
			if (random.nextFloat() < 0.4f) {
				world.addParticle(ParticleTypes.SMOKE,
						x + (random.nextDouble() - 0.5) * 0.4,
						y + random.nextDouble() * 0.3,
						z + (random.nextDouble() - 0.5) * 0.4,
						0.0, 0.05, 0.0);
			}

			// Crackling furnace particles (flame and sparks)
			//if (random.nextFloat() < 0.6f) {
			//	// Flame particles
			//	world.addParticle(ParticleTypes.FLAME,
			//			x + (random.nextDouble() - 0.5) * 0.5,
			//			y + random.nextDouble() * 0.2,
			//			z + (random.nextDouble() - 0.5) * 0.5,
			//			(random.nextDouble() - 0.5) * 0.02,
			//			random.nextDouble() * 0.01 + 0.01,
			//			(random.nextDouble() - 0.5) * 0.02);
			//}

			// Lava spark particles for crackling effect
			if (random.nextFloat() < 0.3f) {
				world.addParticle(ParticleTypes.LAVA,
						x + (random.nextDouble() - 0.5) * 0.6,
						y + random.nextDouble() * 0.4,
						z + (random.nextDouble() - 0.5) * 0.6,
						(random.nextDouble() - 0.5) * 0.05,
						random.nextDouble() * 0.03,
						(random.nextDouble() - 0.5) * 0.05);
			}

			// Small ember particles
			if (random.nextFloat() < 0.4f) {
				Direction direction = state.get(FACING);
				double particleX = pos.getX() + 0.5;
				double particleY = pos.getY() + random.nextDouble() * 6.0 / 16.0;
				double particleZ = pos.getZ() + 0.5;

				double offset = 0.52;
				double sideOffset = random.nextDouble() * 0.6 - 0.3;

				switch (direction) {
					case WEST -> particleX -= offset;
					case EAST -> particleX += offset;
					case NORTH -> particleZ -= offset;
					case SOUTH -> particleZ += offset;
				}

				if (direction.getAxis() == Direction.Axis.X) {
					particleZ += sideOffset;
				} else {
					particleX += sideOffset;
				}

				world.addParticle(ParticleTypes.SMALL_FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
				world.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
			}

			// Occasional large smoke puffs
			if (random.nextFloat() < 0.1f) {
				world.addParticle(ParticleTypes.LARGE_SMOKE,
						x + (random.nextDouble() - 0.5) * 0.2,
						y + 0.2,
						z + (random.nextDouble() - 0.5) * 0.2,
						0.0, 0.1, 0.0);
			}
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BloomeryBlockEntity bloomery) {
				bloomery.dropContents(world, pos);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
