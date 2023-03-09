package com.sigmundgranaas.forgero.minecraft.common.selector;

import static com.sigmundgranaas.forgero.minecraft.common.selector.BlockSelectionUtils.around;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

/**
 * BlockSelector that selects chainedBlocks in a radius around the root position.
 * <p>
 * Will exclude blocks that are not valid, and try to find valid diagonal blocks.
 *
 * @author StevePlays
 */
public class RadiusVeinMiningBlockSelectionStrategy implements BlockSelector {
	private final int depth;
	private final Predicate<BlockPos> isBlockValid;

	private Set<BlockPos> selectedBlocks = new HashSet<>();
	private Set<BlockPos> newBlocksToScan = new HashSet<>();

	public RadiusVeinMiningBlockSelectionStrategy(int depth, Predicate<BlockPos> isBlockValid) {
		this.depth = depth;
		this.isBlockValid = isBlockValid;
	}

	/**
	 * Selects blocks in a radius around the root position.
	 *
	 * @param rootPos origin position of the selection.
	 * @return A set of all the blocks that are valid and in the radius around the root position.
	 * Will return an empty set if the root block is not valid
	 */
	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos) {
		// return early if the root block is not a valid selection
		if (!isBlockValid.test(rootPos)) {
			return new HashSet<>();
		}

		selectedBlocks = new HashSet<>();
		selectedBlocks.add(rootPos);

		Set<BlockPos> blocksToScan = new HashSet<>();
		blocksToScan.add(rootPos);

		//Scanned blocks is used to prevent infinite loops
		Set<BlockPos> scannedBlocks = new HashSet<>();

		//Continue checking blocks until depth <= 0
		for (int i = this.depth; i > 0 && !blocksToScan.isEmpty(); i--) {
			newBlocksToScan = new HashSet<>();

			for (BlockPos blockToScanPos : blocksToScan) {
				//Skip blocks that have already been scanned
				if (scannedBlocks.contains(blockToScanPos)) {
					continue;
				}
				//Check all blocks around the block to scan and add to selection if valid
				Set<BlockPos> blocksAroundScannedBlock = around(blockToScanPos);
				blocksAroundScannedBlock.forEach(this::handleScannedBlock);

				scannedBlocks.add(blockToScanPos);
			}
			//Reset blocks to scan for a new depth
			blocksToScan.clear();
			blocksToScan.addAll(newBlocksToScan);
		}

		return selectedBlocks;
	}

	//Handling each block to see of it should propagate or stop the selection
	protected void handleScannedBlock(BlockPos blockPos) {
		if (isBlockValid.test(blockPos)) {
			selectedBlocks.add(blockPos);
			newBlocksToScan.add(blockPos);
		}
	}
}
