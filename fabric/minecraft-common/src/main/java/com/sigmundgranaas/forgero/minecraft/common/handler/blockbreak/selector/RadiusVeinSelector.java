package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelectionUtils.getBlockPositionsAround;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * BlockSelector that selects chained blocks in a radius around the root position.
 * <p>
 * Will exclude blocks that are not valid, and try to find valid diagonal blocks.
 *
 * @author Steveplays28
 */
@Getter
@Accessors(fluent = true)
public class RadiusVeinSelector implements BlockSelector {
	public static final String TYPE = "forgero:radius";
	public static final JsonBuilder<RadiusVeinSelector> BUILDER = HandlerBuilder.fromObject(RadiusVeinSelector.class, RadiusVeinSelector::fromJson);

	private final int depth;
	private final BlockFilter filter;
	private Function<BlockPos, Predicate<BlockPos>> rootPosValidator = (BlockPos root) -> (BlockPos blockPos) -> false;

	private Set<BlockPos> selectedBlocks = new HashSet<>();
	private Set<BlockPos> newBlocksToScan = new HashSet<>();

	public RadiusVeinSelector(int depth, BlockFilter filter) {
		this.depth = depth;
		this.filter = filter;
	}

	public RadiusVeinSelector(int depth, BlockFilter filter, Function<BlockPos, Predicate<BlockPos>> rootPosValidator) {
		this.depth = depth;
		this.filter = filter;
		this.rootPosValidator = rootPosValidator;
	}


	/**
	 * Constructs an {@link RadiusVeinSelector} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link RadiusVeinSelector}.
	 */
	public static RadiusVeinSelector fromJson(JsonObject json) {
		int radius = json.get("depth").getAsInt();
		BlockFilter filter = BlockFilter.fromJson(json.get("filter"));
		return new RadiusVeinSelector(radius, filter);
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
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		// return early if the root block is not a valid selection
		if (!filter.filter(source, rootPos, rootPos)) {
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
				Set<BlockPos> blocksAroundScannedBlock = getBlockPositionsAround(blockToScanPos);
				blocksAroundScannedBlock.forEach(pos -> handleScannedBlock(pos, rootPos, source));

				scannedBlocks.add(blockToScanPos);
			}
			//Reset blocks to scan for a new depth
			blocksToScan.clear();
			blocksToScan.addAll(newBlocksToScan);
		}

		return selectedBlocks;
	}

	//Handling each block to see of it should propagate or stop the selection
	protected void handleScannedBlock(BlockPos blockPos, BlockPos root, Entity source) {
		if (filter.filter(source, blockPos, root)) {
			selectedBlocks.add(blockPos);
			newBlocksToScan.add(blockPos);
		}
	}
}
