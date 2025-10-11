package com.sigmundgranaas.forgero.properties.minecraft.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.properties.minecraft.loot.handler.LootHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A property that defines custom behavior for loot drops from blocks or entities.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "handler": {
 *     "type": "forgero:apply_functions",
 *     "functions": [
 *       {
 *         "type": "forgero:auto_smelt",
 *         "filter": {
 *           "type": "forgero:tag",
 *           "tag": "forgero:smeltable_ores"
 *         }
 *       },
 *       {
 *         "type": "forgero:item_transform",
 *         "input": {
 *           "type": "forgero:tag",
 *           "tag": "minecraft:logs"
 *         },
 *         "output": "minecraft:charcoal",
 *         "count": 2
 *       }
 *     ]
 *   },
 *   "condition": {
 *     "type": "forgero:target_has_tag",
 *     "tag": "minecraft:zombies"
 *   }
 * }
 * </pre>
 */
public record LootProperty(LootHandler handler, @Nullable Condition condition) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_loot_drop");
	public static final ResolutionKey<List<LootProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<LootProperty> PROPERTY_KEY = new PropertyKey<>(LootProperty.class, KEY_ID.toString());

	public static Codec<LootProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				LootHandler.CODEC.fieldOf("handler").forGetter(LootProperty::handler),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (handler, condition) -> new LootProperty(handler, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<LootProperty, List<LootProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<LootProperty> apply(OptimizedBakedResult<LootProperty> baked, DynamicContext context) {
			return baked.stream(context).collect(Collectors.toList());
		}
	}
}
