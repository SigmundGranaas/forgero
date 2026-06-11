package com.sigmundgranaas.forgero.properties.minecraft.onhitblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector.BlockSelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Orchestrator property for On-Hit-Block events. It defines the "when" (when a block is hit),
 * the "which blocks" (via a selector with filters), and the "what" (via a list of effects).
 *
 * <p>Note: This is different from block breaking. This triggers when blocks are hit/attacked,
 * not when they are mined or broken.</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:single"
 *   },
 *   "effects": [
 *     { "type": "forgero:block_sound", "sound": "minecraft:block.stone.break", "volume": 1.0 },
 *     { "type": "forgero:block_particle", "particle": "minecraft:flame", "count": 5 }
 *   ],
 *   "condition": {
 *     "type": "forgero:is_sneaking"
 *   }
 * }
 * </pre>
 */
public record OnHitBlockProperty(
		BlockSelector selector,
		List<OnHitBlockEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_hit_block");
	public static final ResolutionKey<List<OnHitBlockProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnHitBlockProperty> PROPERTY_KEY = new PropertyKey<>(OnHitBlockProperty.class, KEY_ID.toString());

	public static Codec<OnHitBlockProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BlockSelector.CODEC.fieldOf("selector").forGetter(OnHitBlockProperty::selector),
				Codec.list(OnHitBlockEffect.CODEC).fieldOf("effects").forGetter(OnHitBlockProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnHitBlockProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnHitBlockProperty, List<OnHitBlockProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnHitBlockProperty> apply(OptimizedBakedResult<OnHitBlockProperty> baked) {
			return baked.all().collect(Collectors.toList());
		}
	}
}
