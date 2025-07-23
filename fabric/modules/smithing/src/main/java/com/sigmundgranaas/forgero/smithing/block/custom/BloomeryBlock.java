package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.fuel.FuelType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * A bloomery block used for smelting operations in the smithing module.
 * This block can be lit using flint and steel when fuel is present,
 * and produces various particle effects and sounds when active.
 *
 * <p>The bloomery has a custom shape (14 pixels height) and supports
 * horizontal facing directions. It can be fueled with coal or charcoal
 * and displays visual and audio effects when lit.</p>
 */
public class BloomeryBlock extends BlockWithEntity {
	/** The horizontal facing direction of the bloomery */
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	/** Whether the bloomery is currently lit/active */
	public static final BooleanProperty LIT = Properties.LIT;
	
	/** Custom shape: 14 pixels height (0-14), full width (0-16) */
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);

	// Used to track the previous lit state for sizzling sound logic
	private boolean wasPreviouslyLit = false;

	// Constructor for BloomeryBlock with default state.
	public BloomeryBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, false));
	}



	// Gets the outline shape of the bloomery block.
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	// Creates a new bloomery block entity at the specified position.
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BloomeryBlockEntity(pos, state);
	}

	// Returns the block entity ticker for server-side processing.
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : checkType(type, ModBlockEntities.BLOOMERY, BloomeryBlockEntity::serverTick);
	}

	// Handles player interaction with the bloomery block.
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			ItemStack heldItem = player.getStackInHand(hand);
			BlockEntity blockEntity = world.getBlockEntity(pos);

			if (!(blockEntity instanceof BloomeryBlockEntity bloomery)) {
				return ActionResult.PASS;
			}

			// Insert coal/charcoal into inventory
			if (isFuel(heldItem)) {
				if (bloomery.insertFuel(heldItem)) {
					if (!player.getAbilities().creativeMode) {
						heldItem.decrement(1);
					}
					return ActionResult.SUCCESS;
				}
				return ActionResult.PASS;
			}

			// Use flint & steel to light if fuel is present and not already lit
			if (heldItem.isOf(Items.FLINT_AND_STEEL) && !state.get(LIT)) {
				int fuelTime = bloomery.consumeFuelForLighting();
				if (fuelTime > 0) {
					// Damage flint & steel
					if (!player.getAbilities().creativeMode) {
						heldItem.damage(1, player, p -> p.sendToolBreakStatus(hand));
					}
					bloomery.lightWithFuel(fuelTime);
					world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL);
					bloomery.syncLitStateWithExtensions(world, pos, true);
					world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
					return ActionResult.SUCCESS;
				}
				return ActionResult.PASS;
			}
		}
		return ActionResult.PASS;
	}

	// Gets the fuel type from an item stack, or null if not fuel
	private FuelType getFuelType(ItemStack stack) {
		if (stack.isOf(Items.COAL)) return FuelType.COAL;
		if (stack.isOf(Items.CHARCOAL)) return FuelType.CHARCOAL;
		return null;
	}

	// Checks if the given item stack is a valid fuel for the bloomery.
	private boolean isFuel(ItemStack stack) {
		return getFuelType(stack) != null;
	}

	// Displays random particle effects and plays sounds when the bloomery is lit.
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		boolean isLit = state.get(LIT);

		if (isLit) {
			wasPreviouslyLit = true;
			double x = pos.getX() + 0.5;
			double y = pos.getY() + 1.4;
			double z = pos.getZ() + 0.5;

			// Sound effects
			if (random.nextFloat() < 0.1f) {
				// Main crackling/burning sound
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
						0.3f + random.nextFloat() * 0.2f, // Volume: 0.3-0.5
						0.8f + random.nextFloat() * 0.4f, // Pitch: 0.8-1.2
						false);
			}

			// Occasional furnace burning sounds
			if (random.nextFloat() < 0.05f) {
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS,
						0.2f + random.nextFloat() * 0.1f, // Volume: 0.2-0.3
						0.9f + random.nextFloat() * 0.2f, // Pitch: 0.9-1.1
						false);
			}

			// Rare lava popping sounds for dramatic effect
			if (random.nextFloat() < 0.02f) {
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
						0.1f + random.nextFloat() * 0.1f, // Volume: 0.1-0.2
						1.2f + random.nextFloat() * 0.3f, // Pitch: 1.2-1.5
						false);
			}

			// Campfire smoke particles
			if (random.nextFloat() < 0.8f) {
				// Main smoke column
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
						x + (random.nextDouble() - 0.5) * 0.3,
						y + 0.2,
						z + (random.nextDouble() - 0.5) * 0.3,
						0.0, 0.07, 0.0);
			}

			// Additional smoke variation
			if (random.nextFloat() < 0.4f) {
				world.addParticle(ParticleTypes.SMOKE,
						x + (random.nextDouble() - 0.5) * 0.4,
						y + random.nextDouble() * 0.3,
						z + (random.nextDouble() - 0.5) * 0.4,
						0.0, 0.05, 0.0);
			}

			// Lava spark particles for crackling effect
			if (random.nextFloat() < 0.3f) {
				world.addParticle(ParticleTypes.LAVA,
						x + (random.nextDouble() - 0.5) * 0.6,
						y + random.nextDouble() * 0.4,
						z + (random.nextDouble() - 0.5) * 0.6,
						(random.nextDouble() - 0.5) * 0.05,
						random.nextDouble() * 0.03,
						(random.nextDouble() - 0.5) * 0.05);
			}

			// Small ember particles
			if (random.nextFloat() < 0.4f) {
				Direction direction = state.get(FACING);
				double particleX = pos.getX() + 0.5;
				double particleY = pos.getY() + random.nextDouble() * 6.0 / 16.0;
				double particleZ = pos.getZ() + 0.5;

				double offset = 0.52;
				double sideOffset = random.nextDouble() * 0.6 - 0.3;

				switch (direction) {
					case WEST -> particleX -= offset;
					case EAST -> particleX += offset;
					case NORTH -> particleZ -= offset;
					case SOUTH -> particleZ += offset;
				}

				if (direction.getAxis() == Direction.Axis.X) {
					particleZ += sideOffset;
				} else {
					particleX += sideOffset;
				}

				world.addParticle(ParticleTypes.SMALL_FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
				world.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
			}

			// Occasional large smoke puffs
			if (random.nextFloat() < 0.1f) {
				world.addParticle(ParticleTypes.LARGE_SMOKE,
						x + (random.nextDouble() - 0.5) * 0.2,
						y + 0.2,
						z + (random.nextDouble() - 0.5) * 0.2,
						0.0, 0.1, 0.0);
			}
		} else {
			// Only play the sizzling sound once when transitioning from lit to unlit
			if (wasPreviouslyLit) {
				wasPreviouslyLit = false;
				world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
	}


	// Called when the block state is replaced.
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	// Gets the placement state for the bloomery block when placed by a player.
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	// Adds the block's properties to the state manager.
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	// Gets the render type for the bloomery block.
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
