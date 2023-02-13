package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

import java.util.*;

public class VeinMiningStrategy implements BlockBreakingStrategy {
	private final VeinBreaking handler;

	public VeinMiningStrategy(VeinBreaking handler) {
		this.handler = handler;
	}

	public VeinMiningStrategy(PropertyData data) {
		this.handler = new VeinBreaking((int) data.getValue(), data.getTags().get(0), data.getDescription());
	}

	@Override
	public List<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
		BlockState rootState = world.getBlockState(rootPos);
		int depth = handler.depth();
		var availableBlocks = new TreeMap<BlockPos, BlockState>();
//		var queue = new PriorityQueue<BlockPos>();

		if (BlockBreakingHandler.isBreakableBlock(world, rootPos, player) && rootState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(handler.tag())))) {
			availableBlocks.put(rootPos, world.getBlockState(rootPos));
//			queue.add(rootPos);

			calculateNextBlocks(availableBlocks, depth, world);
		}

		return availableBlocks.keySet().stream().toList();
	}

	private SortedMap<BlockPos, BlockState> calculateNextBlocks(SortedMap<BlockPos, BlockState> currentBlocks, int depth, BlockView world) {
		if (depth <= 0) {
			return Collections.emptySortedMap();
		}

		BlockPos currentBlockPos = currentBlocks.lastKey();
		if (currentBlockPos == null) {
			return Collections.emptySortedMap();
		}

		var rootBlockState = currentBlocks.get(currentBlocks.firstKey());
		var rootBlock = currentBlocks.get(currentBlocks.firstKey()).getBlock();
		var directions = Arrays.asList(Direction.values());

//		queue.remove();
		// Reversing the direction to make the algorithm prioritize blocks in other direction than up and down
		Collections.reverse(directions);

		for (Direction direction : directions) {
			BlockPos newBlock = currentBlockPos.offset(direction, 1);

			if (!currentBlocks.containsKey(newBlock)) {
				// Axes are an exception here, because they should mine blocks of other types included in their tag as well in one go
				if (currentBlocks.get(newBlock).getBlock() == rootBlock || (canBeMinedByAxe(rootBlockState) && canBeMinedByAxe(world.getBlockState(newBlock)))) {
					currentBlocks.put(newBlock, world.getBlockState(newBlock));
//					queue.add(newBlock);

					depth -= 1;
				}

				for (Direction direction2 : directions) {
					BlockPos newBlock2 = newBlock.offset(direction2, 1);
					if (world.getBlockState(newBlock2).getBlock() == rootBlock || (canBeMinedByAxe(rootBlock.getDefaultState()) && canBeMinedByAxe(world.getBlockState(newBlock2))) && !currentBlocks.containsKey(newBlock2)) {
						currentBlocks.put(newBlock2, world.getBlockState(newBlock2));
//						queue.add(newBlock2);

						depth -= 1;
					}
				}
				if (depth < 1) {
					return Collections.emptySortedMap();
				}
			}
		}

		currentBlocks.putAll(calculateNextBlocks(currentBlocks, depth, world));

		return Collections.emptySortedMap();
	}

	private boolean canBeMinedByAxe(BlockState blockState) {
		return blockState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier("forgero", "vein_mining_logs")));
	}
}
