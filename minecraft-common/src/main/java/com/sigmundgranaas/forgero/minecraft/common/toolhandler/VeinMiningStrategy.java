package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	public Set<BlockPos> getAvailableBlocks(BlockView world, BlockPos rootPos, PlayerEntity player) {
		BlockState rootState = world.getBlockState(rootPos);
		this.currentDepth = handler.depth();

		if (BlockBreakingHandler.isBreakableBlock(world, rootPos, player) && rootState.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(handler.tag())))) {
			return getVeinMineableBlocksAtPosition(rootPos, world);
		}

		return Collections.emptySet();
	}

	private HashSet<BlockPos> getVeinMineableBlocksAtPosition(BlockPos rootBlockPos, BlockView world) {
		var rootBlockState = world.getBlockState(rootBlockPos);

		// Take a block
		// Check around it in every direction
		// For each direction also check around it in every direction
		// Continue until depth <= 0
		// Then reset depth to handler.depth() and go to 2 (the next direction)

		var veinMineableBlocks = new HashSet<BlockPos>();
		veinMineableBlocks.add(rootBlockPos);
		var blocksToScan = new HashSet<BlockPos>();
		blocksToScan.add(rootBlockPos);
		var scannedBlocks = new HashSet<BlockPos>();

		for (int i = this.currentDepth; i > 0 && !blocksToScan.isEmpty(); i--) {
			HashSet<BlockPos> newBlocksToScan = new HashSet<>();

			for (BlockPos blockToScanPos : blocksToScan) {
				if (scannedBlocks.contains(blockToScanPos)) {
					continue;
				}

				var blocksAroundScannedBlock = getBlocksAroundBlock(blockToScanPos);
				blocksAroundScannedBlock.forEach(blockPos -> {
					if (canBeMined(rootBlockState, world.getBlockState(blockPos)) && !world.getBlockState(blockToScanPos).isAir()) {
						veinMineableBlocks.add(blockPos);
						newBlocksToScan.add(blockPos);
					}
				});

				scannedBlocks.add(blockToScanPos);
			}

			blocksToScan.clear();
			blocksToScan.addAll(newBlocksToScan);
		}

		return veinMineableBlocks;
	}

	private HashSet<BlockPos> getBlocksAroundBlock(BlockPos blockPos) {
		var directions = Direction.values();
		var eightWayDirections = EightWayDirection.values();
		var offsetBlockPositions = new HashSet<BlockPos>();

		for (Direction direction : directions) {
			BlockPos offsetBlockPos = blockPos.offset(direction, 1);
			offsetBlockPositions.add(offsetBlockPos);
		}

		for (EightWayDirection eightWayDirection : eightWayDirections) {
			BlockPos offsetBlockPos = blockPos.add(eightWayDirection.getOffsetX(), 1, eightWayDirection.getOffsetZ());
			BlockPos offsetBlockPos2 = blockPos.add(eightWayDirection.getOffsetX(), -1, eightWayDirection.getOffsetZ());
			offsetBlockPositions.add(offsetBlockPos);
			offsetBlockPositions.add(offsetBlockPos2);
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
