package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A caching decorator for {@link ResourceProvider} that avoids redundant filesystem traversal.
 * <p>
 * This provider caches:
 * <ul>
 *   <li>Directory listings from {@link #list(ResourcePath, boolean, ResourceFilter)}</li>
 *   <li>Resource content from {@link #read(ResourcePath)}</li>
 *   <li>Source metadata (which mod provided each resource)</li>
 * </ul>
 * <p>
 * The cache is session-scoped (cleared on game restart). For development mode,
 * content hash-based change detection is available via {@link #hasContentChanged(ResourcePath)}.
 * <p>
 * <h2>Usage</h2>
 * <pre>{@code
 * // Wrap an existing provider
 * ResourceProvider cached = new CachingResourceProvider(new FabricResourceProvider("data"));
 *
 * // Use normally - subsequent calls return cached results
 * cached.list(path, true, filter);  // First call: filesystem traversal
 * cached.list(path, true, filter);  // Second call: cache hit
 *
 * // Invalidate when needed (e.g., F3+T reload)
 * cached.invalidateAll();
 * }</pre>
 */
public class CachingResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(CachingResourceProvider.class);

	private final ResourceProvider delegate;
	private final boolean developmentMode;

	// Cache for directory listings: key = "namespace:directory:recursive:filterName"
	private final Map<String, List<ResourcePath>> listCache = new ConcurrentHashMap<>();

	// Cache for resource content: key = ResourcePath.toString()
	private final Map<String, CachedResource> resourceCache = new ConcurrentHashMap<>();

	// Source tracking: which provider/mod each resource came from
	private final Map<String, String> sourceMap = new ConcurrentHashMap<>();

	/**
	 * Cached resource content with metadata for change detection.
	 *
	 * @param content   The raw byte content of the resource
	 * @param hash      SHA-256 hash of the content for change detection
	 * @param timestamp When this resource was cached
	 * @param source    The source/mod that provided this resource (if tracked)
	 */
	public record CachedResource(
			byte[] content,
			String hash,
			long timestamp,
			String source
	) {
		/**
		 * Returns the content as a string (UTF-8).
		 */
		public String contentAsString() {
			return new String(content, StandardCharsets.UTF_8);
		}

		/**
		 * Creates a new InputStream from the cached content.
		 */
		public InputStream newInputStream() {
			return new ByteArrayInputStream(content);
		}
	}

	/**
	 * Creates a CachingResourceProvider with development mode disabled.
	 *
	 * @param delegate The underlying provider to cache
	 */
	public CachingResourceProvider(ResourceProvider delegate) {
		this(delegate, false);
	}

	/**
	 * Creates a CachingResourceProvider.
	 *
	 * @param delegate        The underlying provider to cache
	 * @param developmentMode If true, enables content hash change detection
	 */
	public CachingResourceProvider(ResourceProvider delegate, boolean developmentMode) {
		this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
		this.developmentMode = developmentMode;
		LOGGER.info("CachingResourceProvider initialized (devMode={})", developmentMode);
	}

	@Override
	public Set<String> getNamespaces() {
		return delegate.getNamespaces();
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		String cacheKey = buildListCacheKey(path, recursive, filter);

		List<ResourcePath> cached = listCache.get(cacheKey);
		if (cached != null) {
			LOGGER.trace("Cache hit for list: {}", cacheKey);
			return cached.stream();
		}

		LOGGER.debug("Cache miss for list: {}, fetching from delegate", cacheKey);
		List<ResourcePath> results = delegate.list(path, recursive, filter).toList();
		listCache.put(cacheKey, results);

		return results.stream();
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		String cacheKey = path.toString();

		CachedResource cached = resourceCache.get(cacheKey);
		if (cached != null && !shouldRefresh(path, cached)) {
			LOGGER.trace("Cache hit for read: {}", cacheKey);
			return Optional.of(cached.newInputStream());
		}

		LOGGER.trace("Cache miss for read: {}", cacheKey);
		Optional<InputStream> result = delegate.read(path);

		if (result.isPresent()) {
			try {
				byte[] content = result.get().readAllBytes();
				String hash = computeHash(content);
				String source = sourceMap.getOrDefault(cacheKey, "unknown");

				CachedResource newCached = new CachedResource(content, hash, System.currentTimeMillis(), source);
				resourceCache.put(cacheKey, newCached);

				return Optional.of(newCached.newInputStream());
			} catch (IOException e) {
				LOGGER.warn("Failed to cache resource content: {}", cacheKey, e);
				// Return a fresh stream from delegate on cache failure
				return delegate.read(path);
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	@Override
	public boolean exists(ResourcePath path) {
		String cacheKey = path.toString();
		if (resourceCache.containsKey(cacheKey)) {
			return true;
		}
		return delegate.exists(path);
	}

	@Override
	public int priority() {
		return delegate.priority();
	}

	@Override
	public String name() {
		return "Caching[" + delegate.name() + "]";
	}

	// ========== Cache Management ==========

	/**
	 * Invalidates all cached data.
	 * Call this when resources may have changed (e.g., F3+T reload).
	 */
	public void invalidateAll() {
		int listCount = listCache.size();
		int resourceCount = resourceCache.size();

		listCache.clear();
		resourceCache.clear();
		sourceMap.clear();

		LOGGER.info("Cache invalidated: {} list entries, {} resource entries cleared", listCount, resourceCount);
	}

	/**
	 * Invalidates cached data for a specific namespace.
	 *
	 * @param namespace The namespace to invalidate
	 */
	public void invalidateNamespace(String namespace) {
		listCache.keySet().removeIf(key -> key.startsWith(namespace + ":"));
		resourceCache.keySet().removeIf(key -> key.startsWith(namespace + ":"));
		sourceMap.keySet().removeIf(key -> key.startsWith(namespace + ":"));

		LOGGER.debug("Cache invalidated for namespace: {}", namespace);
	}

	/**
	 * Invalidates a specific resource.
	 *
	 * @param path The resource path to invalidate
	 */
	public void invalidate(ResourcePath path) {
		String cacheKey = path.toString();
		resourceCache.remove(cacheKey);
		sourceMap.remove(cacheKey);

		// Also invalidate any list caches that might include this path
		String namespace = path.namespace();
		listCache.keySet().removeIf(key -> key.startsWith(namespace + ":"));

		LOGGER.trace("Cache invalidated for resource: {}", cacheKey);
	}

	// ========== Source Tracking ==========

	/**
	 * Records the source (mod ID) for a resource path.
	 * Call this when discovering resources to track provenance.
	 *
	 * @param path   The resource path
	 * @param source The source identifier (e.g., mod ID)
	 */
	public void trackSource(ResourcePath path, String source) {
		sourceMap.put(path.toString(), source);
	}

	/**
	 * Gets the source that provided a resource.
	 *
	 * @param path The resource path
	 * @return The source identifier, or "unknown" if not tracked
	 */
	public String getSource(ResourcePath path) {
		return sourceMap.getOrDefault(path.toString(), "unknown");
	}

	/**
	 * Gets all tracked sources and their resource counts.
	 *
	 * @return A map of source ID to resource count
	 */
	public Map<String, Long> getSourceStatistics() {
		Map<String, Long> stats = new HashMap<>();
		sourceMap.values().forEach(source ->
				stats.merge(source, 1L, Long::sum)
		);
		return stats;
	}

	// ========== Change Detection (Development Mode) ==========

	/**
	 * Checks if a resource's content has changed since it was cached.
	 * Only meaningful in development mode with an actual file-backed provider.
	 *
	 * @param path The resource path to check
	 * @return true if the content has changed or is not cached
	 */
	public boolean hasContentChanged(ResourcePath path) {
		if (!developmentMode) {
			return false;
		}

		String cacheKey = path.toString();
		CachedResource cached = resourceCache.get(cacheKey);
		if (cached == null) {
			return true; // Not cached = treat as changed
		}

		// Re-read from delegate and compare hash
		Optional<InputStream> freshContent = delegate.read(path);
		if (freshContent.isEmpty()) {
			return true; // Resource disappeared
		}

		try {
			byte[] freshBytes = freshContent.get().readAllBytes();
			String freshHash = computeHash(freshBytes);
			return !freshHash.equals(cached.hash());
		} catch (IOException e) {
			LOGGER.warn("Failed to check content change for: {}", cacheKey, e);
			return true; // Assume changed on error
		}
	}

	/**
	 * Gets all resources that have changed since they were cached.
	 * Only meaningful in development mode.
	 *
	 * @return Set of resource paths with changed content
	 */
	public Set<ResourcePath> getChangedResources() {
		if (!developmentMode) {
			return Set.of();
		}

		Set<ResourcePath> changed = new HashSet<>();
		for (String key : resourceCache.keySet()) {
			ResourcePath path = ResourcePath.parse(key);
			if (hasContentChanged(path)) {
				changed.add(path);
			}
		}
		return changed;
	}

	// ========== Cache Statistics ==========

	/**
	 * Returns cache statistics for debugging.
	 *
	 * @return A map of statistic names to values
	 */
	public Map<String, Object> getStatistics() {
		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("listCacheSize", listCache.size());
		stats.put("resourceCacheSize", resourceCache.size());
		stats.put("trackedSources", sourceMap.size());
		stats.put("developmentMode", developmentMode);
		stats.put("sourceBreakdown", getSourceStatistics());
		return stats;
	}

	// ========== Internal Helpers ==========

	private String buildListCacheKey(ResourcePath path, boolean recursive, ResourceFilter filter) {
		// Use filter class name as part of key since ResourceFilter is a predicate
		String filterName = filter.getClass().getSimpleName();
		if (filterName.isEmpty() || filterName.contains("$$Lambda")) {
			filterName = String.valueOf(filter.hashCode());
		}
		return path.namespace() + ":" + path.directory() + ":" + recursive + ":" + filterName;
	}

	private boolean shouldRefresh(ResourcePath path, CachedResource cached) {
		// In development mode, we could check for changes
		// For now, trust the cache until explicitly invalidated
		return false;
	}

	private String computeHash(byte[] content) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(content);
			return bytesToHex(hash);
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is always available, but fall back to simple hash
			return String.valueOf(Arrays.hashCode(content));
		}
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 * Gets the underlying delegate provider.
	 *
	 * @return The delegate provider
	 */
	public ResourceProvider getDelegate() {
		return delegate;
	}

	/**
	 * Checks if development mode is enabled.
	 *
	 * @return true if development mode is enabled
	 */
	public boolean isDevelopmentMode() {
		return developmentMode;
	}
}
