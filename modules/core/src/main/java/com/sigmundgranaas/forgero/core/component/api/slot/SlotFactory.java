package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Builds a concrete {@link Slot} of a particular <em>kind</em> from a parsed slot definition.
 * <p>
 * The kind ({@code Slot.type()}, e.g. {@code forgero:component_upgrade}) selects which factory runs
 * when a slot is materialised from data. A plugin registers a factory for its kind via the
 * registration context so its slot can be authored directly in a part template; a Component-holding
 * kind then loads, contributes, installs, and persists through the standard component pipeline.
 */
@FunctionalInterface
public interface SlotFactory {

	/**
	 * @param spec    the parsed slot definition (id, category, identity tags, accepted tags)
	 * @param content the component installed in the slot, if any
	 * @return a slot of this factory's kind
	 */
	Slot create(SlotSpec spec, Optional<Component> content);

	/**
	 * The data carried by a slot definition, independent of slot kind. Mirrors the fields a
	 * {@code CofSlot}/upgrade-slot entry provides.
	 *
	 * @param id          unique id of this slot instance
	 * @param slotType    semantic category (matched by {@code in_slot_type}); may be null
	 * @param description translatable description key (never null; empty when unset)
	 * @param tags        the slot's identity tags (its context, etc.)
	 * @param validTags   explicit accepted-content tags; empty/null falls back to {@code slotType}
	 */
	record SlotSpec(
			OpenIdentifier id,
			OpenIdentifier slotType,
			String description,
			Set<OpenIdentifier> tags,
			List<OpenIdentifier> validTags
	) {
	}
}
