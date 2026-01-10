package com.sigmundgranaas.forgero.model.config;

import com.sigmundgranaas.forgero.cof.ComponentTypeRegistry;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fluent builder for creating test Component instances.
 * Used in model configuration validation tests to create Components without
 * requiring the full data loading pipeline.
 */
public class TestComponentBuilder {

	private final String id;
	private final Set<String> tags = new HashSet<>();
	private final Map<String, TestComponent> parts = new LinkedHashMap<>();
	private final Map<String, TestComponent> upgrades = new LinkedHashMap<>();

	private TestComponentBuilder(String id) {
		this.id = id;
	}

	public static TestComponentBuilder create(String id) {
		return new TestComponentBuilder(id);
	}

	public static TestComponent material(String name) {
		return create("forgero:" + name)
				.withTags("forgero:materials/roles/tool_material")
				.buildSimple();
	}

	public static TestComponent simpleHead(String material, String headType) {
		return create("forgero:parts/" + material + "-" + headType)
				.withTags("forgero:parts/head", "forgero:parts/types/" + headType, "forgero:parts/categories/head")
				.withPart("material", material(material))
				.buildPart();
	}

	public static TestComponent simpleHandle(String material) {
		return create("forgero:parts/" + material + "-handle")
				.withTags("forgero:parts/handle", "forgero:parts/categories/handle")
				.withPart("material", material(material))
				.buildPart();
	}

	public static TestComponent simpleBinding(String material) {
		return create("forgero:parts/" + material + "-binding")
				.withTags("forgero:parts/binding")
				.withPart("material", material(material))
				.buildPart();
	}

	public static TestComponent simpleBlade(String material, String bladeType) {
		return create("forgero:parts/" + material + "-" + bladeType)
				.withTags("forgero:parts/blade", "forgero:parts/types/" + bladeType)
				.withPart("material", material(material))
				.buildPart();
	}

	public TestComponentBuilder withTags(String... tags) {
		this.tags.addAll(Arrays.asList(tags));
		return this;
	}

	public TestComponentBuilder withPart(String slotName, TestComponent part) {
		this.parts.put(slotName, part);
		return this;
	}

	public TestComponentBuilder withUpgrade(String slotName, TestComponent upgrade) {
		this.upgrades.put(slotName, upgrade);
		return this;
	}

	public TestComponent buildSimple() {
		return new SimpleTestComponent(
				OpenIdentifier.parse(id),
				tags.stream().map(OpenIdentifier::parse).collect(Collectors.toSet())
		);
	}

	public TestComponent buildPart() {
		List<ComponentPart> componentParts = parts.entrySet().stream()
				.map(e -> new ComponentPart(
						OpenIdentifier.parse("forgero:" + e.getKey()),
						e.getValue().id(),
						"",
						SlotValidator.ACCEPT_ALL,
						e.getValue()
				))
				.toList();

		ComponentUpgrades componentUpgrades = createUpgrades();

		return new StructuredTestComponent(
				OpenIdentifier.parse(id),
				tags.stream().map(OpenIdentifier::parse).collect(Collectors.toSet()),
				ComponentStructure.of(componentParts),
				componentUpgrades
		);
	}

	public TestComponent buildEquipment() {
		List<ComponentPart> componentParts = parts.entrySet().stream()
				.map(e -> new ComponentPart(
						OpenIdentifier.parse("forgero:" + e.getKey()),
						e.getValue().id(),
						"",
						SlotValidator.ACCEPT_ALL,
						e.getValue()
				))
				.toList();

		ComponentUpgrades componentUpgrades = createUpgrades();

		return new EquipmentTestComponent(
				OpenIdentifier.parse(id),
				tags.stream().map(OpenIdentifier::parse).collect(Collectors.toSet()),
				ComponentStructure.of(componentParts),
				componentUpgrades
		);
	}

	/**
	 * Creates ComponentUpgrades from the builder's upgrade map.
	 */
	private ComponentUpgrades createUpgrades() {
		if (upgrades.isEmpty()) {
			return ComponentUpgrades.empty();
		}

		List<ComponentUpgradeSlot> slots = upgrades.entrySet().stream()
				.map(e -> {
					OpenIdentifier slotId = OpenIdentifier.parse("forgero:" + e.getKey());
					OpenIdentifier slotType = OpenIdentifier.parse("forgero:upgrade");
					Component content = e.getValue();
					return ComponentUpgradeSlot.filled(slotId, slotType, "", SlotValidator.ACCEPT_ALL, content);
				})
				.toList();

		return ComponentUpgrades.of(slots);
	}

	/**
	 * Marker interface for test components.
	 */
	public interface TestComponent extends Component {}

	/**
	 * Simple component with no structure or upgrades.
	 * Used for materials and other leaf-level components.
	 */
	private record SimpleTestComponent(
			OpenIdentifier id,
			Set<OpenIdentifier> tags
	) implements TestComponent {
		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ComponentTypeRegistry.STATIC_COMPONENT;
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return new HashMap<>();
		}
	}

	/**
	 * Structured component with parts and optional upgrades.
	 * Used for parts like heads and handles.
	 */
	private record StructuredTestComponent(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			ComponentStructure structure,
			ComponentUpgrades componentUpgrades
	) implements TestComponent, StructuredComponent, CustomizableComponent {
		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ComponentTypeRegistry.STRUCTURED_PART;
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return new HashMap<>();
		}

		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new StructuredTestComponent(id, tags, newStructure, componentUpgrades);
		}

		@Override
		public ComponentUpgrades upgrades() {
			return componentUpgrades;
		}

		@Override
		public Component withUpgrades(ComponentUpgrades newUpgrades) {
			return new StructuredTestComponent(id, tags, structure, newUpgrades);
		}
	}

	/**
	 * Equipment component with parts and optional upgrades.
	 * Used for assembled tools and weapons.
	 */
	private record EquipmentTestComponent(
			OpenIdentifier id,
			Set<OpenIdentifier> tags,
			ComponentStructure structure,
			ComponentUpgrades componentUpgrades
	) implements TestComponent, StructuredComponent, CustomizableComponent {
		@Override
		public Set<OpenIdentifier> getTags() {
			return tags;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ComponentTypeRegistry.STRUCTURED_EQUIPMENT;
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return new HashMap<>();
		}

		@Override
		public Component withStructure(ComponentStructure newStructure) {
			return new EquipmentTestComponent(id, tags, newStructure, componentUpgrades);
		}

		@Override
		public ComponentUpgrades upgrades() {
			return componentUpgrades;
		}

		@Override
		public Component withUpgrades(ComponentUpgrades newUpgrades) {
			return new EquipmentTestComponent(id, tags, structure, newUpgrades);
		}
	}
}
