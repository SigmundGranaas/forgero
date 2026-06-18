package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for selectors that support filtering.
 * Handles the common pattern of selecting entities, applying filters, and capping the result size.
 */
public abstract class FilterableSelector implements EntitySelector {
	/**
	 * Hard upper bound (in blocks) on any configurable range/radius a selector may use.
	 * Enforced at construction so that a mistyped or abusive value (e.g. a radius of 1000 on an
	 * {@code on_tick} property) fails loudly during data loading rather than silently issuing a
	 * pathological world query every tick.
	 */
	public static final double MAX_RANGE = 64.0;

	/**
	 * Sentinel for "no cap" — a {@code maxTargets} of this value means every selected entity is kept.
	 */
	public static final int UNLIMITED_TARGETS = Integer.MAX_VALUE;

	private final List<EntityFilter> filters;
	private final FilterMode filterMode;
	private final int maxTargets;

	protected FilterableSelector(List<EntityFilter> filters) {
		this(filters, FilterMode.ALL, UNLIMITED_TARGETS);
	}

	protected FilterableSelector(List<EntityFilter> filters, FilterMode filterMode, int maxTargets) {
		this.filters = filters != null ? filters : Collections.emptyList();
		this.filterMode = filterMode != null ? filterMode : FilterMode.ALL;
		if (maxTargets <= 0) {
			throw new IllegalArgumentException("maxTargets must be > 0, got: " + maxTargets);
		}
		this.maxTargets = maxTargets;
	}

	public List<EntityFilter> filters() {
		return filters;
	}

	public FilterMode filterMode() {
		return filterMode;
	}

	public int maxTargets() {
		return maxTargets;
	}

	@Override
	public final List<Entity> select(Entity source, Entity initialTarget) {
		// First, perform the geometric/strategic selection
		List<Entity> selected = selectEntities(source, initialTarget);

		// Then apply all filters to narrow down the results
		List<Entity> filtered = applyFilters(selected, source);

		// Finally, cap the result to maxTargets (keeping the closest to the anchor)
		return limit(filtered, source, initialTarget);
	}

	/**
	 * Perform the actual entity selection (geometric, strategic, etc.)
	 * Subclasses implement their selection logic here.
	 *
	 * @param source        The source entity (e.g., attacker)
	 * @param initialTarget The initial target entity
	 * @return List of selected entities before filtering
	 */
	protected abstract List<Entity> selectEntities(Entity source, Entity initialTarget);

	/**
	 * The reference entity that the {@link #maxTargets()} cap measures proximity from when trimming
	 * results. Defaults to the initial target; directional selectors (e.g. cone) override this to
	 * use the source.
	 */
	protected Entity limitAnchor(Entity source, Entity initialTarget) {
		return initialTarget;
	}

	/**
	 * Apply all filters to the selected entities according to the configured {@link FilterMode}.
	 * With {@link FilterMode#ALL} an entity must pass every filter; with {@link FilterMode#ANY} it
	 * must pass at least one. An empty filter list passes everything.
	 *
	 * @param entities List of entities to filter
	 * @param source   The source entity for filter context
	 * @return Filtered list of entities
	 */
	protected List<Entity> applyFilters(List<Entity> entities, Entity source) {
		if (filters.isEmpty()) {
			return entities;
		}

		return entities.stream()
				.filter(candidate -> filterMode.matches(filters, source, candidate))
				.toList();
	}

	private List<Entity> limit(List<Entity> entities, Entity source, Entity initialTarget) {
		if (entities.size() <= maxTargets) {
			return entities;
		}

		Entity anchor = limitAnchor(source, initialTarget);
		return entities.stream()
				.sorted(Comparator.comparingDouble(anchor::squaredDistanceTo))
				.limit(maxTargets)
				.toList();
	}
}
