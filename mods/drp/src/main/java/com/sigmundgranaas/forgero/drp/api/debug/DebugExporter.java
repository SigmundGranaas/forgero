package com.sigmundgranaas.forgero.drp.api.debug;

import java.nio.file.Path;
import java.util.Set;

/**
 * API for exporting generated resources to disk for debugging.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * DRPApi.getInstance()
 *     .exportToPath(Path.of("./debug-output"))
 *     .includeTypes(Set.of("tags", "models", "recipes"))
 *     .prettyPrint(true)
 *     .export();
 * }</pre>
 */
public interface DebugExporter {

	/**
	 * Sets which resource types to export.
	 *
	 * @param types Set of type names ("tags", "recipes", "models", "lang", "textures")
	 * @return This exporter for chaining
	 */
	DebugExporter includeTypes(Set<String> types);

	/**
	 * Exports all resource types.
	 *
	 * @return This exporter for chaining
	 */
	DebugExporter includeAll();

	/**
	 * Enables pretty-printing of JSON output.
	 *
	 * @param prettyPrint Whether to format JSON
	 * @return This exporter for chaining
	 */
	DebugExporter prettyPrint(boolean prettyPrint);

	/**
	 * Only exports resources matching the given namespace.
	 *
	 * @param namespace The namespace to filter by
	 * @return This exporter for chaining
	 */
	DebugExporter filterNamespace(String namespace);

	/**
	 * Performs the export.
	 *
	 * @return The number of resources exported
	 */
	int export();

	/**
	 * Performs the export and returns a summary.
	 *
	 * @return Export summary with details
	 */
	ExportSummary exportWithSummary();

	/**
	 * Summary of an export operation.
	 */
	interface ExportSummary {
		int totalExported();

		int tagsExported();

		int recipesExported();

		int modelsExported();

		int langExported();

		int texturesExported();

		Path outputPath();
	}
}
