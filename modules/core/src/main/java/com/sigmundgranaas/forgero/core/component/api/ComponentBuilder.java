package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;

import java.util.*;

/**
 * Fluent builder for constructing components step by step.
 * <p>
 * Example:
 * <pre>{@code
 * Component pickaxe = new ComponentBuilder(OpenIdentifier.of("diamond_pickaxe"))
 *     .tag(Tags.EQUIPMENT)
 *     .tag(Tags.PICKAXE)
 *     .structureSlot("head", Tags.PICKAXE_HEAD, headComponent)
 *     .structureSlot("handle", Tags.HANDLE, handleComponent)
 *     .upgradeSlot("binding", Tags.BINDING, "Optional binding slot")
 *     .property(PropertyKeys.ATTRIBUTES, List.of(durabilityAttr))
 *     .asEquipment()
 *     .build();
 * }</pre>
 */
public final class ComponentBuilder {

	private final OpenIdentifier id;
	private final Set<OpenIdentifier> tags = new LinkedHashSet<>();
	private final Map<String, List<Object>> properties = new LinkedHashMap<>();
	private final List<ComponentPart> structureSlots = new ArrayList<>();
	private final List<ComponentUpgradeSlot> upgradeSlots = new ArrayList<>();
	private boolean isEquipment = false;

	/**
	 * Creates a new builder with the given identifier.
	 */
	public ComponentBuilder(OpenIdentifier id) {
		this.id = Objects.requireNonNull(id, "id cannot be null");
	}

	/**
	 * Creates a new builder, parsing the identifier from a string.
	 */
	public ComponentBuilder(String id) {
		this(OpenIdentifier.parse(id));
	}

	/**
	 * Creates a new builder for a component in the default namespace.
	 */
	public static ComponentBuilder of(String path) {
		return new ComponentBuilder(OpenIdentifier.of(path));
	}

	// === Tags ===

	/**
	 * Adds a tag to the component.
	 */
	public ComponentBuilder tag(OpenIdentifier tag) {
		tags.add(tag);
		return this;
	}

	/**
	 * Adds a tag by parsing a string identifier.
	 */
	public ComponentBuilder tag(String tag) {
		tags.add(OpenIdentifier.parse(tag));
		return this;
	}

	/**
	 * Adds multiple tags.
	 */
	public ComponentBuilder tags(Collection<OpenIdentifier> tags) {
		this.tags.addAll(tags);
		return this;
	}

	/**
	 * Adds multiple tags by parsing string identifiers.
	 */
	public ComponentBuilder tagsFromStrings(Collection<String> tags) {
		tags.forEach(t -> this.tags.add(OpenIdentifier.parse(t)));
		return this;
	}

	// === Properties ===

	/**
	 * Adds properties under the given key. If properties already exist for this key,
	 * the new values are appended.
	 */
	public ComponentBuilder property(String key, List<?> values) {
		properties.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
		return this;
	}

	/**
	 * Adds a single property value under the given key.
	 */
	public ComponentBuilder property(String key, Object value) {
		return property(key, List.of(value));
	}

	/**
	 * Merges all properties from the given map.
	 */
	public ComponentBuilder properties(Map<String, List<?>> props) {
		props.forEach((key, values) -> property(key, values));
		return this;
	}

	// === Structure Slots ===

	/**
	 * Adds a structure slot with type-based validation.
	 */
	public ComponentBuilder structureSlot(OpenIdentifier slotId, OpenIdentifier type,
	                                       String description, Component content) {
		structureSlots.add(ComponentPart.ofType(slotId, type, description, content));
		return this;
	}

	/**
	 * Adds a structure slot with a custom validator.
	 */
	public ComponentBuilder structureSlot(OpenIdentifier slotId, OpenIdentifier type,
	                                       String description, SlotValidator validator, Component content) {
		structureSlots.add(ComponentPart.withValidator(slotId, type, description, validator, content));
		return this;
	}

	/**
	 * Convenience: adds a structure slot using string IDs.
	 * The slot ID is also used as the description.
	 */
	public ComponentBuilder structureSlot(String slotId, String type, Component content) {
		return structureSlot(
				OpenIdentifier.of(slotId),
				OpenIdentifier.of(type),
				slotId,
				content
		);
	}

	/**
	 * Adds a pre-built structure slot.
	 */
	public ComponentBuilder structureSlot(ComponentPart slot) {
		structureSlots.add(slot);
		return this;
	}

	// === Upgrade Slots ===

	/**
	 * Adds an empty upgrade slot with type-based validation.
	 */
	public ComponentBuilder upgradeSlot(OpenIdentifier slotId, OpenIdentifier type, String description) {
		upgradeSlots.add(ComponentUpgradeSlot.emptyOfType(slotId, type, description));
		return this;
	}

	/**
	 * Adds an empty upgrade slot with a custom validator.
	 */
	public ComponentBuilder upgradeSlot(OpenIdentifier slotId, OpenIdentifier type,
	                                     String description, SlotValidator validator) {
		upgradeSlots.add(ComponentUpgradeSlot.emptyWithValidator(slotId, type, description, validator));
		return this;
	}

	/**
	 * Convenience: adds an empty upgrade slot using string IDs.
	 */
	public ComponentBuilder upgradeSlot(String slotId, String type, String description) {
		return upgradeSlot(OpenIdentifier.of(slotId), OpenIdentifier.of(type), description);
	}

	/**
	 * Adds a filled upgrade slot with type-based validation.
	 */
	public ComponentBuilder filledUpgradeSlot(OpenIdentifier slotId, OpenIdentifier type,
	                                           String description, Component content) {
		upgradeSlots.add(ComponentUpgradeSlot.filledOfType(slotId, type, description, content));
		return this;
	}

	/**
	 * Adds a pre-built upgrade slot.
	 */
	public ComponentBuilder upgradeSlot(ComponentUpgradeSlot slot) {
		upgradeSlots.add(slot);
		return this;
	}

	// === Type Selection ===

	/**
	 * Marks this component as equipment.
	 */
	public ComponentBuilder asEquipment() {
		this.isEquipment = true;
		return this;
	}

	/**
	 * Marks this component as a part (not equipment).
	 */
	public ComponentBuilder asPart() {
		this.isEquipment = false;
		return this;
	}

	// === Build ===

	/**
	 * Builds the component, selecting the appropriate implementation type
	 * based on whether structure and/or upgrades were added.
	 *
	 * @return The constructed component
	 */
	public Component build() {
		ComponentStructure structure = structureSlots.isEmpty()
				? null
				: ComponentStructure.of(structureSlots);

		ComponentUpgrades upgrades = upgradeSlots.isEmpty()
				? null
				: ComponentUpgrades.of(upgradeSlots);

		Map<String, List<?>> immutableProperties = new LinkedHashMap<>();
		properties.forEach((k, v) -> immutableProperties.put(k, List.copyOf(v)));

		return ComponentFactory.create(
				id,
				Set.copyOf(tags),
				Map.copyOf(immutableProperties),
				structure,
				upgrades,
				isEquipment
		);
	}

	/**
	 * Returns the ID this builder will use.
	 */
	public OpenIdentifier getId() {
		return id;
	}
}
