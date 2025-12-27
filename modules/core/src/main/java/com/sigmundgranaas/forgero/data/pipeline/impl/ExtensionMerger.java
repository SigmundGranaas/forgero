package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Merges extension resources into their target definitions.
 *
 * <p>Extension resources allow contributing properties to existing definitions
 * without modifying the original files. This is useful for:</p>
 * <ul>
 *   <li>Splitting material definitions across multiple packs (tools, armor, upgrades)</li>
 *   <li>Mod compatibility patches without overwriting base files</li>
 *   <li>Conditional property additions based on loaded mods</li>
 * </ul>
 *
 * <h3>Merge Order</h3>
 * <p>Extensions are applied in ascending priority order (lower values first).
 * Extensions with the same priority are applied in undefined order.</p>
 *
 * <h3>Merge Semantics</h3>
 * <ul>
 *   <li><strong>Tags:</strong> Union (all tags combined)</li>
 *   <li><strong>Attributes:</strong> Concatenate (extension attributes appended)</li>
 *   <li><strong>Properties:</strong> Deep merge (objects merged recursively, arrays concatenated)</li>
 * </ul>
 */
public class ExtensionMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMerger.class);

	/**
	 * Processes raw definitions, merging extensions into their targets.
	 *
	 * @param rawDefinitions The original map of raw definitions (may include extensions)
	 * @return A new map with extensions merged and extension entries removed
	 */
	public Map<OpenIdentifier, RawDefinition> merge(Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		// Separate extensions from other definitions
		List<ExtensionData> extensions = rawDefinitions.values().stream()
				.map(RawDefinition::data)
				.filter(ExtensionData.class::isInstance)
				.map(ExtensionData.class::cast)
				.sorted(Comparator.comparingInt(ExtensionData::priority))
				.toList();

		if (extensions.isEmpty()) {
			return rawDefinitions;
		}

		LOGGER.info("Processing {} extension resources", extensions.size());

		// Group extensions by target
		Map<OpenIdentifier, List<ExtensionData>> extensionsByTarget = extensions.stream()
				.collect(Collectors.groupingBy(ExtensionData::target));

		// Create result map excluding extension entries
		Map<OpenIdentifier, RawDefinition> result = new HashMap<>();
		for (Map.Entry<OpenIdentifier, RawDefinition> entry : rawDefinitions.entrySet()) {
			if (!(entry.getValue().data() instanceof ExtensionData)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}

		// Apply extensions to their targets
		for (Map.Entry<OpenIdentifier, List<ExtensionData>> entry : extensionsByTarget.entrySet()) {
			OpenIdentifier targetId = entry.getKey();
			List<ExtensionData> targetExtensions = entry.getValue();

			RawDefinition targetDef = result.get(targetId);
			if (targetDef == null) {
				LOGGER.warn("Extension target '{}' not found. {} extension(s) will be ignored.",
						targetId, targetExtensions.size());
				continue;
			}

			DefinitionData mergedData = targetDef.data();
			for (ExtensionData extension : targetExtensions) {
				mergedData = mergeInto(mergedData, extension);
			}

			result.put(targetId, new RawDefinition(targetId, mergedData));
			LOGGER.debug("Applied {} extension(s) to '{}'", targetExtensions.size(), targetId);
		}

		return result;
	}

	/**
	 * Merges an extension's data into a target definition using polymorphic dispatch.
	 *
	 * <p>Uses the {@link DefinitionData#withMergedExtension} method to create a new
	 * definition with merged tags, attributes, and properties. Types that don't support
	 * extension merging (like templates) return themselves unchanged.</p>
	 *
	 * @param target    The target definition data
	 * @param extension The extension to merge
	 * @return A new definition with the extension merged
	 */
	private DefinitionData mergeInto(DefinitionData target, ExtensionData extension) {
		List<OpenIdentifier> mergedTags = mergeTags(target.tags(), extension.tags());
		List<AttributeData> mergedAttributes = mergeAttributes(target.attributes(), extension.attributes());
		Map<String, JsonElement> mergedProperties = mergeProperties(target.properties(), extension.properties());

		return target.withMergedExtension(mergedTags, mergedAttributes, mergedProperties);
	}

	/**
	 * Merges two tag lists using union semantics.
	 */
	private List<OpenIdentifier> mergeTags(List<OpenIdentifier> target, List<OpenIdentifier> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		Set<OpenIdentifier> merged = new LinkedHashSet<>(target);
		merged.addAll(extension);
		return new ArrayList<>(merged);
	}

	/**
	 * Merges two attribute lists by concatenation.
	 */
	private List<AttributeData> mergeAttributes(List<AttributeData> target, List<AttributeData> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		List<AttributeData> merged = new ArrayList<>(target);
		merged.addAll(extension);
		return merged;
	}

	/**
	 * Deep merges two property maps.
	 * <ul>
	 *   <li>Arrays: concatenated</li>
	 *   <li>Objects: merged recursively</li>
	 *   <li>Primitives: extension value wins</li>
	 * </ul>
	 */
	private Map<String, JsonElement> mergeProperties(Map<String, JsonElement> target, Map<String, JsonElement> extension) {
		if (extension == null || extension.isEmpty()) {
			return target;
		}
		if (target == null || target.isEmpty()) {
			return extension;
		}

		Map<String, JsonElement> merged = new HashMap<>(target);
		for (Map.Entry<String, JsonElement> entry : extension.entrySet()) {
			String key = entry.getKey();
			JsonElement extValue = entry.getValue();
			JsonElement targetValue = merged.get(key);

			if (targetValue == null) {
				merged.put(key, extValue);
			} else {
				merged.put(key, deepMergeJson(targetValue, extValue));
			}
		}
		return merged;
	}

	/**
	 * Deep merges two JSON elements.
	 */
	private JsonElement deepMergeJson(JsonElement target, JsonElement extension) {
		if (target.isJsonArray() && extension.isJsonArray()) {
			JsonArray merged = new JsonArray();
			target.getAsJsonArray().forEach(merged::add);
			extension.getAsJsonArray().forEach(merged::add);
			return merged;
		}

		if (target.isJsonObject() && extension.isJsonObject()) {
			JsonObject merged = new JsonObject();
			JsonObject targetObj = target.getAsJsonObject();
			JsonObject extObj = extension.getAsJsonObject();

			// Add all target properties
			for (Map.Entry<String, JsonElement> entry : targetObj.entrySet()) {
				merged.add(entry.getKey(), entry.getValue());
			}

			// Merge extension properties
			for (Map.Entry<String, JsonElement> entry : extObj.entrySet()) {
				String key = entry.getKey();
				if (merged.has(key)) {
					merged.add(key, deepMergeJson(merged.get(key), entry.getValue()));
				} else {
					merged.add(key, entry.getValue());
				}
			}
			return merged;
		}

		// For primitives or mismatched types, extension wins
		return extension;
	}
}
