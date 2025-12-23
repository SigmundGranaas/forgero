package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.List;

/**
 * Base class for selectors that support filtering.
 * Handles the common pattern of selecting entities and then applying filters.
 */
public abstract class FilterableSelector implements EntitySelector {
	private final List<EntityFilter> filters;

	protected FilterableSelector(List<EntityFilter> filters) {
		this.filters = filters != null ? filters : Collections.emptyList();
	}

	public List<EntityFilter> filters() {
		return filters;
	}

	@Override
	public final List<Entity> select(Entity source, Entity initialTarget) {
		// First, perform the geometric/strategic selection
		List<Entity> selected = selectEntities(source, initialTarget);

		// Then apply all filters to narrow down the results
		return applyFilters(selected, source);
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
	 * Apply all filters to the selected entities.
	 * Each entity must pass ALL filters to be included in final result.
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
				.filter(candidate -> filters.stream()
						.allMatch(filter -> filter.test(source, candidate)))
				.toList();
	}
}
