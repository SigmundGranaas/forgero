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

	private int currentDepth;

	public VeinMiningStrategy(VeinBreaking handler) {
		this.handler = handler;
	}

	public VeinMiningStrategy(PropertyData data) {
		this.handler = new VeinBreaking((int) data.getValue(), data.getTags().get(0), data.getDescription());
	}

	@Override
	public List<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
		BlockState rootState = world.getBlockState(rootPos);
		this.currentDepth = handler.depth();
		var availableBlocks = new TreeMap<BlockPos, BlockState>();

		if (BlockBreakingHandler.isBreakableBlock(world, rootPos, player) && rootState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(handler.tag())))) {
			availableBlocks.putAll(getVeinMineableBlocksAtPosition(rootPos, world));
		}

		return availableBlocks.keySet().stream().toList();
	}

	private SortedMap<BlockPos, BlockState> getVeinMineableBlocksAtPosition(BlockPos rootBlockPos, BlockView world) {
		var rootBlockState = world.getBlockState(rootBlockPos);
		var directions = Arrays.asList(Direction.values());

		// Reversing the direction to make the algorithm prioritize blocks in other direction than up and down
		Collections.reverse(directions);

		// Take a block
		// Check around it in every direction
		// For each direction also check around it in every direction
		// Continue until depth <= 0
		// Then reset depth to handler.depth() and go to 2 (the next direction)

		ArrayList<BlockPos> veinMineableBlocks = new ArrayList<>();
		veinMineableBlocks.add(rootBlockPos);
		ArrayList<BlockPos> blocksToScan = new ArrayList<>();

		var blocksAroundRootBlock = getBlocksAroundBlock(rootBlockPos);
		blocksAroundRootBlock.forEach(blockPos -> {
			if (!veinMineableBlocks.contains(blockPos) && canBeMined(rootBlockState, world.getBlockState(blockPos))) {
				veinMineableBlocks.add(blockPos);
			}

			if (!blocksToScan.contains(blockPos)) {
				blocksToScan.add(blockPos);
			}
		});

		for (int i = this.currentDepth; i > 0; i--) {
			ArrayList<BlockPos> newBlocksToScan = new ArrayList<>();

			for (BlockPos blockToScanPos : blocksToScan) {
				var blocksAroundScannedBlock = getBlocksAroundBlock(blockToScanPos);
				blocksAroundScannedBlock.forEach(blockPos -> {
					if (canBeMined(rootBlockState, world.getBlockState(blockPos)) && !world.getBlockState(blockToScanPos).isAir()) {
						veinMineableBlocks.add(blockPos);
						newBlocksToScan.add(blockPos);
					}
				});
			}

			blocksToScan.clear();
			blocksToScan.addAll(newBlocksToScan);
		}

		TreeMap<BlockPos, BlockState> veinMineableBlocksMap = new TreeMap<>();
		veinMineableBlocks.forEach(blockPos -> {
			veinMineableBlocksMap.put(blockPos, world.getBlockState(blockPos));
		});

		return veinMineableBlocksMap;
	}

	private ArrayList<BlockPos> getBlocksAroundBlock(BlockPos blockPos) {
		var directions = Direction.values();
		ArrayList<BlockPos> offsetBlockPositions = new ArrayList<>();

		for (Direction direction : directions) {
			BlockPos offsetBlockPos = blockPos.offset(direction, 1);
			offsetBlockPositions.add(offsetBlockPos);
		}

		return offsetBlockPositions;
	}

	private boolean canBeMined(BlockState rootBlockState, BlockState blockState) {
		return blockState.getBlock() == rootBlockState.getBlock() || (canBeMinedByAxe(rootBlockState) && canBeMinedByAxe(blockState));
	}

	private boolean canBeMinedByAxe(BlockState blockState) {
		return blockState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier("forgero", "vein_mining_logs")));
	}
}
