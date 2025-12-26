package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.JSON_ELEMENT_CODEC;
import static com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants.OPEN_IDENTIFIER_CODEC;

/**
 * Codec factory for {@link ExtensionData}.
 *
 * <p>Extension resources extend existing definitions with additional properties.
 * The codec handles:</p>
 * <ul>
 *   <li>{@code type} - Always "forgero:extension"</li>
 *   <li>{@code target} - The definition to extend</li>
 *   <li>{@code priority} - Merge order (default: 0, lower values applied first)</li>
 *   <li>{@code tags} - Optional tags to add</li>
 *   <li>{@code attributes} - Optional attributes to add</li>
 *   <li>{@code properties} - Optional properties to merge</li>
 * </ul>
 */
public class ExtensionCodecs {

	/**
	 * Creates a codec for ExtensionData.
	 *
	 * @param attributeCodec The codec for attribute lists
	 * @return A codec for ExtensionData
	 */
	public static Codec<ExtensionData> create(Codec<List<AttributeData>> attributeCodec) {
		return RecordCodecBuilder.create(instance ->
				instance.group(
						OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(ExtensionData::type),
						OPEN_IDENTIFIER_CODEC.fieldOf("target").forGetter(ExtensionData::target),
						Codec.INT.optionalFieldOf("priority", ExtensionData.DEFAULT_PRIORITY).forGetter(ExtensionData::priority),
						Codec.list(OPEN_IDENTIFIER_CODEC).optionalFieldOf("tags").forGetter(data -> Optional.ofNullable(data.tags())),
						attributeCodec.optionalFieldOf("attributes").forGetter(data -> Optional.ofNullable(data.attributes())),
						Codec.unboundedMap(Codec.STRING, JSON_ELEMENT_CODEC).optionalFieldOf("properties").forGetter(data -> Optional.ofNullable(data.properties()))
				).apply(instance, (type, target, priority, tags, attributes, properties) ->
						new ExtensionData(type, target, priority, tags.orElse(null), attributes.orElse(null), properties.orElse(null))));
	}
}
