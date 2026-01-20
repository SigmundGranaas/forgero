package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.blocks.common.block.AbstractDoubleBlock;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.function.Supplier;

import static net.minecraft.block.Blocks.SMITHING_TABLE;

/**
 * The Upgrade Station block for installing and removing component upgrades.
 * <p>
 * This is a double-wide block that opens a screen handler for modifying
 * Forgero items by adding gems, bindings, and other upgrades.
 */
public class UpgradeStationBlock extends AbstractDoubleBlock {

	private static final Text TITLE = Text.translatable("container.forgero.upgrade_station");

	private static final VoxelShape SHAPE_LEFT;
	private static final VoxelShape SHAPE_RIGHT;

	static {
		SHAPE_LEFT = createLeftShape();
		SHAPE_RIGHT = createRightShape();
	}

	/**
	 * Supplier for ForgeroServices - set during initialization.
	 */
	private static Supplier<ForgeroServices> servicesSupplier = () -> null;

	/**
	 * Sets the services supplier. Called during mod initialization.
	 */
	public static void setServicesSupplier(Supplier<ForgeroServices> supplier) {
		servicesSupplier = supplier;
	}

	public UpgradeStationBlock() {
		super(Settings.copy(SMITHING_TABLE).strength(2.5F).sounds(BlockSoundGroup.WOOD));
	}

	public UpgradeStationBlock(Settings settings) {
		super(settings);
	}

	private static VoxelShape createLeftShape() {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.0625, 0.25, 0.875, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.125, 0.75, 0.25, 0.875, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.875, 0, 1, 1, 1));
		return shape;
	}

	private static VoxelShape createRightShape() {
		VoxelShape shape = VoxelShapes.empty();
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.75, 1.9375, 0.875, 0.9375));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.75, 0.125, 0.0625, 1.9375, 0.875, 0.25));
		shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1, 0.875, 0, 2, 1, 1));
		return shape;
	}

	@Override
	protected VoxelShape getLeftShape() {
		return SHAPE_LEFT;
	}

	@Override
	protected VoxelShape getRightShape() {
		return SHAPE_RIGHT;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
			if (factory != null) {
				player.openHandledScreen(factory);
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
			ForgeroServices services = servicesSupplier.get();
			if (services != null) {
				StationContext context = StationContext.create(services, world, pos);
				return new UpgradeStationScreenHandler(syncId, inventory, context, ScreenHandlerContext.create(world, pos));
			}
			// Fallback - no context (will limit functionality)
			return new UpgradeStationScreenHandler(syncId, inventory, null, ScreenHandlerContext.create(world, pos));
		}, TITLE);
	}
}
