package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import static net.minecraft.block.Blocks.SMITHING_TABLE;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.block.upgradestation.entity.UpgradeStationBlockEntity;
import com.sigmundgranaas.forgero.minecraft.common.registry.entity.block.BlockEntityRegistry;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
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

public class UpgradeStationBlock extends HorizontalFacingBlock implements BlockEntityProvider {

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

		return shape;
	}

	public static VoxelShape right() {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.75, 1.9375, 0.875, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.0625, 1.9375, 0.875, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1, 0.875, 0, 2, 1, 1));

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
			BlockPos actualPos = pos;

			// If this is the RIGHT part, get the position of the LEFT part
			if (state.get(PART) == UpgradeStationBlockPart.RIGHT) {
				Direction facing = state.get(FACING);
				actualPos = pos.offset(facing.rotateClockwise(Direction.Axis.Y));

				// Verify the LEFT part exists with the expected block state
				BlockState leftState = world.getBlockState(actualPos);
				if (!leftState.isOf(this) || leftState.get(PART) != UpgradeStationBlockPart.LEFT) {
					return ActionResult.FAIL;
				}
			}

			// Get the block entity at the actual position
			BlockEntity blockEntity = world.getBlockEntity(actualPos);
			if (blockEntity instanceof NamedScreenHandlerFactory screenHandlerFactory) {
				player.openHandledScreen(screenHandlerFactory);
			} else {
				System.err.println("Failed to open upgrade station GUI: No valid block entity at " + actualPos);
				return ActionResult.FAIL;
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
		return world.getBlockEntity(pos, BlockEntityRegistry.UPGRADE_STATION_BLOCK_ENTITY)
			.map(blockEntity -> (NamedScreenHandlerFactory)blockEntity)
			.orElse(null);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		// Only create a block entity for the LEFT part
		if (state.get(PART) == UpgradeStationBlockPart.LEFT) {
			return new UpgradeStationBlockEntity(pos, state);
		}
		return null;
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

			// Transfer the item to the block entity inventory if applicable
			if (state.get(PART) == UpgradeStationBlockPart.LEFT &&
				world.getBlockEntity(pos) instanceof UpgradeStationBlockEntity blockEntity &&
				itemStack.hasNbt() && itemStack.getNbt().contains("StoredItem")) {
				ItemStack storedItem = ItemStack.fromNbt(itemStack.getNbt().getCompound("StoredItem"));
				blockEntity.setInventoryStack(storedItem);
			}

			// Mark the block entity as dirty to ensure it's saved
			if (world.getBlockEntity(pos) != null) {
				world.getBlockEntity(pos).markDirty();
			}
		}
	}

	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		UpgradeStationBlockPart part = state.get(PART);
		BlockPos otherPartPos;
		BlockPos blockEntityPos = pos;

		if (part == UpgradeStationBlockPart.LEFT) {
			otherPartPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
		} else {
			otherPartPos = pos.offset(state.get(FACING).rotateClockwise(Direction.Axis.Y));
			blockEntityPos = otherPartPos; // If breaking RIGHT part, the inventory is in the LEFT part
		}

		// Drop items from the LEFT part's inventory
		if (!world.isClient && world.getBlockEntity(blockEntityPos) instanceof UpgradeStationBlockEntity blockEntity) {
			ItemStack storedItem = blockEntity.getCompositeInventory().getStack(0);
			if (!storedItem.isEmpty()) {
				Block.dropStack(world, pos, storedItem.copy());
			}
		}

		if (!world.isClient) {
			world.setBlockState(otherPartPos, Blocks.AIR.getDefaultState(), 3);
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
