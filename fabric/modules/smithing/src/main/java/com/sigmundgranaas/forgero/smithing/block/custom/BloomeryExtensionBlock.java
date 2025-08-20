package com.sigmundgranaas.forgero.smithing.block.custom;

import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryExtensionBlockEntity;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.custom.CrucibleItem;
import com.sigmundgranaas.forgero.smithing.fuel.FuelType;
import com.sigmundgranaas.forgero.smithing.block.entity.BloomeryBlockEntity;
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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class BloomeryExtensionBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;

	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

	// Constructor for BloomeryExtensionBlock with default state.
	public BloomeryExtensionBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, false));
	}

	// Creates a new bloomery extension block entity.
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BloomeryExtensionBlockEntity(pos, state);
	}

	// Returns the render type for the bloomery extension block.
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	// Returns the block entity ticker for server-side processing.
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return checkType(type, ModBlockEntities.BLOOMERY_EXTENSION, world.isClient ? null : BloomeryExtensionBlockEntity::serverTick);
	}

	// Handles player interaction with the bloomery extension block.
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof BloomeryExtensionBlockEntity extensionEntity) {
			ItemStack heldItem = player.getStackInHand(hand);

			// If player has empty hand OR is sneaking, try to remove an item
			if (heldItem.isEmpty() || player.isSneaking()) {
				ItemStack removed = extensionEntity.removeFirstItem();
				if (!removed.isEmpty()) {
					player.giveItemStack(removed);
					return ActionResult.SUCCESS;
				}
			}
			// If player has an item, try to place it in the appropriate slot
			else if (!heldItem.isEmpty()) {
				ItemStack remaining;
				// If sneaking and holding ore, add the whole stack at once
				if (player.isSneaking() && isOre(heldItem)) {
					remaining = addWholeOreStack(extensionEntity, heldItem);
				} else {
					remaining = addItemToAppropriateSlot(extensionEntity, heldItem);
				}
				if (remaining.getCount() < heldItem.getCount()) {
					heldItem.setCount(remaining.getCount());
					return ActionResult.SUCCESS;
				}
			}
		}

		return ActionResult.PASS;
	}

	// Adds an entire ore stack to the ore slot if possible.
	private ItemStack addWholeOreStack(BloomeryExtensionBlockEntity entity, ItemStack stack) {
		ItemStack oreSlot = entity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT);
		if (oreSlot.isEmpty()) {
			int toInsert = Math.min(stack.getCount(), stack.getMaxCount());
			entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, stack.copyWithCount(toInsert));
			return stack.copyWithCount(stack.getCount() - toInsert);
		} else if (ItemStack.canCombine(stack, oreSlot)) {
			int space = oreSlot.getMaxCount() - oreSlot.getCount();
			if (space > 0) {
				int toInsert = Math.min(stack.getCount(), space);
				oreSlot.increment(toInsert);
				entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, oreSlot);
				return stack.copyWithCount(stack.getCount() - toInsert);
			}
		}
		return stack;
	}

	// Add this method to get fuel type from items
	private FuelType getFuelType(ItemStack stack) {
		if (stack.isOf(net.minecraft.item.Items.COAL)) return FuelType.COAL;
		if (stack.isOf(net.minecraft.item.Items.CHARCOAL)) return FuelType.CHARCOAL;
		return null;
	}

	// Places items in appropriate slots based on item type.
	private ItemStack addItemToAppropriateSlot(BloomeryExtensionBlockEntity entity, ItemStack stack) {
		// Add fuel handling at the beginning
		FuelType fuelType = getFuelType(stack);
		if (fuelType != null) {
			// Find the main bloomery block relative to this extension's facing
			Direction extensionFacing = entity.getCachedState().get(FACING);
			Direction rightSide = extensionFacing.rotateYClockwise(); // Bloomery is to the right of the extension
			BlockPos bloomeryPos = entity.getPos().offset(rightSide);

			World world = entity.getWorld();
			if (world != null) {
				BlockEntity bloomeryEntity = world.getBlockEntity(bloomeryPos);
				if (bloomeryEntity instanceof BloomeryBlockEntity bloomery) {
					if (bloomery.insertFuel(stack)) {
						return stack.copyWithCount(stack.getCount() - 1);
					}
				}
			}
			return stack;
		}

		boolean hasTool = !entity.getStack(BloomeryExtensionBlockEntity.TOOL_SLOT).isEmpty();
		boolean hasCrucibleOrOre = !entity.getStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT).isEmpty()
				|| !entity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT).isEmpty();

		// If a tool is present, do not accept crucible or ore
		if (hasTool) {
			if (isValidTool(stack) && entity.getStack(BloomeryExtensionBlockEntity.TOOL_SLOT).isEmpty()) {
				entity.setStack(BloomeryExtensionBlockEntity.TOOL_SLOT, stack.copyWithCount(1));
				return stack.copyWithCount(stack.getCount() - 1);
			}
			// Do not accept crucible or ore if tool is present
			return stack;
		}

		// If crucible or ore is present, do not accept a tool
		if (hasCrucibleOrOre) {
			// Accept stacking for ore, but only add one at a time
			if (isOre(stack)) {
				ItemStack oreSlot = entity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT);
				if (oreSlot.isEmpty()) {
					entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, stack.copyWithCount(1));
					return stack.copyWithCount(stack.getCount() - 1);
				} else if (ItemStack.canCombine(stack, oreSlot)) {
					int space = oreSlot.getMaxCount() - oreSlot.getCount();
					if (space > 0) {
						oreSlot.increment(1);
						entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, oreSlot);
						return stack.copyWithCount(stack.getCount() - 1);
					}
				}
			}
			if ((stack.getItem() instanceof CrucibleItem) && entity.getStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT).isEmpty()) {
				// Use custom crucible stack with CustomModelData = 1
				entity.setStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT, entity.createCustomCrucibleStack());
				return stack.copyWithCount(stack.getCount() - 1);
			}
			// Do not accept tool if crucible or ore is present
			return stack;
		}

		// If all slots are empty, allow any valid item, but only add one at a time
		if (stack.getItem() instanceof CrucibleItem && entity.getStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT).isEmpty()) {
			// Use custom crucible stack with CustomModelData = 1
			entity.setStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT, entity.createCustomCrucibleStack());
			return stack.copyWithCount(stack.getCount() - 1);
		}
		else if (isOre(stack)) {
			ItemStack oreSlot = entity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT);
			if (oreSlot.isEmpty()) {
				entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, stack.copyWithCount(1));
				return stack.copyWithCount(stack.getCount() - 1);
			} else if (ItemStack.canCombine(stack, oreSlot)) {
				int space = oreSlot.getMaxCount() - oreSlot.getCount();
				if (space > 0) {
					oreSlot.increment(1);
					entity.setStack(BloomeryExtensionBlockEntity.ORE_SLOT, oreSlot);
					return stack.copyWithCount(stack.getCount() - 1);
				}
			}
		}
		else if (isValidTool(stack) && entity.getStack(BloomeryExtensionBlockEntity.TOOL_SLOT).isEmpty()) {
			entity.setStack(BloomeryExtensionBlockEntity.TOOL_SLOT, stack.copyWithCount(1));
			return stack.copyWithCount(stack.getCount() - 1);
		}

		// If all appropriate slots are full, return the original stack
		return stack;
	}

	// Checks if an item is ore that can be smelted in the bloomery.
	private boolean isOre(ItemStack stack) {
		// Check if the item is in conventional ore tags
		return stack.isIn(net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags.ORES) ||
			   stack.getItem().toString().contains("ore") ||
			   stack.getItem().toString().contains("ingot") ||
			   stack.getItem().toString().contains("raw_");
	}

	// Checks if an item is a valid tool for the tool slot.
	private boolean isValidTool(ItemStack stack) {
		// Accept StateItem as a valid tool (adjust logic for your mod as needed)
		return stack.getItem() instanceof com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
	}

	// Called when the block state is replaced. Drops all items from the inventory.
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BloomeryExtensionBlockEntity extensionEntity) {
				// Drop all items in the inventory
				ItemScatterer.spawn(world, pos, extensionEntity.getInventory());
				world.updateComparators(pos, this);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	// Displays particle effects when the extension is lit, with special particles for crucible and tools.
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		boolean isLit = state.get(LIT);

		if (isLit) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof BloomeryExtensionBlockEntity extensionEntity) {
				ItemStack crucible = extensionEntity.getStack(BloomeryExtensionBlockEntity.CRUCIBLE_SLOT);
				ItemStack tool = extensionEntity.getStack(BloomeryExtensionBlockEntity.TOOL_SLOT);

				// Crucible slot position (center with slight offset)
				double crucibleX = pos.getX() + 0.5;
				double crucibleY = pos.getY() + 0.35;
				double crucibleZ = pos.getZ() + 0.5;

				// Tool slot position (center)
				double toolX = pos.getX() + 0.5;
				double toolY = pos.getY() + 0.35;
				double toolZ = pos.getZ() + 0.5;

				// Empty inventory position (center, slightly above)
				boolean isEmpty = crucible.isEmpty() && tool.isEmpty() && extensionEntity.getStack(BloomeryExtensionBlockEntity.ORE_SLOT).isEmpty();
				double emptyX = pos.getX() + 0.5;
				double emptyY = pos.getY() + 0.7;
				double emptyZ = pos.getZ() + 0.5;

				// Increase particle frequency by raising probabilities
				if (isEmpty) {
					if (random.nextFloat() < 0.4f) {
						world.addParticle(ParticleTypes.SMOKE, emptyX, emptyY, emptyZ, 0.0, 0.01, 0.0);
					}
				} else {
					// Special particles for crucible
					if (!crucible.isEmpty()) {
						if (random.nextFloat() < 0.9f) {
							world.addParticle(ParticleTypes.LAVA, crucibleX, crucibleY, crucibleZ, 0.0, 0.07, 0.0);
						}
						if (random.nextFloat() < 0.3f) {
							for (int i = 0; i < 2; i++) {
								double px = crucibleX + (random.nextDouble() - 0.5) * 0.5;
								double py = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.15;
								double pz = crucibleZ + (random.nextDouble() - 0.5) * 0.5;
								if (random.nextBoolean()) {
									world.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0, 0.0, 0.0);
								} else {
									world.addParticle(ParticleTypes.SMALL_FLAME, px, py, pz, 0.0, 0.0, 0.0);
								}
							}
						}
					} else {
						// Default smoke particles from crucible slot if no crucible
						if (random.nextFloat() < 0.8f) {
							world.addParticle(ParticleTypes.SMOKE, crucibleX, crucibleY, crucibleZ, 0.0, 0.05, 0.0);
						}
					}

					// Special particles for tool
					if (!tool.isEmpty()) {
						if (random.nextFloat() < 0.3f) {
							for (int i = 0; i < 2; i++) {
								double px = toolX + (random.nextDouble() - 0.5) * 0.5;
								double py = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.15;
								double pz = toolZ + (random.nextDouble() - 0.5) * 0.5;
								if (random.nextBoolean()) {
									world.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0, 0.0, 0.0);
								} else {
									world.addParticle(ParticleTypes.SMALL_FLAME, px, py, pz, 0.0, 0.0, 0.0);
								}
							}
						}

						for (int i = 0; i < 2; i++) {
							double px = toolX + (random.nextDouble() - 0.5) * 0.2;
							double py = toolY + 0.2;
							double pz = toolZ + (random.nextDouble() - 0.5) * 0.2;
							world.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, 0.01, 0.0);
						}
					}
				}
			}
		}
	}

	// Returns the outline shape of the bloomery extension block.
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	// Gets the placement state for the bloomery extension when placed by a player.
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();

		// Find the adjacent bloomery block and match its facing
		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos bloomeryPos = pos.offset(direction.getOpposite());
			BlockState bloomeryState = world.getBlockState(bloomeryPos);

			if (bloomeryState.getBlock() instanceof BloomeryBlock) {
				Direction bloomeryFacing = bloomeryState.get(BloomeryBlock.FACING);
				Direction leftSide = bloomeryFacing.rotateYCounterclockwise(); // Get the left side relative to facing

				// If this position is to the left of the bloomery, use the bloomery's facing
				if (direction == leftSide) {
					// Check if the bloomery is lit and inherit that state
					boolean bloomeryIsLit = bloomeryState.get(BloomeryBlock.LIT);
					return this.getDefaultState()
							.with(FACING, bloomeryFacing)
							.with(LIT, bloomeryIsLit);
				}
			}
		}

		// Fallback to default placement if no valid bloomery found
		return this.getDefaultState()
				.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
				.with(LIT, false);
	}

	// Checks if the bloomery extension can be placed at the specified position.
	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (!super.canPlaceAt(state, world, pos)) {
			return false;
		}

		// Check all directions for a bloomery block and determine if this position is to the left
		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockPos bloomeryPos = pos.offset(direction.getOpposite());
			BlockState bloomeryState = world.getBlockState(bloomeryPos);

			if (bloomeryState.getBlock() instanceof BloomeryBlock) {
				Direction bloomeryFacing = bloomeryState.get(BloomeryBlock.FACING);
				Direction leftSide = bloomeryFacing.rotateYCounterclockwise(); // Get the left side relative to facing

				// Check if this extension position is to the left of the bloomery
				if (direction == leftSide) {
					return true;
				}
			}
		}

		return false;
	}

	// Adds the block's properties to the state manager.
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}
