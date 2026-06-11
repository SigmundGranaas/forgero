package com.sigmundgranaas.forgero.properties.minecraft.onsneak;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Orchestrator property for On-Sneak-Toggle events. This triggers when the wielder
 * starts sneaking, enabling active abilities and toggle-based effects.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:aoe",
 *     "radius": 3,
 *     "filters": [
 *       { "type": "forgero:is_hostile" }
 *     ]
 *   },
 *   "effects": [
 *     {
 *       "type": "forgero:particle",
 *       "particle": "minecraft:smoke",
 *       "count": 50
 *     },
 *     {
 *       "type": "forgero:sound",
 *       "sound": "minecraft:entity.player.breath",
 *       "volume": 1.0
 *     }
 *   ]
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Stealth mode - activate cloaking or particles when sneaking</li>
 *   <li>Activate abilities - trigger AOE effects on sneak</li>
 *   <li>Toggle effects - switch between modes</li>
 *   <li>Visual/audio feedback - indicate ability readiness</li>
 * </ul>
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Only triggers when starting to sneak (not continuous)</li>
 *   <li>State is tracked per entity UUID to detect toggles</li>
 *   <li>Memory cleanup on player disconnect prevents leaks</li>
 *   <li>Works with armor and held items</li>
 * </ul>
 *
 * @param selector The entity selector for determining which entities are affected
 * @param effects The list of effects to apply when sneak is toggled on
 * @param condition Optional condition that must be met for effects to apply
 */
public record OnSneakToggleProperty(
		EntitySelector selector,
		List<OnHitEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_sneak_toggle");
	public static final ResolutionKey<List<OnSneakToggleProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnSneakToggleProperty> PROPERTY_KEY = new PropertyKey<>(OnSneakToggleProperty.class, KEY_ID.toString());

	public static Codec<OnSneakToggleProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				EntitySelector.CODEC.fieldOf("selector").forGetter(OnSneakToggleProperty::selector),
				Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnSneakToggleProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnSneakToggleProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnSneakToggleProperty, List<OnSneakToggleProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnSneakToggleProperty> apply(OptimizedBakedResult<OnSneakToggleProperty> baked) {
			return baked.all().collect(Collectors.toList());
		}
	}
}
