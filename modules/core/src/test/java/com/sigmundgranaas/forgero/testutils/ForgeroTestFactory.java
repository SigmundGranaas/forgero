package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

/**
 * Main entry point for creating test objects using fluent builders.
 * Use with static imports for maximum readability in tests.
 * e.g., import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
 */
public class ForgeroTestFactory {

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

	public static AttributeBuilder attribute(OpenIdentifier type) {
		return new AttributeBuilder(type);
	}

	public static StructureSlot structureSlot(String id, OpenIdentifier type, Component content) {
		return new StructureSlot(TestIdentifiers.id(id), type, "", content);
	}

	public static UpgradeSlot upgradeSlot(String id, OpenIdentifier type) {
		return new UpgradeSlot(TestIdentifiers.id(id), type, "", (comp) -> true, Optional.empty());
	}

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
