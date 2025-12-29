package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.StatusModifierData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Codec for serializing and deserializing {@link StatusModifierData} to/from JSON.
 *
 * <h3>JSON Format:</h3>
 * <pre>{@code
 * {
 *   "id": "forgero:sharp",
 *   "display_name": "Sharp",
 *   "priority": 5,
 *   "target": {
 *     "types": ["forgero:tool", "forgero:weapon"],
 *     "ids": [],
 *     "incompatibilities": ["forgero:blunt"]
 *   },
 *   "chance": 0.04,
 *   "attributes": [...],
 *   "properties": {...}
 * }
 * }</pre>
 */
public final class StatusModifierDataCodec {

	private StatusModifierDataCodec() {
		// Utility class
	}

	/**
	 * Codec for the nested TargetData record.
	 */
	public static final Codec<StatusModifierData.TargetData> TARGET_DATA_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC)
							.optionalFieldOf("types")
							.forGetter(data -> data.types() == null ? Optional.empty() : Optional.of(List.copyOf(data.types()))),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC)
							.optionalFieldOf("ids")
							.forGetter(data -> data.ids() == null ? Optional.empty() : Optional.of(List.copyOf(data.ids()))),
					Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC)
							.optionalFieldOf("incompatibilities")
							.forGetter(data -> data.incompatibilities() == null ? Optional.empty() : Optional.of(List.copyOf(data.incompatibilities())))
			).apply(instance, (types, ids, incompatibilities) ->
					new StatusModifierData.TargetData(
							types.map(HashSet::new).map(Set::copyOf).orElse(null),
							ids.map(HashSet::new).map(Set::copyOf).orElse(null),
							incompatibilities.map(HashSet::new).map(Set::copyOf).orElse(null)
					)
			)
	);

	/**
	 * Creates a codec for StatusModifierData.
	 *
	 * @param attributeListCodec Codec for parsing attribute lists (may include conditions)
	 * @return A codec for StatusModifierData
	 */
	public static Codec<StatusModifierData> create(Codec<List<AttributeData>> attributeListCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("id")
								.forGetter(StatusModifierData::id),
						Codec.STRING.optionalFieldOf("display_name", "")
								.forGetter(data -> data.displayName() != null ? data.displayName() : ""),
						Codec.INT.optionalFieldOf("priority", 0)
								.forGetter(StatusModifierData::priority),
						TARGET_DATA_CODEC.optionalFieldOf("target")
								.forGetter(data -> Optional.ofNullable(data.target())),
						Codec.FLOAT.optionalFieldOf("chance", 0f)
								.forGetter(StatusModifierData::chance),
						attributeListCodec.optionalFieldOf("attributes")
								.forGetter(data -> Optional.ofNullable(data.attributes())),
						Codec.unboundedMap(Codec.STRING, CodecConstants.JSON_ELEMENT_CODEC)
								.optionalFieldOf("properties")
								.forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (id, displayName, priority, target, chance, attributes, properties) ->
						new StatusModifierData(
								id,
								displayName.isEmpty() ? id.name() : displayName,
								priority,
								target.orElse(null),
								chance,
								attributes.orElse(null),
								properties.orElse(null)
						)
				)
		);
	}

	/**
	 * Creates a simple codec without attribute condition support.
	 * Useful for testing or simple scenarios.
	 */
	public static Codec<StatusModifierData> createSimple() {
		// Create a condition codec that ignores conditions (empty codec maps)
		var conditionCodec = new com.sigmundgranaas.forgero.core.condition.api.ConditionCodec(
				java.util.Map.of(),
				java.util.Map.of()
		);
		return create(Codec.list(AttributeCodecs.create(conditionCodec)));
	}
}
