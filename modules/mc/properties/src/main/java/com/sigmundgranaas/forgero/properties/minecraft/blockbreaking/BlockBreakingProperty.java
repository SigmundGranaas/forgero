package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.hardness.BlockBreakSpeedCalculator;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.BlockSelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A property that defines custom block-breaking behavior, including how blocks are selected
 * and how breaking speed is calculated. It is a conditional property, meaning its activation
 * can depend on various static and dynamic in-game conditions.
 * <p>
 * This property replaces the old `BlockBreakFeature`.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:radius",
 *     "radius": 2,
 *     "filter": {
 *       "type": "forgero:filter_wrapper",
 *       "filters": [
 *         { "type": "forgero:can_mine" },
 *         { "type": "forgero:same_block" }
 *       ]
 *     }
 *   },
 *   "speed": {
 *     "type": "forgero:all"
 *   },
 *   "condition": {
 *     "type": "forgero:is_root"
 *   }
 * }
 * </pre>
 */
public record BlockBreakingProperty(
	BlockSelector selector,
		BlockBreakSpeedCalculator speed,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "block_breaking");
	public static final ResolutionKey<List<BlockBreakingProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<BlockBreakingProperty> PROPERTY_KEY = new PropertyKey<>(BlockBreakingProperty.class, KEY_ID.toString());

	public static Codec<BlockBreakingProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BlockSelector.CODEC.fieldOf("selector").forGetter(BlockBreakingProperty::selector),
				BlockBreakSpeedCalculator.CODEC.fieldOf("speed").forGetter(BlockBreakingProperty::speed),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, speed, condition) -> new BlockBreakingProperty(selector, speed, condition.orElse(null))));
	}

	public static class Engine extends AbstractConditionalPropertyEngine<BlockBreakingProperty, List<BlockBreakingProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<BlockBreakingProperty> apply(OptimizedBakedResult<BlockBreakingProperty> baked) {
			return baked.all().collect(Collectors.toList());
		}
	}
}
