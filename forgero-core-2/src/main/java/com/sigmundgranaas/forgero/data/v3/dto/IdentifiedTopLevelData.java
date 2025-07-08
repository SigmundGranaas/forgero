// FILE: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/data/v3/dto/IdentifiedTopLevelData.java
package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.api.Taggable; // Import Taggable
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set; // Import Set


/**
 * A wrapper record that associates a unique {@link OpenIdentifier} (derived from the file path)
 * with a raw {@link Object} DTO parsed from JSON. This is the concrete implementation of {@link TopLevelData}.
 *
 * This design allows the core DTOs (e.g., MaterialData) to remain free of external concerns like their own ID,
 * while providing a unified interface {@link TopLevelData} for pipeline stages that need an ID.
 *
 * @param id The canonical unique identifier for this data entry, injected by the loading pipeline.
 * @param data The raw DTO object parsed directly from a JSON file (e.g., MaterialData, PartTemplateData).
 */
public record IdentifiedTopLevelData(
		OpenIdentifier id,
		Object data
) implements TopLevelData, Taggable { // Implement Taggable

	/**
	 * Unwraps the contained DTO and casts it to the specified type.
	 * This is useful when the calling code knows the concrete type it expects.
	 *
	 * @param type The class of the expected DTO type.
	 * @param <T> The type of the expected DTO.
	 * @return The wrapped DTO, cast to the specified type.
	 * @throws ClassCastException if the wrapped data is not an instance of the specified type.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrapAs(@NotNull Class<T> type) {
		if (!type.isInstance(data)) {
			throw new ClassCastException("Wrapped data is not an instance of " + type.getName() + ": " + data.getClass().getName());
		}
		return (T) data;
	}

	// Implementing TopLevelData's methods by delegating to the wrapped 'data' object.
	// This requires runtime casting and careful handling of nulls.

	@Override
	public OpenIdentifier type() {
		if (data instanceof MaterialData m) return m.type();
		if (data instanceof PartTemplateData p) return p.type();
		if (data instanceof EquipmentTemplateData t) return t.type();
		if (data instanceof SchematicData s) return s.type();
		if (data instanceof StaticPartData sp) return sp.type();
		if (data instanceof GeneratedEquipmentData ge) return ge.type();
		if (data instanceof GeneratedPartData gp) return gp.type();
		// Fallback for unexpected or generic data objects, or throw an error.
		throw new IllegalStateException("Unsupported data type wrapped in IdentifiedTopLevelData for type(): " + data.getClass().getName());
	}

	@Override
	public String name() {
		if (data instanceof MaterialData m) return m.name();
		if (data instanceof PartTemplateData p) return p.name();
		if (data instanceof EquipmentTemplateData t) return t.name();
		if (data instanceof SchematicData s) return s.name();
		if (data instanceof StaticPartData sp) return sp.name();
		if (data instanceof GeneratedEquipmentData ge) return ge.name();
		if (data instanceof GeneratedPartData gp) return gp.name();
		throw new IllegalStateException("Unsupported data type wrapped in IdentifiedTopLevelData for name(): " + data.getClass().getName());
	}

	@Override
	public @Nullable List<OpenIdentifier> include() {
		if (data instanceof MaterialData m) return m.include();
		if (data instanceof PartTemplateData p) return p.include();
		if (data instanceof EquipmentTemplateData t) return t.include();
		if (data instanceof SchematicData s) return s.include();
		if (data instanceof StaticPartData sp) return sp.include();
		return null;
	}

	@Override
	public @Nullable Set<OpenIdentifier> tags() {
		// After DataProcessor, this 'tags()' method on the IdentifiedTopLevelData will contain
		// all merged tags from includes and the current DTO.
		if (data instanceof MaterialData m && m.tags() != null) return new HashSet<>(m.tags());
		if (data instanceof PartTemplateData p && p.tags() != null) return new HashSet<>(p.tags());
		if (data instanceof EquipmentTemplateData t && t.tags() != null) return new HashSet<>(t.tags());
		if (data instanceof SchematicData s && s.tags() != null) return new HashSet<>(s.tags());
		if (data instanceof StaticPartData sp && sp.tags() != null) return new HashSet<>(sp.tags());
		if (data instanceof GeneratedPartData gp && gp.tags() != null) return new HashSet<>(gp.tags());
		if (data instanceof GeneratedEquipmentData ge && ge.tags() != null) return new HashSet<>(ge.tags());

		return null;
	}


	// Implementation for Taggable interface
	@Override
	public Set<OpenIdentifier> getTags() {
		// Convert the internal List<OpenIdentifier> to a Set<OpenIdentifier> as required by Taggable.
		Set<OpenIdentifier> currentTags = tags();
		return currentTags != null ? new HashSet<>(currentTags) : Collections.emptySet();
	}

	@Override
	public @Nullable List<AttributeData> attributes() {
		if (data instanceof MaterialData m) return m.attributes();
		if (data instanceof PartTemplateData p) return p.attributes();
		if (data instanceof EquipmentTemplateData t) return t.attributes();
		if (data instanceof StaticPartData sp) return sp.attributes();
		if (data instanceof GeneratedPartData gp) return gp.attributes();
		if (data instanceof GeneratedEquipmentData ge) return ge.attributes();
		return null;
	}

	@Override
	public @Nullable List<FeatureData> features() {
		if (data instanceof MaterialData m) return m.features();
		if (data instanceof PartTemplateData p) return p.features();
		if (data instanceof EquipmentTemplateData t) return t.features();
		if (data instanceof StaticPartData sp) return sp.features();
		if (data instanceof GeneratedPartData gp) return gp.features();
		if (data instanceof GeneratedEquipmentData ge) return ge.features();
		return null;
	}

	@Override
	public Map<OpenIdentifier, AttributeData> getAttributesMap() {
		if (data instanceof MaterialData m) return m.getAttributesMap();
		if (data instanceof PartTemplateData p) return p.getAttributesMap();
		if (data instanceof EquipmentTemplateData t) return t.getAttributesMap();
		if (data instanceof StaticPartData sp) return sp.getAttributesMap();
		if (data instanceof GeneratedPartData gp) return gp.getAttributesMap();
		if (data instanceof GeneratedEquipmentData ge) return ge.getAttributesMap();
		return Collections.emptyMap();
	}

	@Override
	public Map<OpenIdentifier, FeatureData> getFeaturesMap() {
		if (data instanceof MaterialData m) return m.getFeaturesMap();
		if (data instanceof PartTemplateData p) return p.getFeaturesMap();
		if (data instanceof EquipmentTemplateData t) return t.getFeaturesMap();
		if (data instanceof StaticPartData sp) return sp.getFeaturesMap();
		if (data instanceof GeneratedPartData gp) return gp.getFeaturesMap();
		if (data instanceof GeneratedEquipmentData ge) return ge.getFeaturesMap();
		return Collections.emptyMap();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IdentifiedTopLevelData that = (IdentifiedTopLevelData) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
