package com.sigmundgranaas.forgero.block.upgradestation;

import com.sigmundgranaas.forgero.core.Forgero;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
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

import static net.minecraft.block.Blocks.SMITHING_TABLE;

public class UpgradeStationBlock extends HorizontalFacingBlock {

	public static final EnumProperty<UpgradeStationBlockPart> PART = EnumProperty.of("part", UpgradeStationBlockPart.class);
	public static final Block UPGRADE_STATION_BLOCK = new UpgradeStationBlock(Settings.copy(SMITHING_TABLE).strength(2.5F).sounds(BlockSoundGroup.WOOD));
	public static final BlockItem UPGRADE_STATION_ITEM = new BlockItem(UPGRADE_STATION_BLOCK, new Item.Settings());
	// a public identifier for multiple parts of our bigger chest
	public static final Identifier UPGRADE_STATION = new Identifier(Forgero.NAMESPACE, "upgrade_station");

	private static final VoxelShape SHAPE_LEFT;

	private static final VoxelShape SHAPE_RIGHT;

	static {
		SHAPE_LEFT = left();
		SHAPE_RIGHT = right();
	}

	protected UpgradeStationBlock(Settings settings) {
		super(settings);
		this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(PART, UpgradeStationBlockPart.LEFT));
		this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(PART, UpgradeStationBlockPart.LEFT);
	}

	private static VoxelShape left() {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.0625, 0.25, 0.875, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.75, 0.25, 0.875, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.875, 0, 1, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.3125, 0.125, 0.3125));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.6875, 0.3125, 0.125, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.5, 0.125, 1, 0.6875, 0.1875));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.1875, 0.6875, 0.875, 0.875));

		return shape;
	}

	public static VoxelShape right() {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.75, 1.9375, 0.875, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.0625, 1.9375, 0.875, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1, 0.875, 0, 2, 1, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.6875, 0, 0.6875, 2, 0.125, 1));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.6875, 0, 0, 2, 0.125, 0.3125));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1, 0.5, 0.125, 1.75, 0.6875, 0.1875));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.3125, 0.25, 0.1875, 1.6875, 0.875, 0.8125));

		return shape;
	}

	public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

		int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = VoxelShapes.empty();
		}

		return buffer[0];
	}

	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return stateFrom.isOf(this) || super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			//This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
			//a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
			NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

			if (screenHandlerFactory != null) {
				//With this call the server will request the client to open the appropriate Screenhandler
				player.openHandledScreen(screenHandlerFactory);
			}
		}
		return ActionResult.SUCCESS;
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction direction = ctx.getHorizontalPlayerFacing().getOpposite();
		return this.getDefaultState().with(FACING, direction);
	}

	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new UpgradeStationScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), Text.literal("assembly_station"));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		if (!world.isClient) {
			BlockPos blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
			world.breakBlock(blockPos, true, placer, 1);
			world.setBlockState(blockPos, state.with(PART, UpgradeStationBlockPart.RIGHT), 3);
			world.updateNeighbors(pos, Blocks.AIR);
			state.updateNeighbors(world, pos, 3);
		}
	}

	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		UpgradeStationBlockPart part = state.get(PART);
		BlockPos blockPos;
		if (part == UpgradeStationBlockPart.LEFT) {
			blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
		} else {
			blockPos = pos.offset(state.get(FACING).rotateClockwise(Direction.Axis.Y));
		}
		if (!world.isClient) {
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
			world.updateNeighbors(pos, Blocks.AIR);
			state.updateNeighbors(world, pos, 3);

		}
		super.onBreak(world, pos, state, player);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (super.canPlaceAt(state, world, pos)) {
			BlockPos blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
			return !world.getBlockState(blockPos).isSolidBlock(world, blockPos);
		} else {
			return false;
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		if (state.get(PART) == UpgradeStationBlockPart.RIGHT) {
			return BlockRenderType.INVISIBLE;
		}
		return super.getRenderType(state);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		UpgradeStationBlockPart part = state.get(PART);
		if (part == UpgradeStationBlockPart.LEFT) {
			return rotateShape(Direction.SOUTH, state.get(FACING), SHAPE_LEFT.offset(0, 0, 0));
		}
		return rotateShape(Direction.SOUTH, state.get(FACING), SHAPE_RIGHT.offset(-1, 0, 0));
	}

	public enum UpgradeStationBlockPart implements StringIdentifiable {
		RIGHT("right"),
		LEFT("left");

		private final String name;

		UpgradeStationBlockPart(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

		public String asString() {
			return this.name;
		}
	}
}
