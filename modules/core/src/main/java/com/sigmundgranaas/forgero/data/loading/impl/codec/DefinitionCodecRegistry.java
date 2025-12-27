package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for definition codecs, providing a centralized way to look up
 * the appropriate codec for a given type identifier.
 *
 * <p>This registry supports extensibility - custom codecs can be registered
 * for new types without modifying the registry class itself.</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * var registry = DefinitionCodecRegistry.createDefault(attributeCodec, upgradeCodec);
 * Codec<?> codec = registry.codecFor("material");
 * }</pre>
 *
 * <h3>Extensibility:</h3>
 * <pre>{@code
 * registry.register("custom_type", myCustomCodec);
 * }</pre>
 */
public class DefinitionCodecRegistry {
	private final Map<String, Codec<? extends DefinitionData>> codecs;
	private final Codec<ResourceData> defaultCodec;

	/**
	 * Creates a new registry with the specified default codec.
	 *
	 * @param defaultCodec The codec to use for unknown types
	 */
	public DefinitionCodecRegistry(Codec<ResourceData> defaultCodec) {
		this.codecs = new HashMap<>();
		this.defaultCodec = defaultCodec;
	}

	/**
	 * Registers a codec for a specific type identifier.
	 *
	 * @param typeId The type identifier (e.g., "material", "shape")
	 * @param codec  The codec to use for this type
	 */
	public void register(String typeId, Codec<? extends DefinitionData> codec) {
		codecs.put(typeId, codec);
	}

	/**
	 * Retrieves the codec for a given type identifier.
	 *
	 * @param typeId The type identifier to look up
	 * @return The registered codec, or the default codec if not found
	 */
	public Codec<? extends DefinitionData> codecFor(String typeId) {
		return codecs.getOrDefault(typeId, defaultCodec);
	}

	/**
	 * Checks if a codec is registered for the given type identifier.
	 *
	 * @param typeId The type identifier to check
	 * @return true if a codec is registered, false otherwise
	 */
	public boolean hasCodecFor(String typeId) {
		return codecs.containsKey(typeId);
	}

	/**
	 * Creates a registry with default codecs for all standard types.
	 *
	 * <p>Registered types:</p>
	 * <ul>
	 *   <li>{@code material} - Uses ResourceDataCodec</li>
	 *   <li>{@code shape} - Uses ResourceDataCodec</li>
	 *   <li>{@code schematic} - Uses ResourceDataCodec</li>
	 *   <li>{@code cast} - Uses ResourceDataCodec</li>
	 *   <li>{@code static_part} - Uses ResourceDataCodec (with upgrades)</li>
	 *   <li>{@code part_template} - Uses PartTemplateCodecs</li>
	 *   <li>{@code equipment_template} - Uses EquipmentTemplateCodecs</li>
	 *   <li>{@code tool_template} - Legacy alias for equipment_template</li>
	 *   <li>{@code extension} - Uses ExtensionCodecs</li>
	 * </ul>
	 *
	 * @param attributeCodec   Codec for parsing attribute lists
	 * @param upgradeSlotCodec Codec for parsing upgrade slot lists
	 * @return A fully configured registry
	 */
	public static DefinitionCodecRegistry createDefault(
			Codec<List<AttributeData>> attributeCodec,
			Codec<List<UpgradeSlotData>> upgradeSlotCodec
	) {
		Codec<ResourceData> resourceCodec = ResourceDataCodec.create(attributeCodec, upgradeSlotCodec);
		var registry = new DefinitionCodecRegistry(resourceCodec);

		// All resource types use the unified ResourceDataCodec
		registry.register("material", resourceCodec);
		registry.register("shape", resourceCodec);
		registry.register("schematic", resourceCodec);
		registry.register("cast", resourceCodec);
		registry.register("static_part", resourceCodec);

		// Templates have unique structure - use their specific codecs
		registry.register("part_template", PartTemplateCodecs.create(attributeCodec, upgradeSlotCodec));
		registry.register("equipment_template", EquipmentTemplateCodecs.create(attributeCodec, upgradeSlotCodec));
		registry.register("tool_template", EquipmentTemplateCodecs.create(attributeCodec, upgradeSlotCodec)); // Legacy

		// Extension type
		registry.register("extension", ExtensionCodecs.create(attributeCodec));

		return registry;
	}
}
