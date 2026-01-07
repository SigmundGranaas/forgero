package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.JSON_ELEMENT_CODEC;

public class MaterialCodecs {

	public static Codec<MaterialData> create(Codec<List<AttributeData>> attributeCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(MaterialData::type),
						Codec.STRING.fieldOf("name").forGetter(MaterialData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("local_tags").forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						attributeCodec.optionalFieldOf("local_attributes").forGetter(data -> Optional.ofNullable(data.localAttributes())),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, properties) ->
						new MaterialData(type, name, include.orElse(null), tags.orElse(null), localTags.orElse(null), host.orElse(null), attributes.orElse(null), localAttributes.orElse(null), properties.orElse(null))));
	}

	/**
	 * Creates a codec for MaterialData with support for attribute batches.
	 *
	 * <p>This codec supports both traditional {@code "attributes"} arrays and the new
	 * {@code "attribute_batches"} compact syntax. If both are present, they are merged.</p>
	 *
	 * @param conditionCodec The condition codec for parsing conditions
	 * @return A MaterialData codec with batch support
	 */
	public static Codec<MaterialData> createWithBatchSupport(Codec<Condition> conditionCodec) {
		Codec<List<AttributeData>> traditionalCodec = Codec.list(AttributeCodecs.create(conditionCodec));
		Codec<List<AttributeData>> batchCodec = AttributeBatchCodecs.createBatchListCodec(conditionCodec);

		return RecordCodecBuilder.create(instance ->
				instance.group(
						CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(MaterialData::type),
						Codec.STRING.fieldOf("name").forGetter(MaterialData::name),
						Codec.list(CodecConstants.OPEN_IDENTIFIER_CODEC).optionalFieldOf("include").forGetter(data -> Optional.ofNullable(data.include())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						Codec.list(CodecConstants.TAG_IDENTIFIER_CODEC).optionalFieldOf("local_tags").forGetter(data -> Optional.ofNullable(data.localTags())),
						HostCodecs.HOST_DATA_CODEC.optionalFieldOf("host").forGetter(data -> Optional.ofNullable(data.host())),
						traditionalCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						traditionalCodec.optionalFieldOf("local_attributes").forGetter(data -> Optional.ofNullable(data.localAttributes())),
						// Batch attributes
						batchCodec.optionalFieldOf("attribute_batches").forGetter(data -> Optional.empty()),
						batchCodec.optionalFieldOf("local_attribute_batches").forGetter(data -> Optional.empty()),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, name, include, tags, localTags, host, attributes, localAttributes, attributeBatches, localAttributeBatches, properties) -> {
					// Merge traditional and batch attributes
					List<AttributeData> mergedAttributes = mergeAttributes(attributes, attributeBatches);
					List<AttributeData> mergedLocalAttributes = mergeAttributes(localAttributes, localAttributeBatches);

					return new MaterialData(
							type, name,
							include.orElse(null),
							tags.orElse(null),
							localTags.orElse(null),
							host.orElse(null),
							mergedAttributes.isEmpty() ? null : mergedAttributes,
							mergedLocalAttributes.isEmpty() ? null : mergedLocalAttributes,
							properties.orElse(null)
					);
				}));
	}

	private static List<AttributeData> mergeAttributes(Optional<List<AttributeData>> traditional, Optional<List<AttributeData>> batches) {
		List<AttributeData> result = new ArrayList<>();
		traditional.ifPresent(result::addAll);
		batches.ifPresent(result::addAll);
		return result;
	}
}
