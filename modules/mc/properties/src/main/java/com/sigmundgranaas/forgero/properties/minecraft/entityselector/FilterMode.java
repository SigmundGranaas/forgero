package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Locale;

/**
 * Controls how a selector's top-level filter list is combined.
 * <p>
 * Historically selectors only supported {@link #ALL} (every filter must pass), which forced
 * authors to wrap filters in an explicit {@code forgero:or} composite to express disjunction.
 * Exposing the mode directly on the selector via the optional {@code "match"} field removes that
 * boilerplate for the common case.
 */
public enum FilterMode {
	/**
	 * A candidate passes only if it satisfies every filter (logical AND). This is the default and
	 * preserves the original behaviour.
	 */
	ALL,
	/**
	 * A candidate passes if it satisfies at least one filter (logical OR).
	 */
	ANY;

	public static final Codec<FilterMode> CODEC = Codec.STRING.xmap(
			FilterMode::fromString,
			mode -> mode.name().toLowerCase(Locale.ROOT)
	);

	private static FilterMode fromString(String value) {
		return switch (value.toLowerCase(Locale.ROOT)) {
			case "all" -> ALL;
			case "any" -> ANY;
			default -> throw new IllegalArgumentException(
					"Unknown filter match mode: '" + value + "' (expected 'all' or 'any')");
		};
	}

	/**
	 * Tests whether the candidate passes the given filters under this mode.
	 * Callers are expected to short-circuit the empty-filter case before calling this.
	 *
	 * @param filters   the non-empty filter list to evaluate
	 * @param source    the source entity for filter context
	 * @param candidate the entity being tested
	 * @return true if the candidate passes according to this mode
	 */
	public boolean matches(List<EntityFilter> filters, Entity source, Entity candidate) {
		return switch (this) {
			case ALL -> filters.stream().allMatch(filter -> filter.test(source, candidate));
			case ANY -> filters.stream().anyMatch(filter -> filter.test(source, candidate));
		};
	}
}
