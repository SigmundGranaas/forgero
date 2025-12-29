package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.JSON_ELEMENT_CODEC;
import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.OPEN_IDENTIFIER_CODEC;

/**
 * Unified codec for all resource types (materials, shapes, schematics, casts, static parts).
 *
 * <p>This codec handles parsing for all resource types that share the same structural format.
 * The semantic type is determined by the "type" field value in the JSON.</p>
 *
 * <h3>Supported Types:</h3>
 * <ul>
 *   <li>{@code forgero:material} - Base materials like iron or diamond</li>
 *   <li>{@code forgero:shape} - Tool/weapon shapes like pickaxe_head</li>
 *   <li>{@code forgero:schematic} - Shape variants with crafting bonuses</li>
 *   <li>{@code forgero:cast} - Another shape variant mechanism</li>
 *   <li>{@code forgero:static_part} - Non-generated components</li>
 * </ul>
 */
public class ResourceDataCodec {

	/**
	 * Extracts the deprecated target field for backwards compatibility with old JSON files.
	 * This field is deprecated but must still be parsed to support legacy schematic definitions.
	 */
	@SuppressWarnings("deprecation")
	private static Optional<OpenIdentifier> getTargetForBackwardsCompat(ResourceData data) {
		return Optional.ofNullable(data.target());
	}

	/**
	 * Creates a codec for ResourceData with the provided attribute and upgrade codecs.
	 *
	 * @param attributeCodec   Codec for parsing attribute lists
	 * @param upgradeSlotCodec Codec for parsing upgrade slot lists
	 * @return A codec that can parse all resource types into ResourceData
	 */
	public static Codec<ResourceData> create(
			Codec<List<AttributeData>> attributeCodec,
			Codec<List<UpgradeSlotData>> upgradeSlotCodec
	) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ResourceData::type),
						Codec.STRING.fieldOf("name").forGetter(ResourceData::name),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("include")
								.forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags")
								.forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("local_tags")
								.forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host")
								.forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes")
								.forGetter(data -> Optional.ofNullable(data.attributes())),
						attributeCodec.optionalFieldOf("local_attributes")
								.forGetter(data -> Optional.ofNullable(data.localAttributes())),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties")
								.forGetter(data -> Optional.ofNullable(data.properties())),
						upgradeSlotCodec.optionalFieldOf("upgrades")
								.forGetter(data -> Optional.ofNullable(data.upgrades())),
						OPEN_IDENTIFIER_CODEC.optionalFieldOf("target")
								.forGetter(ResourceDataCodec::getTargetForBackwardsCompat)
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, properties, upgrades, target) ->
						new ResourceData(
								type,
								name,
								include.orElse(null),
								tags.orElse(null),
								localTags.orElse(null),
								host.orElse(null),
								attributes.orElse(null),
								localAttributes.orElse(null),
								properties.orElse(null),
								upgrades.orElse(null),
								target.orElse(null)
						)
				)
		);
	}

	/**
	 * Creates a codec for ResourceData without upgrade slot support.
	 * Use this for types that don't have upgrade slots (materials, shapes, schematics, casts).
	 *
	 * @param attributeCodec Codec for parsing attribute lists
	 * @return A codec that can parse resource types without upgrades
	 */
	public static Codec<ResourceData> create(Codec<List<AttributeData>> attributeCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ResourceData::type),
						Codec.STRING.fieldOf("name").forGetter(ResourceData::name),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("include")
								.forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags")
								.forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("local_tags")
								.forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host")
								.forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes")
								.forGetter(data -> Optional.ofNullable(data.attributes())),
						attributeCodec.optionalFieldOf("local_attributes")
								.forGetter(data -> Optional.ofNullable(data.localAttributes())),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties")
								.forGetter(data -> Optional.ofNullable(data.properties())),
						OPEN_IDENTIFIER_CODEC.optionalFieldOf("target")
								.forGetter(ResourceDataCodec::getTargetForBackwardsCompat)
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, properties, target) ->
						new ResourceData(
								type,
								name,
								include.orElse(null),
								tags.orElse(null),
								localTags.orElse(null),
								host.orElse(null),
								attributes.orElse(null),
								localAttributes.orElse(null),
								properties.orElse(null),
								null, // upgrades
								target.orElse(null)
						)
				)
		);
	}

	/**
	 * Creates a codec for ResourceData with support for attribute batches.
	 *
	 * <p>This codec supports both traditional {@code "attributes"} arrays and the new
	 * {@code "attribute_batches"} compact syntax. If both are present, they are merged.</p>
	 *
	 * @param conditionCodec   Codec for parsing conditions
	 * @param upgradeSlotCodec Codec for parsing upgrade slot lists
	 * @return A codec that handles both traditional and batch attribute formats
	 */
	public static Codec<ResourceData> createWithBatchSupport(
			Codec<Condition> conditionCodec,
			Codec<List<UpgradeSlotData>> upgradeSlotCodec
	) {
		Codec<List<AttributeData>> traditionalCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<AttributeData>> batchCodec = AttributeBatchCodecs.createBatchListCodec(conditionCodec);

		return RecordCodecBuilder.create(instance ->
				instance.group(
						OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ResourceData::type),
						Codec.STRING.fieldOf("name").forGetter(ResourceData::name),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("include")
								.forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags")
								.forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("local_tags")
								.forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host")
								.forGetter(data -> Optional.ofNullable(data.host())),
						// Traditional attributes
						traditionalCodec.optionalFieldOf("attributes")
								.forGetter(data -> Optional.ofNullable(data.attributes())),
						traditionalCodec.optionalFieldOf("local_attributes")
								.forGetter(data -> Optional.ofNullable(data.localAttributes())),
						// Batch attributes
						batchCodec.optionalFieldOf("attribute_batches").forGetter(data -> Optional.empty()),
						batchCodec.optionalFieldOf("local_attribute_batches").forGetter(data -> Optional.empty()),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties")
								.forGetter(data -> Optional.ofNullable(data.properties())),
						upgradeSlotCodec.optionalFieldOf("upgrades")
								.forGetter(data -> Optional.ofNullable(data.upgrades())),
						OPEN_IDENTIFIER_CODEC.optionalFieldOf("target")
								.forGetter(ResourceDataCodec::getTargetForBackwardsCompat)
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, attributeBatches, localAttributeBatches, properties, upgrades, target) -> {
					// Merge traditional and batch attributes
					List<AttributeData> mergedAttributes = mergeAttributes(attributes, attributeBatches);
					List<AttributeData> mergedLocalAttributes = mergeAttributes(localAttributes, localAttributeBatches);

					return new ResourceData(
							type,
							name,
							include.orElse(null),
							tags.orElse(null),
							localTags.orElse(null),
							host.orElse(null),
							mergedAttributes.isEmpty() ? null : mergedAttributes,
							mergedLocalAttributes.isEmpty() ? null : mergedLocalAttributes,
							properties.orElse(null),
							upgrades.orElse(null),
							target.orElse(null)
					);
				})
		);
	}

	private static List<AttributeData> mergeAttributes(Optional<List<AttributeData>> traditional, Optional<List<AttributeData>> batches) {
		List<AttributeData> result = new ArrayList<>();
		traditional.ifPresent(result::addAll);
		batches.ifPresent(result::addAll);
		return result;
	}
}
