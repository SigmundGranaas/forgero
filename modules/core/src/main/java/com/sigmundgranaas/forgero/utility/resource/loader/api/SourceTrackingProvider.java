package com.sigmundgranaas.forgero.utility.resource.loader.api;

import java.util.Map;
import java.util.Optional;

/**
 * Extension interface for {@link ResourceProvider} implementations that track
 * which source (mod, file system, etc.) provided each resource.
 * <p>
 * Source tracking enables:
 * <ul>
 *   <li>Debugging which mod provides which resources</li>
 *   <li>Priority-based deduplication at discovery time</li>
 *   <li>Hot-reload targeting for development</li>
 * </ul>
 * <p>
 * Implementations should track the source when listing resources and make
 * it available via {@link #getSource(ResourcePath)}.
 */
public interface SourceTrackingProvider extends ResourceProvider {

	/**
	 * Gets the source identifier for a resource.
	 * The source is typically the mod ID that provided the resource.
	 *
	 * @param path The resource path
	 * @return The source identifier, or empty if not tracked
	 */
	Optional<String> getSource(ResourcePath path);

	/**
	 * Gets all tracked source mappings.
	 *
	 * @return A map of resource path (as string) to source identifier
	 */
	Map<String, String> getAllSourceMappings();

	/**
	 * Clears all tracked source mappings.
	 * Called when cache is invalidated.
	 */
	void clearSourceMappings();
}
