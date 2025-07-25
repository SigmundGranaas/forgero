package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BellowsBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

/**
 * A bellows block that can be placed next to a bloomery to temporarily boost its temperature.
 * When clicked, it blows air into the connected bloomery, allowing temperatures to exceed fuel limits.
 *
 * Features:
 * - Must be placed adjacent to a BloomeryBlock
 * - Shows visual and audio feedback when activated
 * - Has directional placement based on player facing
 * - Displays particle effects when active
 */
public class BellowsBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	// Slightly shorter than a full block to make it visually distinct
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

	// Cache horizontal directions for performance
	private static final Direction[] HORIZONTAL_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
	};

	public BellowsBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(ACTIVE, false));
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BellowsBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (type == ModBlockEntities.BELLOWS) {
			return world.isClient ? null : (w, p, s, be) -> {
				if (be instanceof BellowsBlockEntity bellows) {
					BellowsBlockEntity.serverTick(w, p, s, bellows);
				}
			};
		}
		return null;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof BellowsBlockEntity bellows)) {
			return ActionResult.PASS;
		}

		// Try to activate bellows (blow air)
		if (bellows.tryActivate()) {
			// Play bellows sound with slight randomization
			float pitch = 0.9f + world.getRandom().nextFloat() * 0.2f;
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH,
					SoundCategory.BLOCKS, 0.8f, pitch);

			// Update block state to show active
			world.setBlockState(pos, state.with(ACTIVE, true), Block.NOTIFY_ALL);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState()
				.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
				.with(ACTIVE, false);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (!super.canPlaceAt(state, world, pos)) {
			return false;
		}

		// Check if there's a bloomery block adjacent to any horizontal side
		return hasAdjacentBloomery(world, pos);
	}

	/**
	 * Checks if there's a BloomeryBlock adjacent to the given position
	 */
	private boolean hasAdjacentBloomery(WorldView world, BlockPos pos) {
		for (Direction direction : HORIZONTAL_DIRECTIONS) {
			BlockPos adjacentPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPos);
			if (adjacentState.getBlock() instanceof BloomeryBlock) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (!state.get(ACTIVE)) {
			return;
		}

		// Show air particles when active
		Direction facing = state.get(FACING);
		double baseX = pos.getX() + 0.5;
		double baseY = pos.getY() + 0.7;
		double baseZ = pos.getZ() + 0.5;

		// Create air burst particles in the facing direction
		int particleCount = 2 + random.nextInt(3); // 2-4 particles
		for (int i = 0; i < particleCount; i++) {
			// Calculate particle position with some randomness
			double offsetX = facing.getOffsetX() * 0.8 + (random.nextDouble() - 0.5) * 0.3;
			double offsetY = (random.nextDouble() - 0.5) * 0.2;
			double offsetZ = facing.getOffsetZ() * 0.8 + (random.nextDouble() - 0.5) * 0.3;

			// Calculate particle velocity
			double velocityX = facing.getOffsetX() * 0.1;
			double velocityY = 0.02;
			double velocityZ = facing.getOffsetZ() * 0.1;

			world.addParticle(ParticleTypes.POOF,
					baseX + offsetX, baseY + offsetY, baseZ + offsetZ,
					velocityX, velocityY, velocityZ);
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE);
	}

	/**
	 * Override to handle block updates - deactivate bellows when no longer adjacent to bloomery
	 */
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

		if (!world.isClient && !hasAdjacentBloomery(world, pos)) {
			// Remove the bellows if no longer adjacent to a bloomery
			world.breakBlock(pos, true);
		}
	}
}
