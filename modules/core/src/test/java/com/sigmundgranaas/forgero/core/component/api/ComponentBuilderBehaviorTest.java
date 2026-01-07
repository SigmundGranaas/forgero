package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComponentBuilder Behavior")
class ComponentBuilderBehaviorTest {

	@Nested
	@DisplayName("Basic Construction")
	class BasicConstruction {

		@Test
		@DisplayName("builds static component with id only")
		void buildsStaticComponentWithIdOnly() {
			Component component = new ComponentBuilder("forgero:iron").build();

			assertEquals(OpenIdentifier.parse("forgero:iron"), component.id());
			assertTrue(component.getTags().isEmpty());
		}

		@Test
		@DisplayName("builds component with tags")
		void buildsComponentWithTags() {
			Component component = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.tag("forgero:material")
					.build();

			Set<OpenIdentifier> tags = component.getTags();
			assertEquals(2, tags.size());
			assertTrue(tags.contains(OpenIdentifier.parse("forgero:metal")));
			assertTrue(tags.contains(OpenIdentifier.parse("forgero:material")));
		}

		@Test
		@DisplayName("deduplicates identical tags")
		void deduplicatesIdenticalTags() {
			Component component = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.tag("forgero:metal")
					.tag("forgero:metal")
					.build();

			assertEquals(1, component.getTags().size());
		}

		@Test
		@DisplayName("builds component with properties")
		void buildsComponentWithProperties() {
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);

			Component component = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), List.of(durability))
					.build();

			List<Attribute> attributes = component.properties(Attribute.KEY);
			assertEquals(1, attributes.size());
			assertEquals(100f, ((SimpleAttribute) attributes.get(0)).value());
		}

		@Test
		@DisplayName("accumulates properties under same key")
		void accumulatesPropertiesUnderSameKey() {
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);
			SimpleAttribute damage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f);

			Component component = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), durability)
					.property(Attribute.KEY.key(), damage)
					.build();

			List<Attribute> attributes = component.properties(Attribute.KEY);
			assertEquals(2, attributes.size());
		}
	}

	@Nested
	@DisplayName("Type Inference")
	class TypeInference {

		@Test
		@DisplayName("infers StaticComponent when no structure or upgrades")
		void infersStaticComponentWhenNoStructureOrUpgrades() {
			Component component = new ComponentBuilder("forgero:iron")
					.tag("forgero:material")
					.build();

			assertFalse(component instanceof StructuredComponent);
			assertFalse(component instanceof CustomizableComponent);
		}

		@Test
		@DisplayName("infers StructuredComponent when structure slots added")
		void infersStructuredComponentWhenStructureSlotsAdded() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot("material", "tool_material", material)
					.build();

			assertInstanceOf(StructuredComponent.class, part);
			StructuredComponent structured = (StructuredComponent) part;
			assertEquals(1, structured.structure().allParts().size());
		}

		@Test
		@DisplayName("infers CustomizableComponent when upgrade slots added")
		void infersCustomizableComponentWhenUpgradeSlotsAdded() {
			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.upgradeSlot("gem_slot", "gem", "Socket for gems")
					.build();

			assertInstanceOf(CustomizableComponent.class, part);
			CustomizableComponent customizable = (CustomizableComponent) part;
			assertEquals(1, customizable.upgrades().slots().all().size());
		}

		@Test
		@DisplayName("infers StructuredCustomizable when both structure and upgrades added")
		void infersStructuredCustomizableWhenBothAdded() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot("material", "tool_material", material)
					.upgradeSlot("gem_slot", "gem", "Socket for gems")
					.build();

			assertInstanceOf(StructuredComponent.class, part);
			assertInstanceOf(CustomizableComponent.class, part);
		}

		@Test
		@DisplayName("asEquipment flag affects type selection")
		void asEquipmentFlagAffectsTypeSelection() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:pickaxe_head")
					.build();

			Component equipment = new ComponentBuilder("forgero:pickaxe")
					.structureSlot("head", "pickaxe_head", material)
					.asEquipment()
					.build();

			assertInstanceOf(StructuredComponent.class, equipment);
			assertEquals("forgero:structured_equipment", equipment.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("asPart explicitly sets part type")
		void asPartExplicitlySetsPartType() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot("material", "tool_material", material)
					.asPart()
					.build();

			assertEquals("forgero:structured_part", part.getTypeIdentifier().toString());
		}
	}

	@Nested
	@DisplayName("Structure Slot Construction")
	class StructureSlotConstruction {

		@Test
		@DisplayName("adds structure slot with OpenIdentifier parameters")
		void addsStructureSlotWithOpenIdentifierParameters() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot(
							OpenIdentifier.of("material_slot"),
							OpenIdentifier.of("tool_material"),
							"Material for the head",
							material
					)
					.build();

			StructuredComponent structured = (StructuredComponent) part;
			Optional<ComponentPart> slotPart = structured.structure().getPart(OpenIdentifier.of("material_slot"));
			assertTrue(slotPart.isPresent());
			assertEquals("forgero:iron", slotPart.get().getContent().id().toString());
		}

		@Test
		@DisplayName("adds structure slot with custom validator")
		void addsStructureSlotWithCustomValidator() {
			Component iron = new ComponentBuilder("forgero:iron").tag("forgero:metal").build();

			SlotValidator metalOnly = SlotValidator.custom(c -> c.getTags().contains(OpenIdentifier.parse("forgero:metal")));

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot(
							OpenIdentifier.of("material_slot"),
							OpenIdentifier.of("tool_material"),
							"Metal material only",
							metalOnly,
							iron
					)
					.build();

			StructuredComponent structured = (StructuredComponent) part;
			assertEquals(1, structured.structure().allParts().size());
		}

		@Test
		@DisplayName("adds pre-built ComponentPart")
		void addsPreBuiltComponentPart() {
			Component material = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();
			ComponentPart slot = ComponentPart.ofType(
					OpenIdentifier.of("material"),
					OpenIdentifier.of("tool_material"),
					"Material slot",
					material
			);

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot(slot)
					.build();

			StructuredComponent structured = (StructuredComponent) part;
			assertEquals(1, structured.structure().allParts().size());
		}

		@Test
		@DisplayName("multiple structure slots create complex hierarchy")
		void multipleStructureSlotsCreateComplexHierarchy() {
			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();
			Component oak = new ComponentBuilder("forgero:oak")
					.tag("forgero:tool_material")
					.build();
			Component shape = new ComponentBuilder("forgero:pickaxe_shape")
					.tag("forgero:pickaxe_shape")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot("primary_material", "tool_material", iron)
					.structureSlot("secondary_material", "tool_material", oak)
					.structureSlot("shape", "pickaxe_shape", shape)
					.build();

			StructuredComponent structured = (StructuredComponent) part;
			assertEquals(3, structured.structure().allParts().size());
		}
	}

	@Nested
	@DisplayName("Upgrade Slot Construction")
	class UpgradeSlotConstruction {

		@Test
		@DisplayName("adds empty upgrade slot with type validation")
		void addsEmptyUpgradeSlotWithTypeValidation() {
			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.upgradeSlot(
							OpenIdentifier.of("gem_slot"),
							OpenIdentifier.of("gem"),
							"Socket for gems"
					)
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			assertEquals(1, customizable.upgrades().slots().all().size());
			assertEquals(0, customizable.upgrades().filledCount());
		}

		@Test
		@DisplayName("adds upgrade slot with custom validator")
		void addsUpgradeSlotWithCustomValidator() {
			SlotValidator gemOnly = SlotValidator.custom(c -> c.getTags().contains(OpenIdentifier.parse("forgero:gem")));

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.upgradeSlot(
							OpenIdentifier.of("gem_slot"),
							OpenIdentifier.of("gem"),
							"Gem socket",
							gemOnly
					)
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			assertEquals(1, customizable.upgrades().slots().all().size());
		}

		@Test
		@DisplayName("adds filled upgrade slot")
		void addsFilledUpgradeSlot() {
			Component gem = new ComponentBuilder("forgero:diamond_gem").tag("forgero:gem").build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.filledUpgradeSlot(
							OpenIdentifier.of("gem_slot"),
							OpenIdentifier.of("gem"),
							"Gem socket",
							gem
					)
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			long filledCount = customizable.upgrades().slots().all().stream()
					.filter(slot -> slot instanceof ComponentUpgradeSlot)
					.map(slot -> (ComponentUpgradeSlot) slot)
					.filter(slot -> slot.getContent().isPresent())
					.count();
			assertEquals(1, filledCount);
		}

		@Test
		@DisplayName("adds pre-built ComponentUpgradeSlot")
		void addsPreBuiltComponentUpgradeSlot() {
			ComponentUpgradeSlot slot = ComponentUpgradeSlot.emptyOfType(
					OpenIdentifier.of("gem_slot"),
					OpenIdentifier.of("gem"),
					"Gem socket"
			);

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.upgradeSlot(slot)
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			assertEquals(1, customizable.upgrades().slots().all().size());
		}

		@Test
		@DisplayName("multiple upgrade slots are all accessible")
		void multipleUpgradeSlotsAreAllAccessible() {
			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.upgradeSlot("gem_slot_1", "gem", "First gem socket")
					.upgradeSlot("gem_slot_2", "gem", "Second gem socket")
					.upgradeSlot("rune_slot", "rune", "Rune socket")
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			assertEquals(3, customizable.upgrades().slots().all().size());
		}
	}

	@Nested
	@DisplayName("Static Factory Method")
	class StaticFactoryMethod {

		@Test
		@DisplayName("ComponentBuilder.of creates builder with default namespace")
		void ofCreatesBuilderWithDefaultNamespace() {
			Component component = ComponentBuilder.of("iron").build();

			assertEquals("forgero:iron", component.id().toString());
		}
	}

	@Nested
	@DisplayName("Component Navigation")
	class ComponentNavigation {

		@Test
		@DisplayName("getChildren returns all nested components")
		void getChildrenReturnsAllNestedComponents() {
			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();
			Component oak = new ComponentBuilder("forgero:oak")
					.tag("forgero:tool_material")
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot("head_material", "tool_material", iron)
					.structureSlot("handle_material", "tool_material", oak)
					.build();

			List<Component> children = tool.getChildren();
			assertEquals(2, children.size());
		}

		@Test
		@DisplayName("structure children returns structure parts for StructuredComponent")
		void structureChildrenReturnsStructurePartsForStructuredComponent() {
			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot("material", "tool_material", iron)
					.build();

			StructuredComponent structured = (StructuredComponent) part;
			List<Component> children = structured.structure().children();
			assertEquals(1, children.size());
			assertEquals("forgero:iron", children.get(0).id().toString());
		}

		@Test
		@DisplayName("filled upgrade slots contain installed content")
		void filledUpgradeSlotsContainInstalledContent() {
			Component gem = new ComponentBuilder("forgero:diamond_gem")
					.tag("forgero:gem")
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.filledUpgradeSlot(OpenIdentifier.of("gem_slot"), OpenIdentifier.of("gem"), "Gem", gem)
					.build();

			CustomizableComponent customizable = (CustomizableComponent) part;
			List<Component> upgrades = customizable.upgrades().slots().all().stream()
					.filter(slot -> slot instanceof ComponentUpgradeSlot)
					.map(slot -> (ComponentUpgradeSlot) slot)
					.filter(slot -> slot.getContent().isPresent())
					.map(slot -> slot.getContent().get())
					.toList();
			assertEquals(1, upgrades.size());
			assertEquals("forgero:diamond_gem", upgrades.get(0).id().toString());
		}
	}

	@Nested
	@DisplayName("Immutability")
	class Immutability {

		@Test
		@DisplayName("built component is immutable - tags cannot be modified")
		void builtComponentIsImmutableTagsCannotBeModified() {
			Component component = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.build();

			Set<OpenIdentifier> tags = component.getTags();
			assertThrows(UnsupportedOperationException.class, () -> tags.add(OpenIdentifier.of("new_tag")));
		}

		@Test
		@DisplayName("withProperties creates new component with merged properties")
		void withPropertiesCreatesNewComponentWithMergedProperties() {
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);
			Component original = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), durability)
					.build();

			SimpleAttribute damage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f);
			Component modified = original.withProperties(Map.of(Attribute.KEY.key(), List.of(damage)));

			assertNotSame(original, modified);
			assertEquals(1, original.properties(Attribute.KEY).size());
			assertEquals(2, modified.properties(Attribute.KEY).size());
		}
	}
}
