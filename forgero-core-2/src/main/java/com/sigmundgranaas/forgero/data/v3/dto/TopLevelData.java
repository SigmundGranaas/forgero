package com.sigmundgranaas.forgero.data.v3.dto;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A common interface for all top-group Forgero data DTOs (materials, part templates, tools, schematics, static parts).
 * This interface defines the common fields that are relevant for dependency resolution and property merging.
 * It also provides utility methods for accessing attributes and features as maps for easier merging.
 */
public interface TopLevelData {
	/**
	 * The canonical unique identifier for this data entry. This ID is derived from the file path
	 * during resource loading (e.g., `forgero:iron` for `data/forgero/materials/iron.json`).
	 * This is used for `include` resolution and other internal lookups.
	 *
	 * @return The unique OpenIdentifier for this data entry.
	 */
	OpenIdentifier id();

	@SuppressWarnings("unchecked")
		// Safe due to instanceof check
	<T> T unwrapAs(@NotNull Class<T> type);

	/**
	 * The type of this data entry as defined in the JSON (e.g., `forgero:material`, `forgero:part_template`).
	 *
	 * @return The OpenIdentifier representing the type of this data.
	 */
	OpenIdentifier type();

	/**
	 * A display-friendly name for this data entry, typically specified in the JSON file.
	 *
	 * @return The display name.
	 */
	String name();

	/**
	 * An optional list of other top-group data entries to include. Properties from included entries
	 * will be merged into this entry, with this entry's properties overriding conflicts.
	 *
	 * @return A list of OpenIdentifiers of included data entries, or null if none.
	 */
	@Nullable
	List<OpenIdentifier> include();

	/**
	 * An optional list of tags directly associated with this data entry.
	 *
	 * @return A list of OpenIdentifiers representing tags, or null if none.
	 */
	@Nullable
	Set<OpenIdentifier> tags();

	/**
	 * An optional list of attributes provided by this data entry.
	 *
	 * @return A list of AttributeData, or null if none.
	 */
	@Nullable
	List<AttributeData> attributes();

	/**
	 * An optional list of features provided by this data entry.
	 *
	 * @return A list of FeatureData, or null if none.
	 */
	@Nullable
	List<FeatureData> features();

	/**
	 * Converts the list of attributes into a map, keyed by their unique `id`.
	 * In case of `id` conflicts, the last attribute in the list (or the one from this object) wins.
	 *
	 * @return An unmodifiable map of attribute IDs to AttributeData.
	 */
	default Map<OpenIdentifier, AttributeData> getAttributesMap() {
		if (attributes() == null) return Collections.emptyMap();
		return Objects.requireNonNull(attributes()).stream().collect(Collectors.toMap(AttributeData::id, Function.identity(), (a1, a2) -> a2));
	}

	/**
	 * Converts the list of features into a map, keyed by their `type`.
	 * In case of `type` conflicts, the last feature in the list (or the one from this object) wins.
	 * Note: Features are typically uniquely identified by their `type` for merging purposes.
	 *
	 * @return An unmodifiable map of feature types to FeatureData.
	 */
	default Map<OpenIdentifier, FeatureData> getFeaturesMap() {
		if (features() == null) return Collections.emptyMap();
		return Objects.requireNonNull(features()).stream().collect(Collectors.toMap(FeatureData::type, Function.identity(), (f1, f2) -> f2));
	}
}
