package com.sigmundgranaas.forgero.properties.minecraft.blockuse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.BlockUseEffect;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Property for handling right-click interactions on blocks.
 * Triggers when a player uses an item on a block (right-click).
 *
 * <p>Different from OnHitBlock (which triggers on attack) and BlockBreaking
 * (which is for mining blocks).</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "minecraft:block_use": {
 *     "effects": [
 *       { "type": "forgero:till_soil" },
 *       { "type": "forgero:damage_stack", "damage": 1 },
 *       { "type": "forgero:play_sound", "sound": "minecraft:item.hoe.till" }
 *     ],
 *     "condition": {
 *       "type": "forgero:block_has_tag",
 *       "tag": "minecraft:dirt"
 *     }
 *   }
 * }
 * </pre>
 */
public record BlockUseProperty(
		List<BlockUseEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "block_use");
	public static final ResolutionKey<List<BlockUseProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<BlockUseProperty> PROPERTY_KEY = new PropertyKey<>(BlockUseProperty.class, KEY_ID.toString());

	public static Codec<BlockUseProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.list(BlockUseEffect.CODEC).fieldOf("effects").forGetter(BlockUseProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (effects, condition) -> new BlockUseProperty(effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<BlockUseProperty, List<BlockUseProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<BlockUseProperty> apply(OptimizedBakedResult<BlockUseProperty> baked, DynamicContext context) {
			return baked.stream(context).collect(Collectors.toList());
		}
	}
}
