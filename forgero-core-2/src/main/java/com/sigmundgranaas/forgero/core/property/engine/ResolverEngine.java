package com.sigmundgranaas.forgero.core.property.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The primary implementation of the {@link Resolver} interface.
 * This engine manages a collection of {@link DataTypeEngine}s and orchestrates the two-phase
 * resolution process ("bake" and "apply"). It uses a cache to store the results of the
 * expensive "bake" phase, ensuring high performance for repeated resolutions.
 */
public class ResolverEngine implements Resolver {
	private final Map<OpenIdentifier, DataTypeEngine<?, ?>> engines;
	private final Cache<CacheKey, Object> bakedCache;

	/**
	 * A record used as a key in the cache. It combines the component (which must have a stable
	 * {@code equals} and {@code hashCode} implementation, like records do) and the ID of the
	 * engine being used.
	 */
	private record CacheKey(Component component, OpenIdentifier engineId) {
	}

	/**
	 * Constructs a ResolverEngine with a list of expert engines.
	 *
	 * @param engines A list of all available {@link DataTypeEngine}s that the system can use.
	 */
	public ResolverEngine(List<DataTypeEngine<?, ?>> engines) {
		this.engines = engines.stream()
				.collect(Collectors.toMap(engine -> engine.key().id(), Function.identity()));
		this.bakedCache = Caffeine.newBuilder().maximumSize(1000).build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> Optional<R> resolve(Component component, ResolutionKey<R> key, DynamicContext context) {
		// 1. Find the correct engine for the given key.
		DataTypeEngine<?, ?> untypedEngine = engines.get(key.id());
		if (untypedEngine == null) {
			return Optional.empty();
		}
		// We cast here, but it's safe because the key's type R is tied to the engine's result type.
		DataTypeEngine<Object, R> engine = (DataTypeEngine<Object, R>) untypedEngine;

		// 2. Get the intermediate baked result, computing it only if not in the cache.
		CacheKey cacheKey = new CacheKey(component, key.id());
		Object bakedResult = bakedCache.get(cacheKey, k -> {
			List<Component> componentStream = traverse(component);
			return engine.bake(componentStream.stream());
		});

		// 3. Apply the context to the baked result to get the final result.
		R finalResult = engine.apply(bakedResult, context);
		return Optional.of(finalResult);
	}

	/**
	 * Performs a full, non-recursive, pre-order traversal of the component tree.
	 *
	 * @param component The root component to start traversal from.
	 * @return A stream of all components in the tree.
	 */
	private List<Component> traverse(Component component) {
		List<Component> allComponents = new ArrayList<>();
		Deque<Component> stack = new ArrayDeque<>();
		stack.push(component);

		while (!stack.isEmpty()) {
			Component current = stack.pop();
			allComponents.add(current);
			// Add children to the stack. Reverse them to maintain pre-order traversal.
			List<Component> children = current.getChildren();
			for (int i = children.size() - 1; i >= 0; i--) {
				stack.push(children.get(i));
			}
		}
		return allComponents;
	}
}
