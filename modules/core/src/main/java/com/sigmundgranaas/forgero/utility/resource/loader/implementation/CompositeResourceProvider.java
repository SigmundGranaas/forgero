package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aggregates multiple {@link ResourceProvider}s with configurable priority and conflict resolution.
 * <p>
 * CompositeResourceProvider allows combining resources from multiple sources (files, JARs, mods,
 * programmatic definitions) into a single unified provider. Resources are discovered from all
 * providers, with conflicts resolved according to the configured {@link ConflictStrategy}.
 * <p>
 * Providers are ordered by their {@link ResourceProvider#priority()} - higher priority providers
 * are checked first for reading and take precedence in conflict resolution (depending on strategy).
 *
 * <h2>Conflict Strategies</h2>
 * <ul>
 *   <li>{@link ConflictStrategy#LAST_WINS} - Later providers override earlier ones (like Minecraft data packs)</li>
 *   <li>{@link ConflictStrategy#FIRST_WINS} - First provider wins, duplicates ignored</li>
 *   <li>{@link ConflictStrategy#INCLUDE_ALL} - Include all occurrences (useful for merging)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ResourceProvider provider = CompositeResourceProvider.builder()
 *     .add(new ProgrammaticResourceProvider("overrides", 200))
 *     .add(new FabricResourceProvider("data", 50))
 *     .add(new ClassPathResourceProvider("data", 0))
 *     .withConflictStrategy(ConflictStrategy.LAST_WINS)
 *     .build();
 *
 * // Resources will be discovered from all providers
 * // When reading, highest priority provider with the resource wins
 * }</pre>
 */
public class CompositeResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeResourceProvider.class);

	private final List<ResourceProvider> providers;
	private final ConflictStrategy conflictStrategy;
	private final Set<String> cachedNamespaces;

	/**
	 * Defines how to handle resources that exist in multiple providers during listing.
	 */
	public enum ConflictStrategy {
		/**
		 * Later providers (lower priority) override earlier ones.
		 * This matches Minecraft's data pack behavior where later packs override earlier ones.
		 */
		LAST_WINS,

		/**
		 * First provider (highest priority) wins, duplicates are ignored.
		 * Use this when you want explicit priority ordering.
		 */
		FIRST_WINS,

		/**
		 * Include all occurrences from all providers.
		 * Useful when you want to merge resources rather than override.
		 */
		INCLUDE_ALL
	}

	private CompositeResourceProvider(List<ResourceProvider> providers, ConflictStrategy strategy) {
		// Sort by priority (descending - higher priority first)
		this.providers = providers.stream()
				.sorted(Comparator.comparingInt(ResourceProvider::priority).reversed())
				.toList();
		this.conflictStrategy = strategy;

		// Cache namespaces from all providers
		this.cachedNamespaces = providers.stream()
				.flatMap(p -> p.getNamespaces().stream())
				.collect(Collectors.toUnmodifiableSet());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CompositeResourceProvider initialized with {} providers (strategy={}): {}",
					providers.size(),
					strategy,
					this.providers.stream()
							.map(p -> p.name() + "[" + p.priority() + "]")
							.collect(Collectors.joining(", ")));
		}
	}

	@Override
	public Set<String> getNamespaces() {
		return cachedNamespaces;
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		return switch (conflictStrategy) {
			case FIRST_WINS -> listFirstWins(path, recursive, filter);
			case LAST_WINS -> listLastWins(path, recursive, filter);
			case INCLUDE_ALL -> listIncludeAll(path, recursive, filter);
		};
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	/**
	 * First provider with the resource wins - uses priority ordering.
	 * We track seen paths and skip duplicates.
	 */
	private Stream<ResourcePath> listFirstWins(ResourcePath path, boolean recursive, ResourceFilter filter) {
		Set<String> seen = new HashSet<>();
		return providers.stream()
				.flatMap(p -> p.list(path, recursive, filter))
				.filter(rp -> seen.add(rp.toString()));
	}

	/**
	 * Last provider wins - later entries in the list override earlier ones.
	 * Since providers are sorted by priority (high to low), we iterate in reverse
	 * or use a map where later writes override.
	 */
	private Stream<ResourcePath> listLastWins(ResourcePath path, boolean recursive, ResourceFilter filter) {
		// Use LinkedHashMap to maintain insertion order
		Map<String, ResourcePath> resources = new LinkedHashMap<>();

		// Iterate in reverse priority order (low to high) so higher priority writes last
		List<ResourceProvider> reversed = new ArrayList<>(providers);
		Collections.reverse(reversed);

		for (ResourceProvider provider : reversed) {
			provider.list(path, recursive, filter)
					.forEach(rp -> resources.put(rp.toString(), rp));
		}

		return resources.values().stream();
	}

	/**
	 * Include all occurrences from all providers.
	 */
	private Stream<ResourcePath> listIncludeAll(ResourcePath path, boolean recursive, ResourceFilter filter) {
		return providers.stream()
				.flatMap(p -> p.list(path, recursive, filter));
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		// Always read from highest priority provider that has the resource
		for (ResourceProvider provider : providers) {
			Optional<InputStream> result = provider.read(path);
			if (result.isPresent()) {
				LOGGER.trace("Resource {} found in provider {}", path, provider.name());
				return result;
			}
		}
		LOGGER.trace("Resource {} not found in any provider", path);
		return Optional.empty();
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	@Override
	public boolean exists(ResourcePath path) {
		return providers.stream().anyMatch(p -> p.exists(path));
	}

	@Override
	public int priority() {
		// Return the highest priority among all providers
		return providers.stream()
				.mapToInt(ResourceProvider::priority)
				.max()
				.orElse(0);
	}

	@Override
	public String name() {
		return "Composite[" + providers.stream()
				.map(ResourceProvider::name)
				.collect(Collectors.joining(", ")) + "]";
	}

	/**
	 * Returns the list of providers in priority order (highest first).
	 *
	 * @return Unmodifiable list of providers
	 */
	public List<ResourceProvider> getProviders() {
		return providers;
	}

	/**
	 * Returns the conflict strategy used by this composite provider.
	 *
	 * @return The conflict strategy
	 */
	public ConflictStrategy getConflictStrategy() {
		return conflictStrategy;
	}

	/**
	 * Creates a new builder for constructing a CompositeResourceProvider.
	 *
	 * @return A new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Creates a composite provider from the given providers using the default LAST_WINS strategy.
	 *
	 * @param providers The providers to compose
	 * @return A new CompositeResourceProvider
	 */
	public static CompositeResourceProvider of(ResourceProvider... providers) {
		return builder().addAll(Arrays.asList(providers)).build();
	}

	/**
	 * Creates a composite provider from the given providers using the specified strategy.
	 *
	 * @param strategy  The conflict strategy
	 * @param providers The providers to compose
	 * @return A new CompositeResourceProvider
	 */
	public static CompositeResourceProvider of(ConflictStrategy strategy, ResourceProvider... providers) {
		return builder()
				.addAll(Arrays.asList(providers))
				.withConflictStrategy(strategy)
				.build();
	}

	/**
	 * Builder for constructing CompositeResourceProvider instances.
	 */
	public static class Builder {
		private final List<ResourceProvider> providers = new ArrayList<>();
		private ConflictStrategy strategy = ConflictStrategy.LAST_WINS;

		private Builder() {
		}

		/**
		 * Adds a provider to the composite.
		 *
		 * @param provider The provider to add
		 * @return This builder
		 */
		public Builder add(ResourceProvider provider) {
			Objects.requireNonNull(provider, "provider cannot be null");
			providers.add(provider);
			return this;
		}

		/**
		 * Adds multiple providers to the composite.
		 *
		 * @param providers The providers to add
		 * @return This builder
		 */
		public Builder addAll(Collection<? extends ResourceProvider> providers) {
			Objects.requireNonNull(providers, "providers cannot be null");
			providers.forEach(this::add);
			return this;
		}

		/**
		 * Sets the conflict resolution strategy.
		 *
		 * @param strategy The strategy to use
		 * @return This builder
		 */
		public Builder withConflictStrategy(ConflictStrategy strategy) {
			Objects.requireNonNull(strategy, "strategy cannot be null");
			this.strategy = strategy;
			return this;
		}

		/**
		 * Builds the CompositeResourceProvider.
		 *
		 * @return A new CompositeResourceProvider
		 * @throws IllegalStateException if no providers have been added
		 */
		public CompositeResourceProvider build() {
			if (providers.isEmpty()) {
				throw new IllegalStateException("CompositeResourceProvider requires at least one provider");
			}
			return new CompositeResourceProvider(new ArrayList<>(providers), strategy);
		}
	}
}
