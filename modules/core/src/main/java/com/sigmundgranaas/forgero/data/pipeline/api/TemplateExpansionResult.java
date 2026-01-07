package com.sigmundgranaas.forgero.data.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;

/**
 * Result of template expansion phase, exposing per-template statistics.
 * This allows validation tools to detect templates that produce zero results,
 * which indicates a configuration error.
 */
public record TemplateExpansionResult(
		/**
		 * Map from template ID to the number of components it generated.
		 */
		Map<OpenIdentifier, Integer> templateResultCounts,

		/**
		 * Templates that generated 0 components (ERROR condition).
		 * This is a critical validation failure - a template that produces
		 * no results is effectively dead code and likely indicates:
		 * - Missing materials matching required tags
		 * - Typo in tag references
		 * - Missing content pack dependency
		 */
		List<OpenIdentifier> emptyTemplates,

		/**
		 * Total number of components generated across all templates.
		 */
		int totalGenerated,

		/**
		 * Warnings encountered during generation.
		 */
		List<TemplateWarning> warnings
) {
	/**
	 * @return true if any template produced zero results
	 */
	public boolean hasEmptyTemplates() {
		return !emptyTemplates.isEmpty();
	}

	/**
	 * Warning about a template slot during generation.
	 */
	public record TemplateWarning(
			OpenIdentifier template,
			String slotName,
			String message
	) {
	}

	/**
	 * Creates an empty result for cases where no templates were processed.
	 */
	public static TemplateExpansionResult empty() {
		return new TemplateExpansionResult(Map.of(), List.of(), 0, List.of());
	}
}
