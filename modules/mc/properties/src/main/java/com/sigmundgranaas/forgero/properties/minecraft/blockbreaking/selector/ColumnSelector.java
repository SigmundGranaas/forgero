package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.BlockFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ColumnSelector(int depth, int height, BlockFilter filter) implements BlockSelector {
	public static final String TYPE = "forgero:column";

	public static final Codec<ColumnSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("depth", 1).forGetter(ColumnSelector::depth),
			Codec.INT.optionalFieldOf("height", 1).forGetter(ColumnSelector::height),
			BlockFilter.CODEC.fieldOf("filter").forGetter(ColumnSelector::filter)
	).apply(instance, ColumnSelector::new));

	@Override
	public String type() {
		return TYPE;
	}

	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		if (!filter.filter(source, rootPos, rootPos)) {
			return new HashSet<>();
		}

		Set<BlockPos> testedRoots = new HashSet<>();
		Predicate<BlockPos> notTested = (pos) -> !testedRoots.contains(pos);
		Set<BlockPos> rootsToTest = new HashSet<>();
		List<BlockPos> columnRoots = new ArrayList<>();
		rootsToTest.add(rootPos);

		for (int i = 0; i < depth; i++) {
			Set<BlockPos> nextRoots = new HashSet<>();
			if (columnRoots.size() >= depth) {
				break;
			}

			for (BlockPos root : rootsToTest) {
				if (testedRoots.contains(root)) {
					continue;
				}
				if (columnRoots.size() >= depth) {
					break;
				}

				testedRoots.add(root);
				if (filter.filter(source, root, rootPos)) {
					columnRoots.add(root);
					horizontals(root)
							.filter(notTested)
							.forEach(nextRoots::add);
				}
			}
			rootsToTest = nextRoots;
		}

		return columnRoots.stream()
				.map(root -> handleColumn(root, source))
				.flatMap(Set::stream)
				.collect(Collectors.toUnmodifiableSet());
	}

	private Stream<BlockPos> horizontals(BlockPos root) {
		return Direction.Type.HORIZONTAL.stream().map(root::offset);
	}

	private Set<BlockPos> handleColumn(BlockPos root, Entity source) {
		Set<BlockPos> positions = new HashSet<>();
		positions.add(root);

		// Upwards
		BlockPos currentPos = root.up();
		while (positions.size() < height && filter.filter(source, currentPos, root)) {
			positions.add(currentPos);
			currentPos = currentPos.up();
		}

		// Downwards
		currentPos = root.down();
		while (positions.size() < height && filter.filter(source, currentPos, root)) {
			positions.add(currentPos);
			currentPos = currentPos.down();
		}

		return positions;
	}
}
