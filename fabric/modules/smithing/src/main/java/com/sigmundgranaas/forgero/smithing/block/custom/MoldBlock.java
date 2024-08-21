package com.sigmundgranaas.forgero.smithing.block.custom;


import com.sigmundgranaas.forgero.smithing.block.entity.MoldBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.MOLD;


public class MoldBlock extends BlockWithEntity {
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 2, 16);
	public static final IntProperty PROGRESS = IntProperty.of("progress", 0, 100);
	public static final BooleanProperty FILLED = BooleanProperty.of("filled");

	public MoldBlock(Settings settings) {
		super(settings.nonOpaque());
		setDefaultState(getStateManager().getDefaultState()
				.with(PROGRESS, 0)
				.with(FILLED, false));
	}


	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(PROGRESS, FILLED);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MoldBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof MoldBlockEntity moldEntity) {
				if (!moldEntity.isEmpty() && moldEntity.isSolidified()) {
					ItemStack result = moldEntity.getResult();
					player.sendMessage(Text.literal("Retrieved " + result.getName().getString() + " from the mold."), true);
					player.getInventory().offerOrDrop(result);
					world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					return ActionResult.SUCCESS;
				} else if (!moldEntity.isEmpty()) {
					player.sendMessage(Text.literal("The mold is still cooling."), true);
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return MoldBlock.checkType(type, MOLD, MoldBlockEntity::tick);
	}


	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(FILLED) && state.get(PROGRESS) < 100) {
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 0.5;
			double z = pos.getZ() + 0.5;

			if (state.get(PROGRESS) > 90) {
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, 0.05, 0);
			}

			if (random.nextFloat() < 0.3f) {
				world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.05, 0);
			}
			if (random.nextFloat() < 0.1f) {
				world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}
		}
	}

}
