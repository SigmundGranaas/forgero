package com.sigmundgranaas.forgero.properties.minecraft.useinteraction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;

import net.minecraft.util.UseAction;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Property that defines how an item behaves during use interactions.
 * This is the core orchestrator for stateful use actions like bows, spears, and consumables.
 *
 * <p>Unlike event-based properties (OnHit, OnTick), use interactions have a lifecycle:</p>
 * <ul>
 *   <li><b>on_start</b>: When right-click is initiated</li>
 *   <li><b>on_tick</b>: Every tick while use button is held</li>
 *   <li><b>on_release</b>: When use button is released</li>
 *   <li><b>on_finish</b>: When max use time is reached</li>
 * </ul>
 *
 * <p>This property follows the same patterns as {@link com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty}:</p>
 * <ul>
 *   <li>Handlers are registered with type strings and codecs</li>
 *   <li>Handler dispatch uses instanceof checks for SimpleUseHandler vs ContextualUseHandler</li>
 *   <li>Property implements ConditionalProperty for condition evaluation</li>
 * </ul>
 *
 * <h3>JSON Configuration Example (Bow):</h3>
 * <pre>
 * {
 *   "minecraft:use_interaction": {
 *     "use_action": "BOW",
 *     "max_use_time": 72000,
 *     "used_on_release": true,
 *     "on_start": [
 *       { "type": "forgero:start_use" }
 *     ],
 *     "on_release": [
 *       { "type": "forgero:damage_stack", "damage": 1 }
 *     ]
 *   }
 * }
 * </pre>
 */
public record UseInteractionProperty(
		UseAction useAction,
		int maxUseTime,
		boolean usedOnRelease,
		List<UseHandler> onStart,
		List<UseHandler> onTick,
		List<UseHandler> onRelease,
		List<UseHandler> onFinish,
		@Nullable Condition condition
) implements ConditionalProperty {

	public static final OpenIdentifier KEY_ID = new OpenIdentifier("forgero", "use_interaction");
	public static final ResolutionKey<List<UseInteractionProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<UseInteractionProperty> PROPERTY_KEY = new PropertyKey<>(UseInteractionProperty.class, KEY_ID.toString());

	/**
	 * Codec for UseAction enum.
	 * Uses case-insensitive parsing to accept both "bow" and "BOW" in JSON.
	 */
	public static final Codec<UseAction> USE_ACTION_CODEC = Codec.STRING.xmap(
			str -> UseAction.valueOf(str.toUpperCase()),
			UseAction::name
	);

	/**
	 * Creates a codec for UseInteractionProperty with the given condition codec.
	 */
	public static Codec<UseInteractionProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				USE_ACTION_CODEC.optionalFieldOf("use_action", UseAction.NONE).forGetter(UseInteractionProperty::useAction),
				Codec.INT.optionalFieldOf("max_use_time", 0).forGetter(UseInteractionProperty::maxUseTime),
				Codec.BOOL.optionalFieldOf("used_on_release", false).forGetter(UseInteractionProperty::usedOnRelease),
				Codec.list(UseHandler.CODEC).optionalFieldOf("on_start", Collections.emptyList()).forGetter(UseInteractionProperty::onStart),
				Codec.list(UseHandler.CODEC).optionalFieldOf("on_tick", Collections.emptyList()).forGetter(UseInteractionProperty::onTick),
				Codec.list(UseHandler.CODEC).optionalFieldOf("on_release", Collections.emptyList()).forGetter(UseInteractionProperty::onRelease),
				Codec.list(UseHandler.CODEC).optionalFieldOf("on_finish", Collections.emptyList()).forGetter(UseInteractionProperty::onFinish),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (useAction, maxUseTime, usedOnRelease, onStart, onTick, onRelease, onFinish, condition) ->
				new UseInteractionProperty(useAction, maxUseTime, usedOnRelease, onStart, onTick, onRelease, onFinish, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	/**
	 * @return true if this property has any start handlers
	 */
	public boolean hasStartHandlers() {
		return !onStart.isEmpty();
	}

	/**
	 * @return true if this property has any tick handlers
	 */
	public boolean hasTickHandlers() {
		return !onTick.isEmpty();
	}

	/**
	 * @return true if this property has any release handlers
	 */
	public boolean hasReleaseHandlers() {
		return !onRelease.isEmpty();
	}

	/**
	 * @return true if this property has any finish handlers
	 */
	public boolean hasFinishHandlers() {
		return !onFinish.isEmpty();
	}

	/**
	 * @return true if this property defines a valid use action
	 */
	public boolean hasUseAction() {
		return useAction != UseAction.NONE && maxUseTime > 0;
	}

	/**
	 * Engine for resolving UseInteractionProperty from components.
	 */
	public static class Engine extends AbstractConditionalPropertyEngine<UseInteractionProperty, List<UseInteractionProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<UseInteractionProperty> apply(OptimizedBakedResult<UseInteractionProperty> baked) {
			return baked.all().collect(Collectors.toList());
		}
	}
}
