package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FunctionExecuteHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Block selector that selects block in a straight column.
 */
public class ColumnSelector implements BlockSelector {
	public static final String TYPE = "minecraft:function";
	public static final JsonBuilder<FunctionExecuteHandler> BUILDER = HandlerBuilder.fromObject(FunctionExecuteHandler.class, FunctionExecuteHandler::fromJson);

	private final int depth;
	private final int maxHeight;
	private final Predicate<BlockPos> isBlockValid;

	public ColumnSelector(int depth, int maxHeight, Predicate<BlockPos> isBlockValid) {
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
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		if (!isBlockValid.test(rootPos)) {
			return new HashSet<>();
		}

		//Collections used to keep track of which blocks have been tested and which blocks are to be tested next
		Set<BlockPos> testedRoots = new HashSet<>();
		Predicate<BlockPos> notTested = (pos) -> !testedRoots.contains(pos);
		Set<BlockPos> rootsToTest = new HashSet<>();
		List<BlockPos> columnRoots = new ArrayList<>();
		rootsToTest.add(rootPos);

		//Iterates through the blocks to test and adds the blocks that are valid to the column roots
		for (int i = 0; i < depth; i++) {
			Set<BlockPos> nextRoots = new HashSet<>();
			if (columnRoots.size() > depth) {
				break;
			}
			//Iterates through the blocks to test and adds the blocks that are valid to the column roots
			for (BlockPos root : rootsToTest) {
				if (testedRoots.contains(root)) {
					continue;
				} else if (columnRoots.size() > depth) {
					break;
				}

				testedRoots.add(root);
				if (isBlockValid.test(root)) {
					columnRoots.add(root);
					//Find all the horizontal blocks from a valid root and add them to the blocks to test
					horizontals(root)
							.filter(notTested)
							.forEach(nextRoots::add);
				}
			}
			//Reset the blocks to test to the blocks that were found in the last iteration
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
