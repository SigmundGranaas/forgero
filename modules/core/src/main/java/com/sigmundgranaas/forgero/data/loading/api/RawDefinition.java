package com.sigmundgranaas.forgero.data.loading.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;

/**
 * Stage 1 Output: A raw, unprocessed definition from a single file,
 * associating the canonical ID with the typed DTO.
 *
 * <p>This record provides type-safe access to definition data, eliminating
 * the need for instanceof checks throughout the pipeline.</p>
 *
 * @param id       The canonical ID of the definition (e.g., forgero:iron).
 * @param data     The typed DTO parsed from JSON (implements {@link DefinitionData}).
 * @param priority Merge priority for resolving same-id definitions across packs. Higher wins on
 *                 id-keyed overrides; ties fall back to load order. Defaults to 0.
 */
public record RawDefinition(OpenIdentifier id, DefinitionData data, int priority) {

	/**
	 * Convenience constructor with default priority 0. Keeps existing call sites working.
	 */
	public RawDefinition(OpenIdentifier id, DefinitionData data) {
		this(id, data, 0);
	}
}
