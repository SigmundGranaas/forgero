package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;

/**
 * A central test fixture providing common objects and builders for Forgero core tests.
 * This reduces boilerplate and improves test readability.
 *
 * IMPORTANT: This class delegates to ForgeroTestFactory for object creation to avoid
 * coupling tests to specific implementations.
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

	// Common Slot Type Identifiers (specific to structure)
	public static final OpenIdentifier TOOL_MATERIAL_ID = idFactory.of("tool_material");
	public static final OpenIdentifier ARMOR_MATERIAL_ID = idFactory.of("armor_material");
	public static final OpenIdentifier PICKAXE_HEAD_SHAPE_ID = idFactory.of("pickaxe_head_shape");
	public static final OpenIdentifier ARMOR_PLATE_SHAPE_ID = idFactory.of("armor_plate_shape");

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


	// Helper Methods - Delegate to ForgeroTestFactory to avoid implementation coupling

	/**
	 * Creates a simple component part with the given tag.
	 * Delegates to ForgeroTestFactory to avoid coupling to specific implementations.
	 */
	public Component material(OpenIdentifier id, OpenIdentifier tag) {
		return part(id).withTag(tag).build();
	}

	/**
	 * Creates a simple schematic component.
	 * Delegates to ForgeroTestFactory to avoid coupling to specific implementations.
	 */
	public Component schematic(OpenIdentifier id) {
		return part(id).build();
	}

	/**
	 * Creates a structure slot.
	 * Delegates to ForgeroTestFactory.
	 */
	public ComponentPart slot(OpenIdentifier id, OpenIdentifier type, Component component) {
		return structureSlot(id.toString(), type, component);
	}

	/**
	 * Creates a simple attribute.
	 */
	public SimpleAttribute attribute(OpenIdentifier type, float value) {
		return new SimpleAttribute(type, value);
	}
}
