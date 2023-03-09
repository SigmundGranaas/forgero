package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Block selector that selects block in a straight column.
 */
public class ColumnBlockSelectionStrategy implements BlockSelector {
	private final int depth;
	private final int maxHeight;
	private final Predicate<BlockPos> isBlockValid;

	public ColumnBlockSelectionStrategy(int depth, int maxHeight, Predicate<BlockPos> isBlockValid) {
		this.depth = depth;
		this.maxHeight = maxHeight;
		this.isBlockValid = isBlockValid;
	}

	/**
	 * Selects blocks in straight columns until the max number of blocks is reached or a block is not valid.
	 *
	 * @param rootPos the root position of the selection
	 * @return A set of all the blocks that are valid and in the column around the root position.
	 * Will return an empty set if the root block is not valid
	 */
	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos) {
		if (!isBlockValid.test(rootPos)) {
			return new HashSet<>();
		}
		Set<BlockPos> testedRoots = new HashSet<>();
		Predicate<BlockPos> notTested = (pos) -> !testedRoots.contains(pos);
		Set<BlockPos> rootsToTest = new HashSet<>();
		List<BlockPos> columnRoots = new ArrayList<>();
		rootsToTest.add(rootPos);

		for (int i = 0; i < depth; i++) {
			Set<BlockPos> nextRoots = new HashSet<>();
			if (columnRoots.size() > depth) {
				break;
			}
			for (BlockPos root : rootsToTest) {
				if (testedRoots.contains(root)) {
					continue;
				}
				testedRoots.add(root);
				if (columnRoots.size() < depth) {
					if (isBlockValid.test(root)) {
						columnRoots.add(root);
						horizontals(root)
								.filter(notTested)
								.forEach(nextRoots::add);
					}

				} else {
					break;
				}
			}
			rootsToTest = nextRoots;
		}

		return columnRoots.stream()
				.map(this::handleColumn)
				.flatMap(Set::stream)
				.collect(Collectors.toUnmodifiableSet());
	}

	public Stream<BlockPos> horizontals(BlockPos root) {
		return Direction.Type.HORIZONTAL.stream().map(root::offset);
	}

	/**
	 * Iterates up and down in a column until the max height is reached or a block is not valid.
	 *
	 * @param root root position of the column
	 * @return a set of all the blocks in the column
	 */
	private Set<BlockPos> handleColumn(BlockPos root) {
		Set<BlockPos> positions = new HashSet<>();
		var currentPos = root;
		int current = 0;
		while (isBlockValid.test(currentPos) && current < maxHeight) {
			positions.add(currentPos);
			currentPos = currentPos.up();
			current++;
		}
		currentPos = root.down();
		while (isBlockValid.test(currentPos) && current < maxHeight) {
			positions.add(currentPos);
			currentPos = currentPos.down();
			current++;
		}
		return positions;
	}
}
