package com.sigmundgranaas.forgero.smithing.block.custom;

import static com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.MOLD;

import com.sigmundgranaas.forgero.smithing.block.entity.custom.MoldBlockEntity;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.recipe.Custom.MetalMoldRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
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

public class MoldBlock extends BlockWithEntity {
	public static final IntProperty PROGRESS = IntProperty.of("progress", 0, 100);
	public static final BooleanProperty FILLED = BooleanProperty.of("filled");

	private static final VoxelShape DEFAULT_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 2, 16);

	private final VoxelShape customShape;

	// Constructor for MoldBlock with custom shape.
	public MoldBlock(@NotNull Settings settings, VoxelShape customVoxelShape) {
		super(settings.nonOpaque());
		setDefaultState(getStateManager().getDefaultState()
				.with(PROGRESS, 0)
				.with(FILLED, false));
		this.customShape = customVoxelShape != null ? customVoxelShape : DEFAULT_SHAPE;
	}

	// Returns the outline shape of the mold block.
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return customShape;
	}

	// Adds block properties to the state manager.
	@Override
	protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
		builder.add(PROGRESS, FILLED);
	}

	// Creates a new mold block entity.
	@Override
	public BlockEntity createBlockEntity(BlockPos blockPosition, BlockState blockState) {
		return new MoldBlockEntity(blockPosition, blockState);
	}

	// Returns the render type for the mold block.
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	// Handles player interaction with the mold block.
	@SuppressWarnings("deprecation")
	@Override
	public ActionResult onUse(BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @Nullable PlayerEntity player, @Nullable Hand hand, @NotNull BlockHitResult hit) {
		if (world.isClient || player == null) {
			return ActionResult.PASS;
		}

		@Nullable BlockEntity blockEntity = world.getBlockEntity(blockPosition);
		if (!(blockEntity instanceof MoldBlockEntity moldEntity)) {
			return ActionResult.PASS;
		}

		ItemStack heldItem = player.getStackInHand(hand);

		// Handle crucible pouring
		if (heldItem.getItem() instanceof CrucibleItem crucibleItem && !crucibleItem.isEmpty(heldItem)) {
			if (moldEntity.isEmpty()) {
				// Check for valid recipe
				MetalMoldRecipe recipe = findRecipe(world, heldItem);
				if (recipe != null) {
					// Pour liquid into mold
					if (crucibleItem.removeLiquid(heldItem, recipe.liquidAmount())) {
						moldEntity.pourLiquid(
								recipe.liquid(),
								recipe.liquidAmount(),
								recipe.coolingTime(),
								recipe.getOutput(world.getRegistryManager())
						);

						world.playSound(null, blockPosition, SoundEvents.ITEM_BUCKET_EMPTY_LAVA,
								SoundCategory.BLOCKS, 0.8F, 1.0F);
						player.sendMessage(Text.literal("Poured liquid metal into the mold."), true);
						return ActionResult.SUCCESS;
					}
				} else {
					player.sendMessage(Text.literal("No valid recipe for this liquid and mold combination."), true);
				}
			} else {
				player.sendMessage(Text.literal("The mold is already filled."), true);
			}
			return ActionResult.SUCCESS;
		}

		// Handle result retrieval
		if (heldItem.isEmpty() && !moldEntity.isEmpty()) {
			if (moldEntity.isSolidified()) {
				ItemStack result = moldEntity.takeResult(); // Use takeResult to clear the mold
				player.sendMessage(Text.literal("Retrieved " + result.getName().getString() + " from the mold."), true);
				player.getInventory().offerOrDrop(result);
				world.playSound(null, blockPosition, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
						((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				return ActionResult.SUCCESS;
			} else {
				int progress = blockState.get(PROGRESS);
				player.sendMessage(Text.literal("The mold is still cooling... (" + progress + "%)"), true);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	// Finds a valid recipe for the given crucible and this mold.
	private MetalMoldRecipe findRecipe(World world, ItemStack crucible) {
		// Create a simple inventory for recipe matching
		SimpleInventory inventory = new SimpleInventory(3);
		inventory.setStack(0, new ItemStack(this)); // Mold block as item
		inventory.setStack(2, crucible); // Crucible

		return world.getRecipeManager()
				.getFirstMatch(MetalMoldRecipe.Type.INSTANCE, inventory, world)
				.orElse(null);
	}

	// Returns the block entity ticker for cooling and state updates.
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return MoldBlock.checkType(type, MOLD, MoldBlockEntity::tick);
	}

	// Displays particle effects and plays sounds based on cooling progress.
	@Override
	public void randomDisplayTick(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @NotNull Random random) {
		if (blockState.get(FILLED)) {
			double x = blockPosition.getX() + 0.5;
			double y = blockPosition.getY() + 0.2;
			double z = blockPosition.getZ() + 0.5;

			int progress = blockState.get(PROGRESS);

			// Hot liquid effects (early stages)
			if (progress < 30) {
				// Glowing hot liquid particles
				if (random.nextFloat() < 0.6f) {
					world.addParticle(ParticleTypes.LAVA,
							x + (random.nextDouble() - 0.5) * 0.6,
							y,
							z + (random.nextDouble() - 0.5) * 0.6,
							0, 0.01, 0);
				}

				// Flame particles
				if (random.nextFloat() < 0.4f) {
					world.addParticle(ParticleTypes.FLAME,
							x + (random.nextDouble() - 0.5) * 0.4,
							y + 0.1,
							z + (random.nextDouble() - 0.5) * 0.4,
							0, 0.02, 0);
				}

				// Crackling sounds
				if (random.nextFloat() < 0.1f) {
					world.playSound(x, y, z, SoundEvents.BLOCK_LAVA_POP,
							SoundCategory.BLOCKS, 0.3F, 1.2F + random.nextFloat() * 0.3F, false);
				}
			}

			// Cooling effects (middle stages)
			else if (progress < 70) {
				// Steam/smoke as it cools
				if (random.nextFloat() < 0.5f) {
					world.addParticle(ParticleTypes.SMOKE,
							x + (random.nextDouble() - 0.5) * 0.5,
							y + 0.1,
							z + (random.nextDouble() - 0.5) * 0.5,
							0, 0.05, 0);
				}

				// Occasional small flames
				if (random.nextFloat() < 0.2f) {
					world.addParticle(ParticleTypes.SMALL_FLAME,
							x + (random.nextDouble() - 0.5) * 0.3,
							y + 0.05,
							z + (random.nextDouble() - 0.5) * 0.3,
							0, 0.01, 0);
				}

				// Cooling sounds
				if (random.nextFloat() < 0.05f) {
					world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
							SoundCategory.BLOCKS, 0.2F, 0.8F + random.nextFloat() * 0.4F, false);
				}
			}

			// Final cooling (late stages)
			else if (progress < 100) {
				// Light smoke
				if (random.nextFloat() < 0.3f) {
					world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
							x + (random.nextDouble() - 0.5) * 0.3,
							y + 0.05,
							z + (random.nextDouble() - 0.5) * 0.3,
							0, 0.03, 0);
				}

				// Rare hissing sound
				if (random.nextFloat() < 0.02f) {
					world.playSound(x, y, z, SoundEvents.BLOCK_FIRE_EXTINGUISH,
							SoundCategory.BLOCKS, 0.1F, 1.5F, false);
				}
			}
		}
	}

	// Called when the block state is replaced, drops contents from the mold.
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof MoldBlockEntity moldEntity) {
				moldEntity.dropContents(world, pos);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}
