package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.AbstractBloomeryBlockEntity;



import net.minecraft.block.AbstractBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractBloomeryBlock extends BlockWithEntity
{
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	protected AbstractBloomeryBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState((BlockState) ((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(LIT, false));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		this.openScreen(world, pos, player);
		return ActionResult.CONSUME;
	}

	protected abstract void openScreen(World var1, BlockPos var2, PlayerEntity var3);

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		BlockEntity blockEntity;
		if (itemStack.hasCustomName() && (blockEntity = world.getBlockEntity(pos)) instanceof AbstractBloomeryBlockEntity) {
			((AbstractBloomeryBlockEntity)blockEntity).setCustomName(itemStack.getName());
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.isOf(newState.getBlock())) {
			return;
		}
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof AbstractBloomeryBlockEntity) {
			if (world instanceof ServerWorld) {
				ItemScatterer.spawn(world, pos, (Inventory)((AbstractBloomeryBlockEntity) blockEntity));
				((AbstractBloomeryBlockEntity)blockEntity).getRecipesUsedAndDropExperience((ServerWorld)world, Vec3d.ofCenter(pos));
			}
			world.updateComparators(pos, this);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> checkType(World world, BlockEntityType<T> givenType, BlockEntityType<? extends AbstractBloomeryBlockEntity> expectedType) {
		return world.isClient ? null : AbstractBloomeryBlock.checkType(givenType, expectedType, AbstractBloomeryBlockEntity::tick);
	}
}
