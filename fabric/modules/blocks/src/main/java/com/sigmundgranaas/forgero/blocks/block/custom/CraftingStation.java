package com.sigmundgranaas.forgero.blocks.block.custom;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class CraftingStation extends CraftingTableBlock {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public CraftingStation(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
	}

	public static final Identifier CRAFTING_STATION = new Identifier(Forgero.NAMESPACE, "crafting_station");

	private static final VoxelShape SHAPE_NORTH;
	private static final VoxelShape SHAPE_EAST;
	private static final VoxelShape SHAPE_SOUTH;
	private static final VoxelShape SHAPE_WEST;

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
		Direction dir = state.get(FACING);
		return switch (dir) {
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			case WEST -> SHAPE_WEST;
			default -> VoxelShapes.fullCube();
		};
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(Properties.HORIZONTAL_FACING);
	}

	static {

		VoxelShape shapeS = VoxelShapes.empty();
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.375, 0.5625, 0.75));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.625, 0.5, 0.25, 0.75, 0.5625, 0.75));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.3125, 0.4375, 0.5625, 0.4375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.5625, 0.375, 0.375, 0.625, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.625, 0.5625, 0.375, 0.6875, 0.625, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.8125, 0.375, 0.6875, 1, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.875, 0.4375, 0.875, 0.9375, 0.5625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.9375, 0.375, 0.8125, 1, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.8125, 0.9375, 0.4375, 0.9375, 1, 0.5625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.8125, 0.4375, 0.75, 0.875, 0.5625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.0625, 0.9375, 0.375, 0.1875, 1, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.875, 0.375, 0.3125, 0.9375, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.8125, 0.375, 0.3125, 0.875, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.6875, 1, 0.375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.9375, 0.625, 0.6875, 1, 0.6875));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.4375, 0.8125, 0.5625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.3125, 0.625, 0.5625, 0.4375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.625, 0.5625, 0.6875));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.5625, 0.4375, 0.5625, 0.6875));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.9375, 0.375, 0.3125, 1, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.9375, 0.5625, 0.25, 1, 0.625));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.9375, 0.375, 0.25, 1, 0.4375));
		shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
		SHAPE_SOUTH = shapeS.simplify();

		VoxelShape shapeE = VoxelShapes.empty();
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.25, 0.5, 0.625, 0.75, 0.5625, 0.75));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.75, 0.5625, 0.375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.5, 0.5625, 0.4375, 0.5625, 0.625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.625, 0.625, 0.625, 0.6875));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.3125, 0.625, 0.625, 0.375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.8125, 0.3125, 0.625, 1, 0.6875));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.875, 0.125, 0.5625, 0.9375, 0.3125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.1875, 0.625, 1, 0.3125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.9375, 0.0625, 0.5625, 1, 0.1875));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.8125, 0.25, 0.5625, 0.875, 0.3125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.8125, 0.625, 1, 0.9375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.875, 0.6875, 0.625, 0.9375, 0.8125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.8125, 0.6875, 0.625, 0.875, 0.75));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.375, 1, 0.6875));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.625, 0.9375, 0.3125, 0.6875, 1, 0.6875));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.5, 0.5625, 0.5625, 0.8125, 0.625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.5, 0.375, 0.4375, 0.5625, 0.4375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5, 0.375, 0.6875, 0.5625, 0.4375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.6875, 0.5625, 0.625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.4375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.6875, 0.625, 1, 0.75));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.9375, 0.75, 0.625, 1, 0.8125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.75, 0.4375, 1, 0.8125));
		shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
		SHAPE_EAST = shapeE.simplify();

		VoxelShape shapeN = VoxelShapes.empty();
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.625, 0.5, 0.25, 0.75, 0.5625, 0.75));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.375, 0.5625, 0.75));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.625, 0.5625, 0.6875));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.625, 0.5625, 0.375, 0.6875, 0.625, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.5625, 0.375, 0.375, 0.625, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.8125, 0.375, 0.6875, 1, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.125, 0.875, 0.4375, 0.3125, 0.9375, 0.5625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.1875, 0.9375, 0.375, 0.3125, 1, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.0625, 0.9375, 0.4375, 0.1875, 1, 0.5625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.25, 0.8125, 0.4375, 0.3125, 0.875, 0.5625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.8125, 0.9375, 0.375, 0.9375, 1, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.875, 0.375, 0.8125, 0.9375, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.8125, 0.375, 0.75, 0.875, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.9375, 0.625, 0.6875, 1, 0.6875));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.6875, 1, 0.375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.5625, 0.4375, 0.5625, 0.6875));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.3125, 0.4375, 0.5625, 0.4375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.3125, 0.625, 0.5625, 0.4375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.4375, 0.8125, 0.5625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.9375, 0.375, 0.75, 1, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.75, 0.9375, 0.375, 0.8125, 1, 0.4375));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.75, 0.9375, 0.5625, 0.8125, 1, 0.625));
		shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
		SHAPE_NORTH = shapeN.simplify();

		VoxelShape shapeW = VoxelShapes.empty();
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.75, 0.5625, 0.375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.25, 0.5, 0.625, 0.75, 0.5625, 0.75));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5, 0.375, 0.6875, 0.5625, 0.4375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.3125, 0.625, 0.625, 0.375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.625, 0.625, 0.625, 0.6875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.8125, 0.3125, 0.625, 1, 0.6875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.875, 0.6875, 0.5625, 0.9375, 0.875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.6875, 0.625, 1, 0.8125));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.9375, 0.8125, 0.5625, 1, 0.9375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.8125, 0.6875, 0.5625, 0.875, 0.75));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.0625, 0.625, 1, 0.1875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.875, 0.1875, 0.625, 0.9375, 0.3125));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.8125, 0.25, 0.625, 0.875, 0.3125));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.625, 0.9375, 0.3125, 0.6875, 1, 0.6875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.375, 1, 0.6875));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.4375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.6875, 0.5625, 0.625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.5, 0.5625, 0.4375, 0.5625, 0.625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.5, 0.375, 0.4375, 0.5625, 0.4375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.5, 0.5625, 0.5625, 0.8125, 0.625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.25, 0.625, 1, 0.3125));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.1875, 0.4375, 1, 0.25));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.9375, 0.1875, 0.625, 1, 0.25));
		shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
		SHAPE_WEST = shapeW.simplify();

	}





	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}


}
