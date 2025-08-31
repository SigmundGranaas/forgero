package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.BlockFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record RadiusVeinSelector(int radius, BlockFilter filter) implements BlockSelector {
	public static final String TYPE = "forgero:radius";

	public static final Codec<RadiusVeinSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("radius", 1).forGetter(RadiusVeinSelector::radius),
			BlockFilter.CODEC.fieldOf("filter").forGetter(RadiusVeinSelector::filter)
	).apply(instance, RadiusVeinSelector::new));

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
		Set<BlockPos> selectedBlocks = new HashSet<>();
		selectedBlocks.add(rootPos);

		Set<BlockPos> blocksToScan = new HashSet<>();
		blocksToScan.add(rootPos);
		Set<BlockPos> scannedBlocks = new HashSet<>();

		for (int i = 0; i < radius && !blocksToScan.isEmpty(); i++) {
			Set<BlockPos> newBlocksToScan = new HashSet<>();

			for (BlockPos blockToScanPos : blocksToScan) {
				if (scannedBlocks.contains(blockToScanPos)) {
					continue;
				}

				Set<BlockPos> blocksAroundScannedBlock = getBlockPositionsAround(blockToScanPos);
				for (BlockPos pos : blocksAroundScannedBlock) {
					if (!selectedBlocks.contains(pos) && filter.filter(source, pos, rootPos)) {
						selectedBlocks.add(pos);
						newBlocksToScan.add(pos);
					}
				}
				scannedBlocks.add(blockToScanPos);
			}
			blocksToScan = newBlocksToScan;
		}
		return selectedBlocks;
	}

	private static Set<BlockPos> getBlockPositionsAround(BlockPos pos) {
		Set<BlockPos> positions = new HashSet<>();
		for (Direction direction : Direction.values()) {
			positions.add(pos.offset(direction));
		}
		return positions;
	}
}
