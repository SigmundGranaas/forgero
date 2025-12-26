package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main entry point for creating test objects using fluent builders.
 * Use with static imports for maximum readability in tests.
 * e.g., import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
 */
public class ForgeroTestFactory {

	// =============================================================================================
	// Component Builders
	// =============================================================================================

	public static PartBuilder part(String id) {
		return new PartBuilder(TestIdentifiers.id(id));
	}

	public static PartBuilder part(OpenIdentifier id) {
		return new PartBuilder(id);
	}

	public static ToolBuilder tool(String id) {
		return new ToolBuilder(TestIdentifiers.id(id));
	}

	public static ToolBuilder tool(OpenIdentifier id) {
		return new ToolBuilder(id);
	}

	// =============================================================================================
	// Attribute Builders
	// =============================================================================================

	public static AttributeBuilder attribute(OpenIdentifier type) {
		return new AttributeBuilder(type);
	}

	// =============================================================================================
	// Slot Builders
	// =============================================================================================

	public static StructureSlot structureSlot(String id, OpenIdentifier type, Component content) {
		return new StructureSlot(TestIdentifiers.id(id), type, "", SlotValidator.ACCEPT_ALL, content);
	}

	public static UpgradeSlot upgradeSlot(String id, OpenIdentifier type) {
		return new UpgradeSlot(TestIdentifiers.id(id), type, "", SlotValidator.ACCEPT_ALL, Optional.empty());
	}

	public static UpgradeSlot upgradeSlot(String id, OpenIdentifier type, Predicate<Component> filter) {
		return new UpgradeSlot(TestIdentifiers.id(id), type, "", SlotValidator.custom(filter), Optional.empty());
	}

	public static UpgradeSlot upgradeSlot(String id, OpenIdentifier type, Predicate<Component> filter, Component content) {
		return new UpgradeSlot(TestIdentifiers.id(id), type, "", SlotValidator.custom(filter), Optional.of(content));
	}

	public static UpgradeSlot upgradeSlot(OpenIdentifier id, OpenIdentifier type, Predicate<Component> filter, Optional<Component> content) {
		return new UpgradeSlot(id, type, "", SlotValidator.custom(filter), content);
	}

	// =============================================================================================
	// Utility Methods
	// =============================================================================================

	/**
	 * Creates a map of slots from varargs, using each slot's ID as the key.
	 */
	public static Map<OpenIdentifier, StructureSlot> slotsMap(StructureSlot... slots) {
		return Arrays.stream(slots)
				.collect(Collectors.toMap(StructureSlot::id, s -> s));
	}

	// =============================================================================================
	// Engine and Service Factories (abstracts implementation details)
	// =============================================================================================

	/**
	 * Creates a Resolver instance for testing.
	 * Abstracts away the concrete implementation.
	 */
	public static Resolver resolver() {
		return new ResolverEngine();
	}

	/**
	 * Creates an AttributeEngine instance for testing.
	 * Abstracts away the concrete implementation.
	 */
	public static AttributeEngine attributeEngine() {
		return new AttributeEngine();
	}

	/**
	 * Creates a ComponentMutater instance for testing.
	 * Abstracts away the concrete implementation.
	 */
	public static ComponentMutater mutater() {
		return new ComponentMutaterImpl();
	}

	// =============================================================================================
	// Attribute Builder
	// =============================================================================================

	public static class AttributeBuilder {
		private final OpenIdentifier type;
		private float value = 0;
		private Condition condition = Condition.ALWAYS_TRUE;

		public AttributeBuilder(OpenIdentifier type) {
			this.type = type;
		}

		public AttributeBuilder withValue(float value) {
			this.value = value;
			return this;
		}

		public AttributeBuilder withCondition(Condition condition) {
			this.condition = condition;
			return this;
		}

		public Attribute build() {
			return new SimpleAttribute(type, value, condition);
		}
	}
}
