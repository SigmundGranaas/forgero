package com.sigmundgranaas.forgero.properties.minecraft.onblockplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.effects.block.BlockEffect;
import com.sigmundgranaas.forgero.effects.block.BlockSelector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * The Orchestrator property for On-Block-Place events. This triggers when a player
 * places a block while holding an item with this property, enabling builder-assist
 * features, auto-placement, and block-based abilities.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "selector": {
 *     "type": "forgero:radius",
 *     "radius": 2,
 *     "include_center": false
 *   },
 *   "effects": [
 *     {
 *       "type": "forgero:entity_effect_at_block",
 *       "radius": 3,
 *       "effect": {
 *         "type": "forgero:particle",
 *         "particle": "minecraft:happy_villager",
 *         "count": 10
 *       }
 *     },
 *     {
 *       "type": "forgero:sound_at_block",
 *       "sound": "minecraft:block.note_block.chime",
 *       "volume": 1.0,
 *       "pitch": 1.5
 *     }
 *   ],
 *   "condition": {
 *     "type": "forgero:has_tag",
 *     "tag": "minecraft:builder_tool"
 *   }
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Auto-torch placement - place torch, auto-place more in radius</li>
 *   <li>Pattern building - place one block, fill a pattern</li>
 *   <li>Builder feedback - particles/sounds when placing</li>
 *   <li>AOE fertilize - place crop, fertilize nearby crops</li>
 *   <li>Illumination assist - place block, light up nearby area</li>
 * </ul>
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Only triggers when player places blocks (not dispensers)</li>
 *   <li>BlockSelector determines which positions get effects</li>
 *   <li>BlockEffects can target entities, modify blocks, or create visuals</li>
 *   <li>Item must be held (not in inventory) when placing</li>
 * </ul>
 *
 * @param selector The block selector for determining which positions are affected
 * @param effects The list of block effects to apply at selected positions
 * @param condition Optional condition that must be met for effects to apply
 */
public record OnBlockPlaceProperty(
		BlockSelector selector,
		List<BlockEffect> effects,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_block_place");
	public static final ResolutionKey<List<OnBlockPlaceProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnBlockPlaceProperty> PROPERTY_KEY = new PropertyKey<>(OnBlockPlaceProperty.class, KEY_ID.toString());

	public static Codec<OnBlockPlaceProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BlockSelector.CODEC.fieldOf("selector").forGetter(OnBlockPlaceProperty::selector),
				Codec.list(BlockEffect.CODEC).fieldOf("effects").forGetter(OnBlockPlaceProperty::effects),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (selector, effects, condition) -> new OnBlockPlaceProperty(selector, effects, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnBlockPlaceProperty> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}
	}
}
