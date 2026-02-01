package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory for creating test components that mirror real bow/arrow structures.
 * Encapsulates all the low-level component construction details.
 */
class TestComponentFactory {

	/**
	 * Creates a bow without an equipped arrow.
	 */
	Component createBow() {
		return createBowWithArrow(null);
	}

	/**
	 * Creates a bow with an optional equipped arrow.
	 */
	Component createBowWithArrow(Component equippedArrow) {
		List<ComponentPart> slots = new ArrayList<>();

		// Limb slot
		slots.add(slot("limb", part("forgero:parts/oak-bow_limb")));

		// Equipped arrow slot (optional)
		if (equippedArrow != null) {
			slots.add(slot("equipped-arrow", equippedArrow));
		}

		// String slot
		slots.add(slot("string", part("forgero:parts/string-bow_string")));

		return createStructuredComponent("forgero:equipment/oak-bow", slots, "ranged/bow");
	}

	/**
	 * Creates an arrow with head, shaft, and fletching.
	 */
	Component createArrow() {
		List<ComponentPart> slots = List.of(
			slot("head", materialPart("forgero:iron", "materials/roles/arrow_head_material")),
			slot("shaft", materialPart("forgero:oak", "materials/types/wood")),
			slot("fletching", part("forgero:feather"))
		);

		return createStructuredComponent("forgero:equipment/iron-arrow", slots, "ranged/arrow");
	}

	/**
	 * Creates a layered tool with slots at different render orders (1, 10, 100).
	 * Used for testing rendering order behavior.
	 */
	Component createLayeredTool() {
		List<ComponentPart> slots = List.of(
			slot("base", part("forgero:parts/test-base")),
			slot("overlay", part("forgero:parts/test-overlay")),
			slot("gem", part("forgero:parts/test-gem"))
		);

		return createStructuredComponent("forgero:equipment/test-layered_tool", slots, "tool");
	}

	/**
	 * Creates a tool with a background layer (negative order -10).
	 * Used for testing negative order rendering.
	 */
	Component createToolWithBackground() {
		List<ComponentPart> slots = List.of(
			slot("background", part("forgero:parts/test-background")),
			slot("normal", part("forgero:parts/test-normal"))
		);

		return createStructuredComponent("forgero:equipment/test-tool_with_background", slots, "tool");
	}

	/**
	 * Creates a component with two slots having equal order values.
	 * Used for testing deterministic tie-breaking.
	 */
	Component createComponentWithEqualOrders() {
		List<ComponentPart> slots = List.of(
			slot("slot_a", part("forgero:parts/test-slot_a")),
			slot("slot_b", part("forgero:parts/test-slot_b"))
		);

		return createStructuredComponent("forgero:equipment/test-equal_orders", slots, "tool");
	}

	/**
	 * Creates an arrow with a contextual shaft that should use "arrow_shaft" context.
	 * Used for testing contextual model selection.
	 */
	Component createArrowWithContextualShaft() {
		List<ComponentPart> slots = List.of(
			slot("head", materialPart("forgero:iron", "materials/roles/arrow_head_material")),
			slot("shaft", materialPart("forgero:oak", "materials/types/wood")),
			slot("fletching", part("forgero:feather"))
		);

		return createStructuredComponent("forgero:equipment/test-contextual_arrow", slots, "ranged/arrow");
	}

	/**
	 * Creates a tool with contextual material that should NOT match arrow_shaft context.
	 * Used for testing context mismatch (should use default model).
	 */
	Component createToolWithContextualMaterial() {
		List<ComponentPart> slots = List.of(
			slot("head", materialPart("forgero:oak", "materials/types/wood"))
		);

		return createStructuredComponent("forgero:equipment/test-contextual_tool", slots, "tool");
	}

	/**
	 * Creates a tool with material that has multiple context options.
	 * Used for testing that correct context is selected from multiple options.
	 */
	Component createToolWithMultiContextMaterial() {
		List<ComponentPart> slots = List.of(
			slot("handle", materialPart("forgero:oak", "materials/types/wood"))
		);

		return createStructuredComponent("forgero:equipment/test-multi_context_tool", slots, "tool");
	}

	/**
	 * Creates a component with an ID that has no registered model.
	 * Used for testing missing model handling.
	 */
	Component createUnregisteredComponent() {
		return new StaticComponent(
			OpenIdentifier.parse("forgero:unregistered_component_no_model"),
			Set.of(),
			new HashMap<>()
		);
	}

	/**
	 * Creates a composite component with a slot referencing a missing child.
	 * Used for testing handling of missing slot components.
	 */
	Component createComponentWithMissingChild() {
		List<ComponentPart> slots = new ArrayList<>();
		// Valid slot
		slots.add(slot("base", part("forgero:parts/test-base")));
		// Missing child (component exists but has no model)
		slots.add(slot("missing", createUnregisteredComponent()));

		return createStructuredComponent("forgero:equipment/test-layered_tool", slots, "tool");
	}

	/**
	 * Creates a component that references a mount point that doesn't exist.
	 * Used for testing missing mount point handling.
	 */
	Component createComponentWithMissingMount() {
		// Uses test-pickaxe model which has mount points defined,
		// but the child will be placed in a slot that references non-existent mount
		List<ComponentPart> slots = List.of(
			slot("head", part("forgero:parts/test-pickaxe_head"))
		);

		return createStructuredComponent("forgero:equipment/test-pickaxe", slots, "tool");
	}

	/**
	 * Creates a component with empty structure.
	 * Used for testing edge case handling.
	 */
	Component createEmptyComponent() {
		return createStructuredComponent("forgero:equipment/test-layered_tool", List.of(), "tool");
	}

	/**
	 * Creates a deeply nested component hierarchy.
	 * Used for testing stack overflow prevention.
	 */
	Component createDeeplyNestedComponent(int depth) {
		if (depth <= 0) {
			return part("forgero:parts/test-base");
		}

		Component child = createDeeplyNestedComponent(depth - 1);
		List<ComponentPart> slots = List.of(slot("child", child));

		return createStructuredComponent("forgero:equipment/test-layered_tool", slots, "tool");
	}

	/**
	 * Creates a pickaxe with oak handle.
	 * Used for testing equipment-type-specific handle textures.
	 */
	Component createPickaxeWithOakHandle() {
		List<ComponentPart> slots = List.of(
			slot("head", part("forgero:parts/iron-pickaxe_head")),
			slot("handle", materialPart("forgero:oak", "materials/types/wood")),
			slot("binding", part("forgero:parts/leather-binding"))
		);

		return createStructuredComponent("forgero:equipment/test-pickaxe_with_context", slots, "tool/pickaxe");
	}

	/**
	 * Creates a sword with oak handle.
	 * Used for testing equipment-type-specific handle textures.
	 */
	Component createSwordWithOakHandle() {
		List<ComponentPart> slots = List.of(
			slot("blade", part("forgero:parts/iron-sword_blade")),
			slot("handle", materialPart("forgero:oak", "materials/types/wood")),
			slot("guard", part("forgero:parts/iron-sword_guard"))
		);

		return createStructuredComponent("forgero:equipment/test-sword_with_context", slots, "tool/sword");
	}

	/**
	 * Creates a tool with three gem slots, each containing a different gem.
	 * Used for testing multi-gem equipment.
	 */
	Component createToolWithThreeGems() {
		Component diamond = materialPart("forgero:diamond", "upgrades/types/gem");
		Component emerald = materialPart("forgero:emerald", "upgrades/types/gem");
		Component ruby = materialPart("forgero:ruby", "upgrades/types/gem");

		List<ComponentPart> slots = List.of(
			slot("base", part("forgero:parts/test-base")),
			slot("gem_slot_1", diamond),
			slot("gem_slot_2", emerald),
			slot("gem_slot_3", ruby)
		);

		return createStructuredComponent("forgero:equipment/test-multi_gem_tool", slots, "tool");
	}

	/**
	 * Creates a pickaxe with a leather binding that has a diamond gem.
	 * Used for testing binding-in-pickaxe context.
	 */
	Component createPickaxeWithBindingAndGem() {
		Component gem = materialPart("forgero:diamond", "upgrades/types/gem");

		// Binding with gem slot
		List<ComponentPart> bindingSlots = List.of(slot("gem_slot", gem));
		Component binding = createStructuredComponent(
			"forgero:parts/leather-binding",
			bindingSlots,
			"parts/binding"
		);

		// Pickaxe with binding
		List<ComponentPart> pickaxeSlots = List.of(
			slot("head", part("forgero:parts/iron-pickaxe_head")),
			slot("handle", part("forgero:parts/oak-handle")),
			slot("binding", binding)
		);

		return createStructuredComponent("forgero:equipment/test-pickaxe_with_context", pickaxeSlots, "tool/pickaxe");
	}

	/**
	 * Creates an axe with a leather binding that has a diamond gem.
	 * Used for testing binding-in-axe context.
	 */
	Component createAxeWithBindingAndGem() {
		Component gem = materialPart("forgero:diamond", "upgrades/types/gem");

		// Binding with gem slot
		List<ComponentPart> bindingSlots = List.of(slot("gem_slot", gem));
		Component binding = createStructuredComponent(
			"forgero:parts/leather-binding",
			bindingSlots,
			"parts/binding"
		);

		// Axe with binding
		List<ComponentPart> axeSlots = List.of(
			slot("head", part("forgero:parts/iron-axe_head")),
			slot("handle", part("forgero:parts/oak-handle")),
			slot("binding", binding)
		);

		return createStructuredComponent("forgero:equipment/test-axe_with_context", axeSlots, "tool/axe");
	}

	// ==================== Low-Level Construction ====================

	private ComponentPart slot(String id, Component content) {
		return new ComponentPart(
			OpenIdentifier.parse("forgero:" + id),
			OpenIdentifier.parse("forgero:slot_type"),
			"description",
			SlotValidator.ACCEPT_ALL,
			content
		);
	}

	private Component part(String id) {
		return new StaticComponent(
			OpenIdentifier.parse(id),
			Set.of(),
			new HashMap<>()
		);
	}

	private Component materialPart(String id, String tag) {
		return new StaticComponent(
			OpenIdentifier.parse(id),
			Set.of(OpenIdentifier.parse("forgero:" + tag)),
			new HashMap<>()
		);
	}

	/**
	 * Creates a structured component with custom slots and tags.
	 * Useful for creating test components with specific structures.
	 */
	StructuredComponent createStructuredComponent(String id, List<ComponentPart> slots, String... tags) {
		Set<OpenIdentifier> tagSet = Arrays.stream(tags)
			.map(tag -> OpenIdentifier.of("forgero", tag))
			.collect(Collectors.toSet());

		return new MockStructuredComponent(
			OpenIdentifier.parse(id),
			tagSet,
			ComponentStructure.of(slots)
		);
	}

	/**
	 * Minimal mock for testing - only implements required methods.
	 */
	private record MockStructuredComponent(
		OpenIdentifier id,
		Set<OpenIdentifier> tags,
		ComponentStructure structure
	) implements StructuredComponent {

		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new MockStructuredComponent(id, tags, newStructure);
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Collections.emptyMap();
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ComponentTypeRegistry.STRUCTURED_EQUIPMENT;
		}
	}
}
