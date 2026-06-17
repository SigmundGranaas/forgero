package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper for ComponentUpgradeSlot instances in a SlotContainer.
 * Filters the heterogeneous SlotContainer to only expose upgrade slots.
 *
 * @param slots The container holding all slots (only ComponentUpgradeSlot instances are exposed)
 */
public record ComponentUpgrades(SlotContainer slots) {

	/**
	 * Creates upgrades from varargs ComponentUpgradeSlot instances.
	 */
	public static ComponentUpgrades of(ComponentUpgradeSlot... slots) {
		return new ComponentUpgrades(SlotContainer.of(List.of(slots)));
	}

	/**
	 * Creates upgrades from a collection of ComponentUpgradeSlot instances.
	 */
	public static ComponentUpgrades of(Collection<ComponentUpgradeSlot> slots) {
		return new ComponentUpgrades(SlotContainer.of(slots));
	}

	/**
	 * Creates upgrades from a heterogeneous collection of {@link Slot} kinds. Used by the loader
	 * once slot kinds are dispatched by their {@code kind} — the container holds any Slot, and the
	 * upgrade-typed views ({@link #allUpgradeSlots()} etc.) still expose only ComponentUpgradeSlots.
	 */
	public static ComponentUpgrades ofSlots(Collection<? extends Slot> slots) {
		return new ComponentUpgrades(SlotContainer.of(slots));
	}

	/**
	 * Creates empty upgrades (component has upgrade capability but no slots defined).
	 */
	public static ComponentUpgrades empty() {
		return new ComponentUpgrades(SlotContainer.empty());
	}

	/**
	 * Gets a ComponentUpgradeSlot by ID.
	 */
	public Optional<ComponentUpgradeSlot> get(OpenIdentifier id) {
		return slots.get(id)
				.filter(slot -> slot instanceof ComponentUpgradeSlot)
				.map(slot -> (ComponentUpgradeSlot) slot);
	}

	/**
	 * Checks if a ComponentUpgradeSlot with the given ID exists.
	 */
	public boolean contains(OpenIdentifier id) {
		return get(id).isPresent();
	}

	/**
	 * Returns all components currently filling ComponentUpgradeSlot instances.
	 * Empty slots are not included.
	 */
	public List<Component> filledContents() {
		return slots.all().stream()
				.filter(slot -> slot instanceof ComponentUpgradeSlot)
				.map(slot -> (ComponentUpgradeSlot) slot)
				.flatMap(slot -> slot.getContent().stream())
				.toList();
	}

	/**
	 * Component contents of <em>all</em> slot kinds (not just upgrade slots) that opt into
	 * traversal and contribute a Component. This is the source for component-tree traversal /
	 * property &amp; stat compilation, so any {@link Slot} kind holding a Component participates.
	 * <p>
	 * For the current content this is identical to {@link #filledContents()} — every
	 * {@code ComponentUpgradeSlot} returns {@code true} from {@link Slot#includeInTraversal()} and
	 * exposes its content via {@link Slot#componentContent()} — but a plugin slot kind that holds a
	 * Component is now included, and one that opts out (or holds non-Component state) is excluded.
	 */
	public List<Component> traversableContents() {
		return slots.all().stream()
				.filter(Slot::includeInTraversal)
				.flatMap(slot -> slot.componentContent().stream())
				.toList();
	}

	/**
	 * Returns a new upgrades with the specified ComponentUpgradeSlot updated.
	 */
	public ComponentUpgrades withSlot(ComponentUpgradeSlot slot) {
		return new ComponentUpgrades(slots.with(slot));
	}

	/**
	 * Returns true if there are no ComponentUpgradeSlot instances.
	 */
	public boolean isEmpty() {
		return upgradeSlots().findAny().isEmpty();
	}

	/**
	 * Returns the number of ComponentUpgradeSlot instances.
	 */
	public int size() {
		return (int) upgradeSlots().count();
	}

	/**
	 * Returns the number of filled ComponentUpgradeSlot instances.
	 */
	public int filledCount() {
		return (int) upgradeSlots().filter(ComponentUpgradeSlot::isFilled).count();
	}

	/**
	 * Returns true if all ComponentUpgradeSlot instances are filled.
	 */
	public boolean allFilled() {
		return upgradeSlots().allMatch(ComponentUpgradeSlot::isFilled);
	}

	/**
	 * Returns all ComponentUpgradeSlot instances as a list.
	 */
	public List<ComponentUpgradeSlot> allUpgradeSlots() {
		return upgradeSlots().toList();
	}

	/**
	 * Helper to get stream of ComponentUpgradeSlot instances from the heterogeneous container.
	 */
	private java.util.stream.Stream<ComponentUpgradeSlot> upgradeSlots() {
		return slots.all().stream()
				.filter(slot -> slot instanceof ComponentUpgradeSlot)
				.map(slot -> (ComponentUpgradeSlot) slot);
	}
}
