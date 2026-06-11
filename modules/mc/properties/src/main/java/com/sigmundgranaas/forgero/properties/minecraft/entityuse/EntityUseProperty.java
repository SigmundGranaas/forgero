package com.sigmundgranaas.forgero.properties.minecraft.entityuse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.EntityUseEffect;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Property for handling right-click interactions on entities.
 * Triggers when a player uses an item on an entity (right-click).
 *
 * <p>Different from OnHit (which triggers on attack) and UseInteraction
 * (which is for stateful item use like bows).</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "minecraft:entity_use": {
 *     "effects": [
 *       { "type": "forgero:heal_entity", "amount": 5.0 },
 *       { "type": "forgero:damage_stack", "damage": 1 }
 *     ],
 *     "condition": {
 *       "type": "forgero:target_has_tag",
 *       "tag": "minecraft:tameable"
 *     }
 *   }
 * }
 * </pre>
 */
public record EntityUseProperty(
		List<EntityUseEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "entity_use");
	public static final ResolutionKey<List<EntityUseProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<EntityUseProperty> PROPERTY_KEY = new PropertyKey<>(EntityUseProperty.class, KEY_ID.toString());

	public static Codec<EntityUseProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.list(EntityUseEffect.CODEC).fieldOf("effects").forGetter(EntityUseProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (effects, condition) -> new EntityUseProperty(effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<EntityUseProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
