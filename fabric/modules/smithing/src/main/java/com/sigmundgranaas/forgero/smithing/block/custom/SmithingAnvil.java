package com.sigmundgranaas.forgero.smithing.block.custom;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.recipe.SmithingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SmithingAnvil extends BlockWithEntity implements BlockEntityProvider {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final Identifier SMITHING_ANVIL_ID = new Identifier(Forgero.NAMESPACE, "smithing_anvil");

	private static final VoxelShape SHAPE_NORTH;
	private static final VoxelShape SHAPE_EAST;
	private static final VoxelShape SHAPE_SOUTH;
	private static final VoxelShape SHAPE_WEST;

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

	public SmithingAnvil(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
		return switch (state.get(FACING)) {
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			case WEST -> SHAPE_WEST;
			default -> VoxelShapes.fullCube();
		};
	}

	@Override
	public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
		@Nullable var placementState = super.getPlacementState(ctx);
		if (placementState == null) {
			return this.getDefaultState();
		}

		return placementState.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(Properties.HORIZONTAL_FACING);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SmithingAnvilBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResult onUse(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @Nullable PlayerEntity player, @Nullable Hand hand, @NotNull BlockHitResult blockHitResult) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity = world.getBlockEntity(blockPosition);
		if (player == null || !(blockEntity instanceof SmithingAnvilBlockEntity smithingAnvilBlockEntity)) {
			return ActionResult.PASS;
		}

		@NotNull var stackInHand = player.getStackInHand(hand);
		Inventory inventory = smithingAnvilBlockEntity.getInventory();

		if (stackInHand.isEmpty()) {
			// If the player is not holding anything, give them the item from the anvil
			if (!inventory.getStack(0).isEmpty()) {
				ItemStack stackToGive = inventory.getStack(0).copy();
				inventory.setStack(0, ItemStack.EMPTY);
				player.getInventory().offerOrDrop(stackToGive);
				smithingAnvilBlockEntity.markDirty();
			}
		} else {
			// Player is holding something
			if (inventory.getStack(0).isEmpty()) {
				// Put one item from the player's hand into the anvil
				ItemStack stackToPut = stackInHand.copy();
				stackToPut.setCount(1);
				inventory.setStack(0, stackToPut);
				stackInHand.decrement(1);
				smithingAnvilBlockEntity.markDirty();
			} else if (stackInHand.getItem().equals(ModItems.SMITHING_HAMMER)) {
				// Smith the tool part using the smithing hammer
				Optional<SmithingRecipe> recipeOpt = world.getRecipeManager().getFirstMatch(
						SmithingRecipe.Type.INSTANCE,
						new SimpleInventory(inventory.getStack(0), stackInHand),
						world
				);
				if (recipeOpt.isPresent()) {
					recipeOpt.get().craft(smithingAnvilBlockEntity.getInventory(), world.getRegistryManager());
					smithingAnvilBlockEntity.markDirty();
				}
			} else {
				// Try to swap items if they're different
				if (!ItemStack.areItemsEqual(stackInHand, inventory.getStack(0))) {
					ItemStack temp = inventory.getStack(0).copy();
					ItemStack stackToPut = stackInHand.copy();
					stackToPut.setCount(1);
					inventory.setStack(0, stackToPut);
					stackInHand.decrement(1);
					player.getInventory().offerOrDrop(temp);
					smithingAnvilBlockEntity.markDirty();
				}
			}
		}

		return ActionResult.SUCCESS;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof SmithingAnvilBlockEntity smithingAnvil) {
				ItemScatterer.spawn(world, pos, smithingAnvil.getInventory());
				world.updateComparators(pos, this);
			}
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
}
