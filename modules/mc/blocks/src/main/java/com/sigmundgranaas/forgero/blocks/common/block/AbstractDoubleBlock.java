package com.sigmundgranaas.forgero.blocks.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import javax.annotation.Nullable;

/**
 * Base class for double-wide station blocks (Upgrade Station, Assembly Station).
 * <p>
 * This abstract class handles the common logic for two-block-wide stations:
 * <ul>
 *   <li>Block placement with automatic partner block creation</li>
 *   <li>Block breaking with partner removal</li>
 *   <li>Voxel shape rotation based on facing direction</li>
 *   <li>Render type handling (only LEFT part is visible)</li>
 * </ul>
 * <p>
 * Subclasses implement the screen handler factory and specific voxel shapes.
 */
public abstract class AbstractDoubleBlock extends HorizontalFacingBlock {

	/**
	 * Part property for left/right block identification.
	 */
	public static final EnumProperty<BlockPart> PART = EnumProperty.of("part", BlockPart.class);

	protected AbstractDoubleBlock(Settings settings) {
		super(settings);
		this.setDefaultState(getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(PART, BlockPart.LEFT));
	}

	/**
	 * Gets the voxel shape for the left part.
	 */
	protected abstract VoxelShape getLeftShape();

	/**
	 * Gets the voxel shape for the right part.
	 */
	protected abstract VoxelShape getRightShape();

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction direction = ctx.getHorizontalPlayerFacing().getOpposite();
		return this.getDefaultState().with(FACING, direction);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		if (!world.isClient) {
			BlockPos partnerPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
			world.breakBlock(partnerPos, true, placer, 1);
			world.setBlockState(partnerPos, state.with(PART, BlockPart.RIGHT), 3);
			world.updateNeighbors(pos, Blocks.AIR);
			state.updateNeighbors(world, pos, 3);
		}
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		BlockPart part = state.get(PART);
		BlockPos partnerPos;
		if (part == BlockPart.LEFT) {
			partnerPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
		} else {
			partnerPos = pos.offset(state.get(FACING).rotateClockwise(Direction.Axis.Y));
		}
		if (!world.isClient) {
			world.setBlockState(partnerPos, Blocks.AIR.getDefaultState(), 3);
			world.updateNeighbors(pos, Blocks.AIR);
			state.updateNeighbors(world, pos, 3);
		}
		super.onBreak(world, pos, state, player);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (super.canPlaceAt(state, world, pos)) {
			BlockPos partnerPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
			return !world.getBlockState(partnerPos).isSolidBlock(world, partnerPos);
		}
		return false;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		if (state.get(PART) == BlockPart.RIGHT) {
			return BlockRenderType.INVISIBLE;
		}
		return super.getRenderType(state);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		BlockPart part = state.get(PART);
		Direction facing = state.get(FACING);

		if (part == BlockPart.LEFT) {
			return rotateShape(Direction.SOUTH, facing, getLeftShape());
		}
		return rotateShape(Direction.SOUTH, facing, getRightShape().offset(-1, 0, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART);
	}

	/**
	 * Rotates a voxel shape from one facing direction to another.
	 *
	 * @param from  Original facing direction
	 * @param to    Target facing direction
	 * @param shape The shape to rotate
	 * @return The rotated shape
	 */
	public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

		int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
					buffer[1] = VoxelShapes.union(buffer[1],
							VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = VoxelShapes.empty();
		}

		return buffer[0];
	}

	/**
	 * Enumeration of block parts.
	 */
	public enum BlockPart implements StringIdentifiable {
		LEFT("left"),
		RIGHT("right");

		private final String name;

		BlockPart(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
