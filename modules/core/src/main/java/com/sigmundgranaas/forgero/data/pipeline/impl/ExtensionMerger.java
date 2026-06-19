package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.api.data.ExtensionData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.template.UpgradeSlotData;
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
 *   <li><strong>Upgrades:</strong> Concatenate with override (duplicate IDs: extension wins with warning)</li>
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

		LOGGER.debug("Processing {} extension resources", extensions.size());

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
	 * definition with merged tags, attributes, properties, and upgrade slots.</p>
	 *
	 * @param target    The target definition data
	 * @param extension The extension to merge
	 * @return A new definition with the extension merged
	 */
	private DefinitionData mergeInto(DefinitionData target, ExtensionData extension) {
		List<OpenIdentifier> mergedTags = MergeOps.union(target.tags(), extension.tags());
		List<AttributeData> mergedAttributes = MergeOps.mergeAttributesById(target.attributes(), extension.attributes());
		Map<String, JsonElement> mergedProperties = MergeOps.mergeProperties(target.properties(), extension.properties());
		List<UpgradeSlotData> mergedUpgrades = MergeOps.mergeUpgradesById(target.upgrades(), extension.upgrades());

		return target.withMergedExtension(mergedTags, mergedAttributes, mergedProperties, mergedUpgrades);
	}
}
