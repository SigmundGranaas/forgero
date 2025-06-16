package com.sigmundgranaas.forgero.smithing.block.custom;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.component.HeatedItemComponent;
import com.sigmundgranaas.forgero.smithing.component.SmithingProgress;
import com.sigmundgranaas.forgero.smithing.item.ModItems;
import com.sigmundgranaas.forgero.smithing.item.SmithingTongs;
import com.sigmundgranaas.forgero.smithing.recipe.SmithingRecipe;
import com.sigmundgranaas.forgero.smithing.recipe.SmithingRecipeManager;
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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
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
		ItemStack anvilItem = inventory.getStack(0);

		// Update heat of item on anvil
		if (!anvilItem.isEmpty()) {
			boolean nearHeat = isNearHeatSource(world, blockPosition);
			HeatedItemComponent.updateHeat(anvilItem, nearHeat);
		}

		if (stackInHand.isEmpty()) {
			// Trying to pick up with bare hands
			if (!anvilItem.isEmpty()) {
				if (HeatedItemComponent.canPickupWithHands(anvilItem)) {
					ItemStack stackToGive = anvilItem.copy();
					inventory.setStack(0, ItemStack.EMPTY);
					player.getInventory().offerOrDrop(stackToGive);
					smithingAnvilBlockEntity.markDirty();

					world.playSound(null, blockPosition, SoundEvents.ITEM_ARMOR_EQUIP_IRON,
							SoundCategory.BLOCKS, 0.5f, 1.0f);
				} else {
					player.sendMessage(Text.literal("The item is too hot to pick up with bare hands! Use tongs.")
							.formatted(Formatting.RED), true);
					return ActionResult.FAIL;
				}
			}
		} else if (stackInHand.getItem() instanceof SmithingTongs) {
			// Using tongs
			handleTongsInteraction(stackInHand, inventory, player, world, blockPosition, smithingAnvilBlockEntity);
		} else if (stackInHand.getItem().equals(ModItems.SMITHING_HAMMER)) {
			// Using smithing hammer
			handleHammerInteraction(anvilItem, player, world, blockPosition, smithingAnvilBlockEntity);
		} else {
			// Trying to place an item
			if (anvilItem.isEmpty()) {
				if (HeatedItemComponent.canPickupWithHands(stackInHand)) {
					ItemStack stackToPut = stackInHand.copy();
					stackToPut.setCount(1);
					inventory.setStack(0, stackToPut);
					stackInHand.decrement(1);
					smithingAnvilBlockEntity.markDirty();

					world.playSound(null, blockPosition, SoundEvents.BLOCK_ANVIL_PLACE,
							SoundCategory.BLOCKS, 0.3f, 1.0f);
				} else {
					player.sendMessage(Text.literal("The item is too hot to handle! Use tongs.")
							.formatted(Formatting.RED), true);
					return ActionResult.FAIL;
				}
			} else {
				player.sendMessage(Text.literal("The anvil already has an item on it.")
						.formatted(Formatting.YELLOW), true);
			}
		}

		return ActionResult.SUCCESS;
	}

	private void handleTongsInteraction(ItemStack tongs, Inventory inventory, PlayerEntity player,
										World world, BlockPos pos, SmithingAnvilBlockEntity blockEntity) {
		ItemStack anvilItem = inventory.getStack(0);
		ItemStack tongsHeldItem = SmithingTongs.getHeldItem(tongs);

		if (SmithingTongs.isEmpty(tongs)) {
			// Tongs are empty, try to pick up from anvil
			if (!anvilItem.isEmpty()) {
				SmithingTongs.setHeldItem(tongs, anvilItem.copy());
				inventory.setStack(0, ItemStack.EMPTY);
				blockEntity.markDirty();

				world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_IRON,
						SoundCategory.BLOCKS, 0.5f, 1.2f);
				player.sendMessage(Text.literal("Picked up item with tongs").formatted(Formatting.GREEN), true);
			}
		} else {
			// Tongs have an item, try to place on anvil
			if (anvilItem.isEmpty()) {
				inventory.setStack(0, tongsHeldItem.copy());
				SmithingTongs.setHeldItem(tongs, ItemStack.EMPTY);
				blockEntity.markDirty();

				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_PLACE,
						SoundCategory.BLOCKS, 0.3f, 1.0f);
				player.sendMessage(Text.literal("Placed item on anvil").formatted(Formatting.GREEN), true);
			} else {
				player.sendMessage(Text.literal("The anvil already has an item on it.")
						.formatted(Formatting.YELLOW), true);
			}
		}
	}

	private void handleHammerInteraction(ItemStack anvilItem, PlayerEntity player, World world,
										 BlockPos pos, SmithingAnvilBlockEntity blockEntity) {
		if (anvilItem.isEmpty()) {
			player.sendMessage(Text.literal("There's nothing to work on the anvil.")
					.formatted(Formatting.YELLOW), true);
			return;
		}

		// Find applicable recipe
		Optional<SmithingRecipe> recipeOpt = SmithingRecipeManager.findRecipe(anvilItem);
		if (recipeOpt.isEmpty()) {
			player.sendMessage(Text.literal("This item cannot be smithed.")
					.formatted(Formatting.RED), true);
			return;
		}

		SmithingRecipe recipe = recipeOpt.get();

		// Check if item can be worked at current temperature
		if (!recipe.canWork(anvilItem)) {
			int heat = HeatedItemComponent.getHeat(anvilItem);
			if (heat < recipe.getMinWorkingHeat()) {
				player.sendMessage(Text.literal("The item is too cold to work. Heat it up first!")
						.formatted(Formatting.BLUE), true);
			} else {
				player.sendMessage(Text.literal("The item is too hot to work properly. Let it cool down a bit.")
						.formatted(Formatting.RED), true);
			}
			return;
		}

		// Set recipe ID if not already set
		if (SmithingProgress.getRecipeId(anvilItem).isEmpty()) {
			SmithingProgress.setRecipeId(anvilItem, recipe.getId().toString());
		}

		// Add hammer strike
		SmithingProgress.addHammerStrike(anvilItem);
		HeatedItemComponent.reduceHeat(anvilItem, HeatedItemComponent.HAMMER_HEAT_REDUCTION);
		HeatedItemComponent.setWorked(anvilItem, true);

		// Play anvil sound
		world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);

		// Show progress to player
		int strikes = SmithingProgress.getHammerStrikes(anvilItem);
		int required = recipe.getRequiredHammerStrikes();

		player.sendMessage(Text.literal(String.format("Progress: %d/%d strikes", strikes, required))
				.formatted(Formatting.GREEN), true);
		player.sendMessage(HeatedItemComponent.getHeatText(anvilItem), true);

		// Check if smithing is complete
		if (recipe.isComplete(anvilItem)) {
			if (recipe.requiresCooling() && !SmithingProgress.isCooled(anvilItem)) {
				player.sendMessage(Text.literal("Smithing complete! Now cool the item in water to finish.")
						.formatted(Formatting.GOLD), true);
			} else {
				// Transform item
				ItemStack result = recipe.craft(anvilItem);
				if (!result.isEmpty()) {
					blockEntity.getInventory().setStack(0, result);
					player.sendMessage(Text.literal("Smithing successful!")
							.formatted(Formatting.GOLD), true);
					world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 0.8f);
				}
			}
		}

		blockEntity.markDirty();
	}

	private boolean isNearHeatSource(World world, BlockPos anvilPos) {
		// Check for lava, fire, or other heat sources within 3 blocks
		for (int x = -3; x <= 3; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -3; z <= 3; z++) {
					BlockPos checkPos = anvilPos.add(x, y, z);
					BlockState state = world.getBlockState(checkPos);

					if (state.isOf(net.minecraft.block.Blocks.LAVA) ||
							state.isOf(net.minecraft.block.Blocks.FIRE) ||
							state.isOf(net.minecraft.block.Blocks.MAGMA_BLOCK) ||
							state.isOf(net.minecraft.block.Blocks.CAMPFIRE) ||
							state.isOf(net.minecraft.block.Blocks.SOUL_CAMPFIRE)) {
						return true;
					}
				}
			}
		}
		return false;
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
