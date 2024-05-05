package com.sigmundgranaas.forgero.minecraft.common.feature;

import java.util.Set;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.BlockBreakHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.BlockBreakSpeedCalculator;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Single;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * <p>Handles the block-breaking feature by utilizing different selectors and hardness calculators
 * based on provided JSON configurations. This feature is configurable with various parameters
 * to customize the block-breaking behavior.</p>
 *
 * <p><b>JSON Configuration Format:</b>
 * The JSON configuration allows specifying the type, selector, hardness calculation, predicate,
 * title, and description. Here are the detailed explanations and examples:</p>
 *
 * <h3>Complete Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "minecraft:block_breaking",
 *   "selector": {
 *     "type": "forgero:radius",
 *     "filter": [
 *       "forgero:same_block"
 *     ],
 *     "radius": 1
 *   },
 *   "speed": "forgero:all",
 *   "predicate": {
 *     "type": "minecraft:block",
 *     "tag": "forgero:vein_mining_ores"
 *   },
 *   "title": "feature.forgero.vein_mining.title",
 *   "description": "feature.forgero.ore_vein_mining.description"
 * }
 * </pre>
 *
 * <h3>Selector:</h3>
 * <p>The selector parameter defines how blocks are selected when the feature is triggered.
 * Different types of selectors like column, pattern, and radius can be used. Here are
 * descriptions and examples of each:</p>
 *
 * <h4>Column Selector:</h4>
 * <p>Selects blocks in a straight column. It has parameters like depth and maxHeight.</p>
 * <pre>
 * "selector": {
 *   "type": "forgero:column",
 *   "depth": 3,
 *   "maxHeight": 10
 * }
 * </pre>
 *
 * <h4>Pattern Selector:</h4>
 * <p>Selects blocks based on a string pattern, which can be rotated based on the player’s
 * facing direction. A pattern of Xs, spaces, and Cs (representing the center position) is used.</p>
 *
 * <p>Example 3x3 pattern:</p>
 * <pre>
 * "selector": {
 *   "type": "forgero:pattern",
 *   "pattern": ["xxx", "xcx", "xxx"]
 * }
 * </pre>
 *
 * <h4>Radius Vein Selector:</h4>
 * <p>Selects chained blocks in a radius around the root position, excluding invalid blocks and
 * finding valid diagonal blocks.</p>
 * <pre>
 * "selector": {
 *   "type": "forgero:radius",
 *   "depth": 5
 * }
 * </pre>
 * <p>
 * /**
 * <h3>Filter:</h3>
 * <p>Filters are instrumental within selectors to refine the block selection process further.
 * They determine how the block selection propagates by allowing or disallowing blocks based on
 * specified conditions. Filters can be expressed in various formats and combined to create more
 * complex selection criteria.</p>
 *
 * <h4>Examples:</h4>
 * <p>Single filter example:</p>
 * <pre>
 *  "filter": "forgero:similar_block"
 * </pre>
 *
 * <p>Array of filters example:</p>
 * <pre>
 *  "filter": ["forgero:same_block", "forgero:can_mine"]
 * </pre>
 *
 * <p>Object as a filter example:</p>
 * <pre>
 *  "filter": {"type": "minecraft:block", "tag": "forgero:vein_mining_ores"}
 * </pre>
 *
 * <h4>Filter Types:</h4>
 *
 * <h5>Same Block:</h5>
 * <p>This filter will select only the exact same block.</p>
 *
 * <h5>Similar Block:</h5>
 * <p>Blocks that share a tag in the format <code>namespace:similar_block/tagname</code> are selected.
 * These tags can be added typically through configurations, e.g., <code>similar_block/coal.json</code>:</p>
 * <pre>
 * {
 *   "replace": false,
 *   "values": [
 *     "#minecraft:coal_ores",
 *     "minecraft:coal_block"
 *   ]
 * }
 * </pre>
 *
 * <h5>Can Mine:</h5>
 * <p>This filter includes all blocks that the player can currently mine, ensuring that only blocks
 * that the player has proficiency in are selected. It can be combined with other filters for more refined
 * selection, e.g.,</p>
 * <pre>
 * "filter": [
 *   "forgero:can_mine",
 *   {
 *     "type": "minecraft:block",
 *     "tag": "forgero:vein_mining_ores"
 *   }
 * ]
 * </pre>
 *
 * <h3>Hardness Calculation:</h3>
 * <p>This optional parameter calculates the hardness of the blocks being broken.</p>
 * <pre>
 *   "speed": "forgero:all",
 * </pre>
 *
 * <h3>Predicate:</h3>
 * <p>This is a requirement for this handler to be triggered.</p>
 * <pre>
 * "predicate": {
 *   "type": "minecraft:block",
 *   "tag": "forgero:vein_mining_ores"
 * }
 * </pre>
 *
 * <h3>Title and Description (Optional):</h3>
 * <p>Optional parameters for providing additional information or description about the feature.</p>
 * <pre>
 * "title": "feature.forgero.vein_mining.title",
 * "description": "feature.forgero.ore_vein_mining.description"
 * </pre>
 */
public class BlockBreakFeature extends BasePredicateFeature implements BlockBreakHandler {
	public static final String BLOCK_BREAK_TYPE = "minecraft:block_breaking";
	public static final ClassKey<BlockBreakFeature> KEY = new ClassKey<>(BLOCK_BREAK_TYPE, BlockBreakFeature.class);
	public static final String SELECTOR = "selector";
	public static final String SPEED = "speed";
	public static final FeatureBuilder<BlockBreakFeature> BUILDER = FeatureBuilder.of(BLOCK_BREAK_TYPE, BlockBreakFeature::buildFromBase);
	private final BlockSelector selector;
	private final BlockBreakSpeedCalculator hardnessCalculator;

	public BlockBreakFeature(BasePredicateData data, BlockSelector selector, BlockBreakSpeedCalculator calculator) {
		super(data);
		this.selector = selector;
		this.hardnessCalculator = calculator;
		if (!data.type().equals(BLOCK_BREAK_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + BLOCK_BREAK_TYPE);
		}
	}

	private static BlockBreakFeature buildFromBase(BasePredicateData data, JsonElement element) {
		BlockSelector selector = BlockSelector.DEFAULT;
		BlockBreakSpeedCalculator blockHardnessCalculator = Single.DEFAULT;
		if (element.isJsonObject() && element.getAsJsonObject().has(SELECTOR)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(BlockSelector.KEY, object.get(SELECTOR));
			if (handlerOpt.isPresent()) {
				selector = handlerOpt.get();
			}
		}

		if (element.isJsonObject() && element.getAsJsonObject().has(SPEED)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(BlockBreakSpeedCalculator.KEY, object.get(SPEED));
			if (handlerOpt.isPresent()) {
				blockHardnessCalculator = handlerOpt.get();
			}
		}

		return new BlockBreakFeature(data, selector, blockHardnessCalculator);
	}

	@Override
	public Set<BlockPos> selectBlocks(Entity source, BlockPos target) {
		return selector.select(target, source);
	}

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {
		return hardnessCalculator.calculateBlockBreakingDelta(source, target, selectedBlocks);
	}

	@Override
	public void onUsed(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {

	}
}
