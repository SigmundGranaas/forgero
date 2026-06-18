package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Base interface for MUTABLE slot types.
 * Slots are containers that can be added to or removed from components.
 * Each slot implementation defines its own content type, validation, and behavior.
 *
 * NOTE: This is for MUTABLE slots only. Immutable component structure uses ComponentPart, not Slot.
 *
 * Implementations include:
 * - ComponentUpgradeSlot: Holds component upgrades (gems, bindings, etc.)
 * - ArrowSlot: Holds arrow items for bows (plugin example)
 * - SoulSlot: Holds soul data for stat tracking (plugin example)
 */
public interface Slot {
	/**
	 * @return Unique identifier for this slot within its parent component
	 */
	OpenIdentifier id();

	/**
	 * @return Slot implementation type for codec dispatch
	 * Examples: "forgero:component_upgrade", "forgero:arrow", "forgero:soul"
	 */
	OpenIdentifier type();

	/**
	 * @return Slot category/sub-type (e.g., "forgero:gem", "forgero:binding", "forgero:arrow")
	 * This is the semantic type of what the slot holds.
	 */
	OpenIdentifier slotType();

	/**
	 * @return Human-readable description
	 */
	String description();

	/**
	 * Filters or transforms the properties this slot's content contributes, per PropertyKey.
	 * <p>
	 * <b>Scope:</b> this hook is applied on the <em>custom-property</em> resolution path (on-hit,
	 * on-tick, tooltip, …), not on attribute compilation ({@code StatFold}). To suppress a slot's
	 * contribution to the compiled tree <em>entirely</em> (attributes and properties), return
	 * {@code false} from {@link #includeInTraversal()} instead — {@code filterProperties} is for
	 * <em>partial</em>, per-property-key shaping of what does get through.
	 *
	 * @param propertyKey The property type being resolved
	 * @param properties Properties from this slot's content (if applicable)
	 * @return Filtered properties that should contribute to parent
	 */
	default <P extends Property> Stream<P> filterProperties(
		PropertyKey<P> propertyKey,
		Stream<P> properties
	) {
		return properties;  // Default: pass through all properties
	}

	/**
	 * Controls component tree traversal during property resolution.
	 *
	 * @return true if this slot's content should be included in property resolution traversal
	 */
	default boolean includeInTraversal() {
		return true;  // Default: include in traversal
	}

	/**
	 * The {@link Component} this slot contributes to the parent's compiled tree, if any.
	 * <p>
	 * This is the generic hook the stat/property compiler uses to fold a slot's content into the
	 * parent — it lets any slot <em>kind</em> participate, not just {@code ComponentUpgradeSlot}.
	 * A slot that holds a Component (a gem, a rune, a potion modeled as a Component) returns it; a
	 * slot that holds non-Component state (e.g. {@code StatusModifierSlot}, an {@code ArrowSlot})
	 * returns {@link Optional#empty()} and contributes through its own machinery instead.
	 *
	 * @return the Component content to fold into the parent, or empty if this slot contributes none
	 */
	default Optional<Component> componentContent() {
		return Optional.empty();  // Default: a slot contributes no Component to the compiled tree
	}

	/**
	 * The slot's identity tags (its context, etc.), matched by {@code in_slot_type} alongside the
	 * slot type. Generic accessor so serialization can read tags off any kind; kinds without tags
	 * return empty.
	 */
	default java.util.Set<com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier> tags() {
		return java.util.Set.of();
	}

	/**
	 * Returns this slot with the given Component installed (or emptied when absent), preserving the
	 * slot's kind, identity, and validation. This is the overlay seam the NBT round-trip uses: the
	 * pristine slot (built from data) is the source of truth for the slot's configuration, and only
	 * the mutable Component content is restored onto it. A slot whose content is not a Component
	 * ignores this and returns itself.
	 *
	 * @param content the Component to install, or empty to clear the slot
	 * @return a slot of the same kind carrying the given content
	 */
	default Slot withComponentContent(Optional<Component> content) {
		return this;  // Default: non-Component slots carry no overlayable content
	}

	/**
	 * Validates whether the given Component may be installed in this slot, returning an error message
	 * if not. This is the generic compatibility seam the slot manager uses to install into any slot
	 * <em>kind</em> (not just {@code ComponentUpgradeSlot}). A kind with no install restriction (the
	 * default) accepts anything; {@code ComponentUpgradeSlot} delegates to its {@code SlotValidator}.
	 *
	 * @param content the Component a caller wants to install
	 * @return an error message if the content is not accepted, or empty if it is
	 */
	default java.util.Optional<String> validate(Component content) {
		return java.util.Optional.empty();  // Default: accept any content
	}

	/**
	 * Whether this slot kind holds a {@link Component} and is therefore a valid target for
	 * auto-routed Component installation. The slot manager's {@code install(target, upgrade)} only
	 * considers kinds that opt in here, so a kind holding non-Component state (e.g.
	 * {@code StatusModifierSlot}) is never picked as a destination for a Component upgrade.
	 * {@code ComponentUpgradeSlot} and any Component-holding plugin slot return {@code true}.
	 */
	default boolean acceptsComponent() {
		return false;  // Default: a slot does not accept auto-routed Component installs
	}
}
