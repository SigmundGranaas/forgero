package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

/**
 * Modifies blocks in the world, enabling Frost Walker-style effects or environmental manipulation.
 * This handler respects world protection systems and requires careful configuration.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Frost Walker (Water to Ice):</b></p>
 * <pre>
 * {
 *   "type": "forgero:modify_block",
 *   "action": "replace",
 *   "target_block": "minecraft:water",
 *   "replacement": "minecraft:ice",
 *   "consume_item": false
 * }
 * </pre>
 *
 * <p><b>Lava to Obsidian:</b></p>
 * <pre>
 * {
 *   "type": "forgero:modify_block",
 *   "action": "replace",
 *   "target_block": "minecraft:lava",
 *   "replacement": "minecraft:obsidian",
 *   "consume_item": false
 * }
 * </pre>
 *
 * <p><b>Break Block (Any):</b></p>
 * <pre>
 * {
 *   "type": "forgero:modify_block",
 *   "action": "destroy",
 *   "replacement": "minecraft:air",
 *   "consume_item": false
 * }
 * </pre>
 *
 * <h3>Actions:</h3>
 * <ul>
 *   <li><b>REPLACE</b> - Replaces the block with the replacement block</li>
 *   <li><b>DESTROY</b> - Breaks the block (drops controlled by consume_item flag)</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Frost Walker enchantment - freeze water</li>
 *   <li>Environmental manipulation - convert lava, extinguish fire</li>
 *   <li>Mining effects - instant block breaking</li>
 *   <li>Utility effects - clear foliage, remove obstacles</li>
 * </ul>
 *
 * <h3>Important Safety Notes:</h3>
 * <ul>
 *   <li><b>Permissions:</b> Comprehensive checks including hardness, block entities, and multiblock structures</li>
 *   <li><b>Protected Areas:</b> Respects spawn protection and claim systems</li>
 *   <li><b>Unbreakable Blocks:</b> Cannot modify bedrock, barriers, or command blocks</li>
 *   <li><b>Block Entities:</b> Drops inventory contents from containers before destruction</li>
 *   <li><b>Multiblock Structures:</b> Properly handles beds, doors, and tall plants</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * Block modifications trigger block updates and may cause lag if used excessively.
 * Recommended for occasional use, not continuous tick-based effects.
 *
 * @param action The modification action (REPLACE or DESTROY)
 * @param targetBlock Optional filter - only modify blocks matching this identifier
 * @param replacement The block to place (for REPLACE) or minecraft:air (for DESTROY)
 * @param consumeItem If true and action is DESTROY, prevents block drops
 * @param handleMultiblock If true, properly handles multiblock structures (beds, doors, etc.)
 */
public record ModifyBlockHandler(BlockAction action, Optional<Identifier> targetBlock, Identifier replacement, boolean consumeItem, boolean handleMultiblock) implements ContextualEffectHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyBlockHandler.class);
	public static final String TYPE = "forgero:modify_block";
	public static final Codec<ModifyBlockHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockAction.CODEC.fieldOf("action").forGetter(ModifyBlockHandler::action),
			Identifier.CODEC.optionalFieldOf("target_block").forGetter(ModifyBlockHandler::targetBlock),
			Identifier.CODEC.fieldOf("replacement").forGetter(ModifyBlockHandler::replacement),
			Codec.BOOL.optionalFieldOf("consume_item", false).forGetter(ModifyBlockHandler::consumeItem),
			Codec.BOOL.optionalFieldOf("handle_multiblock", true).forGetter(ModifyBlockHandler::handleMultiblock)
	).apply(instance, ModifyBlockHandler::new));

	@Override
	public void apply(Entity source, Entity target) {
		if (target.getWorld().isClient) {
			return; // Server-side only
		}

		World world = target.getWorld();
		BlockPos pos = target.getBlockPos();
		BlockState currentState = world.getBlockState(pos);

		// Security check: Block hardness (prevent breaking bedrock, barriers, etc.)
		float hardness = currentState.getHardness(world, pos);
		if (hardness < 0) {
			LOGGER.debug("Cannot modify unbreakable block: {} at {}", currentState.getBlock(), pos);
			return; // Unbreakable block (bedrock, barriers, etc.)
		}

		// Check if current block matches target filter (if specified)
		if (targetBlock.isPresent()) {
			Block expectedBlock = Registries.BLOCK.get(targetBlock.get());
			if (expectedBlock == null) {
				LOGGER.debug("Invalid target block identifier: {}", targetBlock.get());
				return;
			}
			if (currentState.getBlock() != expectedBlock) {
				return; // Target block doesn't match
			}
		}

		// Permission check - only allow if source is a player with permission
		if (source instanceof PlayerEntity player) {
			if (!world.canPlayerModifyAt(player, pos)) {
				return; // Player doesn't have permission to modify this block
			}
		} else {
			// Non-player entities cannot modify blocks
			return;
		}

		// Get replacement block
		Block replacementBlock = Registries.BLOCK.get(replacement);
		if (replacementBlock == null) {
			LOGGER.debug("Invalid replacement block identifier: {}", replacement);
			return; // Invalid replacement block
		}

		// Handle block entities (containers, etc.)
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof Inventory inventory) {
			// Drop inventory contents to prevent item loss
			dropInventoryContents(world, pos, inventory);
		}

		// Handle multiblock structures if enabled
		if (handleMultiblock) {
			handleMultiblockStructure(world, pos, currentState);
		}

		// Perform the modification action
		switch (action) {
			case REPLACE -> {
				world.setBlockState(pos, replacementBlock.getDefaultState());
			}
			case DESTROY -> {
				// breakBlock's second parameter: true = drop items, false = no drops
				world.breakBlock(pos, !consumeItem);
			}
		}
	}

	/**
	 * Handles multiblock structures by breaking connected parts.
	 * Prevents floating bed tops, door halves, etc.
	 *
	 * @param world The world
	 * @param pos Position of the block being modified
	 * @param state Block state
	 */
	private void handleMultiblockStructure(World world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();

		// Handle beds
		if (block instanceof BedBlock) {
			BedPart part = state.get(BedBlock.PART);
			Direction facing = state.get(BedBlock.FACING);
			BlockPos otherHalf = part == BedPart.HEAD ? pos.offset(facing.getOpposite()) : pos.offset(facing);

			if (world.getBlockState(otherHalf).getBlock() instanceof BedBlock) {
				world.breakBlock(otherHalf, !consumeItem);
			}
		}

		// Handle doors
		if (block instanceof DoorBlock) {
			DoubleBlockHalf half = state.get(DoorBlock.HALF);
			BlockPos otherHalf = half == DoubleBlockHalf.UPPER ? pos.down() : pos.up();

			if (world.getBlockState(otherHalf).getBlock() instanceof DoorBlock) {
				world.breakBlock(otherHalf, !consumeItem);
			}
		}

		// Handle tall plants/flowers
		if (block instanceof TallPlantBlock) {
			DoubleBlockHalf half = state.get(TallPlantBlock.HALF);
			BlockPos otherHalf = half == DoubleBlockHalf.UPPER ? pos.down() : pos.up();

			if (world.getBlockState(otherHalf).getBlock() instanceof TallPlantBlock) {
				world.breakBlock(otherHalf, !consumeItem);
			}
		}
	}

	/**
	 * Drops inventory contents from a container to prevent item loss.
	 *
	 * @param world The world
	 * @param pos Position of the container
	 * @param inventory The inventory to drop
	 */
	private void dropInventoryContents(World world, BlockPos pos, Inventory inventory) {
		for (int i = 0; i < inventory.size(); i++) {
			net.minecraft.item.ItemStack stack = inventory.getStack(i);
			if (!stack.isEmpty()) {
				net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
					world,
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5,
					stack.copy()
				);
				world.spawnEntity(itemEntity);
			}
		}
		inventory.clear();
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * The type of block modification to perform.
	 */
	public enum BlockAction implements StringIdentifiable {
		/** Replace the block with the replacement block */
		REPLACE,
		/** Destroy the block (with optional drops) */
		DESTROY;

		public static final Codec<BlockAction> CODEC = StringIdentifiable.createCodec(
				BlockAction::values,
				(value) -> String.valueOf(BlockAction.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
