package com.sigmundgranaas.forgero.core.property.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The primary implementation of the {@link Resolver} interface.
 * This engine orchestrates the two-phase resolution process ("bake" and "apply") for a given
 * {@link DataTypeEngine}. It uses a cache to store the results of the expensive "bake" phase,
 * ensuring high performance for repeated resolutions.
 */
public class ResolverEngine implements Resolver {
	private final Cache<CacheKey, Object> bakedCache;

	/**
	 * A record used as a key in the cache. It combines the component (which must have a stable
	 * {@code equals} and {@code hashCode} implementation, like records do) and the ID of the
	 * engine being used.
	 */
	private record CacheKey(Component component, OpenIdentifier engineId) {
	}

	public ResolverEngine() {
		this.bakedCache = Caffeine.newBuilder().maximumSize(1000).build();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <B, R> R resolve(Component component, DataTypeEngine<B, R> engine, DynamicContext context) {
		// 1. Get the intermediate baked result, computing it only if not in the cache.
		// The engine's own key is used for caching to distinguish between different engine types.
		CacheKey cacheKey = new CacheKey(component, engine.key().id());

		B bakedResult = (B) bakedCache.get(cacheKey, k -> {
			List<Component> componentList = traverse(component);
			return engine.bake(componentList.stream());
		});

		// 2. Apply the context to the baked result to get the final result.
		return engine.apply(bakedResult, context);
	}

	/**
	 * Performs a full, non-recursive, pre-order traversal of the component tree.
	 *
	 * @param component The root component to start traversal from.
	 * @return A list of all components in the tree.
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
