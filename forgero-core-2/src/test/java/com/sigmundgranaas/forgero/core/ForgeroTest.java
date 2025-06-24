package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.variant.StaticComponent;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Property;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A central test fixture providing common objects and builders for Forgero core tests.
 * This reduces boilerplate and improves test readability.
 */
public class ForgeroTest {
	public static final IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	// Common Component Identifiers
	public static final OpenIdentifier OAK_ID = idFactory.of("oak");
	public static final OpenIdentifier IRON_ID = idFactory.of("iron");
	public static final OpenIdentifier DIAMOND_ID = idFactory.of("diamond");
	public static final OpenIdentifier PICKAXE_HEAD_ID = idFactory.of("pickaxe_head");
	public static final OpenIdentifier HANDLE_ID = idFactory.of("handle");
	public static final OpenIdentifier PICKAXE_ID = idFactory.of("pickaxe");
	public static final OpenIdentifier SWORD_ID = idFactory.of("sword");
	public static final OpenIdentifier BLADE_ID = idFactory.of("blade");

	// Common Slot Type Identifiers
	public static final OpenIdentifier MATERIAL_ID = idFactory.of("material");
	public static final OpenIdentifier SCHEMATIC_ID = idFactory.of("schematic");

	// Common Slot Identifiers
	public static final OpenIdentifier HEAD_SLOT_ID = idFactory.of("head_slot");
	public static final OpenIdentifier HANDLE_SLOT_ID = idFactory.of("handle_slot");
	public static final OpenIdentifier BINDING_SLOT_ID = idFactory.of("binding_slot");
	public static final OpenIdentifier GEM_SLOT_ID = idFactory.of("gem_slot");


	// Common Tags (for item types)
	public static final OpenIdentifier UNDEAD_TAG = idFactory.of("undead");
	public static final OpenIdentifier WOOD_TAG = idFactory.of("wood");
	public static final OpenIdentifier METAL_TAG = idFactory.of("metal");
	public static final OpenIdentifier GEM_TAG = idFactory.of("gem");


	// Common Slot Type Tags (for slot type validation)
	public static final OpenIdentifier HANDLE_TAG = idFactory.of("handle_slot_type");
	public static final OpenIdentifier PICKAXE_HEAD_TAG = idFactory.of("pickaxe_head_slot_type");
	public static final OpenIdentifier BINDING_TAG = idFactory.of("binding_slot_type");
	public static final OpenIdentifier GEM_SLOT_TYPE_TAG = idFactory.of("gem_slot_type");
	public static final OpenIdentifier BLADE_TAG = idFactory.of("blade_slot_type");


	// Builder Methods
	public StaticComponent part(OpenIdentifier id, Set<OpenIdentifier> tags, List<Property> props) {
		return new StaticComponent(id, tags, props);
	}
	public StaticComponent part(OpenIdentifier id, OpenIdentifier tag, List<Property> props) {
		return new StaticComponent(id, Set.of(tag), props);
	}

	public StaticComponent part(OpenIdentifier id, Set<OpenIdentifier> tags) {
		return part(id, tags, Collections.emptyList());
	}

	public StaticComponent part(OpenIdentifier id, OpenIdentifier tag) {
		return part(id, Set.of(tag), Collections.emptyList());
	}

	public StaticComponent material(OpenIdentifier id, OpenIdentifier tag, List<Property> props) {
		return part(id, Set.of(tag), props);
	}

	public StaticComponent material(OpenIdentifier id, OpenIdentifier tag) {
		return part(id, Set.of(tag), Collections.emptyList());
	}

	public StaticComponent schematic(OpenIdentifier id) {
		return new StaticComponent(id, Collections.emptySet(), Collections.emptyList());
	}

	public StructureSlot slot(OpenIdentifier id, OpenIdentifier type, Component component) {
		return new StructureSlot(id, type, "A slot", component);
	}

	public Attribute attribute(OpenIdentifier type, float value) {
		return new Attribute(type, value);
	}
}
