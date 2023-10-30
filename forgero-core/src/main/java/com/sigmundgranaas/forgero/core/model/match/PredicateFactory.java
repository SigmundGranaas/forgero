package com.sigmundgranaas.forgero.core.model.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

/**
 * The PredicateFactory class serves as a factory for creating Matchable instances.
 */
public class PredicateFactory {

	private static final List<PredicateBuilder> builders = new ArrayList<>();


	private static final LoadingCache<JsonElement, Matchable> cache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
					new CacheLoader<>() {
						public @NotNull Matchable load(@NotNull JsonElement element) {
							return builders.stream()
									.map(builder -> builder.create(element))
									.flatMap(Optional::stream)
									.findAny()
									.orElseGet(() -> {
										Forgero.LOGGER.error("Found predicate element with no corresponding predicate builder: {}, the corresponding entry will always fail matching checks.", element);
										return Matchable.DEFAULT_FALSE;
									});
						}
					}
			);

	public static void register(PredicateBuilder builder) {
		builders.add(builder);
	}

	public static void register(Supplier<PredicateBuilder> builder) {
		builders.add(builder.get());
	}

	/**
	 * Create a Matchable object by applying registered builders on the provided JsonElement.
	 * <p>
	 * If none of the builders can create a Matchable from the input JsonElement,
	 * a default Matchable that always returns false is returned.
	 *
	 * @param element The JsonElement to create the Matchable from.
	 * @return The created Matchable.
	 */
	public Matchable create(JsonElement element) {
		try {
			return cache.get(element);
		} catch (Exception e) {
			return Matchable.DEFAULT_FALSE;
		}
	}
}
