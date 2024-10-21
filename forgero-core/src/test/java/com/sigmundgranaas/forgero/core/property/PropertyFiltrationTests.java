package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.type.SimpleType;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class PropertyFiltrationTests {

	@Test
	void rootPropertiesAreUnfiltered() {
		Feature feature = new TestFeature();
		Attribute testAttribute = new TestAttribute();
		CompositeContainer container = new CompositeContainer(List.of(new SimpleContainer(List.of(feature, testAttribute))));

		List<Property> properties = container.getRootProperties();

		Assertions.assertEquals(2, properties.size());
	}

	@Test
	void onlyAttributesAreFilteredIfContextIsAvailable() {
		Feature feature = new TestFeature();
		Attribute testAttribute = new TestAttribute();
		CompositeContainer container = new CompositeContainer(List.of(new SimpleContainer(List.of(feature, testAttribute))));

		List<Property> properties = container.getRootProperties(Matchable.DEFAULT_FALSE, MatchContext.of());

		Assertions.assertEquals(1, properties.size());
	}

	@Test
	void filledSlotsOnlyFiltersAttributes() {
		Feature feature = new TestFeature();
		Attribute testAttribute = new TestAttribute();

		State testState = new SimpleState("test", Type.of("TEST"), List.of(feature, testAttribute));
		FilledSlot slot = new FilledSlot(0, Type.of("TEST"), testState, "", Set.of(Category.ALL));

		List<Property> properties = slot.getRootProperties(Matchable.DEFAULT_FALSE, MatchContext.of());

		Assertions.assertEquals(1, properties.size());
	}

	@Test
	void partsOnlyFiltersAttributes() {
		Feature feature = new TestFeature();
		Attribute testAttribute = new TestAttribute();

		State schematic = new SimpleState("schematic", Type.of("TEST"), List.of(testAttribute));
		State material = new SimpleState("material", Type.of("TEST"), List.of(feature));

		ConstructedSchematicPart part = new ConstructedSchematicPart.SchematicPartBuilder(schematic, material)
				.name("part")
				.nameSpace("test")
				.type(Type.of("TEST"))
				.build();

		List<Property> properties = part.getRootProperties(Matchable.DEFAULT_FALSE, MatchContext.of());

		Assertions.assertEquals(1, properties.size());
	}

	@Test
	void slotsFilterDynamicAttributes() {
		Feature feature = new TestFeature();
		Attribute disabledAttribute = new TestDynamicAttribute(false);
		Attribute activeAttribute = new TestDynamicAttribute(true);

		State testState = new SimpleState("test", Type.of("TEST"), List.of(feature, disabledAttribute, activeAttribute));
		FilledSlot slot = new FilledSlot(0, Type.of("TEST"), testState, "", Set.of(Category.OFFENSIVE));

		List<Property> properties = slot.getRootProperties(Matchable.DEFAULT_FALSE, MatchContext.of());

		// We are expecting the dynamic attributes to be checked in the slot logic,
		// while the dynamic features should always be passed along.
		Assertions.assertEquals(2, properties.size());
	}

	private record TestFeature() implements Feature {

		@Override
		public String type() {
			return "forgero:test";
		}

		@Override
		public boolean applyCondition(Matchable target, MatchContext context) {
			return false;
		}

		@Override
		public boolean isDynamic() {
			return true;
		}
	}

	private record TestAttribute() implements Attribute {

		@Override
		public String getAttributeType() {
			return "test";
		}

		@Override
		public NumericOperation getOperation() {
			return NumericOperation.ADDITION;
		}

		@Override
		public float getValue() {
			return 1;
		}

		@Override
		public Category getCategory() {
			return Category.PASS;
		}

		@Override
		public int getLevel() {
			return 1;
		}

		@Override
		public List<String> targets() {
			return List.of();
		}

		@Override
		public String targetType() {
			return "";
		}

		@Override
		public Context getContext() {
			return Context.of("test");
		}

		@Override
		public int getPriority() {
			return 1;
		}

		@Override
		public String getId() {
			return "test";
		}

		@Override
		public float leveledValue() {
			return 0;
		}

		@Override
		public ComputedAttribute compute() {
			return null;
		}

		@Override
		public String type() {
			return "test";
		}

		@Override
		public boolean applyCondition(Matchable target, MatchContext context) {
			return false;
		}
	}
	private record TestDynamicAttribute(boolean isActive) implements Attribute {

		@Override
		public String getAttributeType() {
			return "test";
		}

		@Override
		public NumericOperation getOperation() {
			return NumericOperation.ADDITION;
		}

		@Override
		public float getValue() {
			return 1;
		}

		@Override
		public Category getCategory() {
			return Category.OFFENSIVE;
		}

		@Override
		public int getLevel() {
			return 1;
		}

		@Override
		public List<String> targets() {
			return List.of();
		}

		@Override
		public String targetType() {
			return "";
		}

		@Override
		public Context getContext() {
			return Context.of("test");
		}

		@Override
		public int getPriority() {
			return 1;
		}

		@Override
		public String getId() {
			return "test";
		}

		@Override
		public float leveledValue() {
			return 0;
		}

		@Override
		public ComputedAttribute compute() {
			return null;
		}

		@Override
		public String type() {
			return "test";
		}

		@Override
		public boolean applyCondition(Matchable target, MatchContext context) {
			return isActive;
		}

		@Override
		public boolean isDynamic() {
			return true;
		}
	}
}
