package com.sigmundgranaas.forgero.cof.dto;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The canonical Data Transfer Object for a Forgero component.
 * This is the single, unified intermediate format for all processed component definitions
 * before they are built into runtime objects. It is also used for serialization.
 *
 * @param id             The unique identifier of the component.
 * @param componentType  A string identifier for the component's concrete class (e.g., "forgero:static_part").
 * @param tags           The complete set of tags for the component.
 * @param properties     A map of fully parsed, runtime-typed properties (e.g., key "forgero:attributes" -> value List<Attribute>).
 * @param structure      The component's structure, if it has one.
 * @param upgrades       The component's upgrade slots, if it has any.
 * @param cofVersion     The version of the COF schema used for serialization.
 */
public record CofComponent(
		OpenIdentifier id,
		OpenIdentifier componentType,
		Optional<Set<OpenIdentifier>> tags,
		Optional<Map<String, List<?>>> properties,
		Optional<CofStructure> structure,
		Optional<CofUpgrades> upgrades,
		Optional<Integer> cofVersion
) {
}
