package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation;

import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.entity.AssemblyStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.UpgradeStationBlock;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.block.Blocks.DEEPSLATE;

public class AssemblyStationBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	public static final @NotNull String ASSEMBLY_STATION_NAME = "assembly_station";
	/**
	 * An {@link Identifier} for multiple parts of this {@link AssemblyStationBlock}.
	 */
	public static final @NotNull Identifier ASSEMBLY_STATION_IDENTIFIER = new Identifier(Forgero.NAMESPACE, ASSEMBLY_STATION_NAME);
	public static final @NotNull String ASSEMBLY_STATION_TRANSLATION_KEY = "block.forgero.assembly_station";
	public static final @NotNull EnumProperty<AssemblyStationPart> PART = EnumProperty.of("part", AssemblyStationPart.class);
	public static final @NotNull Block ASSEMBLY_STATION_BLOCK = new AssemblyStationBlock(
			Settings.copy(DEEPSLATE).strength(3.5F, 6.0F).solidBlock((BlockState state, BlockView world, BlockPos pos) -> true));
	public static final @NotNull BlockItem ASSEMBLY_STATION_ITEM = new BlockItem(ASSEMBLY_STATION_BLOCK, new Item.Settings());
	public static final int DISASSEMBLY_INVENTORY_SIZE = 1;
	public static final int RESULT_INVENTORY_SIZE = 9;

	private static final @NotNull VoxelShape SHAPE_LEFT;
	private static final @NotNull VoxelShape SHAPE_RIGHT;

	static {
		VoxelShape shapeLeft = VoxelShapes.empty();
		shapeLeft = VoxelShapes.union(shapeLeft, VoxelShapes.cuboid(0, 0.875, 0, 1, 1, 1));
		shapeLeft = VoxelShapes.union(shapeLeft, VoxelShapes.cuboid(0.1875, 0, 0.0625, 0.375, 0.625, 0.25));
		shapeLeft = VoxelShapes.union(shapeLeft, VoxelShapes.cuboid(0.1875, 0, 0.75, 0.375, 0.625, 0.9375));
		shapeLeft = VoxelShapes.union(shapeLeft, VoxelShapes.cuboid(0.1875, 0.625, 0.0625, 1, 0.875, 0.9375));

		SHAPE_LEFT = shapeLeft.simplify();

		VoxelShape shapeRight = VoxelShapes.empty();
		shapeRight = VoxelShapes.union(shapeRight, VoxelShapes.cuboid(1, 0.875, 0, 1.875, 1, 1));
		shapeRight = VoxelShapes.union(shapeRight, VoxelShapes.cuboid(1, 0.625, 0.0625, 1.8125, 0.875, 0.9375));
		shapeRight = VoxelShapes.union(shapeRight, VoxelShapes.cuboid(1.625, 0, 0.0625, 1.8125, 0.625, 0.25));
		shapeRight = VoxelShapes.union(shapeRight, VoxelShapes.cuboid(1.625, 0, 0.75, 1.8125, 0.625, 0.9375));

		SHAPE_RIGHT = shapeRight.simplify();
	}

	protected AssemblyStationBlock(@NotNull Settings settings) {
		super(settings);
		this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(PART, AssemblyStationPart.LEFT));
		this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(PART, AssemblyStationPart.LEFT);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isSideInvisible(@NotNull BlockState blockState, @NotNull BlockState blockStateFrom, @NotNull Direction direction) {
		return blockStateFrom.isOf(this) || super.isSideInvisible(blockState, blockStateFrom, direction);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResult onUse(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @Nullable PlayerEntity player, @Nullable Hand hand, @NotNull BlockHitResult hit) {
		if (world.isClient || player == null) {
			return ActionResult.SUCCESS;
		}

		// This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
		// a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
		@Nullable NamedScreenHandlerFactory screenHandlerFactory = blockState.createScreenHandlerFactory(world, blockPosition);
		if (screenHandlerFactory == null) {
			return ActionResult.SUCCESS;
		}

		// Request the client to open the appropriate ScreenHandler
		player.openHandledScreen(screenHandlerFactory);
		return ActionResult.SUCCESS;
	}

	@Override
	public @NotNull BlockState getPlacementState(@NotNull ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition) {
		@Nullable var blockEntity = world.getBlockEntity(blockPosition);
		if (!(blockEntity instanceof AssemblyStationBlockEntity assemblyStationBlockEntity)) {
			return new SimpleNamedScreenHandlerFactory(
					(syncId, inventory, player) -> new AssemblyStationScreenHandler(
							syncId, inventory, ScreenHandlerContext.create(world, blockPosition),
							new SimpleInventory(DISASSEMBLY_INVENTORY_SIZE), new SimpleInventory(RESULT_INVENTORY_SIZE)
					),
					Text.literal(ASSEMBLY_STATION_NAME)
			);
		}

		return new SimpleNamedScreenHandlerFactory(
				(syncId, inventory, player) -> new AssemblyStationScreenHandler(
						syncId, inventory, ScreenHandlerContext.create(world, blockPosition),
						assemblyStationBlockEntity.getDisassemblyInventory(), assemblyStationBlockEntity.getResultInventory()
				),
				Text.literal(ASSEMBLY_STATION_NAME)
		);
	}

	@Override
	public void onPlaced(@NotNull World world, @NotNull BlockPos blockPosition, @NotNull BlockState blockState, @Nullable LivingEntity placer, @Nullable ItemStack itemStack) {
		super.onPlaced(world, blockPosition, blockState, placer, itemStack);
		if (world.isClient) {
			return;
		}

		@NotNull var offsetBlockPosition = blockPosition.offset(blockState.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
		world.breakBlock(offsetBlockPosition, true, placer, 1);
		world.setBlockState(offsetBlockPosition, blockState.with(PART, AssemblyStationPart.RIGHT), 3);
		world.updateNeighbors(blockPosition, Blocks.AIR);
		blockState.updateNeighbors(world, blockPosition, 3);
	}

	@Override
	public void onBreak(@NotNull World world, @NotNull BlockPos blockPosition, @NotNull BlockState blockState, @Nullable PlayerEntity player) {
		AssemblyStationPart part = blockState.get(PART);
		BlockPos blockPos;
		if (part == AssemblyStationPart.LEFT) {
			blockPos = blockPosition.offset(blockState.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
		} else {
			blockPos = blockPosition.offset(blockState.get(FACING).rotateClockwise(Direction.Axis.Y));
		}
		if (!world.isClient) {
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
			world.updateNeighbors(blockPosition, Blocks.AIR);
			blockState.updateNeighbors(world, blockPosition, 3);

		}
		super.onBreak(world, blockPosition, blockState, player);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean canPlaceAt(@NotNull BlockState blockState, @NotNull WorldView world, @NotNull BlockPos blockPosition) {
		if (super.canPlaceAt(blockState, world, blockPosition)) {
			BlockPos blockPos = blockPosition.offset(blockState.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
			return !world.getBlockState(blockPos).isSolidBlock(world, blockPos);
		} else {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockRenderType getRenderType(@NotNull BlockState blockState) {
		if (blockState.get(PART) == AssemblyStationPart.RIGHT) {
			return BlockRenderType.INVISIBLE;
		}

		return super.getRenderType(blockState);
	}

	protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
		builder.add(FACING, PART);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull VoxelShape getOutlineShape(@NotNull BlockState blockState, @NotNull BlockView world, @NotNull BlockPos blockPosition, @NotNull ShapeContext context) {
		@NotNull var assemblyStationPart = blockState.get(PART);
		if (assemblyStationPart == AssemblyStationPart.LEFT) {
			return UpgradeStationBlock.rotateShape(Direction.SOUTH, blockState.get(FACING), SHAPE_LEFT.offset(0, 0, 0));
		}

		return UpgradeStationBlock.rotateShape(Direction.SOUTH, blockState.get(FACING), SHAPE_RIGHT.offset(-1, 0, 0));
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(@NotNull BlockPos blockPosition, @NotNull BlockState blockState) {
		if (blockState.get(PART) == AssemblyStationPart.LEFT) {
			return new AssemblyStationBlockEntity(blockPosition, blockState);
		} else {
			return null;
		}
	}

	public enum AssemblyStationPart implements StringIdentifiable {
		RIGHT("right"),
		LEFT("left");

		private final @NotNull String name;

		AssemblyStationPart(@NotNull String name) {
			this.name = name;
		}

		@Override
		public @NotNull String toString() {
			return this.name;
		}

		@Override
		public @NotNull String asString() {
			return this.name;
		}
	}
}
