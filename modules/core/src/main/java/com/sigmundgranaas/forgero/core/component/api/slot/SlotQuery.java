package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Fluent builder for querying and filtering slots.
 * <p>
 * Provides composable predicates for finding slots matching specific criteria.
 * All filter methods return {@code this} for method chaining.
 * <p>
 * Example usage:
 * <pre>{@code
 * SlotManager manager = services.slotManager();
 *
 * // Find all empty gem slots
 * List<UpgradeSlot> gemSlots = manager.queryUpgradeSlots(tool)
 *     .ofType(OpenIdentifier.of("forgero:gem"))
 *     .onlyEmpty()
 *     .execute();
 *
 * // Find slots that can accept a specific upgrade
 * List<UpgradeSlot> compatibleSlots = manager.queryUpgradeSlots(tool)
 *     .onlyEmpty()
 *     .matching(slot -> slot.validator().test(upgrade))
 *     .execute();
 *
 * // Check if any binding slot exists
 * boolean hasBindingSlot = manager.queryUpgradeSlots(tool)
 *     .ofType(BINDING_TYPE)
 *     .exists();
 *
 * // Get first compatible empty slot
 * Optional<UpgradeSlot> slot = manager.queryUpgradeSlots(tool)
 *     .ofType(GEM_TYPE)
 *     .onlyEmpty()
 *     .compatibleWith(gemComponent)
 *     .first();
 * }</pre>
 *
 * @param <S> The type of slot being queried (UpgradeSlot or StructureSlot)
 */
public interface SlotQuery<S extends Slot> {

	/**
	 * Filters slots by type identifier.
	 * <p>
	 * Only includes slots where {@code slot.type().equals(type)}.
	 *
	 * @param type The slot type to match
	 * @return This query for chaining
	 */
	SlotQuery<S> ofType(OpenIdentifier type);

	/**
	 * Filters slots by matching any of the provided types (OR condition).
	 * <p>
	 * Includes slots where the slot type equals any of the provided types.
	 *
	 * @param types The slot types to match (any match)
	 * @return This query for chaining
	 */
	SlotQuery<S> ofAnyType(OpenIdentifier... types);

	/**
	 * Filters slots with a custom predicate.
	 * <p>
	 * This allows for arbitrary filtering logic. All predicates added via
	 * this method (and other filter methods) are combined with AND logic.
	 *
	 * @param predicate The predicate to apply
	 * @return This query for chaining
	 */
	SlotQuery<S> matching(Predicate<S> predicate);

	/**
	 * Only includes empty slots (for UpgradeSlot queries).
	 * <p>
	 * Has no effect on StructureSlot queries (structure slots are always filled).
	 * <p>
	 * For {@code UpgradeSlot}, this filters for {@code slot.isEmpty() == true}.
	 *
	 * @return This query for chaining
	 */
	SlotQuery<S> onlyEmpty();

	/**
	 * Only includes filled slots (for UpgradeSlot queries).
	 * <p>
	 * For {@code UpgradeSlot}, this filters for {@code slot.isFilled() == true}.
	 * For {@code StructureSlot}, this has no effect (always included).
	 *
	 * @return This query for chaining
	 */
	SlotQuery<S> onlyFilled();

	/**
	 * Filters slots that can accept a specific component.
	 * <p>
	 * Tests each slot's validator against the provided component.
	 * Only includes slots where {@code slot.validator().test(component) == true}.
	 *
	 * @param component The component to test against slot validators
	 * @return This query for chaining
	 */
	SlotQuery<S> compatibleWith(Component component);

	/**
	 * Filters slots by description matching a regex pattern.
	 * <p>
	 * Uses {@code String.matches(regex)} to test the slot description.
	 *
	 * @param descriptionPattern Regex pattern to match against slot.description()
	 * @return This query for chaining
	 */
	SlotQuery<S> descriptionMatches(String descriptionPattern);

	/**
	 * Executes the query and returns all matching slots.
	 * <p>
	 * Applies all filters in the order they were added and returns
	 * the resulting list of slots.
	 *
	 * @return List of slots matching all filters (may be empty)
	 */
	List<S> execute();

	/**
	 * Executes the query and returns the first matching slot.
	 * <p>
	 * This is equivalent to {@code execute().stream().findFirst()},
	 * but may be more efficient as it can short-circuit.
	 *
	 * @return First matching slot, or empty if none found
	 */
	Optional<S> first();

	/**
	 * Checks if any slot matches the query.
	 * <p>
	 * This is equivalent to {@code first().isPresent()}, but more
	 * semantically clear.
	 *
	 * @return true if at least one slot matches all filters
	 */
	boolean exists();

	/**
	 * Counts slots matching the query.
	 * <p>
	 * This is equivalent to {@code execute().size()}.
	 *
	 * @return Number of matching slots
	 */
	int count();
}
